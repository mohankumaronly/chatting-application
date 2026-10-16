package com.rockranger.chat.common.util;

import java.util.regex.Pattern;

public final class NormalizationUtil {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private NormalizationUtil() {}

    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return trimmed.length() <= 320 && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static String normalizeUsername(String username) {
        if (username == null) return null;
        return username.trim().toLowerCase();
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        String normalized = normalizeUsername(username);
        return USERNAME_PATTERN.matcher(normalized).matches();
    }
}
