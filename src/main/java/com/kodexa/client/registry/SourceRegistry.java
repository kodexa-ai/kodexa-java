package com.kodexa.client.registry;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.connectors.Connector;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SourceRegistry {

    private static SourceRegistry INTERNAL_REGISTRY = new SourceRegistry();

    private Map<String, Connector> connectors = new HashMap<>();

    public static SourceRegistry getInstance() {
        return INTERNAL_REGISTRY;
    }

    public void addConnector(Connector connector) {
        connectors.put(connector.getName(), connector);
    }

    public InputStream getSource(Document document) {
        String CONNECTOR_KEY = "connector";
        if (document.getMetadata().containsKey(CONNECTOR_KEY)) {
            String connector = String.valueOf(document.getMetadata().get(CONNECTOR_KEY));
            if (connectors.containsKey(connector)) {
                return connectors.get(connector).getSource(document);
            } else {
                throw new KodexaException("Unable to find connector [" + connector + "]");
            }
        } else {
            throw new KodexaException("Document does not have connector metadata");
        }
    }
}
