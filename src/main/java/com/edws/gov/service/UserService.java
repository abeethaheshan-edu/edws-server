package com.edws.gov.service;

import com.edws.gov.common.PageResponse;
import com.edws.gov.dto.user.*;
import com.edws.gov.entity.*;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import com.edws.gov.repo.*;
import com.edws.gov.security.AccessControl;
import com.edws.gov.security.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.edws.gov.dto.PageResponseDTO;
import com.edws.gov.dto.user.CitizenMemberDTO;
import com.edws.gov.entity.FamilyMember;
import com.edws.gov.entity.UserProfile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final GnDivisionRepository gnDivisionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionContext session;
    private final AccessControl access;
    private final ProvinceRepository provinceRepo;
    private final DistrictRepository districtRepo;
    private final AddressRepository addressRepo;

    public UserService(UserRepository userRepository,
                       GnDivisionRepository gnDivisionRepository,
                       PasswordEncoder passwordEncoder,
                       SessionContext session,
                       AccessControl access,
                       ProvinceRepository provinceRepo,
                       DistrictRepository districtRepo,
                       AddressRepository addressRepo
                       ) {
        this.userRepository = userRepository;
        this.gnDivisionRepository = gnDivisionRepository;
        this.passwordEncoder = passwordEncoder;
        this.session = session;
        this.access = access;
        this.provinceRepo = provinceRepo;
        this.districtRepo = districtRepo;
        this.addressRepo = addressRepo;
    }

    @Transactional
    public UserResponseDTO createCitizen(CitizenRequestDTO request) {
        User officer = session.getCurrentUser();
        GnDivision division = resolveResidenceDivision(officer, request);

        String email = normalise(request.email());
        if (userRepository.existsByEmail(email)) throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        if (userRepository.existsByProfile_Nic(request.nic())) throw new ApiException(ErrorCode.NIC_ALREADY_EXISTS);

        District district = districtRepo.findById(division.getDistrictId())
                .orElseThrow(() -> new ApiException(ErrorCode.GN_DIVISION_NOT_FOUND,"GN division points at a missing district"));
        Province province = provinceRepo.findById(division.getProvinceId())
                .orElseThrow(() -> new ApiException(ErrorCode.GN_DIVISION_NOT_FOUND,"GN division points at a missing province"));

        String temporaryPassword = generateTemporaryPassword();

        User citizen = new User();
        citizen.setEmail(email);
        citizen.setPassword(passwordEncoder.encode(temporaryPassword));
        citizen.setTempPassword(true);
        citizen.setRole(Role.USER);
        citizen.setStatus(UserStatus.ACTIVE);
        citizen.setProfile(new UserProfile(request.nic(), request.fullName(), request.phone(),new Address(request.address())));
        citizen.setGnDivision(division);
        citizen.setDistrict(district);
        citizen.setProvince(province);
        citizen.setRegisteredBy(officer);
        citizen.setMembers(toMembers(request.members()));

        if (citizen.getProfile() != null) {
            citizen.getProfile().setSecondaryPhone(request.secondaryPhone());
        }

        User savedUser = userRepository.save(citizen);

        List<Address> properties  = saveOtherProperties(request.properties() , savedUser.getId());
        savedUser.setProperties(properties);
        User savedWithProperties = userRepository.save(savedUser);

        return savedWithProperties.toUserResponseDTO();
    }


    @Transactional
    public UserResponseDTO saveFamilyMembers(List<CitizenRequestDTO> familyMembers, String householderId) {
        if (familyMembers == null || familyMembers.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "At least one family member is required");
        }

        User officer = session.getCurrentUser();
        GnDivision division = resolveOfficerDivision(officer);
        User householder = loadCitizenInDivision(householderId, division);

        if (!householder.isHouseHolder()) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER,"User " + householderId + " is not a household head");
        }

        assertNewIdentifiers(familyMembers);
        District district = districtRepo.findById(division.getDistrictId())
                .orElseThrow(() -> new ApiException(ErrorCode.GN_DIVISION_NOT_FOUND,"GN division points at a missing district"));
        Province province = provinceRepo.findById(division.getProvinceId())
                .orElseThrow(() -> new ApiException(ErrorCode.GN_DIVISION_NOT_FOUND,"GN division points at a missing province"));

        Address householdAddress = householder.getProfile() == null ? null : householder.getProfile().getAddress();

        Map<User, String> credentials = new LinkedHashMap<>();
        List<User> members = new ArrayList<>(familyMembers.size());

        for (CitizenRequestDTO request : familyMembers) {
            if (request.gnDivisionId() != null && !division.getId().equals(request.gnDivisionId())) {
                throw new ApiException(ErrorCode.OUT_OF_ADMINISTRATIVE_SCOPE, "Family members must belong to the household's GN division");
            }

            String temporaryPassword = generateTemporaryPassword();

            User member = new User();
            member.setEmail(normalise(request.email()));
            member.setPassword(passwordEncoder.encode(temporaryPassword));
            member.setTempPassword(true);
            member.setHouseHolder(false);
            member.setRole(Role.USER);
            member.setStatus(UserStatus.ACTIVE);
            member.setProfile(new UserProfile(request.nic(), request.fullName(), request.phone(),new Address(request.address())));
            member.setGnDivision(division);
            member.setDistrict(district);
            member.setProvince(province);
            member.setRegisteredBy(officer);

            credentials.put(member, temporaryPassword);
            members.add(member);
        }

        List<User> savedMembers = userRepository.saveAll(members);
        List<User> household = householder.getFamilyMembers() == null ? new ArrayList<>() : new ArrayList<>(householder.getFamilyMembers());
        household.addAll(savedMembers);
        householder.setFamilyMembers(household);

        savedMembers.forEach(m -> m.setFamilyMembers(new ArrayList<>(List.of(householder))));

        List<User> toSave = new ArrayList<>(savedMembers);
        toSave.add(householder);
        List<User> users = userRepository.saveAll(toSave);
        Optional<User> houseHolderWithFamilyMembers = userRepository.findById(householderId);

        log.info("Officer {} added {} family members to householder {}",officer.getId(), savedMembers.size(), householder.getId());
        return  houseHolderWithFamilyMembers.get().toUserResponseDTO();
        // send email and phone
    }

    private void assertNewIdentifiers(List<CitizenRequestDTO> members) {
        Set<String> emails = new HashSet<>();
        Set<String> nics = new HashSet<>();

        for (CitizenRequestDTO req : members) {
            String email = normalise(req.email());

            if (!emails.add(email)) {
                throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS,
                        "Email " + req.email() + " appears more than once in this request");
            }
            if (!nics.add(req.nic())) {
                throw new ApiException(ErrorCode.NIC_ALREADY_EXISTS,
                        "NIC " + req.nic() + " appears more than once in this request");
            }
            if (userRepository.existsByEmail(email)) {
                throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS,
                        "An account already exists for " + req.email());
            }
            if (userRepository.existsByProfile_Nic(req.nic())) {
                throw new ApiException(ErrorCode.NIC_ALREADY_EXISTS,
                        "An account already exists for NIC " + req.nic());
            }
        }
    }

    private GnDivision resolveResidenceDivision(User officer, CitizenRequestDTO request) {
        GnDivision own = resolveOfficerDivision(officer);

        if (request.gnDivisionId() != null && !own.getId().equals(request.gnDivisionId())) {
            throw new ApiException(ErrorCode.OUT_OF_ADMINISTRATIVE_SCOPE,"You can only register citizens in your own GN division");
        }
        if (request.districtId() != null && !request.districtId().equals(own.getDistrictId())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER,"districtId does not match the GN division");
        }
        if (request.provinceId() != null && !request.provinceId().equals(own.getProvinceId())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER,
                    "provinceId does not match the GN division");
        }
        return own;
    }

    private List<Address> saveOtherProperties(List<AddressDto> requests, String householderId) {

        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> divisionIds = requests.stream()
                .map(AddressDto::gnDivision)
                .collect(Collectors.toSet());

        Map<String, GnDivision> divisions = gnDivisionRepository.findAllById(divisionIds)
                .stream()
                .collect(Collectors.toMap(GnDivision::getId, Function.identity()));

        List<Address> otherProperties = requests.stream()
        .map(req -> {
            GnDivision d = divisions.get(req.gnDivision());
            Address address = new Address();
            address.setHouseHolderId(householderId);
            address.setGnDivision(d.getId());
            address.setHouseName(req.houseName());
            address.setHouseNo(req.houseNo());
            address.setStreetAddress1(req.streetAddress1());
            address.setStreetAddress2(req.streetAddress2());
            address.setCity(req.city());
            address.setZipCode(req.zipCode());
            address.setPrimary(false);

            if (req.latitude() != null && req.longitude() != null) {
                address.setLocation(
                        new GeoJsonPoint(req.longitude(), req.latitude())
                );
            }

            return address;
        })
        .toList();

        return addressRepo.saveAll(otherProperties);
    }

    private List<User> loadFamilyMembers(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<String> distinct = ids.stream().distinct().toList();
        List<User> found = userRepository.findAllById(distinct);
        if (found.size() != distinct.size()) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "One or more family member ids do not exist");
        }
        return new ArrayList<>(found);
    }


    public PageResponseDTO<UserResponseDTO> findCitizens(String search, int page, int pageSize) {
        User officer = session.getCurrentUser();
        GnDivision division = resolveOfficerDivision(officer);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, pageSize),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> citizens = userRepository.findByGnDivisionAndRole(division, Role.USER, pageable);
        String term = search == null ? "" : search.trim().toLowerCase();

        List<UserResponseDTO> items = citizens.getContent().stream()
                .filter(citizen -> matchesSearch(citizen, term))
                .map(User::toUserResponseDTO)
                .toList();

        return new PageResponseDTO<>(items, page, pageSize, citizens.getTotalElements());
    }

    private boolean matchesSearch(User citizen, String term) {
        if (term.isEmpty()) {
            return true;
        }

        UserProfile profile = citizen.getProfile();
        String name = profile == null || profile.getFullName() == null ? "" : profile.getFullName().toLowerCase();
        String nic = profile == null || profile.getNic() == null ? "" : profile.getNic().toLowerCase();
        String email = citizen.getEmail() == null ? "" : citizen.getEmail().toLowerCase();

        return name.contains(term) || nic.contains(term) || email.contains(term);
    }

    private List<FamilyMember> toMembers(List<CitizenMemberDTO> members) {
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        return members.stream()
                .filter(member -> member.fullName() != null && !member.fullName().isBlank())
                .map(member -> {
                    FamilyMember entity = new FamilyMember();
                    entity.setName(member.fullName());
                    entity.setNic(member.nic());
                    entity.setPhone(member.phone());
                    entity.setEmail(member.email());
                    return entity;
                })
                .toList();
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private GnDivision resolveOfficerDivision(User officer) {
        GnDivision reference = officer.getGnDivision();
        if (reference == null || reference.getId() == null) {
            throw new ApiException(ErrorCode.GN_DIVISION_NOT_ASSIGNED);
        }
        return gnDivisionRepository.findById(reference.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.GN_DIVISION_NOT_FOUND));
    }


    private User loadCitizenInDivision(String citizenId, GnDivision division) {
        User citizen = userRepository.findByIdAndRole(citizenId, Role.USER)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        GnDivision citizenDivision = citizen.getGnDivision();
        if (citizenDivision == null || !division.getId().equals(citizenDivision.getId())) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        return citizen;
    }

    private String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
