package com.executionos.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class RootController {

    @GetMapping
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Nexus | Workflow Execution Platform API is running",
                        "swagger", "/swagger-ui/index.html",
                        "health", "/api/system/health"
                )
        );
    }
}
