package com.edws.gov.security;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;


public final class SecurityRoutes {

    private SecurityRoutes() {
    }

    public static final class Auth {
        private Auth() {
        }

        public static final String BASE = "/auth";

        public static final String LOGIN = "/login";
        public static final String REFRESH = "/refresh";
        public static final String ME = "/me";
        public static final String ROLES_ME = "/roles/me";
        public static final String ROLES = "/roles";
        public static final String FORGOT_PASSWORD = "/password/forgot";
        public static final String RESET_PASSWORD = "/password/reset/{token}";
        public static final String UPDATE_PASSWORD = "/password/update";

        public static final String ABS_LOGIN = BASE + LOGIN;
        public static final String ABS_REFRESH = BASE + REFRESH;
        public static final String ABS_ME = BASE + ME;
        public static final String ABS_ROLES_ME = BASE + ROLES_ME;
        public static final String ABS_ROLES = BASE + ROLES;
        public static final String ABS_FORGOT_PASSWORD = BASE + FORGOT_PASSWORD;
        public static final String ABS_RESET_PASSWORD = BASE + "/password/reset/**";
        public static final String ABS_UPDATE_PASSWORD = BASE + UPDATE_PASSWORD;
    }

    public static final class Users {
        private Users() {
        }

        public static final String BASE = "/users";

        public static final String ME = "/me";
        public static final String CITIZENS = "/citizens";
        public static final String ADDRESS = "/citizens/{householderId}/family-members";
        public static final String CITIZEN_BY_ID = "/citizens/{citizenId}";
        public static final String CITIZENS_BY_GN_DIVISION = "/citizens/gn-division/{gnDivisionId}";

        public static final String OFFICIALS = "/officials";
        public static final String OFFICIAL_BY_ID = "/officials/{userId}";
        public static final String OFFICIAL_TEAM = "/officials/{userId}/team";

        public static final String ABS_ALL = BASE + "/**";
    }

    public static final class Alerts {
        private Alerts() {
        }

        public static final String BASE = "/alerts";

        public static final String ALL = "/all";
        public static final String CREATE = "";
        public static final String BY_ID = "/{alertId}";
        public static final String DETAILS = "/{alertId}/details";
        public static final String STATUS = "/{alertId}/status";

        public static final String ABS_ALL = BASE + "/**";
    }

    public static final class Infra {
        private Infra() {
        }

        public static final String ERROR = "/error";
        public static final String HEALTH = "/actuator/health";
        public static final String HEALTH_PROBES = "/actuator/health/**";
        public static final String INFO = "/actuator/info";
        public static final String ACTUATOR_ALL = "/actuator/**";
    }


    public static final String[] PUBLIC_ROUTES = {
            Auth.ABS_LOGIN,
            Auth.ABS_REFRESH,
            Auth.ABS_FORGOT_PASSWORD,
            Auth.ABS_RESET_PASSWORD,
            Infra.ERROR,
            Infra.HEALTH,
            Infra.HEALTH_PROBES,
            Infra.INFO
    };

    public static final String[] PROTECTED_ROUTES = {
            Auth.ABS_ME,
            Auth.ABS_ROLES_ME,
            Auth.ABS_ROLES,
            Auth.ABS_UPDATE_PASSWORD,
            Users.ABS_ALL,
            Alerts.ABS_ALL,
            Infra.ACTUATOR_ALL
    };


    private static final List<PathPattern> PUBLIC_PATTERNS =
            Arrays.stream(PUBLIC_ROUTES)
                    .map(PathPatternParser.defaultInstance::parse)
                    .toList();


    public static boolean isPublic(String path) {
        PathContainer container = PathContainer.parsePath(path);
        return PUBLIC_PATTERNS.stream().anyMatch(pattern -> pattern.matches(container));
    }


    public static void validate() {
        for (String route : PROTECTED_ROUTES) {
            for (String publicRoute : PUBLIC_ROUTES) {
                if (route.equals(publicRoute)) {
                    throw new IllegalStateException(
                            "SecurityRoutes: '" + route + "' is declared both PUBLIC and PROTECTED. "
                                    + "PUBLIC is matched first, so the route would be open. "
                                    + "Remove it from one of the two lists.");
                }
            }

            if (isLiteral(route) && isPublic(route)) {
                throw new IllegalStateException(
                        "SecurityRoutes: protected route '" + route + "' is already matched by a "
                                + "PUBLIC pattern, so it would be reachable without a token. "
                                + "Narrow the public pattern.");
            }
        }

        assertNoDuplicates(PUBLIC_ROUTES, "PUBLIC_ROUTES");
        assertNoDuplicates(PROTECTED_ROUTES, "PROTECTED_ROUTES");
    }

    private static boolean isLiteral(String route) {
        return route.indexOf('*') < 0 && route.indexOf('{') < 0;
    }

    private static void assertNoDuplicates(String[] routes, String listName) {
        for (int i = 0; i < routes.length; i++) {
            for (int j = i + 1; j < routes.length; j++) {
                if (routes[i].equals(routes[j])) {
                    throw new IllegalStateException(
                            "SecurityRoutes: '" + routes[i] + "' appears twice in " + listName + ".");
                }
            }
        }
    }
}
