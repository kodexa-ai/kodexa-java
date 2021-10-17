package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.sqlite.SQLiteConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Persistence Layer that works with the document to allow it to be interacted with
 */
public class SqlitePersistenceLayer {

    private final static ObjectMapper OBJECT_MAPPER_MSGPACK;
    private final Document document;

    Map<Integer, String> nodeTypes;

    Map<Integer, String> featureTypeNames;

    private final String FEATURE_INSERT = "INSERT INTO ft (cn_id, f_type, binary_value, single, tag_uuid) VALUES (?,?,?,?,?)";
    private final String CONTENT_NODE_INSERT = "INSERT INTO cn (pid, nt, idx) VALUES (?,?,?)";
    private final String CONTENT_NODE_INSERT_WITH_ID = "INSERT INTO cn (id, pid, nt, idx) VALUES (?,?,?,?)";
    private final String CONTENT_NODE_PART_INSERT = "INSERT INTO cnp (cn_id, pos, content, content_idx) VALUES (?,?,?,?)";

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
                    "CREATE TABLE ft (id integer primary key, cn_id integer, f_type INTEGER, binary_value blob, single integer, tag_uuid text)");

            handle.execute("CREATE UNIQUE INDEX n_type_uk ON n_type(name);");
            handle.execute("CREATE UNIQUE INDEX f_type_uk ON f_type(name);");
            handle.execute("CREATE INDEX cn_perf ON cn(nt);");
            handle.execute("CREATE INDEX cn_perf2 ON cn(pid);");
            handle.execute("CREATE INDEX cnp_perf ON cnp(cn_id, pos);");
            handle.execute("CREATE INDEX f_perf ON ft(cn_id);");
            handle.execute("CREATE INDEX f_perf2 ON ft(tag_uuid);");

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
            this.loadDocument();
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
                document.setVersion(baseDocument.getVersion());


                if (document.getVersion().equals("4.0.0") || document.getVersion().equals("2.0.0")) {

                    handle.execute("CREATE TABLE ft (id integer primary key,cn_id integer,f_type INTEGER, binary_value blob,single integer,tag_uuid text)");
                    handle.execute("insert into ft select f.id, f.cn_id, f.f_type, fv.binary_value, fv.single, null from f, f_value fv where fv.id = f.id");
                    handle.execute("drop table f");
                    handle.execute("drop table f_value");
                    handle.execute("CREATE INDEX f_perf ON ft(cn_id);");
                    handle.execute("CREATE INDEX f_perf2 ON ft(tag_uuid);");
                    document.setVersion("4.0.1");
                    flushMetadata();
                }

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
                    handle.createQuery("SELECT id, nt, pid, idx FROM cn where pid is null")
                            .mapToMap()
                            .list();
            for (Map<String, Object> contentNode : contentNodes) {
                document.setContentNode(buildNode(contentNode, handle));
            }

            return contentNodes;
        });
    }

    public void updateNode(ContentNode node) {
        jdbi.withHandle(handle -> {
            updateNode(handle, node, node.getParentId(), false);
            return null;
        });
    }

    private ContentNode buildNode(Map<String, Object> contentNodeValues, Handle handle) {
        ContentNode contentNode = new ContentNode(this.document);
        contentNode.setUuid(String.valueOf(contentNodeValues.get("id")));
        contentNode.setType(nodeTypes.get(contentNodeValues.get("nt")));
        contentNode.setIndex(Integer.valueOf(String.valueOf(contentNodeValues.get("idx"))));

        Object parentId = contentNodeValues.get("pid");
        contentNode.setParentId(parentId != null ? Integer.valueOf(String.valueOf(parentId)) : null);

        List<Map<String, Object>> features =
                handle.createQuery("SELECT id, f_type, binary_value, single FROM ft where cn_id = :nodeId").bind("nodeId", contentNode.getUuid())
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
            ContentFeature contentFeature = new ContentFeature();
            contentFeature.setFeatureType(featureTypeNames.get(feature.get("f_type")).split(":")[0]);
            contentFeature.setName(featureTypeNames.get(feature.get("f_type")).split(":")[1]);
            contentFeature.setSingle(Integer.valueOf(1).equals(feature.get("single")));
            TypeReference<ArrayList<Object>> typeRef
                    = new TypeReference<>() {
            };

            try {
                contentFeature.setValue(OBJECT_MAPPER_MSGPACK.readValue((byte[]) feature.get("binary_value"), typeRef));
            } catch (IOException e) {
                throw new KodexaException("Unable to unpack value for feature", e);
            }

            contentNode.getFeatures().add(contentFeature);
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

    private void updateNode(Handle handle, ContentNode contentNode, Integer parentId, boolean includeChildren) {
        if (contentNode == null)
            return;

        int nodeTypeId = getNodeTypeId(handle, contentNode.getType());

        if (contentNode.getUuid() == null) {
            int nodeId = (int) handle.createUpdate(CONTENT_NODE_INSERT).bind(0, parentId).bind(1, nodeTypeId).bind(2, contentNode.getIndex()).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()");
            contentNode.setUuid(String.valueOf(nodeId));
        } else {
            // We need to delete everything for the node to replace it
            handle.execute("delete from ft where cn_id=?", contentNode.getUuid());
            handle.execute("delete from cnp where cn_id=?", contentNode.getUuid());
            handle.execute("delete from cn  where id=?", contentNode.getUuid());

            handle.createUpdate(CONTENT_NODE_INSERT_WITH_ID)
                    .bind(0, contentNode.getUuid())
                    .bind(1, parentId)
                    .bind(2, nodeTypeId)
                    .bind(3, contentNode.getIndex())
                    .execute();
        }

        if (contentNode.getContentParts() == null || contentNode.getContentParts().isEmpty()) {
            if (contentNode.getContent() != null) {
                contentNode.setContentParts(List.of(contentNode.getContent()));
            }
        }

        int pos = 0;
        for (Object contentPart : contentNode.getContentParts()) {
            if (contentPart instanceof String) {
                handle.execute(CONTENT_NODE_PART_INSERT, contentNode.getUuid(), pos, contentPart, null);
            } else {
                handle.execute(CONTENT_NODE_PART_INSERT, contentNode.getUuid(), pos, null, contentPart);
            }
            pos++;
        }

        for (ContentFeature feature : contentNode.getFeatures()) {
            writeFeature(handle, feature, contentNode);
        }

        if (includeChildren) {
            for (ContentNode child : contentNode.getChildren()) {
                updateNode(handle, child, Integer.valueOf(contentNode.getUuid()), true);
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
            String tagUuid = null;

            if ("tag".equals(feature.getFeatureType()) && feature.getValue().size() > 0) {
                tagUuid = (String) ((Map) feature.getValue().get(0)).get("uuid");
            }

            return (Integer) handle.createUpdate(FEATURE_INSERT).bind(0, contentNode.getUuid()).bind(1, fTypeId).bind(2, packedValue).bind(3, feature.isSingle()).bind(4, tagUuid).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()");

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

        if (featureTypeNames!=null) {
            Integer hit = featureTypeNames.entrySet().stream()
                    .filter(e -> e.getValue().equals(type))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (hit != null)
                return hit;
        }

        Optional<Map<String, Object>> nodeType = handle.createQuery("SELECT id, name FROM f_type where name is :featureType").bind("featureType", type)
                .mapToMap()
                .findFirst();
        Integer newId = nodeType.map(stringObjectMap -> (int) stringObjectMap.get("id")).orElseGet(() -> (Integer) handle.createUpdate("INSERT INTO f_type(name) VALUES(?)").bind(0, type).executeAndReturnGeneratedKeys("id").mapToMap().first().get("last_insert_rowid()"));
        featureTypeNames =
                handle.createQuery("SELECT id, name FROM f_type")
                        .mapToMap()
                        .collect(Collectors.toMap(x -> Integer.valueOf(String.valueOf(x.get("id"))), x -> String.valueOf(x.get("name"))));

        return newId;
    }

    private void flushMetadata() {
        jdbi.withHandle(handle -> {
            try {
                Document copyDocument = new Document();
                copyDocument.setUuid(document.getUuid());
                copyDocument.setSource(document.getSource());
                copyDocument.setClasses(document.getClasses());
                copyDocument.setLabels(document.getLabels());
                copyDocument.setMetadata(document.getMetadata());
                copyDocument.setUuid(document.getUuid());
                copyDocument.setMixins(document.getMixins());
                copyDocument.setVersion(document.getVersion());

                byte[] metadataBytes = OBJECT_MAPPER_MSGPACK.writeValueAsBytes(copyDocument);
                handle.execute("INSERT INTO metadata(metadata, id) VALUES(?, ?)\n" +
                        "  ON CONFLICT(id) DO UPDATE SET metadata=?", metadataBytes, 1, metadataBytes);
            } catch (JsonProcessingException e) {
                throw new KodexaException("Unable to flush metadata to KDDB", e);
            }
            return null;
        });
    }

    public List<ContentNode> getChildNodes(ContentNode contentNode) {
        return jdbi.withHandle(handle -> {
            List<Map<String, Object>> childNodes =
                    handle.createQuery("SELECT id, nt, pid, idx FROM cn where pid is :nodeId").bind("nodeId", contentNode.getUuid())
                            .mapToMap()
                            .list();
            List<ContentNode> children = new ArrayList<>();
            for (Map<String, Object> childNode : childNodes) {
                ContentNode child = buildNode(childNode, handle);
                child.setParent(contentNode);
                children.add(child);
            }

            return children;
        });
    }

    public String getAllContentForContentNode(ContentNode contentNode, String separator) {
        return null;
    }

    public int getNodeCountByType(String type) {
        return (int) jdbi.withHandle(handle -> handle.createQuery("select count(1) as num from cn where nt=:nt").bind("nt", nodeTypes.entrySet()
                .stream()
                .filter(entry -> type.equals(entry.getValue()))
                .map(Map.Entry::getKey).findFirst().orElse(-1)).mapToMap().first().get("num"));
    }

    public ImmutablePair<InputStream, Long> toInputStream() {
        try {
            flushMetadata();
            return new ImmutablePair<>(new FileInputStream(dbPath), Files.size(Path.of(dbPath)));
        } catch (IOException e) {
            throw new KodexaException("Unable to read KDDB file from " + dbPath);
        }
    }

    public List<ContentNode> getTaggedNodes() {

        // Get all the feature types that are tags - then lets find all those nodes
        return jdbi.withHandle(handle -> {
            List<ContentNode> nodes = new ArrayList<>();
            List<Map<String, Object>> contentNodesRaw =
                    handle.createQuery("select * from cn where id in (select cn_id from ft where f_type in (select id from f_type where name like 'tag:%'))")
                            .mapToMap()
                            .list();
            for (Map<String, Object> contentNodeRaw : contentNodesRaw) {
                nodes.add(buildNode(contentNodeRaw, handle));
            }
            return nodes;
        });
    }

    public List<ContentNode> getTaggedNodeByTagUuid(String tagUuid) {
        // Get all the feature types that are tags - then lets find all those nodes
        return jdbi.withHandle(handle -> {
            List<ContentNode> nodes = new ArrayList<>();
            List<Map<String, Object>> contentNodesRaw =
                    handle.createQuery("select * from cn where id in (select cn_id from ft where tag_uuid = :tagUuid")
                            .bind("tagUuid", tagUuid)
                            .mapToMap()
                            .list();
            for (Map<String, Object> contentNodeRaw : contentNodesRaw) {
                nodes.add(buildNode(contentNodeRaw, handle));
            }
            return nodes;
        });
    }

    public ContentNode getNodeByUuid(String nodeUuid) {
        // Get all the feature types that are tags - then lets find all those nodes
        return jdbi.withHandle(handle -> {
            Map<String, Object> contentNodeRaw =
                    handle.createQuery("select * from cn where id = :nodeUuid")
                            .bind("nodeUuid", nodeUuid)
                            .mapToMap()
                            .first();
            return buildNode(contentNodeRaw, handle);
        });
    }

    public List<ContentNode> getNodesByType(String nodeType) {
        // Get all the feature types that are tags - then lets find all those nodes
        return jdbi.withHandle(handle -> {
            List<ContentNode> nodes = new ArrayList<>();
            List<Map<String, Object>> contentNodesRaw =
                    handle.createQuery("select * from cn where nt = :ntId")
                            .bind("ntId", getNodeTypeId(handle, nodeType))
                            .mapToMap()
                            .list();
            for (Map<String, Object> contentNodeRaw : contentNodesRaw) {
                nodes.add(buildNode(contentNodeRaw, handle));
            }
            return nodes;
        });
    }
}
