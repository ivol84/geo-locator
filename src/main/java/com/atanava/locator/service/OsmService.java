package com.atanava.locator.service;

import com.atanava.locator.model.Point;
import com.atanava.locator.repository.PointRepository;
import com.atanava.locator.service.cache.RatingCache;
import com.atanava.locator.service.utils.JsonUtil;
import com.atanava.locator.service.utils.UrlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
					part = get(format, addressDetails, pointTo).get(0);
				}
				result.add(part);
			}
		} else {
			result = osmProvider.get(url);
			Map<PointTo, JsonNode> converted = JsonUtil.convertToMap(result, format);
			converted.forEach(addressCache::put);
			pointTos = converted.keySet();
			urlCache.put(url, pointTos);
			result = JsonUtil.combine(converted.values());
			save(pointTos);
		}
		if (isSimple(addressDetails)) {
			result = JsonUtil.getSimple(result);
		}
		return result;
	}

	private void save(Set<PointTo> pointTos) {
		log.info("Trying to save points");
		pointTos.forEach(pointTo -> {
			Point point = new Point(pointTo.getPointId(), pointTo.getOsmIds());
			repository.save(point);
		});
	}

	public ArrayNode get(String format, Integer addressDetails, PointTo pointTo) {
		ArrayNode result = JsonUtil.combine(List.of(addressCache.get(pointTo)));

		if (result == null || result.isEmpty()) {
			Point point =  repository.findById(pointTo.getPointId())
					.orElseThrow();
			pointTo = new PointTo(point.getPointId(), point.getOsmIds(), format);
			String url = UrlBuilder.getUrl(format, pointTo.getOsmIds());
			ArrayNode source = osmProvider.get(url);
			Map<PointTo, JsonNode> converted = JsonUtil.convertToMap(source, format);
			converted.forEach(addressCache::put);
			result = JsonUtil.combine(converted.values());
		}
		if (isSimple(addressDetails)) {
			result = JsonUtil.getSimple(result);
		}
		return result;
	}

	private boolean isSimple(Integer addressDetails) {
		return addressDetails == null || addressDetails <= 0;
	}
}
