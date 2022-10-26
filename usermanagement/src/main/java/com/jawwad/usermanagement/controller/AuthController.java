package com.jawwad.usermanagement.controller;

import com.jawwad.usermanagement.DTO.LoginRequest;
import com.jawwad.usermanagement.DTO.LoginResponse;
import com.jawwad.usermanagement.DTO.RegisterRequest;
import com.jawwad.usermanagement.DTO.RoleCreationRequest;
import com.jawwad.usermanagement.service.KeyCloakService;
import com.jawwad.usermanagement.config.KeycloakProvider;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotAuthorizedException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class AuthController {
    private final KeycloakProvider keycloakProvider;
    private final KeyCloakService keyCloakService;

    @PostMapping("/oauth/register")
    public ResponseEntity<?> register(@NotNull @RequestBody RegisterRequest registerRequest) throws Exception {
        return ResponseEntity.ok(keyCloakService.create(registerRequest));
    }

    @GetMapping("/oauth/getAllRoles")
    public ResponseEntity<List<RoleRepresentation>> getAllRoles() throws Exception {
        return ResponseEntity.ok(keyCloakService.getAllRoles());
    }

    @GetMapping("/oauth/getRoleByName/{rolename}")
    public ResponseEntity<RoleRepresentation> getRoleByName(@PathVariable String rolename) throws Exception {
        return ResponseEntity.ok(keyCloakService.findRoleByRoleName(rolename));
    }

    @PostMapping("/oauth/addRole")
    public ResponseEntity<Void> addRole(@NotNull @RequestBody RoleCreationRequest roleCreationRequest){
        this.keyCloakService.createRealLevelRole(roleCreationRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<LoginResponse> login(@NotNull @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        try (Keycloak keycloak =
                     keycloakProvider
                             .newKeycloakBuilderWithPasswordCredentials(
                                     loginRequest.getUsername(), loginRequest.getPassword())
                             .build()) {
            AccessTokenResponse accessTokenResponse = keycloak.tokenManager().getAccessToken();
            BeanUtils.copyProperties(accessTokenResponse, loginResponse);
            return ResponseEntity.ok(loginResponse);
        } catch (NotAuthorizedException ex) {
//            log.warn(INVALID_USER_CREDENTIALS, ex);
//            loginResponse.setError(INVALID_CREDENTIAL);
//            loginResponse.setErrorDescription(INVALID_USER_CREDENTIALS);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse);
        }
    }
}
