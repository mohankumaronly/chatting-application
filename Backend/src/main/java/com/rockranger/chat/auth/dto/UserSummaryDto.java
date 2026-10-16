package com.rockranger.chat.auth.dto;

import java.util.UUID;

public class UserSummaryDto {

    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private boolean profileComplete;

    public UserSummaryDto() {}

    public UserSummaryDto(UUID id, String username, String firstName, String lastName, String email, String status, boolean profileComplete) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
        this.profileComplete = profileComplete;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }
}
