package com.kodexa.client.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoredRow {

    private String id;

    private String orgSlug;

    private String slug;

    private String version;

    private String taxonomyRef;

    private String table;

    private int rowNum = 0;

    private String sourceOrdering;

    private Map<String, String> data = new HashMap<>();

}
