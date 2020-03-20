package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A feature that has been applied to a node of the content model
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentFeature {

    private String featureType;
    private String name;
    private List<Object> value = new ArrayList<>();
    private String description;
    private boolean single;

}
