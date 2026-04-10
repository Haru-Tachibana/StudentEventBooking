package com.student.eventbooking.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LatLong {

    private final double latitude;
    private final double longitude;

    @JsonCreator
    public LatLong(
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
}