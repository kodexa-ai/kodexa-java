package com.kodexa.client.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictionaryStore implements DataStore {

    private List<Map<String, Object>> dicts = new ArrayList<>();

    public DictionaryStore(Map<String, Object> data) {
        dicts = (List<Map<String, Object>>) data.get("dicts");
    }
}
