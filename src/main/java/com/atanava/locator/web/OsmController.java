package com.atanava.locator.web;

import com.atanava.locator.model.PointId;
import com.atanava.locator.service.OsmService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

		/** @QUESTION: May be content negatiation will be better??? */
		format = getFormatOrDefault(format);
		ArrayNode created;
		if (q != null) {
			q = q.strip();
			if (q.isEmpty()) {
				throw new IllegalArgumentException("Address must not be empty or null");
			}
			log.info("Create coordinates for address: {}", q);
			q = q.replaceAll(" ", "+");
			created = osmService.createOrUpdate(format, addressdetails, q);
		} else {
			log.info("Create coordinates for address: {}, {}, {}, {}, {}, {}",
				street, city, country1, state, country2, postalcode);

			List<String> address = new ArrayList<>();
			if (street != null) address.add(STREET + "=" + street);
			if (city != null) address.add(CITY + "=" + city);
			if (country1 != null) address.add(COUNTRY + "=" + country1);
			if (state != null) address.add(STATE + "=" + state);
			if (country2 != null) address.add(COUNTRY + "=" + country2);
			if (postalcode != null) address.add(POSTAL_CODE + "=" + postalcode);

			if (address.isEmpty()) {
				throw new IllegalArgumentException("Address must not be empty or null");
			}
			created = osmService.createOrUpdate(format, addressdetails, address.toArray(String[]::new));
		}
		return new ResponseEntity<>(created, HttpStatus.OK);
	}

	@GetMapping("/lookup")
	public ResponseEntity<ArrayNode> getByPoints(@RequestBody (required = false) Set<PointId> pointIds,
												 @RequestParam(required = false) Integer addressdetails,
												 @RequestParam(required = false) String format) {
		log.info("Get addresses for points: {}", pointIds);
		format = getFormatOrDefault(format);

		ArrayNode fetched;
		if (pointIds == null || pointIds.isEmpty()) {
			fetched = osmService.getAll(format, addressdetails);
		} else {
			fetched = osmService.getByPointIds(format, addressdetails, pointIds);
		}

		return new ResponseEntity<>(fetched, HttpStatus.OK);
	}

	private String getFormatOrDefault(String format) {
		return (JSON_V2.equals(format) || GEO_JSON.equals(format) || GEOCODE_JSON.equals(format)) ? format : JSON;
	}

}
