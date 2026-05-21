package com.example.demo.common;

public record ApiErrorResponse(String code, String message, Object details) {
}
