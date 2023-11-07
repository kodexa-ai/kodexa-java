package com.kodexa.client;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the features of a node.
 * This class is serializable, which means it can be converted into a byte stream and restored from it.
 */
@Data
public class NodeFeatures implements Serializable {

    /**
     * The unique identifier of the node.
     */
    private String nodeUuid;

    /**
     * The action to be performed on the node feature.
     * By default, it is set to ADD.
     */
    private NodeFeatureAction nodeFeatureAction = NodeFeatureAction.ADD;

    /**
     * A list of content features associated with the node.
     */
    private List<ContentFeature> features;

}
