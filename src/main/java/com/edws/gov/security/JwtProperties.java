package com.edws.gov.security;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "edws.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        long refreshExpirationMs,
        String issuer
) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "edws-server";
        }
    }
}
