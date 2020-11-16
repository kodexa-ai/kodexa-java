package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A feature that has been applied to a node of the content model
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ContentFeatureDeserializer.class)
@JsonSerialize(using = ContentFeatureSerializer.class)
public class ContentFeature {

    /**
     * The type of feature (ie spatial, html, tag)
     */
    private String featureType;

    /**
     * The name of the feature (ie tag name or div or line etc)
     */
    private String name;

    /**
     * A list of the values that the feature has
     */
    private List<Object> value = new ArrayList<>();

    /**
     * Is the feature a single value
     */
    private boolean single;

}
