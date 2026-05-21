package com.example.demo.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(requireNotNull(ex.status) { "API exception status is required." })
            .body(
                ApiErrorResponse(
                    requireNotNull(ex.code) { "API exception code is required." },
                    requireNotNull(ex.message) { "API exception message is required." },
                    ex.details,
                ),
            )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorCatalog.response(
                    ApiErrorKey.INVALID_JSON,
                    listOf(CommonErrorDetail.INVALID_JSON_BODY.toDetail()),
                ),
            )
}
