package com.gr.archive.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.gr.archive.model.EmailReportService;
import com.gr.archive.model.job.Application;
import com.gr.archive.model.job.Job;
import com.gr.archive.model.job.JobConfig;
import com.gr.archive.model.job.task.QueryTable;
import com.gr.archive.model.job.task.Table;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ArchiveService {

	@Autowired
	JobService jobService;

	@Autowired
	JobConfig jobConfig;

	@Autowired
	List<AtomikosDataSourceBean> dataSourceList;
	
	@Autowired
	EmailReportService emailReportService;
	
//	public void start() throws Exception {
//
//		log.info("Started archiving data...");
//		UserTransactionImp utx = new UserTransactionImp();
//		utx.setTransactionTimeout(300);
//		
//		for (Application app : jobConfig.getArchiveApp()) {
//			
//			for (ArchiveJob job : app.getJobs()) {	
//				if (job.getEnabled()) {	
//					
//					/* Job 1 - Truncating tables */
//					log.info("Trunctating tables..");
//					Connection archiveConnection = null;
//					try {
//						DataSource dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(job.getArchiveTask().getDataSource())).findFirst().orElse(null);
//						archiveConnection = dataSource.getConnection();
//						
//						if (job.getArchiveTask() != null) {
//							List<Table> tableList = new ArrayList<Table>();
//							for (QueryTable queryTable : job.getDataReadTask().getQueryTableList()) 
//								tableList.addAll(queryTable.getTables());
//							jobService.truncateTables(archiveConnection, tableList);
//						}
//			        } catch (Exception e) {
//			            log.error("Exception occurred while truncating tables - " + e.getMessage());
//			        } finally {
//			            if (archiveConnection != null) 
//			            	archiveConnection.close();
//			        }
//				}
//			}
//
//			for (ArchiveJob job : app.getJobs()) {
//				if (job.getEnabled()) {
//					
//					log.info("Archive Job started..");
//					utx.begin(); //transaction begins
//					
//					/* Archiving */
//					Connection primaryConnection = null; Connection archiveConnection = null; String databaseName = null;
//					Map<String, Set<Integer>> feedIds = new LinkedHashMap<>(); List<Table> orderedTableList = new ArrayList<>();
//					
//					try {
//						DataSource dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(job.getDataReadTask().getDataSource())).findFirst().orElse(null);
//						primaryConnection = dataSource.getConnection();
//						
//						/* Job 2 - Find Id's to be archived */
//						for (QueryTable queryTable : job.getDataReadTask().getQueryTableList()) {
//							Map<String, Set<Integer>> feedId = jobService.getIdsToBeArchived(primaryConnection, queryTable, orderedTableList, job.getDataReadTask().getDaysToRetainData());
//
//							for (Map.Entry<String, Set<Integer>> entry : feedId.entrySet()) {
//								log.info("Total entries to be archived for table: " + entry.getKey() + " = " + entry.getValue().size());
//								if (feedIds.containsKey(entry.getKey())) {
//									Set<Integer> tableIds = feedIds.get(entry.getKey());
//									tableIds.addAll(entry.getValue());
//									feedIds.put(entry.getKey(), tableIds);
//								} else {
//									feedIds.put(entry.getKey(), entry.getValue());
//								}
//							}
//						}
//						
//						/* Job 3 - Archive the records = feedIds */
//						dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(job.getArchiveTask().getDataSource())).findFirst().orElse(null);
//						archiveConnection = dataSource.getConnection();
//						databaseName = archiveConnection.getCatalog();
//						jobService.archiveData(primaryConnection, archiveConnection, feedIds, orderedTableList);
//						
//			        } catch (Exception e) {
//			            log.error("Exception occurred while archiving - " + e.getMessage());
//			            if (utx != null)
//			            	utx.rollback();
//			        } finally {
//			            if (primaryConnection != null) 
//			                primaryConnection.close();
//			            if (archiveConnection != null) 
//			            	archiveConnection.close();
//			        }	
//					
//					
//					/*Job 4 - Delete records which have been archived */
//					Connection purgeConnection = null;
//					try {
//						DataSource dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(job.getDataRemovalTask().getDataSource())).findFirst().orElse(null);
//						purgeConnection = dataSource.getConnection();
//						jobService.purgeData(purgeConnection, feedIds, orderedTableList);
//						
//						for (Map.Entry<String, Set<Integer>> entry : feedIds.entrySet()) 
//							log.info("Total entries successfully cleaned up from table: " + entry.getKey() + " = " + entry.getValue().size());
//						
//			        } catch (Exception e) {
//			            log.error("Exception occurred while deleting records which have been archived - " + e.getMessage());
//			            if (utx != null)
//			            	utx.rollback();
//			        } finally {
//			            if (purgeConnection != null) 
//			                purgeConnection.close();
//			        }	
//					
//					log.info("Data successfully archived!");
//					utx.commit(); // transaction ends 
//					
//					
//					/* Job 5 - Create SQL Dump File */
//					String sqlDumpFileName = null;	
//					if (job.getSqlDumpTask() != null) 
//						sqlDumpFileName = jobService.createSqlDumpFile(job.getSqlDumpTask(), databaseName, job.getDataReadTask().getDaysToRetainData());
//					
//					
//					/* Job 6 - Store data to S3 */
//					if (job.getS3StorageTask() != null) 
//						jobService.uploadDumpToS3(job.getS3StorageTask(), sqlDumpFileName, job.getSqlDumpTask().getSavePath());
//				}
//			}
//		}
//	}
	
	public void start() throws Exception {

		log.info("Started archiving data...");	
		emailReportService.setStartTime(new Date());
		
		for (Application app : jobConfig.getApplications()) {
			
			emailReportService.setApplication(app.getName());
			for (Job job : app.getJobs()) {	
				if (job.isEnabled()) {	
					
					/* Pre Job: Truncating tables */
					log.info("Truncating tables..");
					Connection archiveConnection = null;
					try {
						DataSource dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(job.getDataCopyTask().getDataSource())).findFirst().orElse(null);
						archiveConnection = dataSource.getConnection();
						
						if (job.getDataReadTask() != null) {
							List<Table> tableList = new ArrayList<Table>();
							for (QueryTable queryTable : job.getDataReadTask().getQueryTableList()) 
								tableList.addAll(queryTable.getTables());
							jobService.truncateTables(archiveConnection, tableList);
						}
			        } catch (Exception e) {
			            log.error("Exception occurred while truncating tables - " + e.getMessage());
			        } finally {
			            if (archiveConnection != null) 
			            	archiveConnection.close();
			        }
				}
			}

			String databaseName = null;
			List<Job> jobs = app.getJobs();
			
			/* Job 1: Archiving */
			Job archivingJob = jobs.stream().filter(job -> job.getType().equals("archiveJob")).findFirst().orElse(null);
			if(archivingJob !=  null && archivingJob.isEnabled()) {
				
				log.info("Archive Job started..");
				UserTransactionImp utx = new UserTransactionImp();
					
				if (archivingJob.isTransactionEnabled()) {
					utx.setTransactionTimeout(archivingJob.getTransactionTimeout());
					utx.begin(); //transaction begins	
				}
				Connection primaryConnection = null; Connection archiveConnection = null; 
				Map<String, Set<Integer>> feedIds = new LinkedHashMap<>(); List<Table> orderedTableList = new ArrayList<>();					
			
				try {
					DataSource dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(archivingJob.getDataReadTask().getDataSource())).findFirst().orElse(null);
					primaryConnection = dataSource.getConnection();
					
					/* Task 1(getDataReadTask) - Find Id's to be archived */
					for (QueryTable queryTable : archivingJob.getDataReadTask().getQueryTableList()) {
						
						Map<String, Set<Integer>> feedId = jobService.getIdsToBeArchived(primaryConnection, queryTable, orderedTableList, archivingJob.getDataReadTask().getDaysToRetainData());
						emailReportService.setDaysToRetainData(archivingJob.getDataReadTask().getDaysToRetainData());
						
						for (Map.Entry<String, Set<Integer>> entry : feedId.entrySet()) {
							log.info("Total entries to be archived for table: " + entry.getKey() + " = " + entry.getValue().size());
							if (feedIds.containsKey(entry.getKey())) {
								Set<Integer> tableIds = feedIds.get(entry.getKey());
								tableIds.addAll(entry.getValue());
								feedIds.put(entry.getKey(), tableIds);
							} else {
								feedIds.put(entry.getKey(), entry.getValue());
							}
							
							emailReportService.addQueryData(entry.getKey(), entry.getValue().size(), 1);
						}
					}
					
					/* Task 2(getDataCopyTask) - Archive the records = feedIds */
					dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(archivingJob.getDataCopyTask().getDataSource())).findFirst().orElse(null);
					databaseName = dataSource.getConnection().getCatalog();
					for (Map.Entry<String, Set<Integer>> entry : feedIds.entrySet()) {
						log.info("Total entries to be copied for table: " + entry.getKey() + " = " + entry.getValue().size());
						emailReportService.addQueryData(entry.getKey(), entry.getValue().size(), 2);
					}
					jobService.archiveData(primaryConnection, archiveConnection, feedIds, orderedTableList);
					
		        } catch (Exception e) {
		            log.error("Exception occurred while archiving - " + e.getMessage());
		            if (utx != null)
		            	utx.rollback();
		        } finally {
		            if (primaryConnection != null) 
		                primaryConnection.close();
		            if (archiveConnection != null) 
		            	archiveConnection.close();
		        }	
				
					
				/*Task 3(getDataCleanUpTask) - Delete records which have been archived */
				Connection purgeConnection = null;
				try {
					DataSource dataSource = dataSourceList.stream().filter(source -> source.getUniqueResourceName().equals(archivingJob.getDataCleanUpTask().getDataSource())).findFirst().orElse(null);
					purgeConnection = dataSource.getConnection();
					jobService.purgeData(purgeConnection, feedIds, orderedTableList);
					
					for (Map.Entry<String, Set<Integer>> entry : feedIds.entrySet()) {
						log.info("Total entries successfully cleaned up from table: " + entry.getKey() + " = " + entry.getValue().size());
						
						emailReportService.addQueryData(entry.getKey(), entry.getValue().size(), 3);
					}
		        } catch (Exception e) {
		            log.error("Exception occurred while deleting records which have been archived - " + e.getMessage());
		            if (utx != null)
		            	utx.rollback();
		        } finally {
		            if (purgeConnection != null) 
		                purgeConnection.close();
		        }	
				
				log.info("Data successfully archived!");
				utx.commit(); // transaction ends 			
			}
			
			/* Job 2: Create SQL Dump File */
			Job dataDumpingJob = jobs.stream().filter(job -> job.getType().equals("dataDumpingJob")).findFirst().orElse(null);		
			String sqlDumpFileName = null;	
			if (dataDumpingJob !=  null && dataDumpingJob.isEnabled()) 
				sqlDumpFileName = jobService.createSqlDumpFile(dataDumpingJob.getSqlDumpTask(), databaseName, dataDumpingJob.getDataReadTask().getDaysToRetainData());
			emailReportService.setDataDumpingJobTransactionEnabled(dataDumpingJob.isTransactionEnabled());
			emailReportService.setSqlDumpTaskSavePath(dataDumpingJob.getSqlDumpTask().getSavePath() + sqlDumpFileName);
			
			/* Job 3 - Store data to S3 */
			Job backupJob = jobs.stream().filter(job -> job.getType().equals("backupJob")).findFirst().orElse(null);		
			if (backupJob !=  null && backupJob.isEnabled()) 
				jobService.uploadDumpToS3(backupJob.getS3StorageTask(), sqlDumpFileName, backupJob.getSqlDumpTask().getSavePath());
			
			emailReportService.setCreatedFilename(sqlDumpFileName);
			emailReportService.setS3FileLocation(backupJob.getSqlDumpTask().getSavePath());
		}
		
		emailReportService.setEndTime(new Date());
		emailReportService.sendEmail();
	}
}