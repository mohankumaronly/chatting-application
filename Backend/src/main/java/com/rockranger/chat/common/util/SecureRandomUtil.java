package com.rockranger.chat.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureRandomUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureRandomUtil() {}

    public static String generateNumericOtp(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String generateRandomToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateRandomSecretKey(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
