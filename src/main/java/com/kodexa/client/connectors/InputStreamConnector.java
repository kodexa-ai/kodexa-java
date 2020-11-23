package com.kodexa.client.connectors;

import com.kodexa.client.Document;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class InputStreamConnector implements Connector {

    private InputStream inputStream;
    private boolean empty;
    private Map<String, InputStream> cache = new HashMap<>();

    public InputStreamConnector(InputStream inputStream) {
        this.inputStream = inputStream;
        this.empty = false;
    }

    @Override
    public String getName() {
        return "inputStream";
    }

    @Override
    public InputStream getSource(Document document) {
        return cache.get(document.getUuid());
    }

    @Override
    public boolean hasNext() {
        return !empty;
    }

    @Override
    public Document next() {
        Document document = new Document();
        document.getMetadata().put("connector", getName());
        cache.put(document.getUuid(), inputStream);
        empty = true;
        return document;
    }
}
