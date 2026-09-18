package com.edws.gov.dto.auth;

import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Permission;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;

import java.util.Set;


public record AuthenticatedUserResponse(
        String id,
        String email,
        String fullName,
        Role role,
        UserStatus status,
        AdministrativeScope administrativeScope,
        Set<Permission> permissions,
        String gnDivisionId,
        boolean mustChangePassword,
        boolean houseHolder
) {
}
