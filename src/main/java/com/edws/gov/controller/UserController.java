package com.edws.gov.controller;

import com.edws.gov.common.ApiResponse;
import com.edws.gov.dto.PageResponseDTO;
import com.edws.gov.dto.user.OfficialRequestDTO;
import com.edws.gov.dto.user.OfficialTeamRequestDTO;
import com.edws.gov.dto.user.CitizenRequestDTO;
import com.edws.gov.dto.user.UserResponseDTO;
import com.edws.gov.security.SecurityRoutes;
import com.edws.gov.enums.Role;
import com.edws.gov.security.annotation.GnOfficerOnly;
import com.edws.gov.security.annotation.HasRole;
import com.edws.gov.service.OfficialService;
import com.edws.gov.service.UserService;
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
import java.util.List;


@RestController
@RequestMapping(SecurityRoutes.Users.BASE)
public class UserController {

    private final UserService userService;
    private final OfficialService officialService;

    public UserController(UserService userService, OfficialService officialService) {
        this.userService = userService;
        this.officialService = officialService;
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN, Role.PROVINCE_ADMIN, Role.DISTRICT_ADMIN})
    @GetMapping(SecurityRoutes.Users.OFFICIALS)
    public ResponseEntity<ApiResponse<PageResponseDTO<UserResponseDTO>>> findOfficials(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok(ApiResponse.of("Officials retrieved successfully",
                officialService.findAll(search, role, page, pageSize)));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN, Role.PROVINCE_ADMIN, Role.DISTRICT_ADMIN})
    @GetMapping(SecurityRoutes.Users.OFFICIAL_BY_ID)
    public ResponseEntity<ApiResponse<UserResponseDTO>> findOfficial(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.of("Official retrieved successfully",
                officialService.findById(userId)));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN, Role.PROVINCE_ADMIN, Role.DISTRICT_ADMIN})
    @PostMapping(SecurityRoutes.Users.OFFICIALS)
    public ResponseEntity<ApiResponse<UserResponseDTO>> createOfficial(
            @Valid @RequestBody OfficialRequestDTO request) {

        UserResponseDTO created = officialService.create(request);
        return ResponseEntity
                .created(URI.create(SecurityRoutes.Users.BASE + "/officials/" + created.id()))
                .body(ApiResponse.of("Invitation sent successfully to the official's email", created));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN})
    @PutMapping(SecurityRoutes.Users.OFFICIAL_BY_ID)
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateOfficial(
            @PathVariable String userId,
            @Valid @RequestBody OfficialRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.of("Official updated successfully",
                officialService.update(userId, request)));
    }

    @HasRole({Role.SUPER_ADMIN})
    @DeleteMapping(SecurityRoutes.Users.OFFICIAL_BY_ID)
    public ResponseEntity<ApiResponse<Void>> deleteOfficial(@PathVariable String userId) {
        officialService.delete(userId);
        return ResponseEntity.ok(ApiResponse.message("Official removed successfully"));
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN})
    @PutMapping(SecurityRoutes.Users.OFFICIAL_TEAM)
    public ResponseEntity<ApiResponse<UserResponseDTO>> assignTeam(
            @PathVariable String userId,
            @RequestBody OfficialTeamRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.of("Team updated successfully",
                officialService.assignTeam(userId, request)));
    }

    @GnOfficerOnly
    @GetMapping(SecurityRoutes.Users.CITIZENS)
    public ResponseEntity<ApiResponse<PageResponseDTO<UserResponseDTO>>> findCitizens(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok(ApiResponse.of("Citizens retrieved successfully",
                userService.findCitizens(search, page, pageSize)));
    }

    @GnOfficerOnly
    @PostMapping(SecurityRoutes.Users.CITIZENS)
    public ResponseEntity<ApiResponse<UserResponseDTO>> createCitizen(@Valid @RequestBody CitizenRequestDTO request) {
        UserResponseDTO created = userService.createCitizen(request);
        return ResponseEntity
                .created(URI.create(SecurityRoutes.Users.BASE + "/citizens/" + created.id()))
                .body(ApiResponse.of(created));

    }

    @GnOfficerOnly
    @PostMapping(SecurityRoutes.Users.ADDRESS)
    public ResponseEntity<ApiResponse<UserResponseDTO>> saveFamilyMembers(@Valid @RequestBody List<CitizenRequestDTO> request) {
        UserResponseDTO created = userService.saveFamilyMembers(request,"");
        return ResponseEntity
                .created(URI.create(SecurityRoutes.Users.BASE + "/citizens/" + created.id()))
                .body(ApiResponse.of(created));
    }
}
