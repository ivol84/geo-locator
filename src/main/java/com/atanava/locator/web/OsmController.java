package com.atanava.locator.web;

import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import com.atanava.locator.service.OsmService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.atanava.locator.service.OsmConstants.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = OsmController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class OsmController {

    static final String REST_URL = "/api";

    private final OsmService osmService;

    @GetMapping("/search")
    public ResponseEntity<ArrayNode> getByAddress(@RequestParam(required = false) String q,
                                                  @RequestParam(required = false) String street,
                                                  @RequestParam(required = false) String city,
                                                  @RequestParam(required = false) String country1,
                                                  @RequestParam(required = false) String state,
                                                  @RequestParam(required = false) String country2,
                                                  @RequestParam(required = false) String postalcode,
                                                  @RequestParam(required = false) Integer addressdetails,
                                                  @RequestParam(required = false) String format) {

        format = getFormatOrDefault(format);
        ArrayNode created;
        if (q != null) {
            if (q.isEmpty()) {
                throw new IllegalArgumentException("Address must not be empty or null");
            }
            log.info("Create coordinates for address: {}", q);
            created = osmService.createOrUpdate(format, addressdetails, q);
        } else {
            log.info("Create coordinates for address: {}, {}, {}, {}, {}, {}", street, city, country1, state, country2, postalcode);
            String[] address = {STREET + street,
                    CITY + city,
                    COUNTRY + country1,
                    STATE + state,
                    COUNTRY + country2,
                    POSTAL_CODE + postalcode};

            String tmp = null;
            for (String addrDetail : address) {
                if (addrDetail != null && !addrDetail.isEmpty())
                    tmp = addrDetail.strip().trim();
            }
            if (tmp == null || tmp.isEmpty()) {
                throw new IllegalArgumentException("Address must not be empty or null");
            }
            created = osmService.createOrUpdate(format, addressdetails, address);
        }
        return new ResponseEntity<>(created, HttpStatus.OK);
    }

    @GetMapping("/lookup")
    public ResponseEntity<ArrayNode> getAddresses(double latitude, double longitude,
                                                  @RequestParam(required = false) Integer addressdetails,
                                                  @RequestParam(required = false) String format) {
        log.info("Get addresses for coordinates: {}, {}", latitude, longitude);
        ArrayNode fetched = osmService.get(getFormatOrDefault(format), addressdetails,
                new Point(new PointId(latitude, longitude), Set.of()));

        return new ResponseEntity<>(fetched, HttpStatus.OK);
    }

    private String getFormatOrDefault(String format) {
        return (JSON_V2.equals(format) || GEO_JSON.equals(format) || GEOCODE_JSON.equals(format)) ? format : JSON;
    }
}
