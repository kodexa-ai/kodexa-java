package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodexa.client.KodexaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;

@Slf4j
public abstract class AbstractKodexaSession {

    protected final static ObjectMapper messagePackOm;
    protected final static ObjectMapper jsonOm;

    static {
        messagePackOm = new ObjectMapper(new MessagePackFactory());
        messagePackOm.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        jsonOm = new ObjectMapper();
        jsonOm.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected final String organizationSlug;
    protected final String serviceSlug;


    public AbstractKodexaSession(String organizationSlug, String serviceSlug) {
        this.organizationSlug = organizationSlug;
        this.serviceSlug = serviceSlug;
    }

    protected void waitForExecution(CloudSession session, CloudExecution execution) {
        String status = execution.getStatus();
        while (execution.getStatus().equals("PENDING") || execution.getStatus().equals("RUNNING")) {
            try (CloseableHttpClient client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(getRequestConfig())
                    .build()) {
                Thread.sleep(1000);
                String url = KodexaCloud.getUrl() + "/api/sessions/" + session.getId() + "/executions/" + execution.getId();
                HttpGet post = new HttpGet(url);
                post.addHeader("x-access-token", KodexaCloud.getAccessToken());
                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new KodexaException("Unable to create session on Kodexa, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
                }
                execution = jsonOm.readValue(response.getEntity().getContent(), CloudExecution.class);

                if (!execution.getStatus().equals(status)) {
                    log.info("Execution Status changed from " + status + " => " + execution.getStatus());
                    status = execution.getStatus();
                }
            } catch (IOException | InterruptedException e) {
                throw new KodexaException("Unable to connect to Kodexa", e);
            }
        }

        if (execution.getStatus().equals("FAILED")) {
            throw new KodexaException("Processing failed");
        }
    }

    protected RequestConfig getRequestConfig() {
        int timeout = 120; // seconds
        return RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
    }

    protected CloudSession createSession(CloudSessionType sessionType) {
        log.info("Creating session in Kodexa");
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .build()) {
            String url = KodexaCloud.getUrl() + "/api/sessions?" + sessionType + "=" + organizationSlug + "/" + serviceSlug;

            log.info("Connecting to [" + url + "]");

            HttpPost post = new HttpPost(url);
            post.addHeader("x-access-token", KodexaCloud.getAccessToken());
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new KodexaException("Unable to create session on Kodexa, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }
            CloudSession session = jsonOm.readValue(response.getEntity().getContent(), CloudSession.class);
            log.info("Session created [" + session + "]");
            return session;
        } catch (IOException e) {
            throw new KodexaException("Unable to create session on Kodexa", e);
        }
    }
}
