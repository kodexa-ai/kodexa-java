package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentClassification {

    private String label;
    private String taxonomy;
    private String selector;
    private Float confidence;

}
