package com.kodexa.client.remote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a processing exception that has occurred in the
 * Kodexa Cloud
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProcessingException extends RuntimeException {

    private String message;
    private String description;
    private Map<String, Object> diagnostics = new HashMap<>();

}
