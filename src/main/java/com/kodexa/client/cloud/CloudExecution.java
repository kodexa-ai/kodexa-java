package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * An instance of an execution that is running in the Kodexa Cloud
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudExecution {

    /**
     * Unique ID for the execution
     */
    private String id;

    /**
     * The status of the execution
     */
    private String status;

}
