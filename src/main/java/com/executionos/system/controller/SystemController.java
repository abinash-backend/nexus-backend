package com.executionos.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final String serviceName;

    public SystemController(
            @Value("${system.health.service-name:Nexus | Workflow Execution Platform}") String serviceName) {
        this.serviceName = serviceName;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = new HealthResponse(
                "UP",
                serviceName,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public record HealthResponse(
            String status,
            String service,
            Instant timestamp
    ) {
    }
}
