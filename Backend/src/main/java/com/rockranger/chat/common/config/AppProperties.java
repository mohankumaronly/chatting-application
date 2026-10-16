package com.rockranger.chat.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name = "chatting-application";
    private String frontendUrl = "http://localhost:5173";
    private JwtProperties jwt = new JwtProperties();
    private RefreshTokenProperties refreshToken = new RefreshTokenProperties();
    private CookieProperties cookie = new CookieProperties();
    private BrevoProperties brevo = new BrevoProperties();
    private CorsProperties cors = new CorsProperties();
    private OtpProperties otp = new OtpProperties();
    private RegistrationProperties registration = new RegistrationProperties();
    private PasswordResetProperties passwordReset = new PasswordResetProperties();
    private UsernamePolicyProperties usernamePolicy = new UsernamePolicyProperties();
    private AccountDeletionProperties accountDeletion = new AccountDeletionProperties();
    private BlockProperties block = new BlockProperties();
    private RecoveryTokenProperties recoveryToken = new RecoveryTokenProperties();
    private KeyProperties keys = new KeyProperties();
    private CloudinaryProperties cloudinary = new CloudinaryProperties();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFrontendUrl() { return frontendUrl; }
    public void setFrontendUrl(String frontendUrl) { this.frontendUrl = frontendUrl; }
    public JwtProperties getJwt() { return jwt; }
    public void setJwt(JwtProperties jwt) { this.jwt = jwt; }
    public RefreshTokenProperties getRefreshToken() { return refreshToken; }
    public void setRefreshToken(RefreshTokenProperties refreshToken) { this.refreshToken = refreshToken; }
    public CookieProperties getCookie() { return cookie; }
    public void setCookie(CookieProperties cookie) { this.cookie = cookie; }
    public BrevoProperties getBrevo() { return brevo; }
    public void setBrevo(BrevoProperties brevo) { this.brevo = brevo; }
    public CorsProperties getCors() { return cors; }
    public void setCors(CorsProperties cors) { this.cors = cors; }
    public OtpProperties getOtp() { return otp; }
    public void setOtp(OtpProperties otp) { this.otp = otp; }
    public RegistrationProperties getRegistration() { return registration; }
    public void setRegistration(RegistrationProperties registration) { this.registration = registration; }
    public PasswordResetProperties getPasswordReset() { return passwordReset; }
    public void setPasswordReset(PasswordResetProperties passwordReset) { this.passwordReset = passwordReset; }
    public UsernamePolicyProperties getUsernamePolicy() { return usernamePolicy; }
    public void setUsernamePolicy(UsernamePolicyProperties usernamePolicy) { this.usernamePolicy = usernamePolicy; }
    public AccountDeletionProperties getAccountDeletion() { return accountDeletion; }
    public void setAccountDeletion(AccountDeletionProperties accountDeletion) { this.accountDeletion = accountDeletion; }
    public BlockProperties getBlock() { return block; }
    public void setBlock(BlockProperties block) { this.block = block; }
    public RecoveryTokenProperties getRecoveryToken() { return recoveryToken; }
    public void setRecoveryToken(RecoveryTokenProperties recoveryToken) { this.recoveryToken = recoveryToken; }
    public KeyProperties getKeys() { return keys; }
    public void setKeys(KeyProperties keys) { this.keys = keys; }
    public CloudinaryProperties getCloudinary() { return cloudinary; }
    public void setCloudinary(CloudinaryProperties cloudinary) { this.cloudinary = cloudinary; }

    public static class JwtProperties {
        private String issuer = "http://localhost:8080";
        private String audience = "chat-api";
        private long accessTokenTtlSeconds = 600;
        private String signingKeyBase64;

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
        public long getAccessTokenTtlSeconds() { return accessTokenTtlSeconds; }
        public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) { this.accessTokenTtlSeconds = accessTokenTtlSeconds; }
        public String getSigningKeyBase64() { return signingKeyBase64; }
        public void setSigningKeyBase64(String signingKeyBase64) { this.signingKeyBase64 = signingKeyBase64; }
    }

    public static class RefreshTokenProperties {
        private long ttlSeconds = 604800;
        private long rememberTtlSeconds = 2592000;

        public long getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }
        public long getRememberTtlSeconds() { return rememberTtlSeconds; }
        public void setRememberTtlSeconds(long rememberTtlSeconds) { this.rememberTtlSeconds = rememberTtlSeconds; }
    }

    public static class CookieProperties {
        private boolean secure = false;
        private String sameSite = "Lax";
        private String domain = "";
        private String path = "/api/v1/auth";

        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        public String getSameSite() { return sameSite; }
        public void setSameSite(String sameSite) { this.sameSite = sameSite; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }

    public static class BrevoProperties {
        private String apiKey;
        private String senderEmail;
        private String senderName = "Chat App";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getSenderEmail() { return senderEmail; }
        public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
    }

    public static class CorsProperties {
        private String allowedOrigins = "http://localhost:5173";

        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class OtpProperties {
        private long ttlSeconds = 300;
        private long resendCooldownSeconds = 60;
        private int maxAttempts = 5;

        public long getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }
        public long getResendCooldownSeconds() { return resendCooldownSeconds; }
        public void setResendCooldownSeconds(long resendCooldownSeconds) { this.resendCooldownSeconds = resendCooldownSeconds; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    }

    public static class RegistrationProperties {
        private long challengeTtlSeconds = 1200;

        public long getChallengeTtlSeconds() { return challengeTtlSeconds; }
        public void setChallengeTtlSeconds(long challengeTtlSeconds) { this.challengeTtlSeconds = challengeTtlSeconds; }
    }

    public static class PasswordResetProperties {
        private long ttlSeconds = 600;

        public long getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    }

    public static class UsernamePolicyProperties {
        private int cooldownDays = 30;

        public int getCooldownDays() { return cooldownDays; }
        public void setCooldownDays(int cooldownDays) { this.cooldownDays = cooldownDays; }
    }

    public static class AccountDeletionProperties {
        private int graceDays = 20;
        private int batchSize = 100;
        private String schedulerCron = "0 0 * * * *";

        public int getGraceDays() { return graceDays; }
        public void setGraceDays(int graceDays) { this.graceDays = graceDays; }
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        public String getSchedulerCron() { return schedulerCron; }
        public void setSchedulerCron(String schedulerCron) { this.schedulerCron = schedulerCron; }
    }

    public static class BlockProperties {
        private int maxPerHour = 100;

        public int getMaxPerHour() { return maxPerHour; }
        public void setMaxPerHour(int maxPerHour) { this.maxPerHour = maxPerHour; }
    }

    public static class RecoveryTokenProperties {
        private long ttlSeconds = 1728000;
        private String hmacKeyBase64;

        public long getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }
        public String getHmacKeyBase64() { return hmacKeyBase64; }
        public void setHmacKeyBase64(String hmacKeyBase64) { this.hmacKeyBase64 = hmacKeyBase64; }
    }

    public static class KeyProperties {
        private String fieldEncryptionBase64;
        private String identifierHmacBase64;
        private String otpHmacBase64;

        public String getFieldEncryptionBase64() { return fieldEncryptionBase64; }
        public void setFieldEncryptionBase64(String fieldEncryptionBase64) { this.fieldEncryptionBase64 = fieldEncryptionBase64; }
        public String getIdentifierHmacBase64() { return identifierHmacBase64; }
        public void setIdentifierHmacBase64(String identifierHmacBase64) { this.identifierHmacBase64 = identifierHmacBase64; }
        public String getOtpHmacBase64() { return otpHmacBase64; }
        public void setOtpHmacBase64(String otpHmacBase64) { this.otpHmacBase64 = otpHmacBase64; }
    }

    public static class CloudinaryProperties {
        private String cloudName;
        private String uploadPreset;
        private String avatarFolder = "chat-app/avatars";

        public String getCloudName() { return cloudName; }
        public void setCloudName(String cloudName) { this.cloudName = cloudName; }
        public String getUploadPreset() { return uploadPreset; }
        public void setUploadPreset(String uploadPreset) { this.uploadPreset = uploadPreset; }
        public String getAvatarFolder() { return avatarFolder; }
        public void setAvatarFolder(String avatarFolder) { this.avatarFolder = avatarFolder; }
    }
}