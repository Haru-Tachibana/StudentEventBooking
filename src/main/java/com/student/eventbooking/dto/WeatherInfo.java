package com.student.eventbooking.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WeatherInfo {

    private final double temperatureCelsius;
    private final int weatherCode;

    @JsonCreator
    public WeatherInfo(
            @JsonProperty("temperatureCelsius") double temperatureCelsius,
            @JsonProperty("weatherCode") int weatherCode) {
        this.temperatureCelsius = temperatureCelsius;
        this.weatherCode = weatherCode;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }
    public int getWeatherCode() {
        return weatherCode;
    }
}