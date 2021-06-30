package com.kodexa.client;

import org.junit.Test;

import java.io.InputStream;

public class PersistenceLayerTest {

    @Test
    public void loadKddb() {
        InputStream kddbInput = getClass().getClassLoader().getResourceAsStream("fax2.kddb");
        Document document = Document.fromInputStream(kddbInput, "4.0.0");
        assert document.getContentNode().getChildren().size() == 1;
    }

    @Test
    public void newDocument() {
        Document document = new Document();
        document.toBytes();
        document.close();
    }
}
