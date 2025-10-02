package com.cdc.ots_thumbnail_worker.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cdc.ots_thumbnail_worker.controller.dto.ThumbnailRequest;
import com.cdc.ots_thumbnail_worker.service.ThumbnailWorkerService;


@RestController
public class ThumbnailWorkerController {
    
    private final ThumbnailWorkerService thumbnailWorkerService;
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailWorkerController.class);

    public ThumbnailWorkerController(ThumbnailWorkerService thumbnailWorkerService) {
        this.thumbnailWorkerService = thumbnailWorkerService;
    }


    @PostMapping("/generate")
    public ResponseEntity<String> generateThumbnail(@Valid @RequestBody ThumbnailRequest request) {
        logger.info("Received API request to generate thumbnail for: {}", request.getGcsPath());
        thumbnailWorkerService.generateThumbnail(request.getGcsPath());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Thumbnail generation job accepted for: " + request.getGcsPath());
    }
    

}
