package com.kodexa.client;

import lombok.Data;

import java.util.List;

@Data
public class NodeFeatures {

    private String nodeUuid;

    private List<ContentFeature> features;

}
