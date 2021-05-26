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

    private String uuid;

    @JsonProperty("node_type")
    private String type;
    private String content;

    @JsonProperty("content_parts")
    private List<Object> contentParts;

    private List<ContentNode> children = new ArrayList<>();
    private List<ContentFeature> features = new ArrayList<>();

    /**
     * Returns all the contents from this node and all its children
     *
     * @param separator
     * @return
     */
    public String getAllContent(String separator) {
        List<String> allContents = new ArrayList<>();
        allContents.add(this.content);
        for (ContentNode child : this.children) {
            allContents.add(child.getAllContent(separator));
        }
        return String.join(separator, allContents);
    }

}
