package com.rockranger.chat.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private UserSummaryDto user;
    private String accessToken;
    private String refreshToken; // Present for native clients, omitted for web when in cookie
    private String tokenType = "Bearer";
    private long expiresIn;

    public AuthResponse() {}

    public AuthResponse(UserSummaryDto user, String accessToken, long expiresIn) {
        this.user = user;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }

    public AuthResponse(UserSummaryDto user, String accessToken, String refreshToken, long expiresIn) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }

    public UserSummaryDto getUser() { return user; }
    public void setUser(UserSummaryDto user) { this.user = user; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
