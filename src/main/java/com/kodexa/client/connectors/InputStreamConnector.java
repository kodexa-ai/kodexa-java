package com.kodexa.client.connectors;

import com.kodexa.client.Document;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@NoArgsConstructor
public class InputStreamConnector implements Connector {

    private InputStream inputStream;
    private boolean empty;

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
        InputStream inputStream = (InputStream) document.getMetadata().get("stream");
        document.getMetadata().remove("stream");
        return inputStream;
    }

    @Override
    public boolean hasNext() {
        return !empty;
    }

    @Override
    public Document next() {
        Document document = new Document();
        document.getMetadata().put("connector", getName());
        document.getMetadata().put("stream", inputStream);
        empty = true;
        return document;
    }
}
