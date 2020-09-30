package com.kodexa.client.sink;

import com.kodexa.client.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory sink for capturing documents from a pipeline
 */
public class InMemorySink implements Sink {

    private List<Document> documents = new ArrayList<>();

    public Document getDocument(int i) {
        return documents.get(i);
    }

    public int getCount() {
        return documents.size();
    }

    @Override
    public void sink(Document document) {
        documents.add(document);
    }

    @Override
    public String getName() {
        return "In-memory";
    }

}
