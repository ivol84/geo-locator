package com.atanava.locator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

import static com.atanava.locator.service.OsmConstants.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class OsmProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public ArrayNode getByAddress(String address, String format, boolean detailsNeeded) {
        String url = SOURCE_URL + SEARCH + "q=" + address + "&" + FORMAT + format;
        return get(url, detailsNeeded);
    }

    public ArrayNode getByAddress(List<String> address, String format, boolean detailsNeeded) {
        StringBuilder url = new StringBuilder(SOURCE_URL + SEARCH);
        address.forEach(s -> url.append(s).append("&"));
        url.append(FORMAT).append(format);

        return get(url.toString(), detailsNeeded);
    }

    public ArrayNode getByOsmIds(Set<String> osmIds, String format, boolean detailsNeeded) {
        StringBuilder url = new StringBuilder(SOURCE_URL + BY_OSM_IDS);
        osmIds.forEach(id -> url.append(id).append(","));
        url.replace(url.lastIndexOf(","), url.length(), "&" + FORMAT + format);

        return get(url.toString(), detailsNeeded);
    }

    private ArrayNode get(String url, boolean detailsNeeded) {
        url += (detailsNeeded ? "&" + ADDR_DETAILS : "");
        ArrayNode arrayNode = null;
        try {
            JsonNode jsonNode = restTemplate.getForObject(url, JsonNode.class);
            if (jsonNode.isArray()) {
                arrayNode = (ArrayNode) jsonNode;
            } else {
                arrayNode = mapper.createArrayNode().add(jsonNode);
            }
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return arrayNode;
    }
}
