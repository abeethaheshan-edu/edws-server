package com.edws.gov.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CitizenMemberDTO(

        @NotBlank(message = "Member name is required")
        @Size(max = 150, message = "Member name must be at most 150 characters")
        String fullName,

        @Pattern(regexp = "^$|^([0-9]{9}[vVxX]|[0-9]{12})$",
                message = "NIC must be 9 digits followed by V/X, or 12 digits")
        String nic,

        @Pattern(regexp = "^$|^(?:\\+94|0)[0-9]{9}$",
                message = "Phone must be a valid Sri Lankan number")
        String phone,

        @Email(message = "Email must be a valid address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email
) {
}
