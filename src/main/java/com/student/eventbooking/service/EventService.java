package com.student.eventbooking.service;

import com.student.eventbooking.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class EventService {

    private final FirebaseService firebaseService;

    @Autowired
    public EventService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    public Event createEvent(Event event) {
        try {
            String nextId = firebaseService.getNextEventId();
            event.setEventId(nextId);

            if (!firebaseService.studentExists(event.getPublisherId())) {
                throw new IllegalArgumentException("Publisher with ID " + event.getPublisherId() + " does not exist. Please register first.");
            }
            firebaseService.saveEvent(event);
            return event;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getAllEvents() {
        try {
            return firebaseService.getAllEvents();
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Optional<Event> getEventById(String eventId) {
        try {
            Event event = firebaseService.getEventById(eventId);
            return Optional.ofNullable(event);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> searchEvents(String type, String location) {
        try {
            if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("all")) {
                if (location != null && !location.isEmpty()) {
                    return firebaseService.getEventsByTypeAndLocation(type, location);
                } else {
                    return firebaseService.getEventsByType(type);
                }
            } else if (location != null && !location.isEmpty()) {
                return firebaseService.getEventsByLocation(location);
            } else {
                return firebaseService.getAllEvents();
            }
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Event registerStudentForEvent(String eventId, String studentId) {
        try {
            Event event = firebaseService.getEventById(eventId);
            if (event == null) {
                throw new IllegalArgumentException("Event with ID " + eventId + " not found");
            }
            if (!firebaseService.studentExists(studentId)) {
                throw new IllegalArgumentException("Student with ID " + studentId + " does not exist. Please register first.");
            }
            if (event.isFull()) {
                throw new IllegalStateException("Event is full. Cannot register more attendees.");
            }
            if (event.getAttendees().contains(studentId)) {
                throw new IllegalStateException("Student " + studentId + " is already registered for this event");
            }
            event.addAttendee(studentId);
            firebaseService.saveEvent(event);
            return event;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getEventsByType(String type) {
        try {
            return firebaseService.getEventsByType(type);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getEventsByLocation(String location) {
        try {
            return firebaseService.getEventsByLocation(location);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getEventsByDate(String date) {
        try {
            return firebaseService.getEventsByDate(date);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getEventsByPublisherId(String publisherId) {
        try {
            return firebaseService.getEventsByPublisherId(publisherId);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public List<Event> getEventsByAttendee(String studentId) {
        try {
            return firebaseService.getEventsByAttendee(studentId);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Event updateEvent(String eventId, Event updates, String currentPublisherId) {
        try {
            Event existing = firebaseService.getEventById(eventId);
            if (existing == null) {
                throw new IllegalArgumentException("Event with ID " + eventId + " not found");
            }
            if (!existing.getPublisherId().equals(currentPublisherId)) {
                throw new IllegalArgumentException("Only the publisher can update this event");
            }
            existing.setTitle(updates.getTitle());
            existing.setType(updates.getType());
            existing.setDate(updates.getDate());
            existing.setTime(updates.getTime());
            existing.setDuration(updates.getDuration());
            existing.setLocation(updates.getLocation());
            existing.setVenue(updates.getVenue());
            existing.setCost(updates.getCost());
            existing.setMaxParticipants(updates.getMaxParticipants());
            existing.setDescription(updates.getDescription());
            if (updates.getImageUrl() != null) {
                existing.setImageUrl(updates.getImageUrl());
            }
            firebaseService.saveEvent(existing);
            return existing;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public void deleteEvent(String eventId, String currentPublisherId) {
        try {
            Event existing = firebaseService.getEventById(eventId);
            if (existing == null) {
                throw new IllegalArgumentException("Event with ID " + eventId + " not found");
            }
            if (!existing.getPublisherId().equals(currentPublisherId)) {
                throw new IllegalArgumentException("Only the publisher can delete this event");
            }
            firebaseService.deleteEvent(eventId);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Event cancelRegistration(String eventId, String studentId) {
        try {
            Event event = firebaseService.getEventById(eventId);
            if (event == null) {
                throw new IllegalArgumentException("Event with ID " + eventId + " not found");
            }
            boolean removed = event.removeAttendee(studentId);
            if (!removed) {
                throw new IllegalStateException("You are not registered for this event");
            }
            firebaseService.saveEvent(event);
            return event;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }
}