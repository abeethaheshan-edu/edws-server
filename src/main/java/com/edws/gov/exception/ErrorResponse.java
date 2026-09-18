package com.edws.gov.exception;

import java.time.Instant;
import java.util.List;


public record ErrorResponse(
        ErrorType errorType,
        String message,
        String code,
        String path,
        String traceId,
        Instant timestamp,
        List<FieldError> fieldErrors
) {

    public static ErrorResponse of(ErrorCode errorCode, String message, String path, String traceId) {
        return new ErrorResponse(errorCode.getType(), message, errorCode.getCode(),
                path, traceId, Instant.now(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path,
                                   String traceId, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.getType(), message, errorCode.getCode(),
                path, traceId, Instant.now(), fieldErrors);
    }
}
