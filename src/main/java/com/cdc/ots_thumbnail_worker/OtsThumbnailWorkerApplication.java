package com.cdc.ots_thumbnail_worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OtsThumbnailWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OtsThumbnailWorkerApplication.class, args);
    }
}