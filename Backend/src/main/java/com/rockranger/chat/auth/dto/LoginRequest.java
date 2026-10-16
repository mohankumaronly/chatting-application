package com.rockranger.chat.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Identifier (email or username) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    private boolean keepLoggedIn = true;
    private String clientType = "WEB";

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isKeepLoggedIn() { return keepLoggedIn; }
    public void setKeepLoggedIn(boolean keepLoggedIn) { this.keepLoggedIn = keepLoggedIn; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
}
