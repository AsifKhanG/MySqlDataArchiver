package com.gr.archive.model.job.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SQLDumpTask {
    private Boolean takeSqlDump;
    private String savePath;
    private String sqlDumpFileName;
    private String databaseName;
}
