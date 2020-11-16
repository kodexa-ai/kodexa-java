package com.kodexa.client;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in a content tree
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentNode {

    private int index;

    @JsonProperty("node_type")
    private String type;
    private String content;

    @JsonProperty("content_parts")
    private List<Object> contentParts;

    private List<ContentNode> children = new ArrayList<>();
    private List<ContentFeature> features = new ArrayList<>();

}
