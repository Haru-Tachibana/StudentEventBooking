package com.student.eventbooking.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service.account.path}")
    private String serviceAccountPath;

    @Value("${firebase.project.id}")
    private String projectId;

    private String resolveServiceAccountPath() {
        File f = new File(serviceAccountPath);
        if (f.isFile()) return f.getAbsolutePath();
        String cwd = System.getProperty("user.dir");
        f = new File(cwd, serviceAccountPath.replaceFirst("^\\./", ""));
        if (f.isFile()) return f.getAbsolutePath();
        f = new File(cwd, "firebase-service-account.json");
        if (f.isFile()) return f.getAbsolutePath();
        File parent = new File(cwd).getParentFile();
        if (parent != null) {
            f = new File(parent, "firebase-service-account.json");
            if (f.isFile()) return f.getAbsolutePath();
        }
        return serviceAccountPath;
    }

    @PostConstruct
    public void initialize() throws IOException {

        if (FirebaseApp.getApps().isEmpty()) {
            String path = resolveServiceAccountPath();
            FileInputStream serviceAccount = new FileInputStream(path);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}