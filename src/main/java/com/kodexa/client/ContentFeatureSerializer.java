package com.kodexa.client;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ContentFeatureSerializer extends StdSerializer<ContentFeature> {

    public ContentFeatureSerializer() {
        this(null);
    }

    public ContentFeatureSerializer(Class<ContentFeature> t) {
        super(t);
    }

    @Override
    public void serialize(
            ContentFeature value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField("name", value.getFeatureType() + ":" + value.getName());
        jgen.writeArrayFieldStart("value");
        for (Object obj : value.getValue()) {
            jgen.writeObject(obj);
        }
        jgen.writeEndArray();
        jgen.writeBooleanField("single", value.isSingle());
        jgen.writeEndObject();
    }
}
