package com.neobank.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UiBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(UiBackendApplication.class, args);
        System.out.println("UI Backend Service Started on http://localhost:8080");
        System.out.println("WebSocket endpoint: ws://localhost:8080/ws/stats");
    }
}