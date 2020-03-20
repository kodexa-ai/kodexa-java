package com.kodexa.client.connectors;

import com.kodexa.client.Document;

import java.io.InputStream;
import java.util.Iterator;


/**
 * A connector is used at the beginning of a {@link com.kodexa.client.pipeline.Pipeline} to
 * allow one or more documents to be processed
 */
public interface Connector extends Iterator<Document> {

    String getName();

    InputStream getSource(Document document);

}
