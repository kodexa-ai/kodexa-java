package com.kodexa.client.pipeline;

import com.kodexa.client.store.DataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
     * @param name the name of the store
     * @param store The implementation of the store
     */
    public void addStore(String name, DataStore store) {
        stores.put(name, store);
    }

}
