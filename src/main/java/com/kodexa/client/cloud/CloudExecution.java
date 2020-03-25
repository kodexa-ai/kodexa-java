package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private List<CloudStore> stores = new ArrayList();

    private Set<CloudDocumentReference> documentReferences = new HashSet<>();

    private Set<CloudFileReference> fileReferences = new HashSet<>();

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

    public CloudDocumentReference getOutputDocument() {
        for (CloudDocumentReference documentReference : getDocumentReferences()) {
            if (documentReference.getReferenceType().equals("OUTPUT")) {
                return documentReference;
            }
        }
        return null;
    }
}
