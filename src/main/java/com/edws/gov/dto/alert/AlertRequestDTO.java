package com.edws.gov.dto.alert;

import com.edws.gov.enums.AlertSeverity;
import com.edws.gov.enums.AreaScope;
import com.edws.gov.enums.DisasterType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AlertRequestDTO(

        @NotBlank(message = "Alert title is required")
        @Size(max = 150, message = "Alert title must be at most 150 characters")
        String title,

        @NotNull(message = "Disaster type is required")
        DisasterType type,

        @NotNull(message = "Severity is required")
        AlertSeverity severity,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        List<@Valid AlertAttachmentDTO> attachments,

        @NotNull(message = "Administrative scope is required")
        AreaScope areaScope,

        @NotBlank(message = "Specific area is required")
        String areaName,

        @NotNull(message = "Affected area boundary is required")
        @Size(min = 3, message = "A boundary needs at least 3 points")
        List<@Valid AlertBoundaryPointDTO> boundary,

        @PositiveOrZero(message = "Estimated population cannot be negative")
        Integer estimatedPopulation,

        @PositiveOrZero(message = "Critical infrastructure cannot be negative")
        Integer criticalInfrastructure,

        @PositiveOrZero(message = "Displaced families cannot be negative")
        Integer displacedFamilies,

        @PositiveOrZero(message = "Casualties cannot be negative")
        Integer casualties,

        String responseNotes
) {
}
