package com.example.demo.user.auth

import com.example.demo.user.auth.dto.LoginRequest
import com.example.demo.user.auth.dto.LogoutRequest
import com.example.demo.user.auth.dto.RefreshTokenRequest
import com.example.demo.user.auth.dto.RegistrationRequest
import com.example.demo.user.auth.dto.TokenPairResponse
import com.example.demo.user.auth.dto.UserAuthResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserAuthRoutes.BASE_PATH)
class UserAuthController(
    private val userAuthService: UserAuthService,
) {
    @PostMapping(UserAuthRoutes.REGISTER_ROUTE)
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @RequestBody request: RegistrationRequest,
    ): UserAuthResponse = userAuthService.register(request)

    @PostMapping(UserAuthRoutes.LOGIN_ROUTE)
    fun login(
        @RequestBody request: LoginRequest,
    ): UserAuthResponse = userAuthService.login(request)

    @PostMapping(UserAuthRoutes.LOGOUT_ROUTE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        @RequestBody request: LogoutRequest,
    ) {
        userAuthService.logout(authorization, request)
    }

    @PostMapping(UserAuthRoutes.REFRESH_ROUTE)
    fun refresh(
        @RequestBody request: RefreshTokenRequest,
    ): TokenPairResponse = userAuthService.refresh(request)
}
