package com.atanava.locator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class OsmProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public ArrayNode get(String url) {
        ArrayNode addresses = mapper.createArrayNode();
        try {
            JsonNode jsonNode = restTemplate.getForObject(url, JsonNode.class);
            if (jsonNode != null && jsonNode.isArray()) {
                addresses = (ArrayNode) jsonNode;
            } else {
                addresses.add(jsonNode);
            }
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
