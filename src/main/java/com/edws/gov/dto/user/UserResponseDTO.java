package com.edws.gov.dto.user;

import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;

import java.time.Instant;


public record UserResponseDTO(
        String id,
        String email,
        Role role,
        UserStatus status,
        String fullName,
        String nic,
        String phone,
        AddressDto address,
        String gnDivisionId,
        String gnDivisionName,
        String registeredById,
        Boolean isHouseHolder,
        Instant createdAt,
        Instant updatedAt

) {
}
