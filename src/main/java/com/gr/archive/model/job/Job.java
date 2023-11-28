package com.gr.archive.model.job;

import com.gr.archive.model.job.task.DataCleanUpTask;
import com.gr.archive.model.job.task.DataCopyTask;
import com.gr.archive.model.job.task.DataReadTask;
import com.gr.archive.model.job.task.S3StorageTask;
import com.gr.archive.model.job.task.SQLDumpTask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Job {
	private String type;
	private boolean enabled;
	private boolean transactionEnabled;
    private Integer transactionTimeout;
    private DataReadTask dataReadTask;
    private DataCopyTask dataCopyTask;
    private DataCleanUpTask dataCleanUpTask;
    private SQLDumpTask sqlDumpTask;
    private S3StorageTask s3StorageTask;
}
