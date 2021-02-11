package com.kodexa.client.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Base implementation of pagination wrapper
 *
 * @param <T>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page<T> {

    private long totalElements;

    private long totalPages;

    private long numberOfElements;

    private long size;

    private List<T> content;

}
