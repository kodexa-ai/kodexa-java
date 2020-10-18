package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SourceMetadata {

    @JsonProperty("original_filename")
    private String originalFilename;

    @JsonProperty("original_path")
    private String originalPath;
    private String checksum;

    @JsonProperty("last_modified")
    private String lastModified;
    private String created;
    private String connector;

    @JsonProperty("mime_type")
    private String mimeType;
    private Map<String, Object> headers;

}
