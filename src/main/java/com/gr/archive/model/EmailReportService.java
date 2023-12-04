package com.gr.archive.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class EmailReportService {

	private Date startTime; 

	private String sourceDBSlave;
	private String targetDBSource;
	private String sourceDBMaster;
	
	private String application; 	
	private long daysToRetainData; 
	
	private String dataReadTaskSQLQuery; 
	private HashMap<String, Integer> dataReadTask = new HashMap<String, Integer>();
	private HashMap<String, Integer> dataCopyTask = new HashMap<String, Integer>();
	private HashMap<String, Integer> dataCleanUpTask = new HashMap<String, Integer>(); 
	
    private boolean dataDumpingJobTransactionEnabled; 
	private String sqlDumpTaskSavePath; 
	
	private String createdFilename; 
	private String s3FileLocation;  
	
	private Date endTime; 
	
	public void addQueryData(String table, Integer rows, Integer taskNumber) {
		if (taskNumber == 1)
			dataReadTask.put(table, rows);
		else if (taskNumber == 2) 
			dataCopyTask.put(table, rows);
		else if (taskNumber == 3) 
			dataCleanUpTask.put(table, rows);
	}
	
	private String formatDateToString(Date date) {
		String pattern = "EEEEE MMMMM yyyy HH:mm:ss.SSSZ";
		SimpleDateFormat simpleDateFormat =new SimpleDateFormat(pattern, new Locale("en", "EN"));
		return simpleDateFormat.format(date);
	}
	
	public void printReport() {
		log.info("Start Time: " + formatDateToString(startTime));
		newLine();
		
		log.info("ConfiguredDataSources:");
		log.info("\t" + "sourceDBSlave URL: " + sourceDBSlave);
		log.info("\t" + "targetDBSource URL: " + targetDBSource);
		log.info("\t" + "sourceDBMaster URL: " + sourceDBMaster);
		newLine();
		
		log.info("Application: " + application); 
		newLine();
		
		log.info("==============================================================================="); 
		newLine();
		
		log.info("Archiving Job:"); 
		log.info("\t" + "dataReadTask:"); 
		log.info("\t\t" + "No. of days to retain data: " + daysToRetainData); 
		log.info("\t\t" + "SQL Query: " + dataReadTaskSQLQuery); 
		log.info("\t\t\t" + "No. of rows to read from sourceDBSlave tables:");
		
	    for (Map.Entry<String, Integer> entry : dataReadTask.entrySet()) 
            System.out.println("\t\t\t\t" + "[" + entry.getKey() +"]: " + entry.getValue());
		newLine(); 
		newLine();
		
		log.info("\t" + "dataCopyTask:"); 
		log.info("\t\t" + "No. of rows to moved from sourceDBSlave to targetDBSource tables:");
	    for (Map.Entry<String, Integer> entry : dataCopyTask.entrySet()) 
            System.out.println("\t\t\t\t" + "[" + entry.getKey() +"]: " + entry.getValue());
		newLine(); 
		newLine();
	    
		log.info("\t" + "dataCleanUpTask:");
		log.info("\t\t" + "No. of rows to deleted from sourceDBMaster tables:");
	    for (Map.Entry<String, Integer> entry : dataCleanUpTask.entrySet()) 
            System.out.println("\t\t\t\t" + "[" + entry.getKey() +"]: " + entry.getValue());
		newLine(); 
		newLine();

		log.info("Data Dumping Job");
		if (dataDumpingJobTransactionEnabled) 
			log.info("\t" + "transactionEnabled: enabled"); 
		else
			log.info("\t" + "transactionEnabled: disabled");
		
		log.info("\t" + "sqlDumpTask:");
		log.info("\t\t" + "Data dump files created:");
		log.info("\t\t\t" + "file: " + sqlDumpTaskSavePath + "/" + createdFilename);
		newLine();
		newLine();
		
		log.info("Backup Job:");
		log.info("\t" + "s3StorageTask:");
		log.info("\t\t" + "File uploaded to S3: " + s3FileLocation);
		newLine(); 
		
		log.info("==========================================================");
		newLine(); 
		
		log.info("End Time: " + formatDateToString(endTime));
	}
	
	
	public String getReport() {
		String report = "Start Time: " + formatDateToString(startTime) + "\n\n" +
						"ConfiguredDataSources: \n" +
						"\t" + "sourceDBSlave URL: " + sourceDBSlave + "\n" +
						"\t" + "targetDBSource URL: " + targetDBSource + "\n" +
						"\t" + "sourceDBMaster URL: " + sourceDBMaster + "\n\n"+
						"Application: " + application + "\n\n" +
						"===============================================================================" + "\n\n" +
						"Archiving Job:" + "\n" +
						"\t" + "dataReadTask:" + "\n" + 
						"\t\t" + "No. of days to retain data: " + daysToRetainData + "\n" + 
						"\t\t" + "SQL Query: " + dataReadTaskSQLQuery + "\n" + 
						"\t\t\t" + "No. of rows to read from sourceDBSlave tables:" + "\n";
		
		String dataReadTaskString = "";
	    for (Map.Entry<String, Integer> entry : dataReadTask.entrySet()) 
	    	dataReadTaskString = dataReadTaskString + "\t\t\t\t" + "[" + entry.getKey() +"]: " + entry.getValue() + "\n";
	    
	    report = report + dataReadTaskString + "\n\n" +
	    		"\t" + "dataCopyTask:"+ "\n" +
	    		"\t\t" + "No. of rows to moved from sourceDBSlave to targetDBSource tables:"+ "\n";
	    
		String dataCopyTaskString = "";
	    for (Map.Entry<String, Integer> entry : dataCopyTask.entrySet()) 
	    	dataCopyTaskString = dataCopyTaskString + "\t\t\t\t" + "[" + entry.getKey() +"]: " + entry.getValue()  + "\n";
	    
	    report = report + dataCopyTaskString + "\n\n" +
	    		"\t" + "dataCleanUpTask:" + "\n" +
	    		"\t\t" + "No. of rows to deleted from sourceDBMaster tables:" + "\n";
	    
		String dataCleanUpTaskString = "";
	    for (Map.Entry<String, Integer> entry : dataCleanUpTask.entrySet()) 
	    	dataCleanUpTaskString = dataCleanUpTaskString + "\t\t\t\t" + "[" + entry.getKey() +"]: " + entry.getValue() + "\n";
	    
	    report = report + dataCleanUpTaskString + "\n\n" +
	    		"Data Dumping Job\n";
	    
		if (dataDumpingJobTransactionEnabled) 
			report = report + "\t" + "transactionEnabled: enabled" + "\n";
		else
			report = report + "\t" + "transactionEnabled: disabled" + "\n";
		
		report = report + "\t" + "sqlDumpTask:" + "\n" + 
				"\t\t" + "Data dump files created:"+ "\n" + 
				"\t\t\t" + "file: " + sqlDumpTaskSavePath + "/" + createdFilename + "\n\n\n" + 
				"Backup Job:"  + "\n" + 
				"\t" + "s3StorageTask:"  + "\n" + 
				"\t\t" + "File uploaded to S3: " + s3FileLocation  + "\n\n" + 
				"==========================================================" + "\n\n" +
				"End Time: " + formatDateToString(endTime);
		return report;
	}
	
	public void sendEmail() {

		String to = "rfahim@globalrescue.com";
		String from = "web@gmail.com";
		String host = "localhost";

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);

		Session session = Session.getDefaultInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Data Archiver Report - " + formatDateToString(startTime));

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(getReport());
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);
			Transport.send(message);
			System.out.println("Email sent successfully....");
			
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	public void newLine() {
		log.info("\n"); 
	}
}
