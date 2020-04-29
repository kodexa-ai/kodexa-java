package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kodexa.client.store.TableStore;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudStore {

    private String id;
    private String name;

    private TableStore data;

}
