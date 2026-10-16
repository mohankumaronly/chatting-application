package com.rockranger.chat.unit;

import com.rockranger.chat.common.error.ApiException;
import com.rockranger.chat.common.security.AccountStatusGuard;
import com.rockranger.chat.common.security.CurrentUser;
import com.rockranger.chat.common.util.HmacUtil;
import com.rockranger.chat.common.util.NormalizationUtil;
import com.rockranger.chat.common.util.SecureRandomUtil;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CoreSecurityAndUtilityTests {

    @Test
    void testEmailNormalization() {
        assertEquals("mohan@example.com", NormalizationUtil.normalizeEmail("  MOHAN@Example.COM  "));
        assertTrue(NormalizationUtil.isValidEmail("user@example.com"));
        assertFalse(NormalizationUtil.isValidEmail("invalid-email"));
    }

    @Test
    void testUsernameNormalizationAndValidation() {
        assertEquals("mohan_dev", NormalizationUtil.normalizeUsername("  MOHAN_DEV  "));
        assertTrue(NormalizationUtil.isValidUsername("mohan_dev"));
        assertTrue(NormalizationUtil.isValidUsername("mohan2026"));
        assertFalse(NormalizationUtil.isValidUsername("mo")); // too short (< 3)
        assertFalse(NormalizationUtil.isValidUsername("mohan@dev")); // invalid char
        assertFalse(NormalizationUtil.isValidUsername("mohan dev")); // space
    }

    @Test
    void testSecureRandomOtpAndTokens() {
        String otp = SecureRandomUtil.generateNumericOtp(6);
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("^\\d{6}$"));

        String token = SecureRandomUtil.generateRandomToken(32);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testHmacSha256AndConstantTimeEquals() {
        String base64Key = SecureRandomUtil.generateRandomSecretKey(32);
        String data = "test-challenge-1234:REGISTRATION:482193";

        String hmac1 = HmacUtil.computeHmacHex(base64Key, data);
        String hmac2 = HmacUtil.computeHmacHex(base64Key, data);

        assertNotNull(hmac1);
        assertEquals(hmac1, hmac2);
        assertTrue(HmacUtil.constantTimeEquals(hmac1, hmac2));
        assertFalse(HmacUtil.constantTimeEquals(hmac1, "different-hmac"));
    }

    @Test
    void testAccountStatusGuardEnforcesLifecycle() {
        AccountStatusGuard guard = new AccountStatusGuard();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        // Active user should pass
        CurrentUser activeUser = new CurrentUser(userId, sessionId, "test@example.com", "test_user", UserStatus.ACTIVE);
        assertDoesNotThrow(() -> guard.checkActive(activeUser));

        // Pending deletion user must be blocked with 403 ACCOUNT_PENDING_DELETION
        CurrentUser pendingDeletion = new CurrentUser(userId, sessionId, "test@example.com", "test_user", UserStatus.PENDING_DELETION);
        ApiException ex1 = assertThrows(ApiException.class, () -> guard.checkActive(pendingDeletion));
        assertEquals("ACCOUNT_PENDING_DELETION", ex1.getErrorCode().name());

        // Suspended user must be blocked with 403 ACCOUNT_SUSPENDED
        CurrentUser suspended = new CurrentUser(userId, sessionId, "test@example.com", "test_user", UserStatus.SUSPENDED);
        ApiException ex2 = assertThrows(ApiException.class, () -> guard.checkActive(suspended));
        assertEquals("ACCOUNT_SUSPENDED", ex2.getErrorCode().name());

        // Deactivated user must be blocked with 410 ACCOUNT_DELETED
        CurrentUser deactivated = new CurrentUser(userId, sessionId, "test@example.com", "test_user", UserStatus.DEACTIVATED);
        ApiException ex3 = assertThrows(ApiException.class, () -> guard.checkActive(deactivated));
        assertEquals("ACCOUNT_DELETED", ex3.getErrorCode().name());

        // Pending setup user must be blocked with 403 ACCOUNT_NOT_ACTIVE
        CurrentUser pendingSetup = new CurrentUser(userId, sessionId, "test@example.com", "test_user", UserStatus.PENDING_SETUP);
        ApiException ex4 = assertThrows(ApiException.class, () -> guard.checkActive(pendingSetup));
        assertEquals("ACCOUNT_NOT_ACTIVE", ex4.getErrorCode().name());
    }
}
