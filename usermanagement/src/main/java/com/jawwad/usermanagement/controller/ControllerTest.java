package com.jawwad.usermanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/v1")
public class ControllerTest {

    @GetMapping("/teacher")
    @RolesAllowed("teacher")
    public ResponseEntity<String> getAdminResponse() {
        return ResponseEntity.ok("This is admin");
    }

    @GetMapping("/student")
    @RolesAllowed("student")
    public ResponseEntity<String> getTeacherResponse() {
        return ResponseEntity.ok("this is teacher");
    }
}