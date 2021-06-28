package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.msgpack.jackson.dataformat.MessagePackFactory;

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

    Map<Integer, String> nodeTypes;

    Map<Integer, String> featureTypeNames;

    static {
        OBJECT_MAPPER_MSGPACK = new ObjectMapper(new MessagePackFactory());
        OBJECT_MAPPER_MSGPACK.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

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

    public byte[] toBytes() throws IOException {
        return Files.readAllBytes(Path.of(dbPath));
    }
}
