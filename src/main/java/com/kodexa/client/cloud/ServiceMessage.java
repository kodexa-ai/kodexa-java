package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A service message is a request/response object for the Kodexa platform
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceMessage {

    private final static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private String uuid = UUID.randomUUID().toString();
    private String version = "1.0.0";

    private ProcessingStatus status = ProcessingStatus.PENDING;
    private ProcessingException exception = null;

    private Map<String, String> metadata = new HashMap<>();
    private Map<String, Object> options = new HashMap<>();
    private Map<String, Map<String, Object>> stores = new HashMap<>();

    private Document document;

    public static ServiceMessage from(InputStream inputStream) throws IOException {
        return OBJECT_MAPPER.readValue(inputStream, ServiceMessage.class);
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new KodexaException("Unable to serialize service message", e);
        }
    }
}
