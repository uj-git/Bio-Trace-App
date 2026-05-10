package com.biotrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication marks this as the starting point of the app.
// When you run this, Spring Boot starts a web server on port 8080.
@SpringBootApplication
public class BioTraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BioTraceApplication.class, args);
    }

}
