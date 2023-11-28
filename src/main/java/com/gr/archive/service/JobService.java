package com.gr.archive.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gr.archive.model.job.task.QueryTable;
import com.gr.archive.model.job.task.S3StorageTask;
import com.gr.archive.model.job.task.SQLDumpTask;
import com.gr.archive.model.job.task.Table;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JobService {

    public void truncateTables(Connection connection, List<Table> tables) {
        log.info("truncating tables");
        try {
            Statement statement = connection.createStatement();
            String foreignKeyCheck = "set foreign_key_checks=";
            statement.executeQuery(foreignKeyCheck + "0");
            for (Table table : tables) {
                statement.execute("truncate " + table.getName());
            }

            statement.executeQuery(foreignKeyCheck + "1");

        } catch (SQLException ex) {
            log.error("Exception occurred");
            ex.printStackTrace();
        }
    }

    public void archiveData(Connection primaryConnection, Connection archiveConnection, Map<String, Set<Integer>> feedIds, List<Table> tableOrder) throws SQLException {

        Statement statement = primaryConnection.createStatement();

        for (Table table : tableOrder) {
            log.info("Archiving table :" + table.getName());

            String getArchiveDataQuery = createQueryToGetArchiveData(table.getName(), feedIds.get(table.getName()));//table name
            ResultSet resultSet = statement.executeQuery(getArchiveDataQuery);
            insertDataInArchiveDb(archiveConnection, resultSet, table.getName());
        }
        statement.close();
        System.out.println("data archived");

    }


    public Map<String, Set<Integer>> getIdsToBeArchived(Connection connection, QueryTable queryTable, List tableOrder, long daysToRetainData) throws SQLException {
        log.info("getting ids from the datasource to be archived");

        List<Table> tables = queryTable.getTables();

        Map<String, Set<Integer>> archiveTableAndIdSet = new LinkedHashMap<>();

        for (Table table : tables) {
            if (tableOrder.contains(table))
                tableOrder.remove(table);

            tableOrder.add(table);
            Set<Integer> set = new LinkedHashSet<>();
            archiveTableAndIdSet.put(table.getName(), set);
        }
        PreparedStatement preparedStatement = connection.prepareStatement(queryTable.getQuery());

        if (daysToRetainData != 0) {
            preparedStatement.setLong(1, daysToRetainData);
        }

        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()) {
            for (Table table : tables) {
                archiveTableAndIdSet.get(table.getName()).add(rs.getInt(table.getIdAlias()));
            }
        }
        rs.close();

        return archiveTableAndIdSet;
    }

    public String createQueryToGetArchiveData(String tableName, Set<Integer> feedIds) {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("Select * from ");
        queryBuilder.append(tableName);
        queryBuilder.append(" where id in ( ");

        for (Integer id : feedIds) {
            queryBuilder.append(id + ",");
        }

        queryBuilder.delete(queryBuilder.length() - 1, queryBuilder.length());
        queryBuilder.append(")");
        log.info("Query to archive data:"+queryBuilder.toString());

        return queryBuilder.toString();
    }

    public void insertDataInArchiveDb(Connection archiveDbconnection, ResultSet resultSet, String tableName) throws SQLException {
       log.info("Inserting data in archive DB");

        StringBuilder insertQueryBuilder = new StringBuilder();
        insertQueryBuilder.append("insert into ");
        insertQueryBuilder.append(tableName);
        insertQueryBuilder.append(" values ( ");

        ResultSetMetaData metaData = resultSet.getMetaData();
        Integer columnCount = metaData.getColumnCount();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            insertQueryBuilder.append("? ,");
        }
        insertQueryBuilder.replace(insertQueryBuilder.length() - 1, insertQueryBuilder.length(), ")");

        log.info(insertQueryBuilder.toString());

        PreparedStatement pstmt = archiveDbconnection.prepareStatement(insertQueryBuilder.toString());

        while (resultSet.next()) {

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnType(i) == Types.BIT) {
                    pstmt.setByte(i, resultSet.getByte(i));
                } else {
                    pstmt.setString(i, resultSet.getString(i));
                }
            }
            pstmt.addBatch();
        }

        pstmt.executeBatch();
        resultSet.close();
        pstmt.close();

    }


    public void purgeData(Connection connection, Map<String, Set<Integer>> feedIds, List<Table> orderedTableList) throws SQLException {
        log.info("purging data from db");

        for (int i = orderedTableList.size() - 1; i >= 0; i--) {//deleting data in reverse order

            StringBuilder deleteQueryBuilder = new StringBuilder();
            deleteQueryBuilder.append("delete from ");
            deleteQueryBuilder.append(orderedTableList.get(i).getName());
            deleteQueryBuilder.append(" where id=?");

            PreparedStatement deleteRecordsPrepareStatement = connection.prepareStatement(deleteQueryBuilder.toString());
            for (Integer id : feedIds.get(orderedTableList.get(i).getName())) {
                deleteRecordsPrepareStatement.setInt(1, id);
                deleteRecordsPrepareStatement.addBatch();
            }
            deleteRecordsPrepareStatement.executeBatch();
        }
    }

    public String createSqlDumpFile(SQLDumpTask sqlDumpTask, String databaseName, long daysToRetain) {
        log.info("creating sql dump file");
        String mysql_user = System.getenv("mysql_user").trim();
        String mysql_pass = System.getenv("mysql_pass").trim();

        String archiveFromDate = "From_" + LocalDate.now().minusDays(daysToRetain).toString();
        String archiveTillDate = "Till_" + LocalDate.now().toString();
        String fileName = databaseName + "_" + archiveFromDate + "_" + archiveTillDate+".sql";

        String destinationFilePath = sqlDumpTask.getSavePath() + fileName ;
        log.info("destination file path"+destinationFilePath);

        String builder = new StringBuilder().append("mysqldump").append(' ')
                .append("-u").append(' ')
                .append(mysql_user).append(' ')
                .append("-p"+mysql_pass).append(' ')
                .append(databaseName).append(' ')
                .append(">").append(' ')
                .append(destinationFilePath)
                .toString();

        Process runtimeProcess = null;
        try {
            runtimeProcess = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", builder});

        } catch (IOException e) {
            e.printStackTrace();
        }
        int processComplete = 0;
        try {
            InputStream is = runtimeProcess.getErrorStream();
            processComplete = runtimeProcess.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (processComplete == 0) {
            System.out.println("Backup Complete at " + new Date());
        } else {
            System.out.println("Backup Failure");
        }
        return fileName;
    }


    public void uploadDumpToS3(S3StorageTask storageTask, String fileName, String filePath) {

        log.info("uploading data to S3 storage");

        String s3Username = System.getenv("aws_access_key_id").trim();
        String s3AccessKey = System.getenv("aws_secret_access_key").trim();

        AWSCredentials credentials = new BasicAWSCredentials(
                s3Username, s3AccessKey
        );

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1).build();


        PutObjectResult result = s3.putObject("grid-archive-test", fileName, new File(filePath+fileName));

        log.info("filename:"+fileName);
        log.info("filepath:"+filePath);

        ObjectListing objectListing = s3.listObjects(storageTask.getS3BucketName());
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.info(os.getKey());
        }

    }
}
