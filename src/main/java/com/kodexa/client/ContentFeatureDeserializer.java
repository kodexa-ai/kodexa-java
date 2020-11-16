package com.kodexa.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.List;

public class ContentFeatureDeserializer extends StdDeserializer<ContentFeature> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CollectionType collectionType =
            TypeFactory
                    .defaultInstance()
                    .constructCollectionType(List.class, Object.class);

    public ContentFeatureDeserializer() {
        this(null);
    }

    public ContentFeatureDeserializer(Class<ContentFeature> vc) {
        super(vc);
    }

    @Override
    public ContentFeature deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String fullName = node.get("name").asText();
        Boolean single = node.get("single").asBoolean();

        JsonNode nodeValues = node.get("value");

        ContentFeature contentFeature = new ContentFeature();
        contentFeature.setFeatureType(fullName.split(":")[0]);
        contentFeature.setName(fullName.split(":")[1]);
        contentFeature.setSingle(single);

        if (null != nodeValues
                && nodeValues.isArray())
            contentFeature.setValue(mapper.reader(collectionType).readValue(nodeValues));

        return contentFeature;
    }
}
