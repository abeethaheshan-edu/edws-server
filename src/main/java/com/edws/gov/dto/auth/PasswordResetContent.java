package com.edws.gov.dto.auth;

public record PasswordResetContent(
        String fullName,
        String resetLink,
        long expiryMinutes
) {

    @Override
    public String toString() {
        return "PasswordResetContent[fullName=" + fullName + ", resetLink=***]";
    }
}
