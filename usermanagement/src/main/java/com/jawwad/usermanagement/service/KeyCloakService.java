package com.jawwad.usermanagement.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.jawwad.usermanagement.DTO.RegisterRequest;
import com.jawwad.usermanagement.DTO.RoleCreationRequest;
import com.jawwad.usermanagement.config.ConfigData;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeyCloakService {

    private final BCryptPasswordEncoder passwordEncoder;


    private final Keycloak keycloak;

    private final String clientId = ConfigData.clientId;

    private final String realm = ConfigData.realm;


    private final String authUrl = ConfigData.authUrl;

    public void createRealLevelRole(RoleCreationRequest roleCreationRequest) {
        RolesResource roleResource = keycloak.realm(realm).roles();
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleCreationRequest.getRoleName());
        roleRepresentation.setDescription(roleCreationRequest.getRoleDescription());
        roleResource.create(roleRepresentation);
    }

    public RoleRepresentation findRoleByRoleName(String roleName) {
        RolesResource roleResource = keycloak.realm(realm).roles();
        return roleResource.get(roleName).toRepresentation();
    }

    public List<RoleRepresentation> getAllRoles() {
        RolesResource roleResource = keycloak.realm(realm).roles();
        return roleResource.list();
    }


    public void assignRealmLevelRole(String userId, List<RoleRepresentation> roleRepresentationList)
            throws Exception {
        List<ClientRepresentation> clientByName = findClientById(clientId);
        keycloak
                .realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
//                .clientLevel(clientByName.get(0).getId())
                .add(roleRepresentationList);
    }


    public void assigClientLevelRole(String userId, List<RoleRepresentation> roleRepresentationList)
            throws Exception {
        List<ClientRepresentation> clientByName = findClientById(clientId);
        keycloak
                .realm(realm)
                .users()
                .get(userId)
                .roles()
                .clientLevel(clientByName.get(0).getId())
                .add(roleRepresentationList);
    }

    public List<ClientRepresentation> findClientById(String clientId) throws Exception {
        List<ClientRepresentation> clientRepresentationList =
                keycloak.realm(realm).clients().findByClientId(clientId);
        if (CollectionUtils.isEmpty(clientRepresentationList)) {
            throw new Exception("Not Found");
        }
        return clientRepresentationList;
    }


    public RoleResource getClientRoleByName(String roleName) {
        ClientRepresentation clientRepresentation = null;
        try {
            List<ClientRepresentation> clientByName = findClientById(clientId);
            if (CollectionUtils.isEmpty(clientByName)) {
                log.error("Client Not Found " + clientId);
                return null;
            }
            clientRepresentation = clientByName.get(0);
        } catch (Exception e) {
            log.error("Client fetch exception " + clientId, e);
            return null;
        }
        return keycloak.realm(realm).clients().get(clientRepresentation.getId()).roles().get(roleName);
    }


    public String create(RegisterRequest registerRequest) throws Exception {


        CredentialRepresentation credentialRepresentation =
                preparePasswordRepresentation(registerRequest.getPassword());
        UserRepresentation user = prepareUserRepresentation(registerRequest, credentialRepresentation);
        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(user);
        System.out.println(
                "User creation status : " + response.getStatus() + " for username : " + user.getUsername());

        String userId = CreatedResponseUtil.getCreatedId(response);
        System.out.println("Keycloak userId : " + userId);

        try {
            List<RoleRepresentation> roles = new ArrayList<>();
            if (!CollectionUtils.isEmpty(registerRequest.getRoleEntities())) {
                RolesResource rolesResource = keycloak.realm(realm).roles();
                registerRequest
                        .getRoleEntities()
                        .forEach(
                                roleEntity -> {
                                    roles.add(findRoleByRoleName(roleEntity));
                                });
            }

            System.out.println(
                    "Roles Size: "
                            + roles.size()
                            + " --- userEntity.getRoleEntities() size : "
                            + (registerRequest.getRoleEntities() != null ? registerRequest.getRoleEntities().size() : 0));

            if (!CollectionUtils.isEmpty(roles)) {
                assignRealmLevelRole(userId, roles);
                System.out.println(
                        "roles count: "
                                + roles.size()
                                + " saved in Keycloak for username : "
                                + user.getUsername());


            }
        } catch (Exception ex) {
            System.out.println("-----------------------");
            System.out.println(ex);
            System.out.println("-----------------------");
//            System.out.println("Exception while assigning role to user " + userEntity.getUsername(), ex);
//            delete(userId);
//            throw new ExtendedRuntimeException("User creation failed");
        }

        return userId;
    }


    private UserRepresentation prepareUserRepresentation(
            RegisterRequest request, CredentialRepresentation cR) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setEmailVerified(true);
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setCredentials(List.of(cR));
        newUser.setEnabled(true);
        newUser.setRealmRoles(request.getRoleEntities());

        Map<String, List<String>> attributes = new HashMap<>();
/*        attributes.put(AppConstants.ATTRIBUTE_USER_TABLE_ID, Arrays.asList(request.getId().toString()));
        attributes.put(AppConstants.ATTRIBUTE_USER_TYPE, Arrays.asList(request.getType()));
        attributes.put(
                AppConstants.ATTRIBUTE_USER_TYPE_ITEM_CODE, Arrays.asList(request.getTypeItemCode()));*/
        newUser.setAttributes(attributes);
        return newUser;
    }


    private CredentialRepresentation preparePasswordRepresentation(String password)
            throws JsonProcessingException {
        var encryptesPass = passwordEncoder.encode(password);
        String salt = encryptesPass.substring(7, 29);
//        String salt = password.substring(1, 3);
        String encodedString = Base64.getEncoder().encodeToString(salt.getBytes());

        CredentialRepresentation cR = new CredentialRepresentation();
        cR.setTemporary(false);
        cR.setType(CredentialRepresentation.PASSWORD);
        cR.setValue(password);
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

//        cR.setSecretData(objectMapper.writeValueAsString(new SecretData(encryptesPass, encodedString)));
//        cR.setCredentialData(new Gson().toJson(new CredentialData("bcrypt", 1024)));
//        cR.setValue(null);
        return cR;
    }


    @AllArgsConstructor
    class SecretData implements Serializable {
        private String value;
        private String salt;
    }

    @AllArgsConstructor
    class CredentialData implements Serializable {
        private String algorithm;
        private int hashIterations;
    }
}
