package com.example.demo.common;

public record ErrorDetail(String field, String message) {

    public ErrorDetail(ErrorField field, String message) {
        this(field.value(), message);
    }
}
