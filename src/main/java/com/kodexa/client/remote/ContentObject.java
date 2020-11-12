package com.kodexa.client.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentObject {

    private String id;
    private String name;

    private List<String> labels = new ArrayList<>();

    /**
     * The content type (DOCUMENT/NATIVE)
     */
    @JsonProperty("content_type")
    private String contentType;

    /**
     * The reference to the store holding this content object
     * if it is null then the content object is in cache and part of a
     * session
     */
    @JsonProperty("store_ref")
    private String storeRef;
}
