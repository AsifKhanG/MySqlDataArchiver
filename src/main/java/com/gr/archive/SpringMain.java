package com.gr.archive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.gr.archive.model.job.JobConfig;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@ComponentScan("com.gr.archive.config")

public class SpringMain {

    public static void main(String ...args){
        SpringApplication.run(SpringMain.class);
    }
}
