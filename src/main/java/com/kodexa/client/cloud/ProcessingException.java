package com.kodexa.client.cloud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProcessingException extends RuntimeException {

    private String message;
    private String description;
    private Map<String, Object> diagnostics = new HashMap<>();

}
