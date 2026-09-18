package com.edws.gov.service;

import com.edws.gov.dto.user.AddressDto;
import com.edws.gov.dto.user.CitizenRequestDTO;
import com.edws.gov.dto.user.UserResponseDTO;
import com.edws.gov.entity.Address;
import com.edws.gov.entity.AdminProfile;
import com.edws.gov.entity.District;
import com.edws.gov.entity.GnDivision;
import com.edws.gov.entity.Province;
import com.edws.gov.entity.User;
import com.edws.gov.entity.UserProfile;
import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import com.edws.gov.repo.AddressRepository;
import com.edws.gov.repo.DistrictRepository;
import com.edws.gov.repo.GnDivisionRepository;
import com.edws.gov.repo.ProvinceRepository;
import com.edws.gov.repo.UserRepository;
import com.edws.gov.security.AccessControl;
import com.edws.gov.security.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private static final String PROVINCE_ID = "province-1";
    private static final String DISTRICT_ID = "district-1";
    private static final String DIVISION_ID = "division-1";
    private static final String OTHER_DIVISION_ID = "division-2";
    private static final String OFFICER_ID = "officer-1";
    private static final String HEAD_ID = "citizen-1";
    private static final String HASHED = "$2a$12$abcdefghijklmnopqrstuv";

    private static final String HEAD_EMAIL_RAW = "  Nimal@Example.LK  ";
    private static final String HEAD_EMAIL = "nimal@example.lk";
    private static final String HEAD_NIC = "199512345678";
    private static final String SPOUSE_EMAIL = "kamala@example.lk";
    private static final String SPOUSE_NIC = "198812345678";
    private static final String CHILD_EMAIL = "sunil@example.lk";
    private static final String CHILD_NIC = "200512345678";

    @Mock private UserRepository userRepository;
    @Mock private GnDivisionRepository gnDivisionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SessionContext session;
    @Mock private AccessControl access;
    @Mock private ProvinceRepository provinceRepo;
    @Mock private DistrictRepository districtRepo;
    @Mock private AddressRepository addressRepo;

    private UserService userService;

    private Province province;
    private District district;
    private GnDivision division;
    private User officer;
    private User householder;

    private String householderId;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, gnDivisionRepository, passwordEncoder,
                session, access, provinceRepo, districtRepo, addressRepo);

        province = new Province();
        province.setId(PROVINCE_ID);
        province.setName("Western");

        district = new District();
        district.setId(DISTRICT_ID);
        district.setName("Kalutara");
        district.setProvinceId(PROVINCE_ID);

        division = new GnDivision();
        division.setId(DIVISION_ID);
        division.setCode("WP-KL-001");
        division.setName("Panadura North");
        division.setDistrictId(DISTRICT_ID);
        division.setProvinceId(PROVINCE_ID);

        officer = new User();
        officer.setId(OFFICER_ID);
        officer.setEmail("officer@gov.lk");
        officer.setRole(Role.GN_OFFICER);
        officer.setStatus(UserStatus.ACTIVE);
        officer.setProfile(new UserProfile("199012345678", "K. Perera", "0771111111", null));
        officer.setAdminProfile(AdminProfile.builder()
                .employeeId("EMP-001")
                .designation("Grama Niladhari")
                .administrativeScope(AdministrativeScope.GN_DIVISION)
                .build());
        officer.setGnDivision(division);
        officer.setDistrict(district);
        officer.setProvince(province);

        when(session.getCurrentUser()).thenReturn(officer);
        when(gnDivisionRepository.findById(DIVISION_ID)).thenReturn(Optional.of(division));
        when(gnDivisionRepository.findById(OTHER_DIVISION_ID)).thenReturn(Optional.empty());
        when(districtRepo.findById(DISTRICT_ID)).thenReturn(Optional.of(district));
        when(provinceRepo.findById(PROVINCE_ID)).thenReturn(Optional.of(province));
        when(gnDivisionRepository.findAllById(any())).thenReturn(List.of(division));
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByProfile_Nic(anyString())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(HEAD_ID);
            }
            return saved;
        });

        when(userRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<User> saved = invocation.getArgument(0);
            int index = 0;
            for (User user : saved) {
                if (user.getId() == null) {
                    user.setId("member-" + (++index));
                }
            }
            return saved;
        });

        when(addressRepo.saveAll(anyList())).thenAnswer(invocation -> {
            List<Address> saved = invocation.getArgument(0);
            int index = 0;
            for (Address address : saved) {
                if (address.getId() == null) {
                    address.setId("address-" + (++index));
                }
            }
            return saved;
        });
    }

    @Test
    @Order(1)
    @DisplayName("createCitizen registers a household head, hashes the password and stores other properties")
    void createCitizen() {
        CitizenRequestDTO request = new CitizenRequestDTO(
                HEAD_EMAIL_RAW,
                "Nimal Silva",
                HEAD_NIC,
                "0771234567",
                homeAddress(),
                DIVISION_ID,
                DISTRICT_ID,
                PROVINCE_ID,
                List.of(otherProperty()),
                List.of());

        UserResponseDTO created = userService.createCitizen(request);

        assertNotNull(created.id());
        assertEquals(HEAD_EMAIL, created.email());
        assertEquals(Role.USER, created.role());
        assertEquals(UserStatus.ACTIVE, created.status());
        assertEquals(Boolean.TRUE, created.isHouseHolder());
        assertEquals(DIVISION_ID, created.gnDivisionId());
        assertEquals("Panadura North", created.gnDivisionName());
        assertEquals(OFFICER_ID, created.registeredById());
        assertEquals("Nimal Silva", created.fullName());
        assertEquals(HEAD_NIC, created.nic());
        assertNotNull(created.address());
        assertEquals("Panadura", created.address().city());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        User persisted = userCaptor.getAllValues().get(0);
        assertEquals(HEAD_EMAIL, persisted.getEmail());
        assertEquals(HASHED, persisted.getPassword());
        assertTrue(persisted.isTempPassword());
        assertTrue(persisted.isHouseHolder());
        assertEquals(Role.USER, persisted.getRole());
        assertEquals(DIVISION_ID, persisted.getGnDivision().getId());
        assertEquals(DISTRICT_ID, persisted.getDistrict().getId());
        assertEquals(PROVINCE_ID, persisted.getProvince().getId());
        assertEquals(OFFICER_ID, persisted.getRegisteredBy().getId());

        ArgumentCaptor<List<Address>> addressCaptor = ArgumentCaptor.captor();
        verify(addressRepo).saveAll(addressCaptor.capture());

        List<Address> savedAddresses = addressCaptor.getValue();
        assertEquals(1, savedAddresses.size());

        Address property = savedAddresses.get(0);
        assertEquals(created.id(), property.getHouseHolderId());
        assertEquals(DIVISION_ID, property.getGnDivision());
        assertEquals("Moratuwa", property.getCity());
        assertFalse(property.isPrimary());
        assertNotNull(property.getLocation());
        assertEquals(79.8816, property.getLocation().getX());
        assertEquals(6.7730, property.getLocation().getY());

        User persistedWithProperties = userCaptor.getAllValues().get(1);
        assertEquals(1, persistedWithProperties.getProperties().size());

        householderId = created.id();
        householder = persistedWithProperties;
    }

    @Test
    @Order(2)
    @DisplayName("saveFamilyMembers creates members as users and links them to the householder both ways")
    void saveFamilyMembers() {
        assertNotNull(householderId, "createCitizen must run before saveFamilyMembers");
        givenExistingHouseholder();

        List<CitizenRequestDTO> members = List.of(
                familyMember(SPOUSE_EMAIL, "Kamala Silva", SPOUSE_NIC, "0772222222"),
                familyMember(CHILD_EMAIL, "Sunil Silva", CHILD_NIC, "0773333333"));

        UserResponseDTO response = userService.saveFamilyMembers(members, householderId);

        assertEquals(householderId, response.id());
        assertEquals(Boolean.TRUE, response.isHouseHolder());

        ArgumentCaptor<List<User>> saveAllCaptor = ArgumentCaptor.captor();
        verify(userRepository, times(2)).saveAll(saveAllCaptor.capture());

        List<User> createdMembers = saveAllCaptor.getAllValues().get(0);
        assertEquals(2, createdMembers.size());

        User spouse = createdMembers.get(0);
        assertEquals(SPOUSE_EMAIL, spouse.getEmail());
        assertEquals(SPOUSE_NIC, spouse.getProfile().getNic());
        assertEquals(HASHED, spouse.getPassword());
        assertEquals(Role.USER, spouse.getRole());
        assertEquals(UserStatus.ACTIVE, spouse.getStatus());
        assertTrue(spouse.isTempPassword());
        assertFalse(spouse.isHouseHolder());
        assertEquals(DIVISION_ID, spouse.getGnDivision().getId());
        assertEquals(DISTRICT_ID, spouse.getDistrict().getId());
        assertEquals(PROVINCE_ID, spouse.getProvince().getId());
        assertEquals(OFFICER_ID, spouse.getRegisteredBy().getId());
        assertNotNull(spouse.getId());

        User child = createdMembers.get(1);
        assertEquals(CHILD_EMAIL, child.getEmail());
        assertFalse(child.isHouseHolder());
        assertNotNull(child.getId());

        List<User> linked = saveAllCaptor.getAllValues().get(1);
        assertEquals(3, linked.size());

        User linkedHouseholder = linked.get(2);
        assertEquals(householderId, linkedHouseholder.getId());
        assertEquals(2, linkedHouseholder.getFamilyMembers().size());
        assertEquals(List.of(spouse.getId(), child.getId()),
                linkedHouseholder.getFamilyMembers().stream().map(User::getId).toList());

        assertEquals(List.of(householderId),
                linked.get(0).getFamilyMembers().stream().map(User::getId).toList());
        assertEquals(List.of(householderId),
                linked.get(1).getFamilyMembers().stream().map(User::getId).toList());
    }

    @Test
    @Order(3)
    @DisplayName("saveFamilyMembers rejects an empty member list")
    void rejectsEmptyMemberList() {
        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.saveFamilyMembers(List.of(), HEAD_ID));

        assertEquals(ErrorCode.VALIDATION_FAILED, thrown.getErrorCode());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @Order(4)
    @DisplayName("saveFamilyMembers rejects a payload repeating a NIC and writes nothing")
    void rejectsDuplicateNicWithinRequest() {
        givenExistingHouseholder();

        List<CitizenRequestDTO> members = List.of(
                familyMember("first@example.lk", "First Member", SPOUSE_NIC, "0774444444"),
                familyMember("second@example.lk", "Second Member", SPOUSE_NIC, "0775555555"));

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.saveFamilyMembers(members, HEAD_ID));

        assertEquals(ErrorCode.NIC_ALREADY_EXISTS, thrown.getErrorCode());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @Order(5)
    @DisplayName("saveFamilyMembers rejects an email that is already registered")
    void rejectsExistingEmail() {
        givenExistingHouseholder();
        when(userRepository.existsByEmail(SPOUSE_EMAIL)).thenReturn(true);

        List<CitizenRequestDTO> members =
                List.of(familyMember(SPOUSE_EMAIL, "Kamala Silva", SPOUSE_NIC, "0772222222"));

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.saveFamilyMembers(members, HEAD_ID));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, thrown.getErrorCode());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @Order(6)
    @DisplayName("saveFamilyMembers refuses a citizen who is not a household head")
    void rejectsNonHouseholder() {
        User member = new User();
        member.setId("member-9");
        member.setRole(Role.USER);
        member.setStatus(UserStatus.ACTIVE);
        member.setHouseHolder(false);
        member.setGnDivision(division);
        when(userRepository.findByIdAndRole("member-9", Role.USER)).thenReturn(Optional.of(member));

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.saveFamilyMembers(
                        List.of(familyMember("nephew@example.lk", "Nephew", "196612345678", "0777777777")),
                        "member-9"));

        assertEquals(ErrorCode.INVALID_PARAMETER, thrown.getErrorCode());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @Order(7)
    @DisplayName("saveFamilyMembers refuses an unknown householder id")
    void rejectsUnknownHouseholder() {
        when(userRepository.findByIdAndRole("missing", Role.USER)).thenReturn(Optional.empty());

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.saveFamilyMembers(
                        List.of(familyMember("ghost@example.lk", "Ghost", "195512345678", "0778888888")),
                        "missing"));

        assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
    }

    @Test
    @Order(8)
    @DisplayName("saveFamilyMembers refuses a member outside the household GN division")
    void rejectsMemberInForeignDivision() {
        givenExistingHouseholder();

        CitizenRequestDTO foreign = new CitizenRequestDTO(
                "foreign@example.lk", "Foreign Member", "194412345678", "0779999999",
                homeAddress(), OTHER_DIVISION_ID, DISTRICT_ID, PROVINCE_ID,
                List.of(), List.of());

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.saveFamilyMembers(List.of(foreign), HEAD_ID));

        assertEquals(ErrorCode.OUT_OF_ADMINISTRATIVE_SCOPE, thrown.getErrorCode());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    @Order(9)
    @DisplayName("createCitizen refuses a GN division outside the officer's own")
    void rejectsForeignGnDivision() {
        CitizenRequestDTO request = new CitizenRequestDTO(
                "outsider@example.lk", "Outsider Perera", "194412345678", "0779999999",
                homeAddress(), OTHER_DIVISION_ID, DISTRICT_ID, PROVINCE_ID,
                List.of(), List.of());

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.createCitizen(request));

        assertEquals(ErrorCode.OUT_OF_ADMINISTRATIVE_SCOPE, thrown.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @Order(10)
    @DisplayName("createCitizen refuses a NIC that is already registered")
    void rejectsExistingNic() {
        when(userRepository.existsByProfile_Nic(HEAD_NIC)).thenReturn(true);

        CitizenRequestDTO request = new CitizenRequestDTO(
                "another@example.lk", "Another Person", HEAD_NIC, "0770000000",
                homeAddress(), DIVISION_ID, DISTRICT_ID, PROVINCE_ID,
                List.of(), List.of());

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.createCitizen(request));

        assertEquals(ErrorCode.NIC_ALREADY_EXISTS, thrown.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @Order(11)
    @DisplayName("createCitizen stores no address documents when no properties are supplied")
    void createCitizenWithoutProperties() {
        CitizenRequestDTO request = new CitizenRequestDTO(
                "solo@example.lk", "Solo Fernando", "199712345678", "0771212121",
                homeAddress(), DIVISION_ID, DISTRICT_ID, PROVINCE_ID,
                List.of(), List.of());

        UserResponseDTO created = userService.createCitizen(request);

        assertNotNull(created.id());
        verify(addressRepo, never()).saveAll(anyList());
    }

    @Test
    @Order(12)
    @DisplayName("createCitizen fails when the officer has no GN division assigned")
    void rejectsOfficerWithoutDivision() {
        officer.setGnDivision(null);

        CitizenRequestDTO request = new CitizenRequestDTO(
                "nodiv@example.lk", "No Division", "199812345678", "0771313131",
                homeAddress(), null, null, null,
                List.of(), List.of());

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.createCitizen(request));

        assertEquals(ErrorCode.GN_DIVISION_NOT_ASSIGNED, thrown.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @Order(13)
    @DisplayName("createCitizen fails when the GN division points at a missing district")
    void rejectsMissingDistrict() {
        when(districtRepo.findById(DISTRICT_ID)).thenReturn(Optional.empty());

        CitizenRequestDTO request = new CitizenRequestDTO(
                "orphan@example.lk", "Orphan Record", "199612345678", "0771414141",
                homeAddress(), DIVISION_ID, DISTRICT_ID, PROVINCE_ID,
                List.of(), List.of());

        ApiException thrown = assertThrows(ApiException.class,
                () -> userService.createCitizen(request));

        assertEquals(ErrorCode.GN_DIVISION_NOT_FOUND, thrown.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    private void givenExistingHouseholder() {
        User head = new User();
        head.setId(HEAD_ID);
        head.setEmail(HEAD_EMAIL);
        head.setRole(Role.USER);
        head.setStatus(UserStatus.ACTIVE);
        head.setHouseHolder(true);
        head.setProfile(new UserProfile(HEAD_NIC, "Nimal Silva", "0771234567", null));
        head.setGnDivision(division);
        head.setDistrict(district);
        head.setProvince(province);
        head.setFamilyMembers(new ArrayList<>());

        when(userRepository.findByIdAndRole(HEAD_ID, Role.USER)).thenReturn(Optional.of(head));
        when(userRepository.findById(HEAD_ID)).thenReturn(Optional.of(head));
        when(userRepository.existsByProfile_Nic(SPOUSE_NIC)).thenReturn(false);
    }

    private AddressDto homeAddress() {
        return new AddressDto("Silva House", "45/2", "Temple Road", null,
                "12500", "Panadura", DIVISION_ID, 6.7133, 79.9026);
    }

    private AddressDto otherProperty() {
        return new AddressDto("Paddy Land", "12", "Lake Road", null,
                "10400", "Moratuwa", DIVISION_ID, 6.7730, 79.8816);
    }

    private CitizenRequestDTO familyMember(String email, String fullName, String nic, String phone) {
        return new CitizenRequestDTO(email, fullName, nic, phone, homeAddress(),
                DIVISION_ID, DISTRICT_ID, PROVINCE_ID, List.of(), List.of());
    }
}