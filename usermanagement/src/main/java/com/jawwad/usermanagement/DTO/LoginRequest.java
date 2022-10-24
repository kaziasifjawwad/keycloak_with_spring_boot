package com.jawwad.usermanagement.DTO;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String username;
    private String password;
}