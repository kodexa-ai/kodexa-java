package com.kodexa.client;

import com.kodexa.client.store.RemoteTableDataStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

@Slf4j
public class BasicRemoteStoreTest {

    @Test
    @Ignore
    public void basicTest() {
        RemoteTableDataStore remoteTableDataStore = new RemoteTableDataStore("fund-performance/8a8a8300771b5d2701771b805ffa0009-data-store");
        List<Map<String, String>> data = remoteTableDataStore.getTable("/distributions");
        log.info(data.toString());
    }

}
