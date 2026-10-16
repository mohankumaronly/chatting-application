package com.rockranger.chat.profile.dto;

public class UsernameCheckResponse {

    private String username;
    private boolean available;

    public UsernameCheckResponse() {}

    public UsernameCheckResponse(String username, boolean available) {
        this.username = username;
        this.available = available;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
