package com.kodexa.client.store;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class JsonDocumentStore extends AbstractFileSystemDocumentStore {

    @Override
    protected String getExtension() {
        return "json";
    }

    public JsonDocumentStore(String path) {
        super(path, false);
    }

    public JsonDocumentStore(String path, boolean forceInitialize) {
        super(path, forceInitialize);
    }

    @Override
    public Document getDocument(int position) {
        try {
            return Document.fromJson(FileUtils.readFileToString(getFile(index.get(position)), Charset.defaultCharset()));
        }
        catch (IOException e) {
            throw new KodexaException("Unable to read the document at index " + position, e);
        }
    }
}
