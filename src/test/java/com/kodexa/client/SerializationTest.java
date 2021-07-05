package com.kodexa.client;

import org.junit.Assert;
import org.junit.Test;

public class SerializationTest {

    @Test
    public void basicSerialization() {
        Document document = new Document();
        document.getSource().setOriginalPath("test");
        document.getSource().setConnector("test");
        document.addMixin("spatial");

        Document newDocument = Document.fromBytes(document.toBytes());
        Assert.assertEquals("test", newDocument.getSource().getOriginalPath());
        Assert.assertEquals("test", newDocument.getSource().getConnector());
        Assert.assertTrue(newDocument.getMixins().contains("spatial"));

    }
}
