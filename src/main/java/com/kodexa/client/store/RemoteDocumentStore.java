package com.kodexa.client.store;

import com.kodexa.client.KodexaException;
import com.kodexa.client.registry.SourceRegistry;
import com.kodexa.client.remote.AbstractKodexaConnection;
import com.kodexa.client.remote.CloudExecution;
import com.kodexa.client.remote.KodexaPlatform;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;

@Slf4j
public class RemoteDocumentStore extends AbstractKodexaConnection {

    private final String ref;

    public RemoteDocumentStore(String ref) {
        this.ref = ref;
    }

    @SneakyThrows
    public void putNative(String path, InputStream content) throws FileAlreadyExistsException {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .build()) {

            URI uri = URI.create(KodexaPlatform.getUrl() + "/api/stores/" + this.ref.replace(":", "/") + "/fs/");
            URI fullUri = new URI(uri.getScheme(), uri.getHost(), uri.getPath() + path, null);
            log.info("Connecting to [" + fullUri + "]");
            HttpPost post = new HttpPost(fullUri);
            post.addHeader("x-access-token", KodexaPlatform.getAccessToken());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ByteArrayBody fileBody = new ByteArrayBody(IOUtils.toByteArray(content), path);
            builder.addPart("file", fileBody);
            post.setEntity(builder.build());
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                if (response.getStatusLine().getStatusCode() == 400) {
                    throw new FileAlreadyExistsException("There is already a document family at path [" + path + "]");
                } else {
                    throw new KodexaException("Unable to create a session, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
                }
            }
            log.info("Upload complete");
        }
    }
}
