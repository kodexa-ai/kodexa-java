package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudExceptionDetail {

    private String message;
    private int statusCode;
    private String errorMessage;
    private String errorType;
    private String executedVersion;
    private String help;

}
