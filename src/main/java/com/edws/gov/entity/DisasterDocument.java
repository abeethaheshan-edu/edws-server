package com.edws.gov.entity;

import com.edws.gov.enums.DocumentStatus;
import com.edws.gov.enums.WarningLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "documents")
@Getter
@Setter
public class DisasterDocument extends BaseDocument {

    private String type;
    private String category;
    private String title;
    private String description;
    private WarningLevel warningLevel;
    private DocumentStatus status = DocumentStatus.DRAFT;

    private List<String> images = new ArrayList<>();
    private String pdfUrl;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPolygon area;
    private GeoJsonPolygon buffer;

    private List<DocumentApprover> approvers = new ArrayList<>();

    private String createdBy;
}
