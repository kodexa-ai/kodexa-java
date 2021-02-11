package com.kodexa.client.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kodexa.client.KodexaException;
import com.kodexa.client.remote.AbstractKodexaConnection;
import com.kodexa.client.remote.KodexaPlatform;
import com.kodexa.client.remote.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A remote data table store
 */
@Slf4j
public class RemoteTableDataStore extends AbstractKodexaConnection {

    private final String ref;

    public RemoteTableDataStore(String ref) {
        this.ref = ref;
    }

    /**
     * Returns a list of maps that represent all the data in the store
     *
     * @param tableName table name
     * @param page      the page number
     * @param pageSize  page size
     * @return A list of maps each representing a 'row'
     */
    @SneakyThrows
    public List<Map<String, String>> getTable(String tableName, int page, int pageSize) {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(getRequestConfig())
                .build()) {
            String url = KodexaPlatform.getUrl() + "/api/stores/" + ref.replace(":", "/") + "/rows";
            log.info("Connecting to [" + url + "]");

            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("page", String.valueOf(page)).setParameter("pageSize", String.valueOf(pageSize)).setParameter("table", tableName);

            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.addHeader("x-access-token", KodexaPlatform.getAccessToken());

            HttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new KodexaException("Unable to create a session, check your access token and URL [" + response.getStatusLine().getStatusCode() + "]");
            }

            String responseJson = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Page<StoredRow> rowPage = jsonOm.readValue(responseJson, new TypeReference<Page<StoredRow>>() {
            });

            return rowPage.getContent().stream().map(StoredRow::getData).collect(Collectors.toList());


        } catch (IOException e) {
            throw new KodexaException("Unable to create a session on Kodexa", e);
        }
    }

    public List<Map<String, String>> getTable(String tableName) {

        // TODO handle more than 9999 rows
        return getTable(tableName, 1, 9999);

    }
}
