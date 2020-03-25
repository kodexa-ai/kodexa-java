package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudDocumentReference {

    private String id;

    private String referenceType;

    private String name;

    private CloudDocument cloudDocument;

}
