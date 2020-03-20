package com.kodexa.client.cloud;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.pipeline.PipelineContext;
import com.kodexa.client.registry.SourceRegistry;
import com.kodexa.client.steps.PipelineStep;
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
 * A step that is hosted in the Kodexa Cloud
 */
@Slf4j
public class KodexaService extends AbstractKodexaSession implements PipelineStep {

    private final Options options;

    public KodexaService(String organizationSlug, String serviceSlug) {
        this(organizationSlug, serviceSlug, new Options());
    }

    public KodexaService(String organizationSlug, String serviceSlug, Options options) {
        super(organizationSlug, serviceSlug);
        this.options = options;
    }

    @Override
    public Document process(Document document, PipelineContext context) {

        CloudSession session = this.createSession(CloudSessionType.service);
        CloudExecution execution = executeService(session, document, context);
        waitForExecution(session, execution);
        return document;
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

    @Override
    public String getName() {
        return "Kodexa Service [" + KodexaCloud.getUrl() + "/" + organizationSlug + "/" + serviceSlug + "]";
    }

}
