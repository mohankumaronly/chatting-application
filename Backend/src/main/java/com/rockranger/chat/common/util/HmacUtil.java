package com.rockranger.chat.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

public final class HmacUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private HmacUtil() {}

    public static String computeHmacHex(String base64Key, String data) {
        if (base64Key == null || data == null) {
            throw new IllegalArgumentException("Key and data cannot be null");
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(keyBytes, HMAC_SHA256));
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC", e);
        }
    }

    public static String computeSha256Hex(String data) {
        if (data == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate SHA-256", e);
        }
    }

    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
