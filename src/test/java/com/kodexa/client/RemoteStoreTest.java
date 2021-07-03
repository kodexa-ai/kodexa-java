package com.kodexa.client;

import com.kodexa.client.remote.KodexaPlatform;
import com.kodexa.client.store.RemoteDocumentStore;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;

public class RemoteStoreTest {

    @Test
    @Ignore
    public void basicNative() {
        KodexaPlatform.setUrl("https://demo.kodexa.ai");
        RemoteDocumentStore rds = new RemoteDocumentStore("kodexa-smoketest/8a8a83537a699a0e017a6c7bbeb90000-processing:1.0.0");
        InputStream is = getClass().getClassLoader().getResourceAsStream("apple-10k.pdf");
        try {
            rds.putNative("Apple 10k.pdf", is);
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }
}
