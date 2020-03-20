package com.kodexa.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodexa.client.cloud.ServiceMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class ServiceMessageTest {

    @Test
    public void testDeserialization() throws IOException {
        ObjectMapper om = new ObjectMapper();
        ServiceMessage sm = om.readValue(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("example-message.json")), ServiceMessage.class);
    }

    @Test
    public void loadMsgPackDoc() throws IOException {
        Document doc = Document.fromMsgPack(IOUtils.toByteArray(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("document.mdoc"))));
        log.info(doc.toJson());
    }
}
