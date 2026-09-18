package com.edws.gov.enums;

import org.springframework.security.core.annotation.ExpressionTemplateValueProvider;

public enum Permission implements ExpressionTemplateValueProvider {
    CREATE_DISASTER_DOCUMENT,
    REVIEW_DISASTER_DOCUMENT,
    APPROVE_DISASTER_DOCUMENT,
    MANAGE_USERS,
    MANAGE_SYSTEM,
    VIEW_REPORTS;

    /**
     * Quoted SpEL literal used by {@code @PreAuthorize("hasAnyAuthority({value})")} on
     * {@link com.edws.gov.security.annotation.HasPermission}. See
     * {@link Role#getExpressionTemplateValue()} for why the quotes are required.
     */
    @Override
    public String getExpressionTemplateValue() {
        return "'" + name() + "'";
    }
}
