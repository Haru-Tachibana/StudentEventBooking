package com.student.eventbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StudentEventBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentEventBookingApplication.class, args);
    }

}