package com.edws.gov.service;

import com.edws.gov.dto.PageResponseDTO;
import com.edws.gov.dto.alert.AlertRequestDTO;
import com.edws.gov.dto.alert.AlertResponseDTO;
import com.edws.gov.dto.alert.AlertStatusRequestDTO;
import com.edws.gov.entity.DisasterAlert;
import com.edws.gov.entity.User;
import com.edws.gov.enums.AlertStatus;
import com.edws.gov.enums.DisasterType;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import com.edws.gov.repo.DisasterAlertRepository;
import com.edws.gov.security.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DisasterAlertService {

    private static final Logger log = LoggerFactory.getLogger(DisasterAlertService.class);

    private final DisasterAlertRepository alertRepository;
    private final MongoTemplate mongoTemplate;
    private final SessionContext session;

    public DisasterAlertService(DisasterAlertRepository alertRepository,
                                MongoTemplate mongoTemplate,
                                SessionContext session) {
        this.alertRepository = alertRepository;
        this.mongoTemplate = mongoTemplate;
        this.session = session;
    }

    public PageResponseDTO<AlertResponseDTO> findAll(String search,
                                                     DisasterType type,
                                                     AlertStatus status,
                                                     int page,
                                                     int pageSize) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                pageSize <= 0 ? 10 : pageSize,
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Query query = new Query();

        if (search != null && !search.isBlank()) {
            String quoted = Pattern.quote(search.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(quoted, "i"),
                    Criteria.where("referenceCode").regex(quoted, "i"),
                    Criteria.where("areaName").regex(quoted, "i")));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        long total = mongoTemplate.count(query, DisasterAlert.class);
        List<DisasterAlert> alerts = mongoTemplate.find(query.with(pageable), DisasterAlert.class);
        Page<DisasterAlert> result = new PageImpl<>(alerts, pageable, total);

        return PageResponseDTO.of(result, AlertResponseDTO::from);
    }

    public AlertResponseDTO findById(String alertId) {
        return AlertResponseDTO.from(load(alertId));
    }

    public AlertResponseDTO create(AlertRequestDTO request, AlertStatus status) {
        User creator = session.getCurrentUser();
        Instant now = Instant.now();

        DisasterAlert alert = DisasterAlert.builder()
                .referenceCode(nextReferenceCode(request))
                .status(status == null ? AlertStatus.DRAFT : status)
                .createdById(creator.getId())
                .createdByName(creator.getProfile() != null ? creator.getProfile().getFullName() : creator.getEmail())
                .createdAt(now)
                .updatedAt(now)
                .build();

        apply(alert, request);
        DisasterAlert saved = alertRepository.save(alert);

        log.info("Alert {} created by {}", saved.getReferenceCode(), creator.getId());
        return AlertResponseDTO.from(saved);
    }

    public AlertResponseDTO update(String alertId, AlertRequestDTO request, AlertStatus status) {
        DisasterAlert alert = load(alertId);

        apply(alert, request);
        if (status != null) {
            alert.setStatus(status);
        }
        alert.setUpdatedAt(Instant.now());

        return AlertResponseDTO.from(alertRepository.save(alert));
    }

    public AlertResponseDTO changeStatus(String alertId, AlertStatusRequestDTO request) {
        DisasterAlert alert = load(alertId);

        alert.setStatus(request.status());
        if (request.note() != null && !request.note().isBlank()) {
            alert.setResponseNotes(request.note());
        }
        alert.setUpdatedAt(Instant.now());

        log.info("Alert {} moved to {}", alert.getReferenceCode(), request.status());
        return AlertResponseDTO.from(alertRepository.save(alert));
    }

    public void delete(String alertId) {
        DisasterAlert alert = load(alertId);
        alertRepository.delete(alert);
        log.info("Alert {} deleted", alert.getReferenceCode());
    }

    private DisasterAlert load(String alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new ApiException(ErrorCode.PATH_NOT_FOUND, "Alert not found: " + alertId));
    }

    private void apply(DisasterAlert alert, AlertRequestDTO request) {
        alert.setTitle(request.title().trim());
        alert.setType(request.type());
        alert.setSeverity(request.severity());
        alert.setDescription(request.description().trim());
        alert.setAreaScope(request.areaScope());
        alert.setAreaName(request.areaName().trim());
        alert.setEstimatedPopulation(request.estimatedPopulation());
        alert.setCriticalInfrastructure(request.criticalInfrastructure());
        alert.setDisplacedFamilies(request.displacedFamilies());
        alert.setCasualties(request.casualties());
        alert.setResponseNotes(request.responseNotes());

        List<DisasterAlert.AlertBoundaryPoint> boundary = new ArrayList<>();
        if (request.boundary() != null) {
            request.boundary().forEach(point -> boundary.add(
                    DisasterAlert.AlertBoundaryPoint.builder().lat(point.lat()).lng(point.lng()).build()));
        }
        alert.setBoundary(boundary);

        List<DisasterAlert.AlertAttachment> attachments = new ArrayList<>();
        if (request.attachments() != null) {
            request.attachments().forEach(file -> attachments.add(
                    DisasterAlert.AlertAttachment.builder()
                            .id(file.id())
                            .name(file.name())
                            .size(file.size())
                            .type(file.type())
                            .build()));
        }
        alert.setAttachments(attachments);
    }

    private String nextReferenceCode(AlertRequestDTO request) {
        String year = String.valueOf(Year.now().getValue());
        String code = request.type().name().substring(0, 2);

        for (int sequence = (int) alertRepository.count() + 1; sequence < 10000; sequence++) {
            String reference = "DMC/%s/%s/%04d".formatted(year, code, sequence);
            if (!alertRepository.existsByReferenceCode(reference)) {
                return reference;
            }
        }

        throw new ApiException(ErrorCode.INTERNAL_ERROR, "Could not allocate an alert reference code");
    }
}
