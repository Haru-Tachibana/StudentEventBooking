package com.student.eventbooking.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private String eventId;

    @NotBlank(message = "Publisher ID is required")
    private String publisherId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Date is required")
    private String date;

    @NotBlank(message = "Time is required")
    private String time;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    private Double cost;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    private List<String> attendees = new ArrayList<>();

    @NotBlank(message = "Description is required")
    private String description;

    private String imageUrl;
    private String eventSeriesId;
    private Double averageRating;
    private Integer ratingCount;

    public Event() {
        this.attendees = new ArrayList<>();
    }

    public Event(String eventId, String publisherId, String title, String type,
                 String date, String time, Integer duration, String location,
                 String venue, Double cost, Integer maxParticipants, String description) {
        this.eventId = eventId;
        this.publisherId = publisherId;
        this.title = title;
        this.type = type;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.location = location;
        this.venue = venue;
        this.cost = cost;
        this.maxParticipants = maxParticipants;
        this.description = description;
        this.attendees = new ArrayList<>();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEventSeriesId() {
        return eventSeriesId;
    }

    public void setEventSeriesId(String eventSeriesId) {
        this.eventSeriesId = eventSeriesId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public boolean isFull() {
        return attendees != null && attendees.size() >= maxParticipants;
    }

    public boolean addAttendee(String studentId) {
        if (attendees == null) {
            attendees = new ArrayList<>();
        }
        if (!attendees.contains(studentId) && !isFull()) {
            attendees.add(studentId);
            return true;
        }
        return false;
    }

    public boolean removeAttendee(String studentId) {
        if (attendees != null) {
            return attendees.remove(studentId);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", publisherId='" + publisherId + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", duration=" + duration +
                ", location='" + location + '\'' +
                ", venue='" + venue + '\'' +
                ", cost=" + cost +
                ", maxParticipants=" + maxParticipants +
                ", attendees=" + attendees +
                '}';
    }
}