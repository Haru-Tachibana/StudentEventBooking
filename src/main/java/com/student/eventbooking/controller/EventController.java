package com.student.eventbooking.controller;

import com.student.eventbooking.model.Event;
import com.student.eventbooking.model.EventRating;
import com.student.eventbooking.model.Student;
import com.student.eventbooking.service.EventService;
import com.student.eventbooking.service.FirebaseAuthService;
import com.student.eventbooking.service.RatingService;
import com.student.eventbooking.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webresources/events")
public class EventController {

    private final EventService eventService;
    private final RatingService ratingService;
    private final FirebaseAuthService firebaseAuthService;
    private final StudentService studentService;


    @Autowired
    public EventController(EventService eventService, RatingService ratingService,
                           StudentService studentService, FirebaseAuthService firebaseAuthService) {
        this.eventService = eventService;
        this.ratingService = ratingService;
        this.studentService = studentService;
        this.firebaseAuthService = firebaseAuthService;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody Event event, HttpServletRequest request) {

        String uid = firebaseAuthService.getUidFromRequest(request);

        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);

        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        event.setPublisherId(studentOpt.get().getStudentId());

        try {

            Event savedEvent = eventService.createEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location) {
        List<Event> events = eventService.searchEvents(type, location);
        return ResponseEntity.ok(events);
    }


    @GetMapping("/my-events")
    public ResponseEntity<?> getMyPublishedEvents(HttpServletRequest request) {
        String uid = firebaseAuthService.getUidFromRequest(request);
        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);
        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        List<Event> events = eventService.getEventsByPublisherId(studentOpt.get().getStudentId());
        return ResponseEntity.ok(events);
    }


    @GetMapping("/my-registrations")
    public ResponseEntity<?> getMyRegistrations(HttpServletRequest request) {
        String uid = firebaseAuthService.getUidFromRequest(request);
        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);
        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        List<Event> events = eventService.getEventsByAttendee(studentOpt.get().getStudentId());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable String eventId) {

        Optional<Event> eventOpt = eventService.getEventById(eventId);

        if (eventOpt.isPresent()) {
            return ResponseEntity.ok(eventOpt.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Event not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<?> registerForEvent(
            @PathVariable String eventId,
            HttpServletRequest request) {

        String uid = firebaseAuthService.getUidFromRequest(request);

        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required. Sign in to register for events.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);
        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String studentId = studentOpt.get().getStudentId();
        try {
            Event updatedEvent = eventService.registerStudentForEvent(eventId, studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully registered for event");
            response.put("event", updatedEvent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register for event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{eventId}/cancel-registration")
    public ResponseEntity<?> cancelRegistration(@PathVariable String eventId, HttpServletRequest request) {
        String uid = firebaseAuthService.getUidFromRequest(request);
        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);
        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String studentId = studentOpt.get().getStudentId();
        try {
            Event updatedEvent = eventService.cancelRegistration(eventId, studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration cancelled");
            response.put("event", updatedEvent);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cancel registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable String eventId,
            @Valid @RequestBody Event event,
            HttpServletRequest request) {

        String uid = firebaseAuthService.getUidFromRequest(request);

        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);
        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String publisherId = studentOpt.get().getStudentId();
        try {
            Event updated = eventService.updateEvent(eventId, event, publisherId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable String eventId, HttpServletRequest request) {

        String uid = firebaseAuthService.getUidFromRequest(request);

        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Optional<Student> studentOpt = studentService.getStudentByFirebaseUid(uid);
        if (studentOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student profile not found. Complete registration first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String publisherId = studentOpt.get().getStudentId();
        try {
            eventService.deleteEvent(eventId, publisherId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Event deleted");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{eventId}/ratings")
    public ResponseEntity<?> submitRating(
            @PathVariable String eventId,
            @Valid @RequestBody EventRating eventRating) {
        try {
            ratingService.submitRating(eventId, eventRating);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Rating submitted successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to submit rating: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{eventId}/rating")
    public ResponseEntity<?> getEventRating(@PathVariable String eventId) {
        RatingService.EventRatingSummary summary = ratingService.getEventRating(eventId);
        if (summary == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Event not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(summary);
    }
}