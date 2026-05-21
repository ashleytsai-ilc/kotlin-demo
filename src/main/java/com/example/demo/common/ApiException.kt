package com.example.demo.common

import org.springframework.http.HttpStatus

class ApiException(
    val status: HttpStatus?,
    val code: String?,
    message: String?,
    val details: Any?,
) : RuntimeException(message)
