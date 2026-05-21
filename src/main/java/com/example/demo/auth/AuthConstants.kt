package com.example.demo.auth

import org.springframework.security.oauth2.core.OAuth2AccessToken

object AuthConstants {
    @JvmField
    val BEARER_AUTHENTICATION_SCHEME: String = OAuth2AccessToken.TokenType.BEARER.value
}
