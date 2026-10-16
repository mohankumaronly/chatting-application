package com.rockranger.chat.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UsernameRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-z0-9_]{3,30}$", message = "Username must be 3-30 lowercase characters, digits or underscore")
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
