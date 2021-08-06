package com.atanava.locator.service;

import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import com.atanava.locator.repository.PointRepository;
import com.atanava.locator.service.cache.RatingCache;
import com.atanava.locator.service.utils.JsonUtil;
import com.atanava.locator.service.utils.UrlBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private final RatingCache<String, Set<Point>> urlCache;
	private final RatingCache<Point, ArrayNode> addressCache;
	private final ObjectMapper mapper;

	public ArrayNode createOrUpdate(String format, Integer addressDetails, String... address) {
		ArrayNode result = mapper.createArrayNode();
		String url = UrlBuilder.getUrl(format, isDetailed(addressDetails), address);
		Set<Point> points = urlCache.get(url);
		if (points != null && !points.isEmpty()) {
			for (Point point : points) {
				ArrayNode forCache = get(format, addressDetails, point);
				addressCache.put(point, forCache);
				result.add(forCache);
			}
		} else {
			result = osmProvider.get(url);
			points = JsonUtil.getPoints(result, format);
			urlCache.put(url, points);
			result = JsonUtil.getConverted(result, format, isDetailed(addressDetails));
			save(points);
		}
		return result;
	}

	private void save(Set<Point> points) {
		log.info("Trying to save points: {}", points);
		points.forEach(repository::save);
	}

	public ArrayNode get(String format, Integer addressDetails, Point point) {
		ArrayNode result = addressCache.get(point);
		if (result == null) {
			point = repository.findById(point.getPointId()).orElseThrow();
			String url = UrlBuilder.getUrl(format, isDetailed(addressDetails), point.getOsmIds());
			ArrayNode source = osmProvider.get(url);
			result = JsonUtil.getConverted(source, format, isDetailed(addressDetails));
		}
		return result;
	}

	private boolean isDetailed(Integer addressDetails) {
		return addressDetails != null && addressDetails > 0;
	}
}
