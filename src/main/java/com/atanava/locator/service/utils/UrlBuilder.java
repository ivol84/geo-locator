package com.atanava.locator.service.utils;

import java.util.Arrays;
import java.util.Set;

import static com.atanava.locator.service.OsmConstants.*;

public class UrlBuilder {

    public static String getUrl(String format, String... address) {
        StringBuilder url = new StringBuilder(SOURCE_URL + SEARCH);
        if (address.length > 1) {
            Arrays.asList(address).forEach(s -> url.append(s).append("&"));
        } else {
            url.append("q=").append(address[0]).append("&");
        }
        url.append(FORMAT).append(format).append("&" + ADDR_DETAILS);

        return url.toString();
    }

    public static String getUrl(String format, Set<String> osmIds) {
        StringBuilder url = new StringBuilder(SOURCE_URL + BY_OSM_IDS);
        osmIds.forEach(id -> url.append(id).append(","));
        url.replace(url.lastIndexOf(","), url.length(), "&" + FORMAT + format).append("&" + ADDR_DETAILS);

        return url.toString();
    }
}
