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

    /**
     * Value spliced into security meta-annotation templates such as
     * {@code @PreAuthorize("hasAnyRole({value})")} on {@link
     * com.edws.gov.security.annotation.HasRole}.
     *
     * <p>Spring Security joins array attributes with commas and inserts the result into
     * the expression as plain text, so every element must already be a quoted SpEL string
     * literal. {@code @HasRole({Role.SUPER_ADMIN, Role.ADMIN})} therefore produces
     * {@code hasAnyRole('SUPER_ADMIN','ADMIN')}. Without the quotes SpEL reads the names
     * as properties and evaluation fails at request time.
     */
    @Override
    public String getExpressionTemplateValue() {
        return "'" + name() + "'";
    }
}
