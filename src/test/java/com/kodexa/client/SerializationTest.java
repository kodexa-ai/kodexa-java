package com.kodexa.client;

import org.junit.Assert;
import org.junit.Test;

public class SerializationTest {

    @Test
    public void basicSeralization() {
        Document document = new Document();
        document.getSource().setOriginalPath("test");
        document.getSource().setConnector("test");

        Document newDocument = Document.fromMsgPack(document.toMsgPack());
        Assert.assertEquals("test", newDocument.getSource().getOriginalPath());
        Assert.assertEquals("test", newDocument.getSource().getConnector());

    }
}
