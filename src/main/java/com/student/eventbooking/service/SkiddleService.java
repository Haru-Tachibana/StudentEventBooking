package com.student.eventbooking.service;

import com.student.eventbooking.config.RedisCacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SkiddleService {

    private final RestTemplate restTemplate;
    private final String skiddleBaseUrl;
    private final String skiddleApiKey;

    public SkiddleService(
            RestTemplate restTemplate,
            @Value("${external.api.skiddle.url}") String skiddleBaseUrl,
            @Value("${external.api.skiddle.key}") String skiddleApiKey) {
        this.restTemplate = restTemplate;
        this.skiddleBaseUrl = skiddleBaseUrl;
        this.skiddleApiKey = skiddleApiKey;
    }

    @Cacheable(value = RedisCacheConfig.CACHE_SKIDDLE, key = "#latitude + '-' + #longitude + '-' + #radiusMiles")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getNearbyEvents(double latitude, double longitude, int radiusMiles) {

        if (skiddleApiKey == null || skiddleApiKey.isBlank()) {
            throw new IllegalStateException("Skiddle API key is not configured (SKIDDLE_API_KEY)");
        }

        String url = UriComponentsBuilder.fromHttpUrl(skiddleBaseUrl + "/events/search/")
                .queryParam("api_key", skiddleApiKey)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("radius", radiusMiles)
                .queryParam("order", "distance")
                .queryParam("description", "1")
                .queryParam("limit", 10)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) return new ArrayList<>();
        
        Object resultsObj = response.get("results");
        if (resultsObj instanceof List) {
            return (List<Map<String, Object>>) resultsObj;
        }
        return new ArrayList<>();
    }
}