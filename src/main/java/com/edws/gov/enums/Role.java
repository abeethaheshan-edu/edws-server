package com.edws.gov.enums;

import org.springframework.security.core.annotation.ExpressionTemplateValueProvider;

public enum Role implements ExpressionTemplateValueProvider {
    SUPER_ADMIN,
    ADMIN,
    PROVINCE_ADMIN,
    DISTRICT_ADMIN,
    DIVISION_ADMIN,
    GN_OFFICER,
    FIELD_OFFICER,
    USER;

    @Override
    public String getExpressionTemplateValue() {
        return "'" + name() + "'";
    }
}
