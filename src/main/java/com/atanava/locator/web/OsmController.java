package com.atanava.locator.web;

import com.atanava.locator.service.OsmService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
		ArrayNode created;
		if (q != null) {
			log.info("Create coordinates for address: {}", q);
			created = osmService.createOrUpdate(q, addressdetails, format);
		} else {
			log.info("Create coordinates for address: {}, {}, {}, {}, {}, {}", street, city, country1, state, country2, postalcode);
			created = osmService.createOrUpdate(
					List.of(STREET + street,
							CITY + city,
							COUNTRY + country1,
							STATE + state,
							COUNTRY + country2,
							POSTAL_CODE + postalcode),
					addressdetails,
					format == null ? JSON : format);
		}
		return new ResponseEntity<>(created, HttpStatus.OK);
	}

	@GetMapping("/lookup")
	public ResponseEntity<ArrayNode> getAddresses(double latitude, double longitude,
												  @RequestParam(required = false) Integer addressdetails,
												  @RequestParam(required = false) String format) {
		log.info("Get addresses for coordinates: {}, {}", latitude, longitude);
		ArrayNode fetched = osmService.get(latitude, longitude, addressdetails, format == null ? JSON : format);

		return new ResponseEntity<>(fetched, HttpStatus.OK);
	}

}
