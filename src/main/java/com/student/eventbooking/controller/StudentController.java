package com.student.eventbooking.controller;

import com.student.eventbooking.model.Event;
import com.student.eventbooking.model.Student;
import com.student.eventbooking.service.FirebaseAuthService;
import com.student.eventbooking.service.FirebaseService;
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
@RequestMapping("/webresources/students")
public class StudentController {

    private final StudentService studentService;
    private final RatingService ratingService;
    private final FirebaseAuthService firebaseAuthService;

    @Autowired
    public StudentController(StudentService studentService, RatingService ratingService,
                             FirebaseAuthService firebaseAuthService) {
        this.studentService = studentService;
        this.ratingService = ratingService;
        this.firebaseAuthService = firebaseAuthService;
    }


    @PostMapping
    public ResponseEntity<?> registerStudent(@Valid @RequestBody Student student,
                                             HttpServletRequest request) {
        String uid = firebaseAuthService.getUidFromRequest(request);
        if (uid == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        try {
            Student savedStudent = studentService.registerStudentWithAuth(student, uid);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentStudent(HttpServletRequest request) {
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(studentOpt.get());
    }


    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }


    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable String studentId) {
        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isPresent()) {
            return ResponseEntity.ok(studentOpt.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{studentId}/pending-ratings")
    public ResponseEntity<?> getPendingRatings(@PathVariable String studentId) {
        if (!studentService.studentExists(studentId)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Student not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        List<Event> pending = ratingService.getPendingRatingsForStudent(studentId);
        return ResponseEntity.ok(pending);
    }
}