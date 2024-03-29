package com.kodexa.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A Kodexa document is a representation of a set of content which combines content, metadata
 * and features it is the core model for handling content
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    private final static ObjectMapper OBJECT_MAPPER;

    private final static ObjectMapper OBJECT_MAPPER_MSGPACK;

    public static final String CURRENT_VERSION = "4.0.1";

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        OBJECT_MAPPER_MSGPACK = new ObjectMapper(new MessagePackFactory());
        OBJECT_MAPPER_MSGPACK.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public Document() {
        persistenceLayer = new SqlitePersistenceLayer(this);
    }

    public Document(InputStream kddbInputStream) {
        persistenceLayer = new SqlitePersistenceLayer(kddbInputStream, this);
        persistenceLayer.loadDocument();
        this.setVersion(CURRENT_VERSION);
    }

    public Document(File kddbFile) {
        persistenceLayer = new SqlitePersistenceLayer(kddbFile, this);
        persistenceLayer.loadDocument();
        this.setVersion(CURRENT_VERSION);
    }

    public List<ContentException> getContentExceptions() {
        return persistenceLayer.getContentExceptions();
    }

    @JsonIgnore
    private SqlitePersistenceLayer persistenceLayer;

    public long getNodeCountByType(String type) {
        return persistenceLayer.getNodeCountByType(type);
    }

    public List<ContentNode> getTaggedNodes() {
        return persistenceLayer.getTaggedNodes();
    }

    public ContentNode getNodeByUuid(String uuid) {
        return persistenceLayer.getNodeByUuid(uuid);
    }

    public List<ContentNode> getTaggedNodeByTagUuid(String tagUuid) {
        return persistenceLayer.getTaggedNodeByTagUuid(tagUuid);
    }

    private Map<String, Object> metadata = new HashMap<>();

    @JsonProperty("source")
    private SourceMetadata source = new SourceMetadata();

    @JsonProperty("content_node")
    private ContentNode contentNode;

    private boolean virtual = false;

    private List<String> mixins = new ArrayList<>();

    private List<String> labels = new ArrayList<>();

    private List<String> taxonomies = new ArrayList<>();

    private List<ContentClassification> classes = new ArrayList<>();

    private String uuid = UUID.randomUUID()
                              .toString();

    private String version = Document.CURRENT_VERSION;

    /**
     * Create a new instance of a Document from JSON string
     *
     * @param json String representation of the document JSON
     * @return De-serialized Document
     */
    @Deprecated
    public static Document fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Document.class);
        }
        catch (JsonProcessingException e) {
            throw new KodexaException("Unable to convert to Document from JSON", e);
        }
    }

    /**
     * Create a Document from a message packed (kdx) representation
     *
     * @param bytes the bytes for the message packed document
     * @return An instance of a Document
     */
    @Deprecated
    public static Document fromMsgPack(byte[] bytes) {
        try {
            return OBJECT_MAPPER_MSGPACK.readValue(new ByteArrayInputStream(bytes), Document.class);
        }
        catch (IOException e) {
            throw new KodexaException("Unable to convert to Document from message pack", e);
        }
    }

    /**
     * Create a Document from a message packed (kdx) representation
     *
     * @param is Input stream containing the document
     * @return An instance of a Document
     */
    @Deprecated
    public static Document fromMsgPack(InputStream is) {
        try {
            return OBJECT_MAPPER_MSGPACK.readValue(is, Document.class);
        }
        catch (IOException e) {
            throw new KodexaException("Unable to convert to Document from message pack", e);
        }
    }

    /**
     * Create a Document from a message packed (kdx) representation
     *
     * @param file file containing the document
     * @return An instance of a Document
     */
    @Deprecated
    public static Document fromMsgPack(File file) {
        try {
            return OBJECT_MAPPER_MSGPACK.readValue(new ByteArrayInputStream(FileUtils.readFileToByteArray(file)), Document.class);
        }
        catch (IOException e) {
            throw new KodexaException("Unable to convert to Document from message pack", e);
        }
    }

    public static Document fromText(String text) {
        Document newDocument = new Document();
        newDocument.setContentNode(newDocument.createContentNode("text", text));
        newDocument.getMixins()
                   .add("text");
        return newDocument;
    }

    protected SqlitePersistenceLayer getPersistanceLayer() {
        return persistenceLayer;
    }

    @SuppressWarnings("unchecked")
    public static Document fromUrl(String url) {
        Document newDocument = new Document();
        newDocument.getMetadata()
                   .put("connector", "url");
        newDocument.getMetadata()
                   .put("connector_options", new HashMap<String, Object>());
        ((Map) newDocument.getMetadata()
                          .get("connector_options")).put("url", url);

        SourceMetadata sourceMetadata = new SourceMetadata();
        sourceMetadata.setConnector("url");
        sourceMetadata.setOriginalPath(url);

        newDocument.setSource(sourceMetadata);
        return newDocument;
    }

    public static Document fromKddb(File file) {
        return new Document(file);
    }

    public static Document fromInputStream(InputStream inputStream) {
        return new Document(inputStream);
    }

    public static Document fromBytes(byte[] bytes) {
        return new Document(new ByteArrayInputStream(bytes));
    }

    public byte[] toBytes() {
        return persistenceLayer.toBytes();
    }

    public ImmutablePair<InputStream, Long> toInputStream() {
        return persistenceLayer.toInputStream();
    }

    public void close() {
        persistenceLayer.close();
    }

    /**
     * Add the given label to the document
     *
     * @param label the label to add
     * @return the instance of the document
     */
    public Document addLabel(String label) {
        labels.add(label);
        return this;
    }

    /**
     * Remove the given label to the document
     *
     * @param label the label to remove
     * @return the instance of the document
     */
    public Document removeLabel(String label) {
        labels.remove(label);
        return this;
    }

    /**
     * Convert the document to JSON
     *
     * @param pretty include spacing and new lines if true
     * @return JSON representation of document
     */
    @Deprecated
    public String toJson(boolean pretty) {
        try {
            if (pretty) {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(this);
            }
            else {
                return OBJECT_MAPPER.writeValueAsString(this);
            }
        }
        catch (JsonProcessingException e) {
            throw new KodexaException("Unable to convert Document to JSON", e);
        }
    }

    /**
     * Create a message pack representation of this document
     *
     * @return Byte array of the document packed
     */
    @Deprecated
    public byte[] toMsgPack() {
        try {
            return OBJECT_MAPPER_MSGPACK.writeValueAsBytes(this);
        }
        catch (JsonProcessingException e) {
            throw new KodexaException("Unable to write this document to message pack", e);
        }
    }

    /**
     * Create a JSON representation of this document
     *
     * @return String containing the JSON representation
     */
    @Deprecated
    public String toJson() {
        return toJson(false);
    }

    public ContentNode createContentNode(String type, String content, ContentNode parent, int index) {
        ContentNode contentNode = new ContentNode(this);
        contentNode.setType(type);
        contentNode.setContent(content);
        contentNode.setIndex(index);
        contentNode.setContentParts(List.of(content));

        if (parent != null) {
            contentNode.setParent(parent);
            contentNode.setParentId(Integer.valueOf(parent.getUuid()));
        }

        persistenceLayer.updateNode(contentNode);
        return contentNode;
    }

    public ContentNode createContentNode(String type, String content) {
        return createContentNode(type, content, null,0);
    }

    public void addMixin(String spatial) {
        if (!this.mixins.contains(spatial)) {
            this.mixins.add(spatial);
        }
    }

    public int getNumberOfInsights() {
        return persistenceLayer.getNumberOfInsights();
    }
}
