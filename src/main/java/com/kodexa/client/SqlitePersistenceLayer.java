package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A Persistence Layer that works with the document to allow it to be interacted with
 */
public class SqlitePersistenceLayer {

    private final static ObjectMapper OBJECT_MAPPER_MSGPACK;
    private final Document document;

    Map<Integer, String> nodeTypes;

    Map<Integer, String> featureTypeNames;

    static {
        OBJECT_MAPPER_MSGPACK = new ObjectMapper(new MessagePackFactory());
        OBJECT_MAPPER_MSGPACK.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private String dbPath;
    private Jdbi jdbi;
    private boolean tempFile = false;

    public SqlitePersistenceLayer(Document document) {
        File file = null;
        try {
            this.document = document;
            file = File.createTempFile("kdx", "kddb");
            this.dbPath = file.getAbsolutePath();
            this.tempFile = true;
            file.deleteOnExit();
            this.initializeLayer();
            this.initializeDb();
        } catch (IOException e) {
            throw new KodexaException("Unable to initialize the temp file for KDDB", e);
        }
    }

    private void initializeDb() {

        jdbi.withHandle(handle -> {
            handle.execute("CREATE TABLE version (id integer primary key, version text)");
            handle.execute("CREATE TABLE metadata (id integer primary key, metadata text)");
            handle.execute("CREATE TABLE cn (id integer primary key, nt INTEGER, pid INTEGER, idx INTEGER)");
            handle.execute(
                    "CREATE TABLE cnp (id integer primary key, cn_id INTEGER, pos integer, content text, content_idx integer)");

            handle.execute("CREATE TABLE n_type (id integer primary key, name text)");
            handle.execute("CREATE TABLE f_type (id integer primary key, name text)");
            handle.execute(
                    "CREATE TABLE f_value (id integer primary key, hash integer, binary_value blob, single integer)");
            handle.execute(
                    "CREATE TABLE f (id integer primary key, cn_id integer, f_type INTEGER, fvalue_id integer)");

            handle.execute("CREATE UNIQUE INDEX n_type_uk ON n_type(name);");
            handle.execute("CREATE UNIQUE INDEX f_type_uk ON f_type(name);");
            handle.execute("CREATE INDEX cn_perf ON cn(nt);");
            handle.execute("CREATE INDEX cnp_perf ON cnp(cn_id);");
            handle.execute("CREATE INDEX f_value_hash ON f_value(hash);");

            return handle;
        });
    }

    public void close() {
        if (tempFile) {
            try {
                Files.delete(Path.of(this.dbPath));
            } catch (IOException e) {
                throw new KodexaException("Unable to delete temp file", e);
            }
        }
    }

    public SqlitePersistenceLayer(InputStream kddbInputStream, Document document) {
        final File tempFile;
        try {
            this.document = document;
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

    public SqlitePersistenceLayer(File kddbFile, Document document) {
        this.dbPath = kddbFile.getAbsolutePath();
        this.document = document;
        this.initializeLayer();
    }

    private void initializeLayer() {
        try {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.setJournalMode(SQLiteConfig.JournalMode.OFF);
            jdbi = Jdbi.create("jdbc:sqlite:" + dbPath, config.toProperties());
        } catch (ClassNotFoundException e) {
            throw new KodexaException("Unable to create persistence layer for KDDB object", e);
        }
    }

    protected void loadDocument() {
        // This method will update the document to match the contents
        // of the KDDB database
        jdbi.withHandle(handle -> {

            // We get all the metadata back
            try {
                byte[] metadataPack = (byte[]) handle.createQuery("SELECT * FROM metadata")
                        .mapToMap()
                        .first().get("metadata");

                Document baseDocument = OBJECT_MAPPER_MSGPACK.readValue(metadataPack, Document.class);
                document.setSource(baseDocument.getSource());
                document.setClasses(baseDocument.getClasses());
                document.setLabels(baseDocument.getLabels());
                document.setMetadata(baseDocument.getMetadata());
                document.setUuid(baseDocument.getUuid());

                // Lets get all the node types and feature type/name combinations
                nodeTypes =
                        handle.createQuery("SELECT id, name FROM n_type")
                                .mapToMap()
                                .collect(Collectors.toMap(x -> Integer.valueOf(String.valueOf(x.get("id"))), x -> String.valueOf(x.get("name"))));

                featureTypeNames =
                        handle.createQuery("SELECT id, name FROM f_type")
                                .mapToMap()
                                .collect(Collectors.toMap(x -> Integer.valueOf(String.valueOf(x.get("id"))), x -> String.valueOf(x.get("name"))));
            } catch (IOException e) {
                throw new KodexaException("Unable to unpack metadata", e);
            }

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

        List<Map<String, Object>> features =
                handle.createQuery("SELECT id, f_type, fvalue_id FROM f where cn_id is :nodeId").bind("nodeId", contentNode.getUuid())
                        .mapToMap()
                        .list();

        for (Map<String, Object> feature : features) {

            Map<String, Object> featureValue =
                    handle.createQuery("SELECT binary_value, single FROM f_value where id is :fvalue_id").bind("fvalue_id", feature.get("fvalue_id"))
                            .mapToMap()
                            .first();

            ContentFeature contentFeature = new ContentFeature();
            contentFeature.setFeatureType(featureTypeNames.get(feature.get("f_type")).split(":")[0]);
            contentFeature.setName(featureTypeNames.get(feature.get("f_type")).split(":")[1]);
            contentFeature.setSingle(Integer.valueOf(1) == featureValue.get("single"));
            TypeReference<ArrayList<Object>> typeRef
                    = new TypeReference<>() {
            };

            try {
                contentFeature.setValue(OBJECT_MAPPER_MSGPACK.readValue((byte[]) featureValue.get("binary_value"), typeRef));
            } catch (IOException e) {
                throw new KodexaException("Unable to unpack value for feature", e);
            }
        }

        List<Map<String, Object>> childNodes =
                handle.createQuery("SELECT id, nt, pid FROM cn where pid is :nodeId").bind("nodeId", contentNode.getUuid())
                        .mapToMap()
                        .list();

        for (Map<String, Object> childNode : childNodes) {
            contentNode.getChildren().add(buildNode(childNode, handle));
        }

        return contentNode;
    }

    public byte[] toBytes() {
        try {
            flushMetadata();
            return Files.readAllBytes(Path.of(dbPath));
        } catch (IOException e) {
            throw new KodexaException("Unable to read KDDB file from " + dbPath);
        }
    }

    private void flushMetadata() {
        jdbi.withHandle(handle -> {
            try {
                byte[] metadataBytes = OBJECT_MAPPER_MSGPACK.writeValueAsBytes(document);
                handle.execute("INSERT INTO metadata(metadata, id) VALUES(?, ?)\n" +
                        "  ON CONFLICT(id) DO UPDATE SET metadata=?", metadataBytes, 1, metadataBytes);
            } catch (JsonProcessingException e) {
                throw new KodexaException("Unable to flush metadata to KDDB", e);
            }
            return null;
        });

    }
}
