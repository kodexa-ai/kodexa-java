package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentClassification implements Serializable {

    private String label;
    private String taxonomy;
    private String selector;
    private Float confidence;

}
