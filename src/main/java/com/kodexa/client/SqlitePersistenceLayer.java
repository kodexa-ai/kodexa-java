package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.units.qual.A;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

/**
 * A Persistence Layer that works with the document to allow it to be interacted with
 */
public class SqlitePersistenceLayer {

    private final static ObjectMapper OBJECT_MAPPER_MSGPACK;
    private final Document document;

    Map<Integer, String> nodeTypes;

    Map<Integer, String> featureTypeNames;

    private final String FEATURE_INSERT = "INSERT INTO f (cn_id, f_type, fvalue_id) VALUES (?,?,?)";
    private final String CONTENT_NODE_INSERT = "INSERT INTO cn (pid, nt, idx) VALUES (?,?,?)";
    private final String CONTENT_NODE_PART_INSERT = "INSERT INTO cnp (cn_id, pos, content, content_idx) VALUES (?,?,?,?)";
    private final String NOTE_TYPE_INSERT = "insert into n_type(name) values (?)";
    private final String NODE_TYPE_LOOKUP = "select id from n_type where name = ?";
    private final String FEATURE_VALUE_LOOKUP = "select id from f_value where hash=?";
    private final String FEATURE_VALUE_INSERT = "insert into f_value(binary_value, hash, single) values (?,?,?)";
    private final String FEATURE_TYPE_INSERT = "insert into f_type(name) values (?)";
    private final String FEATURE_TYPE_LOOKUP = "select id from f_type where name = ?";
    private final String METADATA_INSERT = "insert into metadata(id,metadata) values (1,?)";
    private final String METADATA_DELETE = "delete from metadata where id=1";
    private final String VERSION_INSERT = "insert into version(id,version) values (1,'4.0.0')";
    private final String VERSION_DELETE = "delete from version where id=1";

    static {
        OBJECT_MAPPER_MSGPACK = new ObjectMapper(new MessagePackFactory());
        OBJECT_MAPPER_MSGPACK.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private String dbPath;
    private Jdbi jdbi;
    private boolean tempFile = false;
    private final MessageDigest md;

    public SqlitePersistenceLayer(Document document) {
        File file = null;

        try {
            md = MessageDigest.getInstance("SHA-1");

            this.document = document;
            file = File.createTempFile("kdx", "kddb");
            this.dbPath = file.getAbsolutePath();
            this.tempFile = true;
            file.deleteOnExit();
            this.initializeLayer();
            this.initializeDb();


        } catch (IOException | NoSuchAlgorithmException e) {
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
            handle.execute("CREATE INDEX cn_perf2 ON cn(pid);");
            handle.execute("CREATE INDEX cnp_perf ON cnp(cn_id, pos);");
            handle.execute("CREATE INDEX f_perf ON cnp(cn_id);");
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
            md = MessageDigest.getInstance("SHA-1");

            this.document = document;
            tempFile = File.createTempFile("kodexa", "kddb");
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(kddbInputStream, out);
            }
            this.dbPath = tempFile.getAbsolutePath();

            this.initializeLayer();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new KodexaException("Unable to create persistence layer for KDDB object", e);
        }
    }

    public SqlitePersistenceLayer(File kddbFile, Document document) {

        try {
            md = MessageDigest.getInstance("SHA-1");

            this.dbPath = kddbFile.getAbsolutePath();
            this.document = document;
            this.initializeLayer();
        } catch (NoSuchAlgorithmException e) {
            throw new KodexaException("Unable to initialize KDDB", e);
        }

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
                document.setMixins(baseDocument.getMixins());

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
        contentNode.setType(nodeTypes.get(contentNodeValues.get("nt")));

        List<Map<String, Object>> features =
                handle.createQuery("SELECT id, f_type, fvalue_id FROM f where cn_id = :nodeId").bind("nodeId", contentNode.getUuid())
                        .mapToMap()
                        .list();

        List<Map<String, Object>> contentParts =
                handle.createQuery("SELECT content, content_idx FROM cnp where cn_id = :nodeId order by pos asc").bind("nodeId", contentNode.getUuid())
                        .mapToMap()
                        .list();

        List<String> parts = new ArrayList<>();
        contentNode.setContentParts(new ArrayList<>());
        for (Map<String, Object> contentPart : contentParts) {
            if (contentPart.get("content") != null) {
                contentNode.getContentParts().add(contentPart.get("content"));
                parts.add(String.valueOf(contentPart.get("content")));
            } else {
                contentNode.getContentParts().add(contentPart.get("content_idx"));
            }
        }

        contentNode.setContent(String.join(" ", parts));

        for (Map<String, Object> feature : features) {

            Map<String, Object> featureValue =
                    handle.createQuery("SELECT binary_value, single FROM f_value where id = :fvalue_id").bind("fvalue_id", feature.get("fvalue_id"))
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

            contentNode.getFeatures().add(contentFeature);
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
            replaceContent();
            return Files.readAllBytes(Path.of(dbPath));
        } catch (IOException e) {
            throw new KodexaException("Unable to read KDDB file from " + dbPath);
        }
    }

    private void replaceContent() {
        jdbi.withHandle(handle -> {
            handle.execute("delete from f_value");
            handle.execute("delete from f");
            handle.execute("delete from cnp");
            handle.execute("delete from cn");

            writeNode(handle, document.getContentNode(), null, true);

            return null;
        });

    }

    private void writeNode(Handle handle, ContentNode contentNode, Integer parentId, boolean includeChildren) {
        if (contentNode == null)
            return;

        int nodeTypeId = getNodeTypeId(handle, contentNode.getType());
        int nodeId = (int) handle.createUpdate(CONTENT_NODE_INSERT).bind(0, parentId).bind(1, nodeTypeId).bind(2, contentNode.getIndex()).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()");
        contentNode.setUuid(String.valueOf(nodeId));

        if (contentNode.getContentParts() == null || contentNode.getContentParts().isEmpty()) {
            if (contentNode.getContent() != null) {
                contentNode.setContentParts(List.of(contentNode.getContent()));
            }
        }

        int pos = 0;
        for (Object contentPart : contentNode.getContentParts()) {
            if (contentPart instanceof String) {
                handle.execute(CONTENT_NODE_PART_INSERT, nodeId, pos, contentPart, null);
            } else {
                handle.execute(CONTENT_NODE_PART_INSERT, nodeId, pos, null, contentPart);
            }
            pos++;
        }

        for (ContentFeature feature : contentNode.getFeatures()) {
            writeFeature(handle, feature, contentNode);
        }

        if (includeChildren) {
            for (ContentNode child : contentNode.getChildren()) {
                writeNode(handle, child, nodeId, true);
            }
        }
    }

    private int writeFeature(Handle handle, ContentFeature feature, ContentNode contentNode) {
        int fTypeId = getFeatureTypeName(handle, feature.getFeatureType() + ":" + feature.getName());

        // We need to work out the feature value
        try {
            byte[] packedValue = OBJECT_MAPPER_MSGPACK.writeValueAsBytes(feature.getValue());
            Formatter formatter = new Formatter();
            for (byte b : md.digest(packedValue)) {
                formatter.format("%02x", b);
            }
            long hash = new BigInteger(formatter.toString(), 16).mod(BigDecimal.valueOf(pow(10, 8)).toBigInteger()).longValue();
            Optional<Map<String, Object>> result = handle.createQuery(FEATURE_VALUE_LOOKUP).bind(0, hash).mapToMap().findFirst();
            int featureValueId;
            if (result.isPresent()) {
                featureValueId = (int) result.get().get("id");
            } else {
                featureValueId = (Integer) handle.createUpdate(FEATURE_VALUE_INSERT).bind(0, packedValue).bind(1, hash).bind(2, feature.isSingle()).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()");
            }

            return (Integer) handle.createUpdate(FEATURE_INSERT).bind(0, contentNode.getUuid()).bind(1, fTypeId).bind(2, featureValueId).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()");

        } catch (JsonProcessingException e) {
            throw new KodexaException("Unable to pack feature value", e);
        }

    }

    private int getNodeTypeId(Handle handle, String type) {
        Optional<Map<String, Object>> nodeType = handle.createQuery("SELECT id, name FROM n_type where name is :nodeType").bind("nodeType", type)
                .mapToMap()
                .findFirst();

        return nodeType.map(stringObjectMap -> (int) stringObjectMap.get("id")).orElseGet(() -> (Integer) handle.createUpdate("INSERT INTO n_type(name) VALUES(?)").bind(0, type).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()"));
    }

    private int getFeatureTypeName(Handle handle, String type) {
        Optional<Map<String, Object>> nodeType = handle.createQuery("SELECT id, name FROM f_type where name is :featureType").bind("featureType", type)
                .mapToMap()
                .findFirst();

        return nodeType.map(stringObjectMap -> (int) stringObjectMap.get("id")).orElseGet(() -> (Integer) handle.createUpdate("INSERT INTO f_type(name) VALUES(?)").bind(0, type).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()"));
    }

    private void flushMetadata() {
        jdbi.withHandle(handle -> {
            try {
                Document copyDocument = new Document();
                copyDocument.setSource(document.getSource());
                copyDocument.setClasses(document.getClasses());
                copyDocument.setLabels(document.getLabels());
                copyDocument.setMetadata(document.getMetadata());
                copyDocument.setUuid(document.getUuid());
                copyDocument.setMixins(document.getMixins());

                byte[] metadataBytes = OBJECT_MAPPER_MSGPACK.writeValueAsBytes(copyDocument);
                handle.execute("INSERT INTO metadata(metadata, id) VALUES(?, ?)\n" +
                        "  ON CONFLICT(id) DO UPDATE SET metadata=?", metadataBytes, 1, metadataBytes);
            } catch (JsonProcessingException e) {
                throw new KodexaException("Unable to flush metadata to KDDB", e);
            }
            return null;
        });

    }
}
