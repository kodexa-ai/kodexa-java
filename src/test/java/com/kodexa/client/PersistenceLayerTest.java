package com.kodexa.client;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class PersistenceLayerTest {

    @Test
    public void loadKddb() {
        InputStream kddbInput = getClass().getClassLoader().getResourceAsStream("fax2.kddb");
        Document document = Document.fromInputStream(kddbInput);
        assert document.getContentNode().getChildren().size() == 1;
    }

    @Test
    public void testGetNodeCounts() {

        // Test for faster counts

        InputStream kddbInput = getClass().getClassLoader().getResourceAsStream("fax2.kddb");
        Document document = Document.fromInputStream(kddbInput);
        assert document.getNodeCountByType("line") == 18;
    }

    @Test
    public void newDocument() {
        Document document = new Document();
        byte[] bytes = document.toBytes();
        document.close();

        Document doc2 = new Document(new ByteArrayInputStream(bytes));
    }

    @Test
    public void testMemoryConsumption() {

        // Test for faster counts

        while (true) {
            InputStream kddbInput = getClass().getClassLoader().getResourceAsStream("fax2.kddb");
            Document document = Document.fromInputStream(kddbInput);
            document.close();
            System.out.println("Memory: " + Runtime.getRuntime().totalMemory() + " / " + Runtime.getRuntime().freeMemory() + " / " + Runtime.getRuntime().maxMemory());
            System.out.println(
                    System.getProperty("java.io.tmpdir"));
        }
    }
}
