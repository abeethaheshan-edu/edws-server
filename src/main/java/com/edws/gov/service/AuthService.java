package com.edws.gov.service;

import com.edws.gov.config.FrontendProperties;
import com.edws.gov.dto.auth.AuthenticatedUserResponse;
import com.edws.gov.dto.auth.ForgotPasswordRequest;
import com.edws.gov.dto.auth.LoginRequest;
import com.edws.gov.dto.auth.OfficialInvitationContent;
import com.edws.gov.dto.auth.PasswordResetContent;
import com.edws.gov.dto.auth.ResetPasswordRequest;
import com.edws.gov.dto.auth.UpdatePasswordRequest;
import com.edws.gov.entity.PasswordResetToken;
import com.edws.gov.entity.User;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import com.edws.gov.notification.NotificationChannel;
import com.edws.gov.notification.NotificationRecipient;
import com.edws.gov.notification.NotificationRequest;
import com.edws.gov.notification.NotificationService;
import com.edws.gov.repo.PasswordResetTokenRepository;
import com.edws.gov.repo.UserRepository;
import com.edws.gov.security.AuthHeaders;
import com.edws.gov.security.JwtService;
import com.edws.gov.security.SessionContext;
import com.edws.gov.security.TokenType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String RESET_TEMPLATE = "password-reset";
    private static final String INVITATION_TEMPLATE = "official-invitation";
    private static final String ROLE_POLICY_LOCATION = "roles/";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final FrontendProperties frontend;
    private final SessionContext session;

    private final Map<Role, String> rolePolicies = new EnumMap<>(Role.class);

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       NotificationService notificationService,
                       FrontendProperties frontend,
                       SessionContext session) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.frontend = frontend;
        this.session = session;
    }

    @PostConstruct
    void loadRolePolicies() {
        for (Role role : Role.values()) {
            String location = ROLE_POLICY_LOCATION + role.name() + ".json";
            ClassPathResource resource = new ClassPathResource(location);

            if (!resource.exists()) {
                throw new IllegalStateException(
                        "Missing role policy file classpath:" + location
                                + ". Every Role must have one, otherwise the front end receives an "
                                + "empty policy and silently hides every screen for that role.");
            }
            try (InputStream in = resource.getInputStream()) {
                rolePolicies.put(role, new String(in.readAllBytes(), StandardCharsets.UTF_8).trim());
            } catch (IOException ex) {
                throw new IllegalStateException("Could not read classpath:" + location, ex);
            }
        }
        log.info("Loaded {} role policy files", rolePolicies.size());
    }

    public AuthResult login(LoginRequest request) {
        String email = normalise(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Failed login attempt for {}", email);
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        log.info("User {} signed in with role {}", user.getId(), user.getRole());
        return issueTokens(user);
    }

    public AuthResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ErrorCode.TOKEN_MISSING,
                    "Refresh token must be sent in the " + AuthHeaders.REFRESH_TOKEN + " header");
        }

        String userId = jwtService.extractUserId(refreshToken.trim(), TokenType.REFRESH);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        return issueTokens(user);
    }

    public String rolePolicyForCurrentUser() {
        Role role = session.getCurrentUser().getRole();
        if (role == null) {
            throw new ApiException(ErrorCode.ROLE_DEFINITION_NOT_FOUND, "This account has no role");
        }
        String policy = rolePolicies.get(role);
        if (policy == null) {
            throw new ApiException(ErrorCode.ROLE_DEFINITION_NOT_FOUND,
                    "No access policy exists for role " + role);
        }
        return policy;
    }

    public String allRolePolicies() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        rolePolicies.values().forEach(joiner::add);
        return joiner.toString();
    }

    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = normalise(request.email());
        Optional<User> optUser = userRepository.findByEmail(email);

        if (optUser.isEmpty() || optUser.get().getStatus() != UserStatus.ACTIVE) {
            log.info("Password reset requested for an unknown or inactive account");
            return;
        }

        User user = optUser.get();
        String rawToken = generateSecret(32);
        long expiryMinutes = frontend.resetTokenExpiryMinutes();

        resetTokenRepository.deleteByUserId(user.getId());
        resetTokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(sha256(rawToken))
                .expiresAt(Instant.now().plusSeconds(expiryMinutes * 60))
                .createdAt(Instant.now())
                .build());

        notificationService.send(NotificationRequest.builder()
                .template(RESET_TEMPLATE)
                .channel(NotificationChannel.EMAIL)
                .subject("Reset your eDWS password")
                .recipient(NotificationRecipient.builder()
                        .userId(user.getId())
                        .name(fullName(user))
                        .email(user.getEmail())
                        .phone(phone(user))
                        .build())
                .content(new PasswordResetContent(
                        fullName(user),
                        frontend.resetPasswordLink(rawToken),
                        expiryMinutes))
                .build());

        log.info("Password reset link issued for user {}", user.getId());
    }

    public void sendOfficialInvitation(User user, String temporaryPassword) {
        notificationService.send(NotificationRequest.builder()
                .template(INVITATION_TEMPLATE)
                .channel(NotificationChannel.EMAIL)
                .subject("Your eDWS account has been created")
                .recipient(NotificationRecipient.builder()
                        .userId(user.getId())
                        .name(fullName(user))
                        .email(user.getEmail())
                        .phone(phone(user))
                        .build())
                .content(new OfficialInvitationContent(
                        fullName(user),
                        user.getEmail(),
                        user.getRole() == null ? "" : user.getRole().name(),
                        temporaryPassword,
                        frontend.loginLink()))
                .build());

        log.info("Invitation email queued for user {}", user.getId());
    }

    public String generateTemporaryPassword() {
        return "Temp" + generateSecret(6).replaceAll("[^A-Za-z0-9]", "") + "#1";
    }

    public void resetPassword(String rawToken, ResetPasswordRequest request) {
        assertConfirmationMatches(request.newPassword(), request.confirmPassword());

        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(ErrorCode.RESET_TOKEN_INVALID);
        }

        PasswordResetToken token = resetTokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new ApiException(ErrorCode.RESET_TOKEN_INVALID));

        if (token.isUsed()) {
            log.warn("Reset token for user {} was replayed", token.getUserId());
            throw new ApiException(ErrorCode.RESET_TOKEN_INVALID);
        }
        if (token.isExpired()) {
            throw new ApiException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESET_TOKEN_INVALID));

        if (user.getPassword() != null
                && passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.PASSWORD_REUSED);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTempPassword(false);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        resetTokenRepository.save(token);

        log.info("Password reset completed for user {}", user.getId());
    }

    public void updatePassword(UpdatePasswordRequest request) {
        assertConfirmationMatches(request.newPassword(), request.confirmPassword());

        User caller = session.getCurrentUser();

        User user = userRepository.findById(caller.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Failed password update attempt for user {}", user.getId());
            throw new ApiException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.PASSWORD_REUSED);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTempPassword(false);
        userRepository.save(user);

        resetTokenRepository.deleteByUserId(user.getId());

        log.info("Password updated for user {}", user.getId());
    }

    private AuthResult issueTokens(User user) {
        System.out.println(user.getId()+"USERCCC");
        return new AuthResult(
                user.toAuthenticatedResponse(),
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                jwtService.accessTokenTtlSeconds(),
                jwtService.refreshTokenTtlSeconds()
        );
    }

    private void assertConfirmationMatches(String newPassword, String confirmPassword) {
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new ApiException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }
    }

    private String generateSecret(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String fullName(User user) {
        return user.getProfile() == null ? null : user.getProfile().getFullName();
    }

    private String phone(User user) {
        return user.getProfile() == null ? null : user.getProfile().getPhone();
    }

    private String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public record AuthResult(
            AuthenticatedUserResponse user,
            String accessToken,
            String refreshToken,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds
    ) {

        public HttpHeaders toHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.set(AuthHeaders.ACCESS_TOKEN, accessToken);
            headers.set(AuthHeaders.REFRESH_TOKEN, refreshToken);
            headers.set(AuthHeaders.TOKEN_TYPE, AuthHeaders.BEARER);
            headers.set(AuthHeaders.ACCESS_TOKEN_EXPIRES_IN, String.valueOf(accessTokenTtlSeconds));
            headers.set(AuthHeaders.REFRESH_TOKEN_EXPIRES_IN, String.valueOf(refreshTokenTtlSeconds));
            headers.setCacheControl("no-store");
            return headers;
        }
    }
}
