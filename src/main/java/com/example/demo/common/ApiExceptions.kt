package com.example.demo.common

import org.springframework.http.HttpStatus

object ApiExceptions {
    @JvmStatic
    fun conflict(
        key: ApiErrorKey,
        field: ErrorField,
    ): ApiException =
        ApiErrorCatalog.exception(
            HttpStatus.CONFLICT,
            key,
            listOf(ErrorDetail(field, ApiErrorCatalog.message(key))),
        )

    @JvmStatic
    fun invalidCredentials(): ApiException = unauthorizedWithDetail(ApiErrorKey.INVALID_CREDENTIALS, ErrorField.CREDENTIALS)

    @JvmStatic
    fun invalidRefreshToken(): ApiException = unauthorizedWithDetail(ApiErrorKey.INVALID_REFRESH_TOKEN, ErrorField.REFRESH_TOKEN)

    @JvmStatic
    fun unauthorized(): ApiException = unauthorizedWithDetail(ApiErrorKey.UNAUTHORIZED, ErrorField.AUTHORIZATION)

    private fun unauthorizedWithDetail(
        key: ApiErrorKey,
        field: ErrorField,
    ): ApiException =
        ApiErrorCatalog.exception(
            HttpStatus.UNAUTHORIZED,
            key,
            listOf(ErrorDetail(field, ApiErrorCatalog.message(key))),
        )
}
