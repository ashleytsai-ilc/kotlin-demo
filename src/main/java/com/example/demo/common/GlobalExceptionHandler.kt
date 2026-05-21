package com.example.demo.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), ex.getDetails()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadableMessage() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorCatalog.response(
                        ApiErrorKey.INVALID_JSON,
                        List.of(CommonErrorDetail.INVALID_JSON_BODY.toDetail())
                ));
    }
}
