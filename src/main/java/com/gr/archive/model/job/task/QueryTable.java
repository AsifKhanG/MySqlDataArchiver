package com.gr.archive.model.job.task;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryTable {
    private String query;
    private List<Table> tables;
}
