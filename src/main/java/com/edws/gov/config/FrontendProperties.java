package com.edws.gov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edws.frontend")
public record FrontendProperties(
        String baseUrl,
        String resetPasswordPath,
        String loginPath,
        long resetTokenExpiryMinutes
) {

    public FrontendProperties {
        baseUrl = isBlank(baseUrl) ? "http://localhost:4200" : stripTrailingSlash(baseUrl);
        resetPasswordPath = isBlank(resetPasswordPath) ? "/auth/reset-password" : resetPasswordPath;
        loginPath = isBlank(loginPath) ? "/auth/login" : loginPath;
        resetTokenExpiryMinutes = resetTokenExpiryMinutes <= 0 ? 30 : resetTokenExpiryMinutes;
    }

    public String resetPasswordLink(String token) {
        return baseUrl + resetPasswordPath + "?token=" + token;
    }

    public String loginLink() {
        return baseUrl + loginPath;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
