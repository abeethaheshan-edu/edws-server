package com.edws.gov.security.annotation;

import com.edws.gov.enums.Permission;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts a handler to holders of at least one of the listed permissions.
 *
 * <pre>
 * &#64;HasPermission({Permission.MANAGE_USERS})
 * </pre>
 *
 * <p>Permissions are granted as bare authorities by {@code JwtAuthenticationFilter},
 * so this uses {@code hasAnyAuthority} rather than {@code hasAnyRole}.
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority({value})")
public @interface HasPermission {
    Permission[] value();
}
