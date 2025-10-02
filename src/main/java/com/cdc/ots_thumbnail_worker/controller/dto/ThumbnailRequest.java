package com.cdc.ots_thumbnail_worker.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class ThumbnailRequest {
    @NotBlank(message = "gcsPath cannot be null or empty")
    private String gcsPath;

    public String getGcsPath() {
        return gcsPath;
    }

    public void setGcsPath(String gcsPath) {
        this.gcsPath = gcsPath;
    }
}