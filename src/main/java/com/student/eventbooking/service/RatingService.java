package com.student.eventbooking.service;

import com.student.eventbooking.model.Event;
import com.student.eventbooking.model.EventRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class RatingService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_TIME;

    private final FirebaseService firebaseService;

    @Autowired
    public RatingService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    public boolean isEventEnded(Event event) {
        if (event == null || event.getDate() == null || event.getTime() == null || event.getDuration() == null) {
            return false;
        }
        try {
            LocalDate d = LocalDate.parse(event.getDate(), DATE_FORMAT);
            LocalTime t = LocalTime.parse(event.getTime(), TIME_FORMAT);
            LocalDateTime end = LocalDateTime.of(d, t).plusMinutes(event.getDuration());
            return LocalDateTime.now().isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }

    public void submitRating(String eventId, EventRating eventRating) {
        String studentId = eventRating.getStudentId();
        int rating = eventRating.getRating().intValue();
        try {
            Event event = firebaseService.getEventById(eventId);
            if (event == null) {
                throw new IllegalArgumentException("Event with ID " + eventId + " not found");
            }
            if (!isEventEnded(event)) {
                throw new IllegalArgumentException("Event has not ended yet. Ratings can only be submitted after the event.");
            }
            if (event.getAttendees() == null || !event.getAttendees().contains(studentId)) {
                throw new IllegalArgumentException("Student " + studentId + " did not attend this event");
            }

            double currentAvg;
            if (event.getAverageRating() != null) {
                currentAvg = event.getAverageRating().doubleValue();
            } else {
                currentAvg = 0.0;
            }
            int currentCount;
            if (event.getRatingCount() != null) {
                currentCount = event.getRatingCount().intValue();
            } else {
                currentCount = 0;
            }

            boolean alreadyRated = firebaseService.hasStudentRated(eventId, studentId);
            double newAvg;
            int newCount;

            if (alreadyRated) {
                Integer oldRating = firebaseService.getStudentRating(eventId, studentId);
                int previous = (oldRating != null) ? oldRating.intValue() : 0;
                firebaseService.saveRating(eventId, studentId, rating);
                newCount = currentCount;
                newAvg = (currentAvg * currentCount - previous + rating) / currentCount;
            } else {
                firebaseService.saveRating(eventId, studentId, rating);
                newCount = currentCount + 1;
                newAvg = (currentAvg * currentCount + rating) / newCount;
            }

            firebaseService.updateEventRatingFields(eventId, newAvg, newCount);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public EventRatingSummary getEventRating(String eventId) {
        try {
            Event event = firebaseService.getEventById(eventId);
            if (event == null) {
                return null;
            }
            EventRatingSummary summary = new EventRatingSummary();
            summary.setEventId(eventId);
            if (event.getAverageRating() != null) {
                summary.setAverageRating(event.getAverageRating());
            } else {
                summary.setAverageRating(0.0);
            }
            if (event.getRatingCount() != null) {
                summary.setRatingCount(event.getRatingCount());
            } else {
                summary.setRatingCount(0);
            }
            return summary;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getPendingRatingsForStudent(String studentId) {
        try {
            List<Event> allEvents = firebaseService.getAllEvents();
            List<Event> pending = new ArrayList<>();
            for (Event event : allEvents) {
                if (event.getAttendees() == null) {
                    continue;
                }
                if (!event.getAttendees().contains(studentId)) {
                    continue;
                }
                if (!isEventEnded(event)) {
                    continue;
                }
                if (firebaseService.hasStudentRated(event.getEventId(), studentId)) {
                    continue;
                }
                pending.add(event);
            }
            return pending;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public static class EventRatingSummary {
        private String eventId;
        private Double averageRating;
        private Integer ratingCount;

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
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
    }
}