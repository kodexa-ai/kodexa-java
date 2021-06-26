package com.kodexa.client;

import org.junit.Test;

import java.io.InputStream;

public class PersistenceLayerTest {

    @Test
    public void loadKddb() {
        InputStream kddbInput = getClass().getClassLoader().getResourceAsStream("fax2.kddb");
        Document document = Document.fromKddb(kddbInput);

        assert document.getContentNode().getChildren().size() == 1;
    }
}
