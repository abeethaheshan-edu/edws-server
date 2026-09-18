package com.edws.gov.dto.alert;

import com.edws.gov.entity.DisasterAlert;
import com.edws.gov.enums.AlertSeverity;
import com.edws.gov.enums.AlertStatus;
import com.edws.gov.enums.AreaScope;
import com.edws.gov.enums.DisasterType;

import java.time.Instant;
import java.util.List;

public record AlertResponseDTO(
        String id,
        String referenceCode,
        String title,
        DisasterType type,
        AlertSeverity severity,
        AlertStatus status,
        String description,
        List<AlertAttachmentDTO> attachments,
        AreaScope areaScope,
        String areaName,
        List<AlertBoundaryPointDTO> boundary,
        Integer estimatedPopulation,
        Integer criticalInfrastructure,
        Integer displacedFamilies,
        Integer casualties,
        String responseNotes,
        String createdById,
        String createdByName,
        Instant createdAt,
        Instant updatedAt
) {

    public static AlertResponseDTO from(DisasterAlert alert) {
        List<AlertBoundaryPointDTO> boundary = alert.getBoundary() == null
                ? List.of()
                : alert.getBoundary().stream()
                        .map(point -> new AlertBoundaryPointDTO(point.getLat(), point.getLng()))
                        .toList();

        List<AlertAttachmentDTO> attachments = alert.getAttachments() == null
                ? List.of()
                : alert.getAttachments().stream()
                        .map(file -> new AlertAttachmentDTO(file.getId(), file.getName(), file.getSize(), file.getType()))
                        .toList();

        return new AlertResponseDTO(
                alert.getId(),
                alert.getReferenceCode(),
                alert.getTitle(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getDescription(),
                attachments,
                alert.getAreaScope(),
                alert.getAreaName(),
                boundary,
                alert.getEstimatedPopulation(),
                alert.getCriticalInfrastructure(),
                alert.getDisplacedFamilies(),
                alert.getCasualties(),
                alert.getResponseNotes(),
                alert.getCreatedById(),
                alert.getCreatedByName(),
                alert.getCreatedAt(),
                alert.getUpdatedAt()
        );
    }
}
