package com.edws.gov.dto.auth;

public record OfficialInvitationContent(
        String fullName,
        String email,
        String role,
        String temporaryPassword,
        String loginLink
) {

    @Override
    public String toString() {
        return "OfficialInvitationContent[fullName=" + fullName + ", temporaryPassword=***]";
    }
}
