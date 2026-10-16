package com.rockranger.chat.common.security;

import com.rockranger.chat.common.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final AppProperties appProperties;
    private final SecretKey signingKey;

    public JwtTokenService(AppProperties appProperties) {
        this.appProperties = appProperties;
        String base64Key = appProperties.getJwt().getSigningKeyBase64();
        if (base64Key != null && !base64Key.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Key));
        } else {
            this.signingKey = Jwts.SIG.HS256.key().build();
        }
    }

    public String generateAccessToken(UUID userId, UUID sessionId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(appProperties.getJwt().getAccessTokenTtlSeconds());

        return Jwts.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .audience().add(appProperties.getJwt().getAudience()).and()
                .subject(userId.toString())
                .claim("sid", sessionId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseAndValidateToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
