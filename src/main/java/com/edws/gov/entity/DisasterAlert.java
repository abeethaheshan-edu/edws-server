package com.edws.gov.entity;

import com.edws.gov.enums.AlertSeverity;
import com.edws.gov.enums.AlertStatus;
import com.edws.gov.enums.AreaScope;
import com.edws.gov.enums.DisasterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "disaster_alerts")
public class DisasterAlert {

    @Id
    private String id;

    @Indexed(unique = true)
    private String referenceCode;

    private String title;

    private DisasterType type;

    private AlertSeverity severity;

    @Indexed
    private AlertStatus status;

    private String description;

    private List<AlertAttachment> attachments = new ArrayList<>();

    private AreaScope areaScope;

    private String areaName;

    private List<AlertBoundaryPoint> boundary = new ArrayList<>();

    private Integer estimatedPopulation;

    private Integer criticalInfrastructure;

    private Integer displacedFamilies;

    private Integer casualties;

    private String responseNotes;

    private String createdById;

    private String createdByName;

    private Instant createdAt;

    private Instant updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertBoundaryPoint {
        private Double lat;
        private Double lng;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertAttachment {
        private String id;
        private String name;
        private Long size;
        private String type;
    }
}
