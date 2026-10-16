package com.rockranger.chat.auth.dto;

public class RegistrationFinishRequest {

    private boolean skipOptionalProfile = true;

    public boolean isSkipOptionalProfile() { return skipOptionalProfile; }
    public void setSkipOptionalProfile(boolean skipOptionalProfile) { this.skipOptionalProfile = skipOptionalProfile; }
}
