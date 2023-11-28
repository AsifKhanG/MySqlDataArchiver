package com.gr.archive.model.job;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Application {

    private String name;
    private List<Job> jobs;
}
