package com.kodexa.client.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public abstract class AbstractKodexaConnection {

    protected final static ObjectMapper messagePackOm;
    protected final static ObjectMapper jsonOm;

    static {
        messagePackOm = new ObjectMapper(new MessagePackFactory());
        messagePackOm.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        jsonOm = new ObjectMapper();
        jsonOm.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    protected RequestConfig getRequestConfig() {
        int timeout = 120; // seconds
        return RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
    }

}
