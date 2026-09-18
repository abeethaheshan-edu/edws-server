package com.edws.gov.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


public final class ErrorResponseWriter {

    private ErrorResponseWriter() {
    }

    public static void write(HttpServletRequest request,
                             HttpServletResponse response,
                             ErrorCode errorCode,
                             String message) throws IOException {

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String json = "{"
                + "\"errorType\":\"" + errorCode.getType().name() + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"code\":\"" + errorCode.getCode() + "\","
                + "\"path\":\"" + escape(request.getRequestURI()) + "\","
                + "\"traceId\":\"" + TraceId.next() + "\","
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"fieldErrors\":[]"
                + "}";

        response.getWriter().write(json);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
