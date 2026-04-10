package com.student.eventbooking.service;

import com.student.eventbooking.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class StudentService {

    private static final String NTU_EMAIL_SUFFIX = "@my.ntu.ac.uk";

    private final FirebaseService firebaseService;

    @Autowired
    public StudentService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    public static String normaliseNtuId(String studentId) {
        if (studentId == null || studentId.length() != 8) return studentId;
        char first = Character.toUpperCase(studentId.charAt(0));
        String digits = studentId.substring(1);
        return first + digits;
    }

    public Student registerStudent(Student student) {
        String normalisedId = normaliseNtuId(student.getStudentId());
        student.setStudentId(normalisedId);
        try {
            if (firebaseService.studentExists(normalisedId)) {
                throw new IllegalArgumentException("Student with ID " + normalisedId + " already exists");
            }
            firebaseService.saveStudent(student);
            return student;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Student registerStudentWithAuth(Student student, String firebaseUid) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            throw new IllegalArgumentException("Authentication required");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalisedId = normaliseNtuId(student.getStudentId());
        student.setStudentId(normalisedId);
        student.setFirebaseUid(firebaseUid);
        try {
            Student existingByUid = firebaseService.getStudentByFirebaseUid(firebaseUid);
            if (existingByUid != null) {
                throw new IllegalArgumentException("This account is already linked to a student");
            }
            if (firebaseService.studentExists(normalisedId)) {
                throw new IllegalArgumentException("Student with ID " + normalisedId + " already exists");
            }
            firebaseService.saveStudent(student);
            return student;
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Optional<Student> getStudentByFirebaseUid(String firebaseUid) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            return Optional.empty();
        }
        try {
            Student student = firebaseService.getStudentByFirebaseUid(firebaseUid);
            return Optional.ofNullable(student);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }


    public List<Student> getAllStudents() {
        try {
            return firebaseService.getAllStudents();
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Optional<Student> getStudentById(String studentId) {
        try {
            Student student = firebaseService.getStudentById(studentId);
            return Optional.ofNullable(student);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public boolean studentExists(String studentId) {
        try {
            return firebaseService.studentExists(studentId);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }

    public Optional<Student> getStudentByEmail(String email) {
        if (email == null || !email.endsWith(NTU_EMAIL_SUFFIX)) {
            return Optional.empty();
        }
        String localPart = email.substring(0, email.length() - NTU_EMAIL_SUFFIX.length());
        String normalisedId = normaliseNtuId(localPart);
        try {
            Student student = firebaseService.getStudentById(normalisedId);
            return Optional.ofNullable(student);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore interrupted", e);
        }
    }
}