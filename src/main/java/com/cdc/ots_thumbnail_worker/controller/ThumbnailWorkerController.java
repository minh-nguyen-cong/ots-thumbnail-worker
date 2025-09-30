package com.cdc.ots_thumbnail_worker.controller;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cdc.ots_thumbnail_worker.worker.ThumbnailWorker;


@Controller
public class ThumbnailWorkerController {
    
    private final ThumbnailWorker thumbnailWorker;
    private final Logger logger = Logger.getLogger(ThumbnailWorkerController.class.getName());

    public ThumbnailWorkerController(ThumbnailWorker thumbnailWorker) {
        this.thumbnailWorker = thumbnailWorker;
    }


    @PostMapping("/")
    public String generateThumbnail(@RequestBody String entity) {
        logger.info("Received entity: " + entity);
        thumbnailWorker.generateThumbnail();
        
        return entity;
    }
    

}
