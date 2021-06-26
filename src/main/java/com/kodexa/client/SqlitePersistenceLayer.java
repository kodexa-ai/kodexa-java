package com.kodexa.client;

import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * A Persistence Layer that works with the document to allow it to be interacted with
 */
public class SqlitePersistenceLayer {

    private String dbPath;
    private Jdbi jdbi;

    public SqlitePersistenceLayer() {
        this.dbPath = ":memory:";
        this.initializeLayer();
    }

    public SqlitePersistenceLayer(InputStream kddbInputStream) {
        final File tempFile;
        try {
            tempFile = File.createTempFile("kodexa", "kddb");
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(kddbInputStream, out);
            }
            this.dbPath = tempFile.getAbsolutePath();

            this.initializeLayer();
        } catch (IOException e) {
            throw new KodexaException("Unable to create persistence layer for KDDB object", e);
        }
    }

    public SqlitePersistenceLayer(File kddbFile) {
        this.dbPath = kddbFile.getAbsolutePath();
        this.initializeLayer();
    }

    private void initializeLayer() {
        try {
            Class.forName("org.sqlite.JDBC");
            jdbi = Jdbi.create("jdbc:sqlite:" + dbPath);
        } catch (ClassNotFoundException e) {
            throw new KodexaException("Unable to create persistence layer for KDDB object", e);
        }
    }

    protected void loadDocument(Document document) {
        // This method will update the document to match the contents
        // of the KDDB database
        jdbi.withHandle(handle -> {
            List<Map<String, Object>> contentNodes =
                    handle.createQuery("SELECT id, nt, pid FROM cn where pid is null")
                            .mapToMap()
                            .list();
            for (Map<String, Object> contentNode : contentNodes) {
                document.setContentNode(buildNode(contentNode, handle));
            }

            return contentNodes;
        });

    }

    private ContentNode buildNode(Map<String, Object> contentNodeValues, Handle handle) {
        ContentNode contentNode = new ContentNode();
        contentNode.setUuid(String.valueOf(contentNodeValues.get("id")));

        List<Map<String, Object>> childNodes =
                handle.createQuery("SELECT id, nt, pid FROM cn where pid is :nodeId").bind("nodeId", contentNode.getUuid())
                        .mapToMap()
                        .list();

        for (Map<String, Object> childNode : childNodes) {
            contentNode.getChildren().add(buildNode(childNode, handle));
        }

        return contentNode;
    }
}
