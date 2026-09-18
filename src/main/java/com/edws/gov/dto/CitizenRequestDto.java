package com.edws.gov.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CitizenRequestDto {
    private String email;
    private String password;
    //private UserProfileDto profile;
    private AdminProfileDto adminProfile;
    private String provinceId;
    private String districtId;
    private String gnDivisionId;
}
