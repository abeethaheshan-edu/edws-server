package com.edws.gov.entity;

import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Permission;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfile {

    private String employeeId;

    @DocumentReference
    private Department department;

    private String designation;
    private AdministrativeScope administrativeScope;
    private String officeAddress;
    private String reportsToAdminId;

}
