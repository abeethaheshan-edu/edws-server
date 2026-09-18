package com.edws.gov.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String nic;
    private String fullName;
    private String phone;
    private String secondaryPhone;
    private Address address;

    public UserProfile(
            @NotBlank(message = "NIC is required")
            @Pattern(
                    regexp = "^([0-9]{9}[vVxX]|[0-9]{12})$",
                    message = "NIC must be 9 digits followed by V/X, or 12 digits"
            )
            String nic,

            @NotBlank(message = "Full name is required")
            @Size(max = 150, message = "Full name must be at most 150 characters")
            String fullName,

            @NotBlank(message = "Phone number is required")
            @Pattern(
                    regexp = "^(?:\\+94|0)[0-9]{9}$",
                    message = "Phone must be a valid Sri Lankan number, e.g. 0771234567 or +94771234567"
            )
            String phone,

            Address address
    ) {
        this.nic = nic;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }
}
