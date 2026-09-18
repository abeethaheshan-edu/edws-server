package com.edws.gov.service;

import com.edws.gov.dto.PageResponseDTO;
import com.edws.gov.dto.user.OfficialRequestDTO;
import com.edws.gov.dto.user.OfficialTeamRequestDTO;
import com.edws.gov.dto.user.UserResponseDTO;
import com.edws.gov.entity.AdminProfile;
import com.edws.gov.entity.User;
import com.edws.gov.entity.UserProfile;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import com.edws.gov.repo.DistrictRepository;
import com.edws.gov.repo.GnDivisionRepository;
import com.edws.gov.repo.ProvinceRepository;
import com.edws.gov.repo.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OfficialService {

    private static final Logger log = LoggerFactory.getLogger(OfficialService.class);

    private final UserRepository userRepository;
    private final GnDivisionRepository gnDivisionRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final AuthService authService;
    private final SessionContext session;

    public OfficialService(UserRepository userRepository,
                           GnDivisionRepository gnDivisionRepository,
                           ProvinceRepository provinceRepository,
                           DistrictRepository districtRepository,
                           PasswordEncoder passwordEncoder,
                           MongoTemplate mongoTemplate,
                           AuthService authService,
                           SessionContext session) {
        this.userRepository = userRepository;
        this.gnDivisionRepository = gnDivisionRepository;
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.authService = authService;
        this.session = session;
    }

    public PageResponseDTO<UserResponseDTO> findAll(String search, Role role, int page, int pageSize) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                pageSize <= 0 ? 10 : pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Query query = new Query();
        query.addCriteria(Criteria.where("role").ne(Role.USER));

        if (search != null && !search.isBlank()) {
            String quoted = Pattern.quote(search.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("email").regex(quoted, "i"),
                    Criteria.where("profile.fullName").regex(quoted, "i")));
        }
        if (role != null) {
            query.addCriteria(Criteria.where("role").is(role));
        }

        long total = mongoTemplate.count(query, User.class);
        List<User> officials = mongoTemplate.find(query.with(pageable), User.class);
        Page<User> result = new PageImpl<>(officials, pageable, total);

        return PageResponseDTO.of(result, User::toUserResponseDTO);
    }

    public UserResponseDTO findById(String userId) {
        return load(userId).toUserResponseDTO();
    }

    public UserResponseDTO create(OfficialRequestDTO request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (request.role() == Role.USER) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "Citizens are registered through the citizen endpoint");
        }

        String temporaryPassword = authService.generateTemporaryPassword();

        User official = new User();
        official.setEmail(email);
        official.setPassword(passwordEncoder.encode(temporaryPassword));
        official.setTempPassword(true);
        official.setHouseHolder(false);
        official.setRole(request.role());
        official.setStatus(UserStatus.ACTIVE);
        UserProfile profile = new UserProfile();
        profile.setFullName(request.fullName().trim());
        profile.setPhone(request.telephone());
        official.setProfile(profile);

        applyScope(official, request);

        User saved = userRepository.save(official);
        authService.sendOfficialInvitation(saved, temporaryPassword);

        log.info("Official {} created by {}", saved.getId(), session.getCurrentUserId());
        return saved.toUserResponseDTO();
    }

    public UserResponseDTO update(String userId, OfficialRequestDTO request) {
        User official = load(userId);
        String email = request.email().trim().toLowerCase();

        if (!email.equals(official.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        official.setEmail(email);
        official.setRole(request.role());

        UserProfile existing = official.getProfile() == null ? new UserProfile() : official.getProfile();
        existing.setFullName(request.fullName().trim());
        existing.setPhone(request.telephone());
        official.setProfile(existing);

        applyScope(official, request);

        return userRepository.save(official).toUserResponseDTO();
    }

    public void delete(String userId) {
        User official = load(userId);
        official.setStatus(UserStatus.INACTIVE);
        userRepository.save(official);

        log.info("Official {} deactivated by {}", userId, session.getCurrentUserId());
    }

    public UserResponseDTO assignTeam(String adminId, OfficialTeamRequestDTO request) {
        User admin = load(adminId);

        List<String> memberIds = new ArrayList<>();
        if (request.reviewerIds() != null) {
            memberIds.addAll(request.reviewerIds());
        }
        if (request.drawerIds() != null) {
            memberIds.addAll(request.drawerIds());
        }

        List<User> members = userRepository.findAllById(memberIds);
        members.forEach(member -> {
            AdminProfile profile = member.getAdminProfile() == null ? new AdminProfile() : member.getAdminProfile();
            profile.setReportsToAdminId(admin.getId());
            member.setAdminProfile(profile);
        });
        userRepository.saveAll(members);

        log.info("{} officials now report to admin {}", members.size(), adminId);
        return admin.toUserResponseDTO();
    }

    private void applyScope(User official, OfficialRequestDTO request) {
        AdminProfile profile = official.getAdminProfile() == null ? new AdminProfile() : official.getAdminProfile();
        profile.setAdministrativeScope(request.administrativeScope());
        profile.setDesignation(request.department());
        profile.setOfficeAddress(request.officeAddress());
        profile.setReportsToAdminId(request.reportsToAdminId());
        official.setAdminProfile(profile);

        if (request.provinceId() != null && !request.provinceId().isBlank()) {
            official.setProvince(provinceRepository.findById(request.provinceId()).orElse(null));
        }
        if (request.districtId() != null && !request.districtId().isBlank()) {
            official.setDistrict(districtRepository.findById(request.districtId()).orElse(null));
        }
        if (request.gnDivisionId() != null && !request.gnDivisionId().isBlank()) {
            official.setGnDivision(gnDivisionRepository.findById(request.gnDivisionId()).orElse(null));
        }
    }

    private User load(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Official not found: " + userId));
    }
}
