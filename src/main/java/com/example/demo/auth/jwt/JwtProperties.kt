package com.example.demo.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.jwt")
@JvmRecord
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Duration,
    val refreshTokenExpiration: Duration,
)
