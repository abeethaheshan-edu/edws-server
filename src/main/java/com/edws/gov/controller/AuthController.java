package com.edws.gov.controller;

import com.edws.gov.common.ApiResponse;
import com.edws.gov.dto.auth.AuthenticatedUserResponse;
import com.edws.gov.dto.auth.ForgotPasswordRequest;
import com.edws.gov.dto.auth.LoginRequest;
import com.edws.gov.dto.auth.ResetPasswordRequest;
import com.edws.gov.dto.auth.UpdatePasswordRequest;
import com.edws.gov.dto.user.UserResponseDTO;
import com.edws.gov.security.AuthHeaders;
import com.edws.gov.enums.Role;
import com.edws.gov.security.SecurityRoutes;
import com.edws.gov.security.SessionContext;
import com.edws.gov.security.annotation.HasRole;
import com.edws.gov.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SecurityRoutes.Auth.BASE)
public class AuthController {

    private final AuthService authService;
    private final SessionContext session;

    public AuthController(AuthService authService, SessionContext session) {
        this.authService = authService;
        this.session = session;
    }

    @PostMapping(SecurityRoutes.Auth.LOGIN)
    public ResponseEntity<ApiResponse<AuthenticatedUserResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);

        return ResponseEntity.ok()
                .headers(result.toHeaders())
                .body(ApiResponse.of("Signed in successfully", result.user()));
    }

    @PostMapping(SecurityRoutes.Auth.REFRESH)
    public ResponseEntity<ApiResponse<AuthenticatedUserResponse>> refresh(
            @RequestHeader(value = AuthHeaders.REFRESH_TOKEN, required = false) String refreshToken) {
        AuthService.AuthResult result = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .headers(result.toHeaders())
                .body(ApiResponse.of("Token refreshed successfully", result.user()));
    }

    @GetMapping(SecurityRoutes.Auth.ME)
    public ResponseEntity<ApiResponse<UserResponseDTO>> me() {
        return ResponseEntity.ok(
                ApiResponse.of("Profile retrieved successfully", session.getCurrentUser().toUserResponseDTO()));
    }

    @GetMapping(value = SecurityRoutes.Auth.ROLES_ME, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> myAccessPolicy() {
        return ResponseEntity.ok(authService.rolePolicyForCurrentUser());
    }

    @HasRole({Role.SUPER_ADMIN, Role.ADMIN})
    @GetMapping(value = SecurityRoutes.Auth.ROLES, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> allAccessPolicies() {
        return ResponseEntity.ok(authService.allRolePolicies());
    }

    @PostMapping(SecurityRoutes.Auth.FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);

        return ResponseEntity.ok(
                ApiResponse.message("If an account exists for that email, a reset link has been sent"));
    }

    @PostMapping(SecurityRoutes.Auth.RESET_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable String token,
                                                           @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(token, request);

        return ResponseEntity.ok(ApiResponse.message("Password reset successfully, please sign in"));
    }

    @PostMapping(SecurityRoutes.Auth.UPDATE_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);

        return ResponseEntity.ok(ApiResponse.message("Password updated successfully"));
    }
}
