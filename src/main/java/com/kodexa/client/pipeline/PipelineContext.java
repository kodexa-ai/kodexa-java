package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.store.DataStore;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The pipeline context is used to manage state through a running
 * pipeline
 */
public class PipelineContext {

    /**
     * The available stores
     */
    public Map<String, DataStore> stores = new HashMap<String, DataStore>();

    /**
     * The last document to be processed
     */
    private Document document;

    /**
     * The parameters that were passed to the pipeline
     */
    @Getter
    @Setter
    private List<PipelineParameter> parameters = new ArrayList<>();

    /**
     * Get a specific store
     *
     * @param name the name of the store to get
     * @return The instance of the store (or null if not found)
     */
    public DataStore getStore(String name) {
        return stores.get(name);
    }

    /**
     * Return a set of the names of the stores in the pipeline
     *
     * @return set of names
     */
    public Set<String> getStoreNames() {
        return stores.keySet();
    }

    /**
     * Add the given store to the pipeline context
     *
     * @param name  the name of the store
     * @param store The implementation of the store
     */
    public void addStore(String name, DataStore store) {
        stores.put(name, store);
    }

    /**
     * Sets the final output document for the pipeline
     *
     * @param document the final output document
     */
    public void setOutputDocument(Document document) {
        this.document = document;
    }

    /**
     * Returns the final output document, or null if there isn't one
     *
     * @return final output document
     */
    public Document getOutputDocument() {
        return this.document;
    }

    public Map<String, Object> getParameterMap() {
        return this.parameters.stream()
                .collect(Collectors.toMap(PipelineParameter::getName, PipelineParameter::getValue));
    }
}
