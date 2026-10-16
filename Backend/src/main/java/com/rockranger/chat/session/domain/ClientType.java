package com.rockranger.chat.session.domain;

public enum ClientType {
    WEB,
    ANDROID,
    IOS,
    DESKTOP;

    public static ClientType fromString(String val) {
        if (val == null) return WEB;
        try {
            return ClientType.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return WEB;
        }
    }
}
