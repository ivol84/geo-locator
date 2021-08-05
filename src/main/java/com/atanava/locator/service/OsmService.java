package com.atanava.locator.service;

import com.atanava.locator.cache.RatingCache;
import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import com.atanava.locator.repository.PointRepository;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class OsmService {
	private final PointRepository repository;
	private final OsmProvider osmProvider;
	private final RatingCache<Point, ArrayNode> cache;

	public ArrayNode createOrUpdate(String address, Integer addressDetails, String format) {
		ArrayNode source = osmProvider.getByAddress(address, format, detailsNeeded(addressDetails));
		saveAndCache(source, format);
		return JsonUtil.getConverted(source, format);
	}

	public ArrayNode createOrUpdate(List<String> address, Integer addressDetails, String format) {
		ArrayNode source = osmProvider.getByAddress(address, format, detailsNeeded(addressDetails));
		saveAndCache(source, format);
		return JsonUtil.getConverted(source, format);
	}

	private void saveAndCache(ArrayNode source, String format) {
		Set<Point> points = JsonUtil.getPoints(source, format);
		log.info("Trying to save points: {}", points);


		points.forEach(repository::save);
	}

	public ArrayNode get(double latitude, double longitude, Integer addressDetails, String format) {
		Point point = repository.findById(new PointId(latitude, longitude)).orElseThrow();
		ArrayNode source = osmProvider.getByOsmIds(point.getOsmIds(), format, detailsNeeded(addressDetails));
		return JsonUtil.getConverted(source, format);
	}

	private boolean detailsNeeded(Integer addressDetails) {
		return addressDetails != null && addressDetails > 0;
	}
}
