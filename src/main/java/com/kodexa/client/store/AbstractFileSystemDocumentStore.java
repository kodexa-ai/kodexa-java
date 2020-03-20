package com.kodexa.client.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import com.kodexa.client.connectors.Connector;
import com.kodexa.client.sink.Sink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractFileSystemDocumentStore implements Sink, Connector, DocumentStore {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    protected final String path;
    protected final boolean forceInitialize;
    protected final File storeFolder;
    protected final File indexFile;
    protected List<String> index = new ArrayList<>();
    private int position = 0;

    protected abstract String getExtension();

    public AbstractFileSystemDocumentStore(String path, boolean forceInitialize) {
        this.path = path;
        this.forceInitialize = forceInitialize;

        this.storeFolder = new File(path);
        this.indexFile = new File(path + File.separator + "index.json");

        if (forceInitialize || !storeFolder.exists()) {
            log.info("Initialize message pack store [" + storeFolder + "]");
            try {
                if (storeFolder.exists()) {
                    FileUtils.deleteDirectory(storeFolder);
                }
                FileUtils.forceMkdir(storeFolder);

                saveIndex();
            } catch (IOException e) {
                throw new KodexaException("Unable to delete store folder [" + path + "]", e);
            }
        } else {
            readIndex();
        }
    }

    public void readIndex() {
        try {
            index = AbstractFileSystemDocumentStore.OBJECT_MAPPER.readValue(
                    indexFile, new TypeReference<List<String>>() {
                    });
        } catch (IOException e) {
            throw new KodexaException("Unable to read index.json for store [" + indexFile.getAbsolutePath() + "]", e);
        }
    }

    public void saveIndex() {
        try {
            AbstractFileSystemDocumentStore.OBJECT_MAPPER.writeValue(indexFile, index);
        } catch (IOException e) {
            throw new KodexaException("Unable to write index.json for store [" + indexFile.getAbsolutePath() + "]", e);
        }
    }

    @Override
    public InputStream getSource(Document document) {
        throw new UnsupportedOperationException("Unable to get source content from the message pack store");
    }

    protected File getFile(String uuid) {
        return new File(storeFolder.getAbsolutePath() + File.separator + uuid + "."+getExtension());
    }

    @Override
    public String getName() {
        return "Message Pack Document Store";
    }

    @Override
    public int getCount() {
        return index.size();
    }

    public void resetConnector() {
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < index.size();
    }

    @Override
    public Document next() {
        Document document = getDocument(position);
        position++;
        return document;
    }

    public abstract Document getDocument(int position);
}
