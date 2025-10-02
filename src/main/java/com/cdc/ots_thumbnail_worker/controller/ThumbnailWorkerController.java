package com.cdc.ots_thumbnail_worker.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdc.ots_thumbnail_worker.controller.dto.GcsEventData;
import com.cdc.ots_thumbnail_worker.service.ThumbnailWorkerService;


@RestController
public class ThumbnailWorkerController {
    
    private final ThumbnailWorkerService thumbnailWorkerService;
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailWorkerController.class);

    public ThumbnailWorkerController(ThumbnailWorkerService thumbnailWorkerService) {
        this.thumbnailWorkerService = thumbnailWorkerService;
    }


    @PostMapping("/")
    public ResponseEntity<String> receiveEvent(@RequestBody GcsEventData event) {
        if (event == null || event.getName() == null) {
            logger.warn("Received an invalid or empty event payload.");
            return ResponseEntity.badRequest().body("Invalid event payload.");
        }

        String gcsPath = event.getName();
        logger.info("Received event for GCS object: {}", gcsPath);
        thumbnailWorkerService.generateThumbnail(gcsPath);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Thumbnail generation job accepted for: " + gcsPath);
    }
    

}
