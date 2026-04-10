package com.student.eventbooking.service;

import com.student.eventbooking.config.RedisCacheConfig;
import com.student.eventbooking.dto.LatLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final String postcodesBaseUrl;

    @Autowired
    public GeocodingService(
            RestTemplate restTemplate,
            @Value("${external.api.postcodes.url}") String postcodesBaseUrl) {
        this.restTemplate = restTemplate;
        this.postcodesBaseUrl = postcodesBaseUrl;
    }

    // Postcode should be formatted without spaces (e.g. NG14FQ) or with (NG1 4FQ).
    @Cacheable(value = RedisCacheConfig.CACHE_GEOCODING, key = "'postcode-' + #postcode")
    public LatLong getLatLongFromPostcode(String postcode) {
        if (postcode == null || postcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Postcode is required");
        }
        String normalised = postcode.trim().replaceAll("\\s+", "");
        String url = postcodesBaseUrl + "/postcodes/" + normalised;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            throw new IllegalStateException("No response from Postcodes.io");
        }
        Object status = response.get("status");
        if (status == null || !Integer.valueOf(200).equals(status)) {
            throw new IllegalArgumentException("Invalid or unknown postcode: " + postcode);
        }
        Object resultObj = response.get("result");
        if (resultObj == null || !(resultObj instanceof Map)) {
            throw new IllegalStateException("Unexpected Postcodes.io response format");
        }

        Map<?, ?> result = (Map<?, ?>) resultObj;
        Object latObj = result.get("latitude");
        Object lonObj = result.get("longitude");

        if (latObj == null || lonObj == null) {
            throw new IllegalStateException("Latitude or longitude missing in Postcodes.io response");
        }

        double lat = toDouble(latObj);
        double lon = toDouble(lonObj);
        return new LatLong(lat, lon);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_GEOCODING, key = "'reverse-' + #latitude + '-' + #longitude")
    @SuppressWarnings("unchecked")
    public Map<String, String> getPostcodeFromLatLong(double latitude, double longitude) {

        String url = postcodesBaseUrl + "/postcodes?lon=" + longitude + "&lat=" + latitude + "&limit=1";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null) throw new IllegalStateException("No response from Postcodes.io");
        Object status = response.get("status");
        if (status == null || !Integer.valueOf(200).equals(status)) {
            throw new IllegalArgumentException("No postcode found for this location.");
        }
        Object resultObj = response.get("result");
        if (resultObj == null || !(resultObj instanceof List) || ((List<?>) resultObj).isEmpty()) {
            throw new IllegalArgumentException("No postcode found for this location.");
        }

        Map<?, ?> first = (Map<?, ?>) ((List<?>) resultObj).get(0);
        String postcode;

        if (first.get("postcode") != null) {
            postcode = String.valueOf(first.get("postcode"));
        } else {
            postcode = null;
        }

        String adminDistrict;

        if (first.get("admin_district") != null) {
            adminDistrict = String.valueOf(first.get("admin_district"));
        } else {
            adminDistrict = null;
        }

        String country;

        if (first.get("country") != null) {
            country = String.valueOf(first.get("country"));
        } else {
            country = null;
        }

        String displayName;

        if (postcode != null) {
            displayName = postcode;
        } else {
            displayName = "";
        }

        if (adminDistrict != null && !adminDistrict.isBlank()) {
            if (displayName.isEmpty()) {
                displayName = displayName + adminDistrict;
            } else {
                displayName = displayName + ", " + adminDistrict;
            }
        }

        if (country != null && !country.isBlank()) {
            if (displayName.isEmpty()) {
                displayName = displayName + country;
            } else {
                displayName = displayName + ", " + country;
            }
        }

        String postcodeOut;

        if (postcode != null) {
            postcodeOut = postcode;
        } else {
            postcodeOut = "";
        }

        String displayNameOut;

        if (displayName.trim().isEmpty()) {
            if (postcode != null) {
                displayNameOut = postcode;
            } else {
                displayNameOut = "your area";
            }
        } else {
            displayNameOut = displayName.trim();
        }

        Map<String, String> out = new java.util.HashMap<>();
        out.put("postcode", postcodeOut);
        out.put("displayName", displayNameOut);

        return out;
    }


    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}