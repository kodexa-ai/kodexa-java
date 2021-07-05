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
        document.setContentNode(document.createContentNode("text", "Hello"));
        document.getContentNode().addFeature("tag", "lemon");

        Document newDocument = Document.fromBytes(document.toBytes());
        Assert.assertEquals("test", newDocument.getSource().getOriginalPath());
        Assert.assertEquals("test", newDocument.getSource().getConnector());
        Assert.assertTrue(newDocument.getMixins().contains("spatial"));
        Assert.assertNotNull(newDocument.getContentNode());
        Assert.assertEquals("text", newDocument.getContentNode().getType());
        Assert.assertEquals("Hello", newDocument.getContentNode().getContent());
        Assert.assertEquals(1, newDocument.getContentNode().getContentParts().size());
        Assert.assertEquals(1, newDocument.getContentNode().getFeatures().size());

    }
}
