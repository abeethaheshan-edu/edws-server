package com.edws.gov.dto.user;

import com.edws.gov.dto.GeoPointDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record  PropertyRequest(
        @NotBlank(message = "Property label is required")
        @Size(max = 100, message = "Label must be at most 100 characters")
        String label,

        @Size(max = 50, message = "House number must be at most 50 characters")
        String houseNo,

        @Size(max = 150, message = "Street must be at most 150 characters")
        String street,

        @Size(max = 100, message = "Town must be at most 100 characters")
        String town,

        @NotBlank(message = "Property GN division is required")
        String gnDivisionId,

        @Valid
        GeoPointDto location
) {

}
