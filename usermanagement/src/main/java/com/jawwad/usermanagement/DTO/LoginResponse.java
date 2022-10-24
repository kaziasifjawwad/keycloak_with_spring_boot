package com.jawwad.usermanagement.DTO;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    @JsonProperty("access_token")
    protected String token;

    @JsonProperty("expires_in")
    protected Long expiresIn;

    @JsonProperty("refresh_expires_in")
    protected Long refreshExpiresIn;

    @JsonProperty("refresh_token")
    protected String refreshToken;

    @JsonProperty("token_type")
    protected String tokenType;

    @JsonProperty("id_token")
    protected String idToken;

    @JsonProperty("not-before-policy")
    protected Integer notBeforePolicy;

    @JsonProperty("session_state")
    protected String sessionState;

    @JsonProperty("scope")
    protected String scope;

    @JsonProperty("error")
    protected String error;

    @JsonProperty("error_description")
    protected String errorDescription;

    @JsonProperty("error_uri")
    protected String errorUri;
}
