package com.gr.archive.model.job;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Configuration
@ConfigurationProperties("jobconfig")
public class JobConfig {
    List<Application> applications;
}
