package com.diary.io.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;

    /**
     * The signing secret is loaded from configuration (app.jwt.secret) so that
     * the key is stable across restarts and is never hard-coded in source. If
     * no secret is configured a random key is generated for local development
     * only (tokens will not survive a restart). A configured secret must be at
     * least 32 bytes for HS256.
     */
    public JwtProvider(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long jwtExpirationMs) {

        if (StringUtils.hasText(secret)) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            this.jwtSecret = Keys.hmacShaKeyFor(keyBytes);
        } else {
            log.warn("No 'app.jwt.secret' configured - generating an ephemeral key. "
                    + "Set app.jwt.secret in production so tokens stay valid across restarts.");
            this.jwtSecret = Jwts.SIG.HS256.key().build();
        }
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpirationMs))
                .signWith(jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** Returns the token expiry, used by the blacklist to bound how long an entry is retained. */
    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
