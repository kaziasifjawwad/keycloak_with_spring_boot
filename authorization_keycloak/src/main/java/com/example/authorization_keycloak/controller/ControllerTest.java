package com.example.authorization_keycloak.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/v1")
public class ControllerTest {

    @GetMapping("/adminonly")
    @RolesAllowed("admin")
    public ResponseEntity<String> getAdminResponse() {
        return ResponseEntity.ok("This is admin");
    }

    @GetMapping("/teacheronly")
    @RolesAllowed("teacher")
    public ResponseEntity<String> getTeacherResponse() {
        return ResponseEntity.ok("this is teacher");
    }

    @GetMapping("/studentonly")
    @RolesAllowed("student")
    public ResponseEntity<String> getStudentResponse() {
        return ResponseEntity.ok("this is student");
    }
}
