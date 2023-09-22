package com.kodexa.client;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NodeFeatures implements Serializable {

    private String nodeUuid;

    private List<ContentFeature> features;

}
