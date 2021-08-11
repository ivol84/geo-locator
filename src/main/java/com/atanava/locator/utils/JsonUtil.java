package com.atanava.locator.utils;

import com.atanava.locator.service.PointTo;
import com.atanava.locator.model.PointId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

import static com.atanava.locator.service.OsmConstants.*;

public class JsonUtil {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static Map<PointTo, JsonNode> convertToMap(ArrayNode source, String format, boolean isMemorySaving) {
        source = normalize(source);
        Map<PointTo, JsonNode> converted = new HashMap<>();

        source.forEach(jsonNode -> {
            PointTo pointTo = getPointTo(jsonNode, format, isMemorySaving);
            JsonNode addressNode = getAddress(jsonNode, format);
            converted.put(pointTo, addressNode);
        });
        return converted;
    }

    public static ArrayNode combine(Collection<JsonNode> parts) {
        ArrayNode result = mapper.createArrayNode();
        parts.stream()
                .filter(jsonNode -> !jsonNode.isArray())
                .forEach(result::add);
        parts.stream()
                .filter(JsonNode::isArray)
                .forEach(jsonNode -> jsonNode.forEach(result::add));

        return result;
    }

    public static ArrayNode getSimple(ArrayNode source) {
        ArrayNode simple = source.deepCopy();
        simple.forEach(jsonNode -> ((ObjectNode) jsonNode).remove(ADDRESS));
        return simple;
    }

    private static ArrayNode normalize(ArrayNode source) {
        return source.findValue(FEATURES) != null ? (ArrayNode) source.findValue(FEATURES) : source;
    }

    private static JsonNode getAddress(JsonNode source, String format) {
        ObjectNode result = mapper.createObjectNode();
        PointId pointId = getPointId(source, format);
        result.put(LATITUDE, pointId.getLat());
        result.put(LONGITUDE, pointId.getLon());
        if (GEOCODE_JSON.equals(format)) {
            result.set(LABEL, source.findValue(LABEL));
            ObjectNode address = (ObjectNode) source.findValue(GEO_CODING);
            address.remove(PLACE_ID);
            address.remove(OSM_TYPE);
            address.remove(OSM_ID);
            address.remove(TYPE);
            address.remove(LABEL);
            address.remove(ADMIN);
            result.set(ADDRESS, address);
        } else {
            result.set(DISPLAY_NAME, source.findValue(DISPLAY_NAME));
            result.set(ADDRESS, source.findValue(ADDRESS));
        }
        return result;
    }

    private static PointTo getPointTo(JsonNode source, String format, boolean isMemorySaving) {
        PointId pointId = getPointId(source, format);

        PointTo pointTo = isMemorySaving ?
                new PointTo(pointId, new HashSet<>(), null, GEOCODE_JSON.equals(format))
                : new PointTo(pointId, new HashSet<>(), format, GEOCODE_JSON.equals(format));

        pointTo.getOsmIds()
                .add(source.findValue(OSM_TYPE)
                        .asText()
                        .substring(0, 1)
                        .toUpperCase() +
                        source.findValue(OSM_ID).asText());
        return pointTo;
    }

    private static PointId getPointId(JsonNode node, String format) {
        PointId pointId;
        switch (format) {
            case JSON, JSON_V2 -> {
                pointId = new PointId(node.findValue(LATITUDE).asDouble(), node.findValue(LONGITUDE).asDouble());
            }
            case GEO_JSON, GEOCODE_JSON -> {
                ArrayNode coordinates = (ArrayNode) node.findValue(COORDINATES);
                pointId = new PointId(coordinates.get(1).asDouble(), coordinates.get(0).asDouble());
            }
            default -> throw new IllegalStateException("Unexpected value: " + format);
        }
        return pointId;
    }
}
