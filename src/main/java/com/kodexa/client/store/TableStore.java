package com.kodexa.client.store;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class TableStore implements DataStore {

    private List<String> columns = new ArrayList<>();

    private List<List<Object>> rows = new ArrayList<>();

    public TableStore(Map<String, Object> data) {
        columns = (List<String>) data.get("columns");
        rows = (List<List<Object>>) data.get("rows");
    }

}
