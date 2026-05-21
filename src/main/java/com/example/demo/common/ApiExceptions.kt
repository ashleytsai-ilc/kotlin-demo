package com.example.demo.common;

import org.springframework.http.HttpStatus;

import java.util.List;

public final class ApiExceptions {

    private ApiExceptions() {
    }

    public static ApiException conflict(ApiErrorKey key, ErrorField field) {
        return ApiErrorCatalog.exception(
                HttpStatus.CONFLICT,
                key,
                List.of(new ErrorDetail(field, ApiErrorCatalog.message(key)))
        );
    }

    public static ApiException invalidCredentials() {
        return unauthorizedWithDetail(ApiErrorKey.INVALID_CREDENTIALS, ErrorField.CREDENTIALS);
    }

    public static ApiException invalidRefreshToken() {
        return unauthorizedWithDetail(ApiErrorKey.INVALID_REFRESH_TOKEN, ErrorField.REFRESH_TOKEN);
    }

    public static ApiException unauthorized() {
        return unauthorizedWithDetail(ApiErrorKey.UNAUTHORIZED, ErrorField.AUTHORIZATION);
    }

    private static ApiException unauthorizedWithDetail(ApiErrorKey key, ErrorField field) {
        return ApiErrorCatalog.exception(
                HttpStatus.UNAUTHORIZED,
                key,
                List.of(new ErrorDetail(field, ApiErrorCatalog.message(key)))
        );
    }
}
