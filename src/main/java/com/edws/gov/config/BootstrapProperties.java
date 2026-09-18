package com.edws.gov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edws.bootstrap")
public record BootstrapProperties(
        boolean enabled,
        String superAdminEmail,
        String superAdminPassword,
        String superAdminName
) {

    public BootstrapProperties {
        superAdminEmail = isBlank(superAdminEmail) ? "superadmin@dmc.gov.lk" : superAdminEmail.trim().toLowerCase();
        superAdminName = isBlank(superAdminName) ? "System Super Admin" : superAdminName;
        superAdminPassword = isBlank(superAdminPassword) ? "" : superAdminPassword;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
