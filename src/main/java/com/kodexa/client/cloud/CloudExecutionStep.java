package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudExecutionStep {

    private String id;
    private String status;
    private CloudExceptionDetail exceptionDetails;

}
