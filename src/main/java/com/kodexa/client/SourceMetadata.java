package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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

    private String cid;

    @JsonProperty("lineage_document_uuid")
    private String lineageDocumentUuid;

    private Map<String, Object> headers;

}
