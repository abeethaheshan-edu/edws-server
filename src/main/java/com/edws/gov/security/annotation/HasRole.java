package com.edws.gov.security.annotation;

import com.edws.gov.enums.Role;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts a handler to the listed roles.
 *
 * <pre>
 * &#64;HasRole({Role.SUPER_ADMIN, Role.ADMIN})
 * </pre>
 *
 * <p>The values are real {@link Role} constants, so a typo or a renamed role is a
 * compile error rather than a runtime failure. {@link Role#getExpressionTemplateValue()}
 * supplies the quoted form the SpEL template needs.
 *
 * <p>Matching is done against the {@code ROLE_}-prefixed authority granted by
 * {@code JwtAuthenticationFilter}; {@code hasAnyRole} adds the prefix itself.
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole({value})")
public @interface HasRole {
    Role[] value();
}
