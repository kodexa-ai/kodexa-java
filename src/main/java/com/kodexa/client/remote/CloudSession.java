package com.kodexa.client.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * An instance of a session in the Kodexa Cloud
 * <p>
 * A session is created using access tokens to allow you to set-up and the run either actions or pipelines
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudSession {

    /**
     * The unique ID of the cloud session
     */
    private String id;

}
