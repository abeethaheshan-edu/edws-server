package com.edws.gov.security;

public final class AuthHeaders {

    private AuthHeaders() {
    }

    public static final String ACCESS_TOKEN = "X-Access-Token";
    public static final String REFRESH_TOKEN = "X-Refresh-Token";
    public static final String TOKEN_TYPE = "X-Token-Type";
    public static final String ACCESS_TOKEN_EXPIRES_IN = "X-Access-Token-Expires-In";
    public static final String REFRESH_TOKEN_EXPIRES_IN = "X-Refresh-Token-Expires-In";

    public static final String BEARER = "Bearer";
}
