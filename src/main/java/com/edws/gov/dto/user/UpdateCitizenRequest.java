package com.edws.gov.dto.user;

import com.edws.gov.enums.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record UpdateCitizenRequest(

        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @Pattern(regexp = "^(?:\\+94|0)[0-9]{9}$",
                message = "Phone must be a valid Sri Lankan number, e.g. 0771234567 or +94771234567")
        String phone,

        @Valid
        AddressDto address,

        UserStatus status
) {
}
