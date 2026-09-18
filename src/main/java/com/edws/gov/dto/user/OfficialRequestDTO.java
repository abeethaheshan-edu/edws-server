package com.edws.gov.dto.user;

import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfficialRequestDTO(

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Telephone is required")
        @Size(max = 20, message = "Telephone must be at most 20 characters")
        String telephone,

        @NotNull(message = "Access role is required")
        Role role,

        @NotNull(message = "Administrative scope is required")
        AdministrativeScope administrativeScope,

        String department,

        String officeAddress,

        String provinceId,

        String districtId,

        String gnDivisionId,

        String reportsToAdminId
) {
}
