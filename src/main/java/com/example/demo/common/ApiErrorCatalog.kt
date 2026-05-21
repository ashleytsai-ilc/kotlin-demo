package com.example.demo.common

import org.springframework.http.HttpStatus

object ApiErrorCatalog {
    private val DEFINITIONS: Map<ApiErrorKey, ApiErrorDefinition> =
        mapOf(
            ApiErrorKey.REGISTRATION_VALIDATION to
                ApiErrorDefinition("VALIDATION_ERROR", "Registration request is invalid."),
            ApiErrorKey.USER_PROFILE_VALIDATION to
                ApiErrorDefinition("VALIDATION_ERROR", "Profile update request is invalid."),
            ApiErrorKey.INVALID_JSON to
                ApiErrorDefinition("VALIDATION_ERROR", "Request body is invalid."),
            ApiErrorKey.USERNAME_ALREADY_EXISTS to
                ApiErrorDefinition("USERNAME_ALREADY_EXISTS", "Username already exists."),
            ApiErrorKey.NICKNAME_ALREADY_EXISTS to
                ApiErrorDefinition("NICKNAME_ALREADY_EXISTS", "Nickname already exists."),
            ApiErrorKey.REGISTRATION_CONFLICT to
                ApiErrorDefinition("REGISTRATION_CONFLICT", "User registration conflicts with existing data."),
            ApiErrorKey.USER_PROFILE_CONFLICT to
                ApiErrorDefinition("PROFILE_UPDATE_CONFLICT", "User profile update conflicts with existing data."),
            ApiErrorKey.INVALID_CREDENTIALS to
                ApiErrorDefinition("INVALID_CREDENTIALS", "Credential is invalid."),
            ApiErrorKey.INVALID_REFRESH_TOKEN to
                ApiErrorDefinition("INVALID_REFRESH_TOKEN", "Refresh token is invalid."),
            ApiErrorKey.UNAUTHORIZED to
                ApiErrorDefinition("UNAUTHORIZED", "Authentication is required."),
        )

    @JvmStatic
    fun exception(
        status: HttpStatus,
        key: ApiErrorKey,
        details: Any?,
    ): ApiException {
        val definition = definition(key)
        return ApiException(status, definition.code, definition.message, details)
    }

    @JvmStatic
    fun response(
        key: ApiErrorKey,
        details: Any?,
    ): ApiErrorResponse {
        val definition = definition(key)
        return ApiErrorResponse(definition.code, definition.message, details)
    }

    @JvmStatic
    fun code(key: ApiErrorKey): String = definition(key).code

    @JvmStatic
    fun message(key: ApiErrorKey): String = definition(key).message

    private fun definition(key: ApiErrorKey): ApiErrorDefinition = requireNotNull(DEFINITIONS[key]) { "Unknown API error key: $key" }
}
