package com.edws.gov.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;


public record CitizenRequestDTO(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @NotBlank(message = "NIC is required")
        @Pattern(regexp = "^([0-9]{9}[vVxX]|[0-9]{12})$",
                message = "NIC must be 9 digits followed by V/X, or 12 digits")
        String nic,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^(?:\\+94|0)[0-9]{9}$",
                message = "Phone must be a valid Sri Lankan number, e.g. 0771234567 or +94771234567")
        String phone,

        @Pattern(regexp = "^$|^(?:\\+94|0)[0-9]{9}$",
                message = "Secondary phone must be a valid Sri Lankan number")
        String secondaryPhone,

        @Valid
        AddressDto address,
        String gnDivisionId,
        String districtId,
        String provinceId,

        @Valid
        @Size(max = 20, message = "At most 20 properties can be registered at once")
        List<AddressDto> properties,

        @Size(max = 20, message = "At most 20 family members can be linked at once")
        @Valid
        @Size(max = 20, message = "At most 20 members can be registered at once")
        List<CitizenMemberDTO> members,

        List<@NotBlank String> familyMemberIds
) {
}
