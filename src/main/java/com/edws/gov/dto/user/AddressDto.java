package com.edws.gov.dto.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;


public record AddressDto(
        @Size(max = 50, message = "House name must be at most 50 characters")
        String houseName,

        @Size(max = 50, message = "House number must be at most 50 characters")
        String houseNo,

        @Size(max = 150, message = "Street address must be at most 150 characters")
        String streetAddress1,

        @Size(max = 150, message = "Street address must be at most 150 characters")
        String streetAddress2,

        @Size(max = 20, message = "Zip code must be at most 20 characters")
        String zipCode,

        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @Size(max = 50)
        String gnDivision,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        Double longitude
) {
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public String fullAddress() {
        return city + " (" + latitude + ", " + longitude + ")";
    }

}
