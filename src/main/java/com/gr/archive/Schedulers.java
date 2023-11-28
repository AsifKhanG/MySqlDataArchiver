package com.gr.archive;

import com.gr.archive.service.ArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Schedulers {

    @Autowired
    ArchiveService archiveService;

    @Scheduled(fixedRate = 120000)
    public void runDataArchive() {
        try {
            log.info("scheduled task running");
            archiveService.start();
        } catch (Exception e) {

        }
    }
}
