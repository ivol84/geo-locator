package com.atanava.locator.service;

import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import com.atanava.locator.repository.PointRepository;
import com.atanava.locator.cache.RatingCache;
import com.atanava.locator.service.utils.JsonUtil;
import com.atanava.locator.service.utils.UrlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.atanava.locator.service.OsmConstants.GEOCODE_JSON;

@Slf4j
@AllArgsConstructor
@Service
public class OsmService {
	private final PointRepository repository;
	private final OsmProvider osmProvider;
	private final RatingCache<String, Set<PointTo>> urlCache;
	private final RatingCache<PointTo, JsonNode> addressCache;
	private final ObjectMapper mapper;

	public ArrayNode createOrUpdate(String format, Integer addressDetails, String... address) {
		ArrayNode result = mapper.createArrayNode();
		String url = UrlBuilder.getUrl(format, address);
		Set<PointTo> pointTos = urlCache.get(url);
		if (pointTos != null && !pointTos.isEmpty()) {
			for (PointTo pointTo : pointTos) {
				JsonNode part = addressCache.get(pointTo);
				if (part == null || part.isEmpty()) {
					getByPointTo(format, addressDetails, pointTo).forEach(result::add);
				} else {
					result.add(part);
				}
			}
		} else {
			result = osmProvider.get(url);
			Map<PointTo, JsonNode> converted = JsonUtil.convertToMap(result, format, addressCache.isMemorySaving());
			converted.forEach(addressCache::put);
			pointTos = converted.keySet();
			urlCache.put(url, pointTos);
			result = JsonUtil.combine(converted.values());
			save(pointTos);
		}
		return isSimple(addressDetails) ? JsonUtil.getSimple(result) : result;
	}

	private ArrayNode getByPointTo(String format, Integer addressDetails, PointTo pointTo) {
		ArrayNode result = JsonUtil.combine(List.of(addressCache.get(pointTo)));

		if (result == null || result.isEmpty()) {
			Point point = repository.findById(pointTo.getPointId())
					.orElseThrow();
			pointTo = getPointTo(point, format);
			result = getByOsmIds(format, pointTo.getOsmIds(), true);
		}
		return isSimple(addressDetails) ? JsonUtil.getSimple(result) : result;
	}

	private void save(Set<PointTo> pointTos) {
		log.info("Trying to save points");
		pointTos.forEach(pointTo -> {
			Point point = new Point(pointTo.getPointId(), pointTo.getOsmIds());
			repository.save(point);
		});
	}

	public ArrayNode getByPointIds(String format, Integer addressDetails, Set<PointId> pointIds) {
		Set<String> osmIds = getOsmIds(repository.findByPointIdIn(pointIds), format);
		ArrayNode result = getByOsmIds(format, osmIds, true);

		return isSimple(addressDetails) ? JsonUtil.getSimple(result) : result;
	}

	public ArrayNode getAll(String format, Integer addressDetails) {
		Set<String> osmIds = getOsmIds(repository.findAll(), format);
		ArrayNode result = getByOsmIds(format, osmIds, false);

		return isSimple(addressDetails) ? JsonUtil.getSimple(result) : result;
	}

	private ArrayNode getByOsmIds(String format, Set<String> osmIds, boolean needToCache) {
		if (osmIds.isEmpty())
			throw new NoSuchElementException();
		String url = UrlBuilder.getUrl(format, osmIds);
		ArrayNode source = osmProvider.get(url);
		Map<PointTo, JsonNode> converted = JsonUtil.convertToMap(source, format, addressCache.isMemorySaving());
		if (needToCache) {
			converted.forEach(addressCache::put);
		}
		return JsonUtil.combine(converted.values());
	}

	private boolean isSimple(Integer addressDetails) {
		return addressDetails == null || addressDetails <= 0;
	}

	private PointTo getPointTo(Point point, String format) {
		format = addressCache.isMemorySaving() ? null : format;
		return new PointTo(point.getPointId(), point.getOsmIds(), format, GEOCODE_JSON.equals(format));
	}

	private Set<String> getOsmIds(Collection<Point> points, String format) {
		return points.stream()
				.map(point -> getPointTo(point, format))
				.flatMap(pointTo -> pointTo.getOsmIds().stream())
				.collect(Collectors.toSet());
	}
}
