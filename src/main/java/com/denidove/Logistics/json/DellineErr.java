package com.denidove.Logistics.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DellineErr(
        Metadata metadata,
        Errors[] errors
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(
            Integer status,
            String detail,
            String generated_at
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Errors(
            Integer code,
            String title,
            String detail,
            String link,
            String[] fields
    ) {}
}
