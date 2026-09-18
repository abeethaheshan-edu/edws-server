package com.edws.gov.dto;

import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Permission;

import java.util.Set;

public class AdminProfileDto {
    private String employeeId;
    private String departmentId;
    private String designation;
    private AdministrativeScope administrativeScope;
    private Set<Permission> permissions;
}
