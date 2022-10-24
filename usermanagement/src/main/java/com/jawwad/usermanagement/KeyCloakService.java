package com.jawwad.usermanagement;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jawwad.usermanagement.config.ConfigData;
import com.jawwad.usermanagement.pojo.AppConstants;
import com.jawwad.usermanagement.DTO.UserEntity;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public void assignClientLevelRole(String userId, List<RoleRepresentation> roleRepresentationList)
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


    public String create(UserEntity userEntity) throws Exception {



        CredentialRepresentation credentialRepresentation =
                preparePasswordRepresentation(userEntity.getPassword());
        UserRepresentation user = prepareUserRepresentation(userEntity, credentialRepresentation);
        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(user);
        System.out.println(
                "User creation status : " + response.getStatus() + " for username : " + user.getUsername());

        String userId = CreatedResponseUtil.getCreatedId(response);
        System.out.println("Keycloak userId : " + userId);

        try {
            List<RoleRepresentation> roles = new ArrayList<>();
            if (!CollectionUtils.isEmpty(userEntity.getRoleEntities())) {
                RolesResource rolesResource = keycloak.realm(realm).roles();
                userEntity
                        .getRoleEntities()
                        .forEach(
                                roleEntity -> {
                                    roles.add(getClientRoleByName(roleEntity).toRepresentation());
//                                    roles.add(getClientRoleByName(roleEntity.getName()).toRepresentation());
                                });
            }

            System.out.println(
                    "Roles Size: "
                            + roles.size()
                            + " --- userEntity.getRoleEntities() size : "
                            + (userEntity.getRoleEntities() != null ? userEntity.getRoleEntities().size() : 0));

            if (!CollectionUtils.isEmpty(roles)) {
                assignClientLevelRole(userId, roles);
                System.out.println(
                        "roles count: "
                                + roles.size()
                                + " saved in Keycloak for username : "
                                + user.getUsername());
            }
        } catch (Exception ex) {
//            System.out.println("Exception while assigning role to user " + userEntity.getUsername(), ex);
//            delete(userId);
//            throw new ExtendedRuntimeException("User creation failed");
        }

        return userId;
    }


    private UserRepresentation prepareUserRepresentation(
            UserEntity request, CredentialRepresentation cR) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setEmailVerified(true);
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setCredentials(List.of(cR));
        newUser.setEnabled(true);

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
       //String salt = password.substring(7, 29);
       String salt = encryptesPass.substring(7,29);
        String encodedString = Base64.getEncoder().encodeToString(salt.getBytes());

        CredentialRepresentation cR = new CredentialRepresentation();
        cR.setTemporary(false);
        cR.setType(CredentialRepresentation.PASSWORD);
        cR.setValue(encryptesPass);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        cR.setSecretData(objectMapper.writeValueAsString(new SecretData(password, encodedString)));
        cR.setCredentialData(new Gson().toJson(new CredentialData("bcrypt", 1024)));
        cR.setValue(null);
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
