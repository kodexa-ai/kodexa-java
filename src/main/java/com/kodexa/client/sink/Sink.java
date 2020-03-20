package com.kodexa.client.sink;

import com.kodexa.client.Document;

public interface Sink {

    void sink(Document document);

    String getName();

    int getCount();
}
