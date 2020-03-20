package com.kodexa.client.store;

import com.kodexa.client.Document;
import com.kodexa.client.KodexaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;

@Slf4j
public class MsgPackDocumentStore extends AbstractFileSystemDocumentStore {

    @Override
    protected String getExtension() {
        return "msgpack";
    }

    public MsgPackDocumentStore(String path) {
        super(path, false);
    }

    public MsgPackDocumentStore(String path, boolean forceInitialize) {
        super(path, forceInitialize);
    }

    @Override
    public void sink(Document document) {
        try {
            readIndex();
            FileUtils.writeByteArrayToFile(getFile(document.getUuid()), document.toMsgPack());
            index.add(document.getUuid());
            saveIndex();
        } catch (IOException e) {
            throw new KodexaException("Unable to write document to store", e);
        }

    }

    @Override
    public Document getDocument(int position) {
        try {
            byte[] msgPack = FileUtils.readFileToByteArray(getFile(index.get(position)));
            return Document.fromMsgPack(msgPack);
        } catch (IOException e) {
            throw new KodexaException("Unable to read the document at index " + position, e);
        }
    }
}
