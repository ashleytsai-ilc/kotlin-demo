package com.example.demo.common;

import org.springframework.http.HttpStatus;

import java.util.Map;

import static java.util.Map.entry;

public final class ApiErrorCatalog {

    private static final Map<ApiErrorKey, ApiErrorDefinition> DEFINITIONS = Map.ofEntries(
            entry(ApiErrorKey.REGISTRATION_VALIDATION,
                    new ApiErrorDefinition("VALIDATION_ERROR", "Registration request is invalid.")),
            entry(ApiErrorKey.USER_PROFILE_VALIDATION,
                    new ApiErrorDefinition("VALIDATION_ERROR", "Profile update request is invalid.")),
            entry(ApiErrorKey.INVALID_JSON,
                    new ApiErrorDefinition("VALIDATION_ERROR", "Request body is invalid.")),
            entry(ApiErrorKey.USERNAME_ALREADY_EXISTS,
                    new ApiErrorDefinition("USERNAME_ALREADY_EXISTS", "Username already exists.")),
            entry(ApiErrorKey.NICKNAME_ALREADY_EXISTS,
                    new ApiErrorDefinition("NICKNAME_ALREADY_EXISTS", "Nickname already exists.")),
            entry(ApiErrorKey.REGISTRATION_CONFLICT,
                    new ApiErrorDefinition("REGISTRATION_CONFLICT", "User registration conflicts with existing data.")),
            entry(ApiErrorKey.USER_PROFILE_CONFLICT,
                    new ApiErrorDefinition("PROFILE_UPDATE_CONFLICT", "User profile update conflicts with existing data.")),
            entry(ApiErrorKey.INVALID_CREDENTIALS,
                    new ApiErrorDefinition("INVALID_CREDENTIALS", "Credential is invalid.")),
            entry(ApiErrorKey.INVALID_REFRESH_TOKEN,
                    new ApiErrorDefinition("INVALID_REFRESH_TOKEN", "Refresh token is invalid.")),
            entry(ApiErrorKey.UNAUTHORIZED,
                    new ApiErrorDefinition("UNAUTHORIZED", "Authentication is required."))
    );

    private ApiErrorCatalog() {
    }

    public static ApiException exception(HttpStatus status, ApiErrorKey key, Object details) {
        ApiErrorDefinition definition = definition(key);
        return new ApiException(status, definition.code(), definition.message(), details);
    }

    public static ApiErrorResponse response(ApiErrorKey key, Object details) {
        ApiErrorDefinition definition = definition(key);
        return new ApiErrorResponse(definition.code(), definition.message(), details);
    }

    public static String code(ApiErrorKey key) {
        return definition(key).code();
    }

    public static String message(ApiErrorKey key) {
        return definition(key).message();
    }

    private static ApiErrorDefinition definition(ApiErrorKey key) {
        ApiErrorDefinition definition = DEFINITIONS.get(key);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown API error key: " + key);
        }
        return definition;
    }
}
