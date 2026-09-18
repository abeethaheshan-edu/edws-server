package com.edws.gov.exception;


public record FieldError(String field, Object rejectedValue, String message) {
}
