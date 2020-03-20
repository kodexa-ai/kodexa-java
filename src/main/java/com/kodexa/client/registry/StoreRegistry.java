package com.kodexa.client.registry;

import com.kodexa.client.KodexaException;
import com.kodexa.client.store.DataStore;
import com.kodexa.client.store.DictionaryStore;
import com.kodexa.client.store.TableStore;

import java.util.Map;


// TODO need to make sure this is plugin/extensible
public class StoreRegistry {

    private static StoreRegistry INTERNAL_REGISTRY = new StoreRegistry();

    public static StoreRegistry getInstance() {
        return INTERNAL_REGISTRY;
    }

    public DataStore deserialize(String type, Map<String, Object> data) {

        switch (type) {
            case "table": {
                return new TableStore(data);
            }
            case "dictionary": {
                return new DictionaryStore(data);
            }
        }

        throw new KodexaException("Unknown store");
    }

}
