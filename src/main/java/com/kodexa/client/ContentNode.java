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

    private ContentNode parent;

    private String uuid;

    @JsonProperty("node_type")
    private String type;
    private String content;

    @JsonProperty("content_parts")
    private List<Object> contentParts;

    private List<ContentFeature> features = new ArrayList<>();

    private final Document document;

    public ContentNode(Document document) {
        this.document = document;
    }

    /**
     * Returns all the contents from this node and all its children
     *
     * @param separator
     * @return
     */
    public String getAllContent(String separator) {
        List<String> allContents = new ArrayList<>();
        allContents.add(this.content);
        for (ContentNode child : this.getChildren()) {
            allContents.add(child.getAllContent(separator));
        }
        return String.join(separator, allContents);
    }

    public List<ContentNode> getChildren() {
        return document.getPersistanceLayer().getChildNodes(this);
    }

    public ContentFeature addFeature(String featureType, String featureName) {
        ContentFeature contentFeature = new ContentFeature();
        contentFeature.setFeatureType(featureType);
        contentFeature.setName(featureName);
        getFeatures().add(contentFeature);

        document.getPersistanceLayer().updateNode(this);
        return contentFeature;
    }

    protected Integer getParentId() {
        if (parent != null) {
            return Integer.valueOf(parent.getUuid());
        } else {
            return null;
        }
    }
}
