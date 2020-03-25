package com.kodexa.client.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.registry.SourceRegistry;
import com.kodexa.client.store.TableStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Abstract base for both Cloud Service and Cloud Pipelines in Kodexa
 */
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

    public TableStore getTableStore(CloudSession session, CloudExecution execution, CloudStore cloudStore) {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .build()) {
            Thread.sleep(1000);
            String url = KodexaCloud.getUrl() + "/api/sessions/" + session.getId() + "/executions/" + execution.getId() + "/stores/" + cloudStore.getId();
            HttpGet post = new HttpGet(url);
            post.addHeader("x-access-token", KodexaCloud.getAccessToken());
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new KodexaException("Unable to get store from Kodexa, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }
            return jsonOm.readValue(response.getEntity().getContent(), TableStore.class);
        } catch (IOException | InterruptedException e) {
            throw new KodexaException("Unable to connect to Kodexa", e);
        }
    }

    public Document getOutputDocument(CloudSession session, CloudExecution execution) {
        CloudDocumentReference outputDocument = execution.getOutputDocument();
        if (outputDocument == null) {
            return null;
        }
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .build()) {
            Thread.sleep(1000);
            String url = KodexaCloud.getUrl() + "/api/sessions/" + session.getId() + "/executions/" + execution.getId() + "/documents/" + outputDocument.getCloudDocument().getId();
            HttpGet post = new HttpGet(url);
            post.addHeader("x-access-token", KodexaCloud.getAccessToken());
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new KodexaException("Unable to get store, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }
            return Document.fromMsgPack(response.getEntity().getContent());
        } catch (IOException | InterruptedException e) {
            throw new KodexaException("Unable to connect to Kodexa", e);
        }
    }

    public void mergeStores(CloudSession session, CloudExecution execution, PipelineContext pipelineContext) {
        for (CloudStore store : execution.getStores()) {
            TableStore tableStore = getTableStore(session, execution, store);
            pipelineContext.addStore(store.getName(), tableStore);
            log.info("Store " + store.getName() + " with " + tableStore.getRows().size() + " rows");
        }
    }


    protected CloudExecution waitForExecution(CloudSession session, CloudExecution execution) {
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
                    throw new KodexaException("Unable to create a session on Kodexa, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
                }

                String responseJson = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                execution = jsonOm.readValue(responseJson, CloudExecution.class);
                if (!execution.getStatus().equals(status)) {
                    log.info("Execution Status changed from " + status + " => " + execution.getStatus());
                    status = execution.getStatus();
                }
            } catch (IOException | InterruptedException e) {
                throw new KodexaException("Unable to connect to Kodexa", e);
            }
        }


        if (execution.getStatus().equals("FAILED")) {
            CloudExceptionDetail exceptionDetail = execution.getExceptionDetail();
            log.error("Failed: " + exceptionDetail.getMessage());
            if (exceptionDetail.getErrorType() != null)
                log.error("Exception Type: " + exceptionDetail.getErrorType());
            log.error("More information is available:\n\n" + exceptionDetail.getHelp() + "\n");
            throw new KodexaException("Failed:" + exceptionDetail.getMessage() + "]");
        } else {
            log.info("Execution completed with " + execution.getStores().size() + " stores and " + execution.getDocumentReferences().size() + " documents");
        }

        return execution;
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
                throw new KodexaException("Unable to create a session, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }
            CloudSession session = jsonOm.readValue(response.getEntity().getContent(), CloudSession.class);
            log.info("Session created [" + session + "]");
            return session;
        } catch (IOException e) {
            throw new KodexaException("Unable to create session on Kodexa", e);
        }
    }

    /**
     * Execute the service in Kodexa
     *
     * @param session  The session to use
     * @param document The document to send
     * @param context  The context for the pipeline
     * @param options  The options for the execution
     * @return An instance of a cloud execution
     */
    public CloudExecution executeService(CloudSession session, Document document, PipelineContext context, Options options) {
        log.info("Executing service in Kodexa");
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .build()) {
            String url = KodexaCloud.getUrl() + "/api/sessions/" + session.getId() + "/execute";
            log.info("Connecting to [" + url + "]");
            HttpPost post = new HttpPost(url);
            post.addHeader("x-access-token", KodexaCloud.getAccessToken());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            if (options.isAttachSource()) {
                log.info("Attaching source file");
                InputStream inputStream = SourceRegistry.getInstance().getSource(document);
                ByteArrayBody fileBody = new ByteArrayBody(IOUtils.toByteArray(inputStream), "test.doc");
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addPart("file", fileBody);
            }

            StringBody optionsBody = new StringBody(jsonOm.writeValueAsString(options.get()), ContentType.APPLICATION_JSON);
            builder.addPart("options", optionsBody);
            post.setEntity(builder.build());
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new KodexaException("Unable to create a session, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }
            String responseJson = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            CloudExecution cloudExecution = jsonOm.readValue(responseJson, CloudExecution.class);
            log.info("Execution started [" + cloudExecution.getId() + "]");
            return cloudExecution;
        } catch (IOException e) {
            throw new KodexaException("Unable to create a session on Kodexa", e);
        }
    }
}
