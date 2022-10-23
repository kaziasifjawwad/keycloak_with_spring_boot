package com.jawwad.usermanagement.controller;

import com.jawwad.usermanagement.DTO.UserEntity;
import com.jawwad.usermanagement.KeyCloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class Tester implements CommandLineRunner {

    private final KeyCloakService keyCloakService;

    @Override
    public void run(String... args) throws Exception {
        UserEntity userEntity = new UserEntity()
                .setDeleted(false)
                .setEmail("asifjawwad@gmail.com")
                .setFirstName("asif")
                .setLastName("jawwad")
                .setPassword("123456789")
                .setId("1")
                .setUsername("kaziasifjawwad")
                .setRoleEntities(List.of("admin"));

        keyCloakService.create(userEntity);

    }
}
