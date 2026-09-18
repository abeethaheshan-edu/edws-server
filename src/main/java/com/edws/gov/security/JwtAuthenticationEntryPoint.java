package com.edws.gov.security;

import com.edws.gov.exception.ErrorCode;
import com.edws.gov.exception.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object recorded = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);

        ErrorCode errorCode = (recorded instanceof ErrorCode code)
                ? code
                : ErrorCode.UNAUTHENTICATED;

        ErrorResponseWriter.write(request, response, errorCode, errorCode.getDefaultMessage());
    }
}
