package com.edws.gov.entity;

import com.edws.gov.dto.auth.AuthenticatedUserResponse;
import com.edws.gov.dto.user.AddressDto;
import com.edws.gov.dto.user.UserResponseDTO;
import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Permission;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.*;

@Document(collection = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseDocument {
    private boolean isTempPassword = true;
    private boolean isHouseHolder = true;

    @JsonIgnore
    private String password;

    private String email;
    private Role role;
    private UserStatus status;
    private UserProfile profile;
    private AdminProfile adminProfile;

    private Set<Permission> permissions = new HashSet<>();

    @DocumentReference(lazy = true)
    private Province province;

    @DocumentReference(lazy = true)
    private District district;

    @DocumentReference(lazy = true)
    private GnDivision gnDivision;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint liveLocation;

    @DocumentReference(lazy = true)
    private List<Address> primaryAddress = new ArrayList<>();

    @DocumentReference(lazy = true)
    private List<Address> properties = new ArrayList<>();

    @DocumentReference(lazy = true)
    private List<User> familyMembers = new ArrayList<>();
    private List<FamilyMember> members = new ArrayList<>();

    @DocumentReference(lazy = true)
    private User registeredBy;


    public UserResponseDTO toUserResponseDTO() {
        AddressDto addressDto = null;
        if (this.getProfile() != null && this.getProfile().getAddress() != null) {
            Address address = this.getProfile().getAddress();

            addressDto = new AddressDto(
                    address.getHouseName(),
                    address.getHouseNo(),
                    address.getStreetAddress1(),
                    address.getStreetAddress2(),
                    address.getZipCode(),
                    address.getCity(),
                    address.getGnDivision(),
                    address.getLocation() != null ? address.getLocation().getY() : null,
                    address.getLocation() != null ? address.getLocation().getX() : null
            );
        }

        return new UserResponseDTO(
                this.getId(),
                this.getEmail(),
                this.getRole(),
                this.getStatus(),
                this.getProfile() != null ? this.getProfile().getFullName() : null,
                this.getProfile() != null ? this.getProfile().getNic() : null,
                this.getProfile() != null ? this.getProfile().getPhone() : null,
                addressDto,
                this.getGnDivision() != null ? this.getGnDivision().getId() : null,
                this.getGnDivision() != null ? this.getGnDivision().getName() : null,
                this.getRegisteredBy() != null ? this.getRegisteredBy().getId() : null,
                this.isHouseHolder,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }

    public AuthenticatedUserResponse toAuthenticatedResponse() {
        AdministrativeScope administrativeScope = null;
        if (this.getAdminProfile() != null) {
            administrativeScope = this.getAdminProfile().getAdministrativeScope();
        }

        AuthenticatedUserResponse authenticatedUserResponse = new AuthenticatedUserResponse(
                this.getId(),
                this.getEmail(),
                this.getProfile() != null ? this.getProfile().getFullName() : null,
                this.getRole(),
                this.getStatus(),
                administrativeScope,
                this.getPermissions() != null ? this.getPermissions() : Collections.emptySet(),
                this.getGnDivision() != null ? this.getGnDivision().getId() : null,
                this.isTempPassword(),
                this.isHouseHolder());

        System.out.println("SSSSSSSSS" + authenticatedUserResponse.id());

        return new AuthenticatedUserResponse(
                this.getId(),
                this.getEmail(),
                this.getProfile() != null ? this.getProfile().getFullName() : null,
                this.getRole(),
                this.getStatus(),
                administrativeScope,
                this.getPermissions() != null ? this.getPermissions() : Collections.emptySet(),
                this.getGnDivision() != null ? this.getGnDivision().getId() : null,
                this.isTempPassword(),
                this.isHouseHolder()
        );
    }



}
