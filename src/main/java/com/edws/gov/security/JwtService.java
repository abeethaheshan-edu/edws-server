package com.edws.gov.security;

import com.edws.gov.entity.User;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.security.Keys;


@Slf4j
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;

        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException(
                    "edws.security.jwt.secret is not configured. Set JWT_SECRET in the environment.");
        }
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "edws.security.jwt.secret must be at least 32 bytes for HS256. "
                            + "Current length: " + keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return generate(user, TokenType.ACCESS, properties.expirationMs());
    }

    public String generateRefreshToken(User user) {
        return generate(user, TokenType.REFRESH, properties.refreshExpirationMs());
    }

    public long accessTokenTtlSeconds() {
        return properties.expirationMs() / 1000;
    }

    public long refreshTokenTtlSeconds() {
        return properties.refreshExpirationMs() / 1000;
    }

    private String generate(User user, TokenType type, long ttlMs) {
        System.out.println("TOKENGENERSTE"+ properties.toString() + user.getId() + " " + signingKey);
        Instant now = Instant.now();
        String compact = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(user.getId())
                .claim("tokenType", type.name())
                .claim("role", user.getRole() == null ? null : user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        System.out.println(compact + "TOKENNNN");
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(user.getId())
                .claim("tokenType", type.name())
                .claim("role", user.getRole() == null ? null : user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generatePasswordResetToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(user.getId())
                .claim("tokenType", TokenType.PASSWORD_RESET.name())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(properties.expirationMs())))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }


    public String extractUserId(String token, TokenType expectedType) {
        Claims claims = parse(token);

        String actualType = claims.get("tokenType", String.class);
        if (!expectedType.name().equals(actualType)) {
            throw new ApiException(ErrorCode.TOKEN_TYPE_MISMATCH,
                    "Expected a " + expectedType.name().toLowerCase() + " token");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
        return subject;
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
    }
}
