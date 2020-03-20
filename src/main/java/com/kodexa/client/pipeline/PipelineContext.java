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

    public Map<String, DataStore> stores = new HashMap<String, DataStore>();

    public DataStore getStore(String name) {
        return stores.get(name);
    }

    public Set<String> getStoreNames() {
        return stores.keySet();
    }

    public void addStore(String name, DataStore store) {
        stores.put(name, store);
    }

}
