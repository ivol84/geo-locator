package com.atanava.locator.service.utils;

import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Set;

import static com.atanava.locator.service.OsmConstants.*;

public class JsonUtil {

    public static Set<Point> getPoints(ArrayNode source, String format) {
        source = normalize(source);
        Set<Point> points = new HashSet<>();
        source.forEach(jsonNode -> {
            PointId pointId = getPointId(jsonNode, format);
            Point point = new Point(pointId, new HashSet<>());
            point.getOsmIds()
                    .add(jsonNode.findValue(OSM_TYPE)
                            .asText()
                            .substring(0, 1)
                            .toUpperCase() +
                            jsonNode.findValue(OSM_ID).asText());
            points.add(point);
        });
        return points;
    }

    public static ArrayNode getConverted(ArrayNode source, String format, boolean isDetailed) {
        source = normalize(source);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode converted = mapper.createArrayNode();

        source.forEach(jsonNode -> {
            ObjectNode node = mapper.createObjectNode();
            PointId pointId = getPointId(jsonNode, format);
            node.put(LATITUDE, pointId.getLatitude());
            node.put(LONGITUDE, pointId.getLongitude());
            node.set(DISPLAY_NAME, GEOCODE_JSON.equals(format) ? jsonNode.findValue(LABEL) : jsonNode.findValue(DISPLAY_NAME));
            if (isDetailed && jsonNode.findValue(ADDRESS) != null) {
                node.set(ADDRESS, jsonNode.findValue(ADDRESS));//TODO implement for geocodejson too
            }
            converted.add(node);
        });
        return converted;
    }

    public static ArrayNode merge(ArrayNode source, ArrayNode dest) {
        for (JsonNode oldNode : dest) {
            for (JsonNode newNode : source) {
                if (!oldNode.equals(newNode)) {
                    dest.add(newNode);
                }
            }
        }
        return dest;
    }

    private static ArrayNode normalize(ArrayNode source) {
        return source.findValue(FEATURES) != null ? (ArrayNode) source.findValue(FEATURES) : source;
    }

    private static PointId getPointId(JsonNode node, String format) {
        PointId pointId;
        switch (format) {
            case JSON, JSON_V2 -> {
                pointId = new PointId(node.findValue(LATITUDE).asDouble(), node.findValue(LONGITUDE).asDouble());
            }
            case GEO_JSON, GEOCODE_JSON -> {
                ArrayNode coordinates = (ArrayNode) node.findValue(COORDINATES);
                pointId = new PointId(coordinates.get(0).asDouble(), coordinates.get(1).asDouble());
            }
            default -> throw new IllegalStateException("Unexpected value: " + format);
        }
        return pointId;
    }
}
