package com.edws.gov.dto.alert;

public record AlertAttachmentDTO(
        String id,
        String name,
        Long size,
        String type
) {
}
