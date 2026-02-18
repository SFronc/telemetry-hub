package com.sfronc.hub.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public final class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private Json() {}

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static byte[] toBytes(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON serialize failed: " + e.getMessage(), e);
        }
    }

    public static <T> T fromBytes(byte[] json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("JSON deserialize failed: " + e.getMessage(), e);
        }
    }

    public static JsonNode toTree(Object value) {
        return MAPPER.valueToTree(value);
    }

    public static <T> T treeToValue(JsonNode node, Class<T> type) {
        try {
            return MAPPER.treeToValue(node, type);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON treeToValue failed: " + e.getMessage(), e);
        }
    }
}
