package com.example.authorization_keycloak.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ControllerTest {

    @GetMapping("/adminonly")
    public ResponseEntity<String> getAdminResponse() {
        return ResponseEntity.ok("This is admin");
    }

    @GetMapping("/teacheronly")
    public ResponseEntity<String> getTeacherResponse() {
        return ResponseEntity.ok("this is teacher");
    }
}
