package com.kodexa.client;

import java.util.ArrayList;
import java.util.List;

/**
 * A feature set is a natural way to pass around features that are related to a specific
 * Document.
 * <p>
 * This is a lightweight way to think about moving just features, when the structure of the document has not been modified
 */
public class FeatureSet {

    private List<AttachedFeature> attachedFeatures = new ArrayList<>();

}
