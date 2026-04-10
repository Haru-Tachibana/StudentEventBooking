package com.student.eventbooking.service;

import com.student.eventbooking.config.RedisCacheConfig;
import com.student.eventbooking.dto.LatLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class MapsService {

    private final String googleMapsApiKey;
    private final GeocodingService geocodingService;

    @Autowired
    public MapsService(
            @Value("${external.api.google.maps.key:}") String googleMapsApiKey,
            GeocodingService geocodingService) {
        String key = "";
        if (googleMapsApiKey != null) {
            key = googleMapsApiKey.trim();
        }
        this.googleMapsApiKey = key;
        this.geocodingService = geocodingService;
    }

    // Builds a Google Maps Embed API iframe URL
    @Cacheable(value = RedisCacheConfig.CACHE_GEOCODING, key = "'map-place-' + #placeQuery")
    public String getEmbedUrl(String placeQuery) {
        if (placeQuery == null || placeQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("Place or address is required");
        }
        if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
            throw new IllegalStateException("Google Maps API key is not configured. Set GOOGLE_MAPS_API_KEY or external.api.google.maps.key in api-keys.properties (and add spring.config.additional-location to application.properties), or set the env var.");
        }
        String encoded = URLEncoder.encode(placeQuery.trim(), StandardCharsets.UTF_8);
        return "https://www.google.com/maps/embed/v1/place?key=" + googleMapsApiKey + "&q=" + encoded;
    }

    // Builds a Google Maps Embed URL for a UK postcode
    @Cacheable(value = RedisCacheConfig.CACHE_GEOCODING, key = "'map-postcode-' + #postcode")
    public String getEmbedUrlByPostcode(String postcode) {
        if (postcode == null || postcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Postcode is required");
        }
        LatLong latLong = geocodingService.getLatLongFromPostcode(postcode);
        String query = latLong.getLatitude() + "," + latLong.getLongitude();
        if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
            throw new IllegalStateException("Google Maps API key is not configured. Set GOOGLE_MAPS_API_KEY or external.api.google.maps.key in api-keys.properties, or set the env var.");
        }
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return "https://www.google.com/maps/embed/v1/place?key=" + googleMapsApiKey + "&q=" + encoded;
    }
}