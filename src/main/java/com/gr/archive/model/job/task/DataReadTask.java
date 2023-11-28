package com.gr.archive.model.job.task;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataReadTask {
    private String dataSource;
    private long daysToRetainData;
    private List<QueryTable> queryTableList;
}
    