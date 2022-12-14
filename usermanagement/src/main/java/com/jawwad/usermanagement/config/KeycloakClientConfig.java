package com.jawwad.usermanagement.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/11/04
 */
@Configuration
public class KeycloakClientConfig {

    private String secretKey = ConfigData.secretKey;

    private String clientId = ConfigData.clientId;

    private String authUrl = ConfigData.authUrl;

    private String realm = ConfigData.realm;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .serverUrl(authUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(secretKey)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
