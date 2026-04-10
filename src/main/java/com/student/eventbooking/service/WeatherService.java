package com.student.eventbooking.service;

import com.student.eventbooking.config.RedisCacheConfig;
import com.student.eventbooking.dto.LatLong;
import com.student.eventbooking.dto.WeatherInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final GeocodingService geocodingService;
    private final String openMeteoBaseUrl;

    @Autowired
    public WeatherService(
            RestTemplate restTemplate,
            GeocodingService geocodingService,
            @Value("${external.api.openmeteo.url}") String openMeteoBaseUrl) {
        this.restTemplate = restTemplate;
        this.geocodingService = geocodingService;
        this.openMeteoBaseUrl = openMeteoBaseUrl;
    }

    // from uk postcode -> postcode.io
    @Cacheable(value = RedisCacheConfig.CACHE_WEATHER, key = "'postcode-' + #postcode")
    public WeatherInfo getWeatherByPostcode(String postcode) {
        LatLong latLong = geocodingService.getLatLongFromPostcode(postcode);
        return getWeatherByLatLong(latLong.getLatitude(), latLong.getLongitude());
    }

    //from longlat -> open-meteo
    @Cacheable(value = RedisCacheConfig.CACHE_WEATHER, key = "'latlon-' + #latitude + '-' + #longitude")
    public WeatherInfo getWeatherByLatLong(double latitude, double longitude) {

        String url = openMeteoBaseUrl + "/forecast?latitude=" + latitude + "&longitude=" + longitude
                + "&current=temperature_2m,weather_code";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null) {
            throw new IllegalStateException("No response from Open-Meteo");
        }
        Object currentObj = response.get("current");

        if (currentObj == null || !(currentObj instanceof Map)) {
            throw new IllegalStateException("Unexpected Open-Meteo response format");
        }
        Map<?, ?> current = (Map<?, ?>) currentObj;
        Object tempObj = current.get("temperature_2m");
        Object codeObj = current.get("weather_code");

        if (tempObj == null || codeObj == null) {
            throw new IllegalStateException("Temperature or weather_code missing in Open-Meteo response");
        }
        double temp = toDouble(tempObj);
        int code = toInt(codeObj);
        return new WeatherInfo(temp, code);
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}