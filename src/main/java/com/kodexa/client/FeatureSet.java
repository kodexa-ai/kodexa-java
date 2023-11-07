package com.kodexa.client;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A feature set is a natural way to pass around features that are related to a specific
 * Document.
 * <p>
 * This is a lightweight way to think about moving just features, when the structure of the document has not been modified
 */
@Data
public class FeatureSet implements Serializable {

    // The URI of the owner for this feature set
    private String ownerUri;

    // The features that have been applied to the document
    private List<NodeFeatures> nodeFeatures = new ArrayList<>();

}
