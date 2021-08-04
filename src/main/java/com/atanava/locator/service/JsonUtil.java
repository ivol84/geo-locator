package com.atanava.locator.utils;

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
		ArrayNode tmp = (GEO_JSON.equals(format) || GEOCODE_JSON.equals(format))
				? (ArrayNode) source.findValue(FEATURES)
				: source;

		Set<Point> points = new HashSet<>();
		tmp.forEach(j -> {
			PointId pointId = getPointId(j, format);
			Point point = new Point(pointId, new HashSet<>());
			point.getOsmIds().add(j.findValue(OSM_TYPE).asText().substring(0,1).toUpperCase() + j.findValue(OSM_ID).asText());
			points.add(point);
		});
		return points;
	}

	public static ArrayNode getConverted(ArrayNode source, String format) {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode converted = mapper.createArrayNode();
		source.forEach(jsonNode -> {
			ObjectNode node = mapper.createObjectNode();
			PointId pointId = getPointId(jsonNode, format);
			node.put(LATITUDE, pointId.getLatitude());
			node.put(LONGITUDE, pointId.getLongitude());
			node.set(DISPLAY_NAME, jsonNode.findValue(DISPLAY_NAME));
			if (jsonNode.findValue(ADDRESS) != null) {
				node.set(ADDRESS, jsonNode.findValue(ADDRESS));
			}
			converted.add(node);
		});
		return converted;
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
