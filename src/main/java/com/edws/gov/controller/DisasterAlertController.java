package com.edws.gov.controller;

import com.edws.gov.common.ApiResponse;
import com.edws.gov.dto.PageResponseDTO;
import com.edws.gov.dto.alert.AlertRequestDTO;
import com.edws.gov.dto.alert.AlertResponseDTO;
import com.edws.gov.dto.alert.AlertStatusRequestDTO;
import com.edws.gov.enums.AlertStatus;
import com.edws.gov.enums.DisasterType;
import com.edws.gov.enums.Role;
import com.edws.gov.security.SecurityRoutes;
import com.edws.gov.security.annotation.HasRole;
import com.edws.gov.service.DisasterAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(SecurityRoutes.Alerts.BASE)
public class DisasterAlertController {

    private final DisasterAlertService alertService;

    public DisasterAlertController(DisasterAlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping(SecurityRoutes.Alerts.ALL)
    public ResponseEntity<ApiResponse<PageResponseDTO<AlertResponseDTO>>> findAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) DisasterType type,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok(ApiResponse.of("Alerts retrieved successfully",
                alertService.findAll(search, type, status, page, pageSize)));
    }

    @GetMapping(SecurityRoutes.Alerts.DETAILS)
    public ResponseEntity<ApiResponse<AlertResponseDTO>> findById(@PathVariable String alertId) {
        return ResponseEntity.ok(ApiResponse.of("Alert retrieved successfully",
                alertService.findById(alertId)));
    }

    @PostMapping(SecurityRoutes.Alerts.CREATE)
    public ResponseEntity<ApiResponse<AlertResponseDTO>> create(
            @Valid @RequestBody AlertRequestDTO request,
            @RequestParam(defaultValue = "PENDING_REVIEW") AlertStatus status) {

        AlertResponseDTO created = alertService.create(request, status);

        return ResponseEntity
                .created(URI.create(SecurityRoutes.Alerts.BASE + "/" + created.id()))
                .body(ApiResponse.of("Alert saved successfully", created));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN, Role.PROVINCE_ADMIN, Role.DISTRICT_ADMIN,
            Role.DIVISION_ADMIN})
    @PutMapping(SecurityRoutes.Alerts.BY_ID)
    public ResponseEntity<ApiResponse<AlertResponseDTO>> update(
            @PathVariable String alertId,
            @Valid @RequestBody AlertRequestDTO request,
            @RequestParam(defaultValue = "PENDING_REVIEW") AlertStatus status) {

        return ResponseEntity.ok(ApiResponse.of("Alert updated successfully",
                alertService.update(alertId, request, status)));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN, Role.PROVINCE_ADMIN})
    @PutMapping(SecurityRoutes.Alerts.STATUS)
    public ResponseEntity<ApiResponse<AlertResponseDTO>> changeStatus(
            @PathVariable String alertId,
            @Valid @RequestBody AlertStatusRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.of("Alert status updated successfully",
                alertService.changeStatus(alertId, request)));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN})
    @DeleteMapping(SecurityRoutes.Alerts.BY_ID)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String alertId) {
        alertService.delete(alertId);
        return ResponseEntity.ok(ApiResponse.message("Alert deleted successfully"));
    }
}
