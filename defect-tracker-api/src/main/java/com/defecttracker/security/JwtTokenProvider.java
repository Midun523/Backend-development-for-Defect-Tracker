package com.defecttracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes (256 bits) long");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateAccessToken(userPrincipal);
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("typ", "ACCESS");
        claims.put("id", userPrincipal.getId());
        claims.put("employeeId", userPrincipal.getEmployeeId());
        claims.put("role", userPrincipal.getRoleName());
        claims.put("isAdmin", userPrincipal.isAdmin());
        claims.put("fullName", userPrincipal.getFullName());

        return Jwts.builder()
                .issuer("defect-tracker")
                .id(java.util.UUID.randomUUID().toString())
                .subject(userPrincipal.getEmail())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(UserPrincipal userPrincipal) {
        return generateAccessToken(userPrincipal);
    }

    public String generateToken(Authentication authentication) {
        return generateAccessToken(authentication);
    }

    public String generateRefreshToken(UserPrincipal userPrincipal) {
        return generateRefreshToken(userPrincipal.getEmail());
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .issuer("defect-tracker")
                .id(java.util.UUID.randomUUID().toString())
                .subject(email)
                .claim("typ", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("typ", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAccessToken(String token) {
        return "ACCESS".equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(getTokenType(token));
    }

    public enum JwtValidationResult {
        VALID,
        EXPIRED,
        INVALID
    }

    public JwtValidationResult validateTokenDetailed(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authToken);
            return JwtValidationResult.VALID;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT validation failed: expired");
            return JwtValidationResult.EXPIRED;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: invalid");
            return JwtValidationResult.INVALID;
        }
    }

    public boolean validateToken(String authToken) {
        return validateTokenDetailed(authToken) == JwtValidationResult.VALID;
    }
}
