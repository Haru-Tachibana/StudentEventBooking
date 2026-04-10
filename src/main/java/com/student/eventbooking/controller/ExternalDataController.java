package com.student.eventbooking.controller;

import com.student.eventbooking.dto.LatLong;
import com.student.eventbooking.dto.WeatherInfo;
import com.student.eventbooking.service.GeocodingService;
import com.student.eventbooking.service.MapsService;
import com.student.eventbooking.service.SkiddleService;
import com.student.eventbooking.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webresources/external")
public class ExternalDataController {

    private final GeocodingService geocodingService;
    private final WeatherService weatherService;
    private final MapsService mapsService;
    private final SkiddleService skiddleService;

    @Autowired
    public ExternalDataController(
            GeocodingService geocodingService,
            WeatherService weatherService,
            MapsService mapsService,
            SkiddleService skiddleService) {
        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
        this.mapsService = mapsService;
        this.skiddleService = skiddleService;
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        try {
            Map<String, String> result = geocodingService.getPostcodeFromLatLong(latitude, longitude);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reverse geocoding failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String postcode) {
        try {

            LatLong latLong = geocodingService.getLatLongFromPostcode(postcode);
            Map<String, Object> body = new HashMap<>();

            body.put("latitude", latLong.getLatitude());
            body.put("longitude", latLong.getLongitude());

            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Geocoding failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }


    @GetMapping("/weather")
    public ResponseEntity<?> weather(@RequestParam String postcode) {
        try {

            WeatherInfo weather = weatherService.getWeatherByPostcode(postcode);
            Map<String, Object> body = new HashMap<>();

            body.put("temperatureCelsius", weather.getTemperatureCelsius());
            body.put("weatherCode", weather.getWeatherCode());

            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Weather failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }


    @GetMapping("/map-embed-url")
    public ResponseEntity<?> mapEmbedUrl(
            @RequestParam(required = false) String postcode,
            @RequestParam(required = false) String place) {
        try {

            String url;
            if (postcode != null && !postcode.trim().isEmpty()) {
                url = mapsService.getEmbedUrlByPostcode(postcode.trim());

            } else if (place != null && !place.trim().isEmpty()) {
                url = mapsService.getEmbedUrl(place.trim());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Provide either postcode or place");
                return ResponseEntity.badRequest().body(error);
            }
            Map<String, String> body = new HashMap<>();
            body.put("embedUrl", url);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Map URL failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/nearby-events")
    public ResponseEntity<?> nearbyEvents(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "20") int radius) {
        try {
            int radiusMiles = Math.min(50, Math.max(1, radius));
            java.util.List<Map<String, Object>> events = skiddleService.getNearbyEvents(latitude, longitude, radiusMiles);
            return ResponseEntity.ok(events);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch nearby events: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}