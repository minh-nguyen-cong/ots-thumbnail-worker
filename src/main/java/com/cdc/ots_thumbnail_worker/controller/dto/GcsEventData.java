package com.cdc.ots_thumbnail_worker.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GcsEventData {
    private String name;

    public String getName() {
        return name;
    }
}