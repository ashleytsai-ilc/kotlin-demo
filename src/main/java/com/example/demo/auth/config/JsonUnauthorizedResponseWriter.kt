package com.example.demo.auth.config;

import com.example.demo.common.ApiErrorCatalog;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.common.ApiErrorResponse;
import com.example.demo.common.CommonErrorDetail;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
public class JsonUnauthorizedResponseWriter {

    private final ObjectMapper objectMapper;

    public JsonUnauthorizedResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorCatalog.response(
                ApiErrorKey.UNAUTHORIZED,
                List.of(CommonErrorDetail.VALID_BEARER_TOKEN_REQUIRED.toDetail())
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
