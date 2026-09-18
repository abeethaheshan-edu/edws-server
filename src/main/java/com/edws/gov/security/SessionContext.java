package com.edws.gov.security;

import com.edws.gov.entity.GnDivision;
import com.edws.gov.entity.User;
import com.edws.gov.exception.ApiException;
import com.edws.gov.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class SessionContext {

    public User getCurrentUser() {
        return getCurrentUserOrEmpty()
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED));
    }

    public Optional<User> getCurrentUserOrEmpty() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isAuthenticated() {
        return getCurrentUserOrEmpty().isPresent();
    }

    public GnDivision getCurrentGnDivision() {
        GnDivision division = getCurrentUser().getGnDivision();
        if (division == null) {
            throw new ApiException(ErrorCode.GN_DIVISION_NOT_ASSIGNED);
        }
        return division;
    }

    public String getCurrentGnDivisionId() {
        return getCurrentGnDivision().getId();
    }
}
