package com.mindmate.backend;

import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class BackendApplication {
    static{
        OpenCV.loadLocally();
    }
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

    }
}