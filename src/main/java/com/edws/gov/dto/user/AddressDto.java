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
        Double longitude,

        @Size(max = 30, message = "Category must be at most 30 characters")
        String category,

        @Size(max = 100, message = "Category note must be at most 100 characters")
        String categoryOther,

        @Size(max = 100, message = "Province must be at most 100 characters")
        String province,

        @Size(max = 100, message = "District must be at most 100 characters")
        String district,

        Boolean primary
) {
    public AddressDto(
            String houseName,
            String houseNo,
            String streetAddress1,
            String streetAddress2,
            String zipCode,
            String city,
            String gnDivision,
            Double latitude,
            Double longitude
    ) {
        this(
            houseName,
            houseNo,
            streetAddress1,
            streetAddress2,
            zipCode,
            city,
            gnDivision,
            latitude,
            longitude,
            null,  // category
            null,  // categoryOther
            null,  // province
            null,  // district
            null   // primary
        );
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public String fullAddress() {
        return city + " (" + latitude + ", " + longitude + ")";
    }

}
