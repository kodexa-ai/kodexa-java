package com.kodexa.client;

import lombok.Data;

@Data
public class ContentClassification {

    private String label;
    private String taxonomy;
    private String selector;
    private Float confidence;

}
