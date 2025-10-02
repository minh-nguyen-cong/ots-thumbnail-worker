package com.cdc.ots_thumbnail_worker.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudStorageEvent {
    private GcsEventData data;

    public GcsEventData getData() {
        return data;
    }
}