package com.kodexa.client.pipeline;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.connectors.Connector;

import java.io.InputStream;

public class DocumentConnector implements Connector {

    private final Document document;
    private boolean available = true;

    public DocumentConnector(Document document) {
        this.document = document;
    }

    @Override
    public String getName() {
        return "document-connector";
    }

    @Override
    public InputStream getSource(Document document) {
        throw new KodexaException("You can not get the source for a document connector");
    }

    @Override
    public boolean hasNext() {
        return available;
    }

    @Override
    public Document next() {
        this.available = false;
        return document;
    }
}
