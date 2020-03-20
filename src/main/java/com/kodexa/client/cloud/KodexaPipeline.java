package com.kodexa.client.cloud;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.connectors.Connector;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.registry.SourceRegistry;
import com.kodexa.client.sink.Sink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * A Kodexa-hosted Pipeline
 */
@Slf4j
public class KodexaPipeline extends AbstractKodexaSession {

    private final Options options;
    private final Connector connector;
    private Sink sink;

    public KodexaPipeline(String organizationSlug, String serviceSlug, Connector connector) {
        this(organizationSlug, serviceSlug, connector, new Options());
    }

    public KodexaPipeline(String organizationSlug, String serviceSlug, Connector connector, Options options) {
        super(organizationSlug, serviceSlug);
        this.options = options;
        this.connector = connector;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    private CloudExecution executeService(CloudSession session, Document document, PipelineContext context) {
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
                throw new KodexaException("Unable to create session on Kodexa, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }
            CloudExecution cloudExecution = jsonOm.readValue(response.getEntity().getContent(), CloudExecution.class);
            log.info("Execution created [" + cloudExecution + "]");
            return cloudExecution;
        } catch (IOException e) {
            throw new KodexaException("Unable to create session on Kodexa", e);
        }
    }

    public PipelineContext run() {

        log.info("Starting pipeline");
        CloudSession session = this.createSession(CloudSessionType.pipeline);
        PipelineContext pipelineContext = new PipelineContext();
        connector.forEachRemaining(document -> {
            CloudExecution execution = executeService(session, document, pipelineContext);
            waitForExecution(session, execution);

            if (sink != null) {
                log.info("Writing to sink " + sink.getName());
                sink.sink(document);
            }

        });

        log.info("Pipeline completed");
        return pipelineContext;
    }

}
