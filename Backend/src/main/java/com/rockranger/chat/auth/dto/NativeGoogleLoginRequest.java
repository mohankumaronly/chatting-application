package com.rockranger.chat.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class NativeGoogleLoginRequest {

    @NotBlank(message = "ID token is required")
    private String idToken;

    private String clientType = "WEB";
    private boolean keepLoggedIn = true;

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    public boolean isKeepLoggedIn() { return keepLoggedIn; }
    public void setKeepLoggedIn(boolean keepLoggedIn) { this.keepLoggedIn = keepLoggedIn; }
}
