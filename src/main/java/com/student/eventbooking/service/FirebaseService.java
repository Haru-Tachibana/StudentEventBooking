package com.student.eventbooking.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.student.eventbooking.model.Event;
import com.student.eventbooking.model.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    // Student Operations
    public void saveStudent(Student student) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getStudentId());
        data.put("name", student.getName());
        data.put("email", student.getEmail());
        data.put("firebaseUid", student.getFirebaseUid());
        if (student.getStudentId() != null) {
            getFirestore().collection("students")
                    .document(student.getStudentId())
                    .set(data)
                    .get();
        }
    }

    public Student getStudentById(String studentId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = getFirestore()
                .collection("students")
                .document(studentId)
                .get()
                .get();

        if (document.exists()) {
            Student student = new Student();
            student.setStudentId(document.getString("studentId"));
            student.setName(document.getString("name"));
            student.setEmail(document.getString("email"));
            student.setFirebaseUid(document.getString("firebaseUid"));
            return student;
        }
        return null;
    }

    public Student getStudentByFirebaseUid(String firebaseUid) throws ExecutionException, InterruptedException {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            return null;
        }
        QuerySnapshot querySnapshot = getFirestore()
                .collection("students")
                .whereEqualTo("firebaseUid", firebaseUid)
                .limit(1)
                .get()
                .get();
        if (querySnapshot.isEmpty()) {
            return null;
        }
        QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
        Student student = new Student();
        student.setStudentId(document.getString("studentId"));
        student.setName(document.getString("name"));
        student.setEmail(document.getString("email"));
        student.setFirebaseUid(document.getString("firebaseUid"));
        return student;
    }


    public Student getStudentByEmail(String email) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = getFirestore()
                .collection("students")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();

        if (!querySnapshot.isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
            Student student = new Student();
            student.setStudentId(document.getString("studentId"));
            student.setName(document.getString("name"));
            student.setEmail(document.getString("email"));
            student.setFirebaseUid(document.getString("firebaseUid"));
            return student;
        }
        return null;
    }

    public boolean studentExists(String studentId) throws ExecutionException, InterruptedException {
        return getFirestore()
                .collection("students")
                .document(studentId)
                .get()
                .get()
                .exists();
    }

    public List<Student> getAllStudents() throws ExecutionException, InterruptedException {
        List<Student> students = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("students")
                .get()
                .get();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            Student student = new Student();
            student.setStudentId(document.getString("studentId"));
            student.setName(document.getString("name"));
            student.setEmail(document.getString("email"));
            student.setFirebaseUid(document.getString("firebaseUid"));
            students.add(student);
        }
        return students;
    }

    // Event Operations
    public void saveEvent(Event event) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("publisherId", event.getPublisherId());
        data.put("title", event.getTitle());
        data.put("type", event.getType());
        data.put("date", event.getDate());
        data.put("time", event.getTime());
        data.put("duration", event.getDuration());
        data.put("location", event.getLocation());
        data.put("venue", event.getVenue());
        data.put("cost", event.getCost());
        data.put("maxParticipants", event.getMaxParticipants());
        if (event.getAttendees() != null) {
            data.put("attendees", event.getAttendees());
        } else {
            data.put("attendees", new ArrayList<>());
        }
        data.put("description", event.getDescription());
        if (event.getImageUrl() != null) {
            data.put("imageUrl", event.getImageUrl());
        }
        if (event.getEventSeriesId() != null) {
            data.put("eventSeriesId", event.getEventSeriesId());
        }
        if (event.getAverageRating() != null) {
            data.put("averageRating", event.getAverageRating());
        }
        if (event.getRatingCount() != null) {
            data.put("ratingCount", event.getRatingCount());
        }
        if (event.getEventId() != null) {
            getFirestore().collection("events")
                    .document(event.getEventId())
                    .set(data)
                    .get();
        }
    }

    public Event getEventById(String eventId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = getFirestore()
                .collection("events")
                .document(eventId)
                .get()
                .get();

        if (document.exists()) {
            return mapToEvent(document);
        }
        return null;
    }

    public List<Event> getAllEvents() throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .get()
                .get();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            events.add(mapToEvent(document));
        }
        return events;
    }

    public List<Event> getEventsByType(String type) throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .whereEqualTo("type", type)
                .get()
                .get();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            events.add(mapToEvent(document));
        }
        return events;
    }

    public List<Event> getEventsByLocation(String location) throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .whereGreaterThanOrEqualTo("location", location)
                .whereLessThanOrEqualTo("location", location + "\uf8ff")
                .get()
                .get();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            String eventLocation = document.getString("location");
            String eventVenue = document.getString("venue");
            if (eventLocation != null && eventLocation.toLowerCase().contains(location.toLowerCase()) ||
                    eventVenue != null && eventVenue.toLowerCase().contains(location.toLowerCase())) {
                events.add(mapToEvent(document));
            }
        }
        return events;
    }

    public List<Event> getEventsByTypeAndLocation(String type, String location) throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .whereEqualTo("type", type)
                .get()
                .get();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            String eventLocation = document.getString("location");
            String eventVenue = document.getString("venue");
            if (eventLocation != null && eventLocation.toLowerCase().contains(location.toLowerCase()) ||
                    eventVenue != null && eventVenue.toLowerCase().contains(location.toLowerCase())) {
                events.add(mapToEvent(document));
            }
        }
        return events;
    }

    public List<Event> getEventsByDate(String date) throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .whereEqualTo("date", date)
                .get()
                .get();

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            events.add(mapToEvent(document));
        }
        return events;
    }

    public boolean eventExists(String eventId) throws ExecutionException, InterruptedException {
        return getFirestore()
                .collection("events")
                .document(eventId)
                .get()
                .get()
                .exists();
    }

    // Event Series Operations
    public String getNextEventId() throws ExecutionException, InterruptedException {
        List<Event> all = getAllEvents();
        int max = 0;
        for (Event e : all) {
            String id = e.getEventId();
            if (id != null && !id.isEmpty()) {
                try {
                    int n = Integer.parseInt(id);
                    if (n > max) {
                        max = n;
                    }
                } catch (NumberFormatException ignored) {

                }
            }
        }
        return String.valueOf(max + 1);
    }

    public List<Event> getEventsByPublisherId(String publisherId) throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .whereEqualTo("publisherId", publisherId)
                .get()
                .get();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            events.add(mapToEvent(document));
        }
        return events;
    }

    public List<Event> getEventsByAttendee(String studentId) throws ExecutionException, InterruptedException {
        List<Event> events = new ArrayList<>();
        QuerySnapshot querySnapshot = getFirestore()
                .collection("events")
                .whereArrayContains("attendees", studentId)
                .get()
                .get();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            events.add(mapToEvent(document));
        }
        return events;
    }

    public void deleteEvent(String eventId) throws ExecutionException, InterruptedException {
        if (eventId != null) {
            getFirestore().collection("events").document(eventId).delete().get();
        }
    }

    private Event mapToEvent(DocumentSnapshot document) {
        Event event = new Event();
        event.setEventId(document.getString("eventId"));
        event.setPublisherId(document.getString("publisherId"));
        event.setTitle(document.getString("title"));
        event.setType(document.getString("type"));
        event.setDate(document.getString("date"));
        event.setTime(document.getString("time"));
        Long duration = document.getLong("duration");
        if (duration != null) {
            event.setDuration(duration.intValue());
        }
        event.setLocation(document.getString("location"));
        event.setVenue(document.getString("venue"));
        Double cost = document.getDouble("cost");
        if (cost != null) {
            event.setCost(cost);
        }
        Long maxParticipants = document.getLong("maxParticipants");
        if (maxParticipants != null) {
            event.setMaxParticipants(maxParticipants.intValue());
        }
        @SuppressWarnings("unchecked")
        List<String> attendees = (List<String>) document.get("attendees");

        if (attendees != null) {
            event.setAttendees(attendees);
        } else {
            event.setAttendees(new ArrayList<>());
        }

        event.setDescription(document.getString("description"));
        event.setEventSeriesId(document.getString("eventSeriesId"));
        Double avgRating = document.getDouble("averageRating");

        if (avgRating != null) {
            event.setAverageRating(avgRating);
        }

        Long ratingCountLong = document.getLong("ratingCount");

        if (ratingCountLong != null) {
            event.setRatingCount(ratingCountLong.intValue());
        }
        return event;
    }

    public void saveRating(String eventId, String studentId, int rating) throws ExecutionException, InterruptedException {
        String docId = eventId + "_" + studentId;
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("studentId", studentId);
        data.put("rating", rating);
        data.put("createdAt", System.currentTimeMillis());
        getFirestore().collection("event_ratings")
                .document(docId)
                .set(data)
                .get();
    }

    public boolean hasStudentRated(String eventId, String studentId) throws ExecutionException, InterruptedException {
        String docId = eventId + "_" + studentId;
        DocumentSnapshot doc = getFirestore().collection("event_ratings")
                .document(docId)
                .get()
                .get();
        return doc.exists();
    }


    public Integer getStudentRating(String eventId, String studentId) throws ExecutionException, InterruptedException {
        String docId = eventId + "_" + studentId;
        DocumentSnapshot doc = getFirestore().collection("event_ratings")
                .document(docId)
                .get()
                .get();
        if (!doc.exists()) {
            return null;
        }
        Long ratingLong = doc.getLong("rating");
        if (ratingLong == null) {
            return null;
        }
        return Integer.valueOf(ratingLong.intValue());
    }

    public void updateEventRatingFields(String eventId, double averageRating, int ratingCount) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("averageRating", averageRating);
        updates.put("ratingCount", ratingCount);
        getFirestore().collection("events")
                .document(eventId)
                .update(updates)
                .get();
    }
}