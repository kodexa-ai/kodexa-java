package com.kodexa.client.pipeline;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple structure for capturing the options for a Kodexa
 * Cloud Action in Java
 */
public class Options {

    private final Map<String, Object> opts = new HashMap<>();

    @Getter
    private boolean attachSource = false;

    @Getter
    private boolean enabled = true;

    @Getter
    private boolean parameterized;

    @Getter
    private String condition;

    public Options set(String name, Object value) {
        opts.put(name, value);
        return this;
    }

    public static Options start() {
        return new Options();
    }

    public Options enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Options parameterized(boolean parameterized) {
        this.parameterized = parameterized;
        return this;
    }

    public Options condition(String condition) {
        this.condition = condition;
        return this;
    }

    public Options attachSource() {
        this.attachSource = true;
        return this;
    }

    public Map<String, Object> get() {
        return opts;
    }
}
