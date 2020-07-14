package com.kodexa.client.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentObject {

    private String id;
    private String name;
    private List<String> tags = new ArrayList<>();

    @JsonProperty("content_type")
    private String contentType;

}
