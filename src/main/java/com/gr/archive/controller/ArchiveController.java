package com.gr.archive.controller;

import com.gr.archive.service.ArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("test/")
public class ArchiveController {

    @Autowired
    ArchiveService service;

    @GetMapping("run")
    public String getData() {
//        service.testdb();
        try {
            service.start();
        }catch (Exception e){
         //   log.error();
        }
        return "hello world";
    }
}
