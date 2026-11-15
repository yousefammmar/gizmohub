package com.gizmohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point. Run with: ./mvnw spring-boot:run  then open http://localhost:8080 */
@SpringBootApplication
public class GizmoHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(GizmoHubApplication.class, args);
    }
}
