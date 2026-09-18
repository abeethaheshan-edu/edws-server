package com.edws.gov.exception;

import java.util.UUID;


public final class TraceId {

    private TraceId() {
    }

    public static String next() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
