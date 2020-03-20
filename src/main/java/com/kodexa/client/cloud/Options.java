package com.kodexa.client.cloud;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Options {

    private Map<String, Object> opts = new HashMap<>();

    @Getter
    private boolean attachSource = false;

    public Options set(String name, Object value) {
        opts.put(name, value);
        return this;
    }

    public static Options start() {
        return new Options();
    }

    public Options attachSource() {
        this.attachSource = true;
        return this;
    }

    public Map<String, Object> get() {
        return opts;
    }
}
