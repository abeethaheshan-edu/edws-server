package com.edws.gov.security;

import com.edws.gov.entity.User;
import com.edws.gov.enums.AdministrativeScope;
import com.edws.gov.enums.Permission;
import com.edws.gov.enums.Role;
import org.springframework.stereotype.Component;


@Component("access")
public class AccessControl {

    private final SessionContext session;

    public AccessControl(SessionContext session) {
        this.session = session;
    }


    public boolean isGnOfficer() {
        return session.getCurrentUserOrEmpty()
                .map(user -> user.getRole() == Role.GN_OFFICER
                        && user.getAdminProfile() != null
                        && user.getAdminProfile().getAdministrativeScope() == AdministrativeScope.GN_DIVISION
                        && user.getGnDivision() != null)
                .orElse(false);
    }


    public boolean isAdmin() {
        return hasAnyRole(Role.SUPER_ADMIN, Role.ADMIN);
    }

    public boolean hasAnyRole(Role... roles) {
        return session.getCurrentUserOrEmpty()
                .map(User::getRole)
                .map(current -> {
                    for (Role role : roles) {
                        if (role == current) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    public boolean hasPermission(Permission permission) {
        return session.getCurrentUserOrEmpty()
                .map(user -> user.getPermissions() != null && user.getPermissions().contains(permission))
                .orElse(false);
    }


    public boolean canManageUsers() {
        return isGnOfficer() || isAdmin() || hasPermission(Permission.MANAGE_USERS);
    }

    public boolean canViewAnyGnDivision() {
        return hasAnyRole(Role.SUPER_ADMIN, Role.ADMIN, Role.PROVINCE_ADMIN, Role.DISTRICT_ADMIN);
    }
}
