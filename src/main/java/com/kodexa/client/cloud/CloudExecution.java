package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.*;

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

    /**
     * List of the step that are part of the execution
     */
    private List<CloudExecutionStep> steps;

    /**
     * List of the stores available from the execution
     */
    private List<CloudStore> stores = new ArrayList<>();

    /**
     * List of the content objects available from the execution
     */
    private List<ContentObject> contentObjects = new ArrayList<>();


    /**
     * The ID of the output document (content object)
     */
    private String outputId;

    /**
     * The ID of the input document (content object)
     */
    private String inputId;

    /**
     * Get the exception details from the steps
     *
     * @return exception details, or null if there wasn't an exception
     */
    public CloudExceptionDetail getExceptionDetail() {
        for (CloudExecutionStep step : steps) {
            if (step.getExceptionDetails() != null)
                return step.getExceptionDetails();
        }
        return null;
    }

    public ContentObject getOutputDocument() {
        return contentObjects.stream().filter(c -> c.getId().equals(outputId)).findFirst().orElse(null);
    }
}
