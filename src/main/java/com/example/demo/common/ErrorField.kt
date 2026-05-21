package com.example.demo.common

enum class ErrorField(private val value: String) {
    AUTHORIZATION("authorization"),
    BODY("body"),
    CREDENTIALS("credentials"),
    NICKNAME("nickname"),
    PASSWORD("password"),
    REFRESH_TOKEN("refresh_token"),
    USER("user"),
    USERNAME("username");

    fun value(): String = value
}
