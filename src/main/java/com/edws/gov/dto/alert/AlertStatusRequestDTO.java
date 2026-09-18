package com.edws.gov.dto.alert;

import com.edws.gov.enums.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record AlertStatusRequestDTO(

        @NotNull(message = "Status is required")
        AlertStatus status,

        String note
) {
}
