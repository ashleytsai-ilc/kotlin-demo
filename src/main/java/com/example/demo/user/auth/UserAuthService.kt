package com.example.demo.user.auth

import com.example.demo.auth.AuthConstants
import com.example.demo.auth.jwt.JwtService
import com.example.demo.auth.jwt.JwtTokenType
import com.example.demo.auth.revocation.RevokedTokenRecorder
import com.example.demo.common.ApiErrorKey
import com.example.demo.common.ApiExceptions.conflict
import com.example.demo.common.ApiExceptions.invalidCredentials
import com.example.demo.common.ApiExceptions.invalidRefreshToken
import com.example.demo.common.ApiExceptions.unauthorized
import com.example.demo.common.ErrorField
import com.example.demo.common.Strings.trimToNull
import com.example.demo.user.account.UlidGenerator
import com.example.demo.user.account.UserAccount
import com.example.demo.user.account.UserRepository
import com.example.demo.user.auth.dto.LoginRequest
import com.example.demo.user.auth.dto.LogoutRequest
import com.example.demo.user.auth.dto.RefreshTokenRequest
import com.example.demo.user.auth.dto.RegistrationRequest
import com.example.demo.user.auth.dto.TokenPairResponse
import com.example.demo.user.auth.dto.UserAuthResponse
import com.example.demo.user.auth.validation.RegistrationValidator
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserAuthService(
    private val userRepository: UserRepository,
    private val ulidGenerator: UlidGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val registrationValidator: RegistrationValidator,
    private val revokedTokenRecorder: RevokedTokenRecorder,
) {
    @Transactional
    fun register(request: RegistrationRequest): UserAuthResponse {
        val validated = registrationValidator.validate(request)
        val username = checkNotNull(validated.username)
        val password = checkNotNull(validated.password)

        if (userRepository.existsByActiveUsernameKey(username)) {
            throw conflict(ApiErrorKey.USERNAME_ALREADY_EXISTS, ErrorField.USERNAME)
        }
        if (validated.nickname != null && userRepository.existsByActiveNicknameKey(validated.nickname)) {
            throw conflict(ApiErrorKey.NICKNAME_ALREADY_EXISTS, ErrorField.NICKNAME)
        }

        val user =
            UserAccount(
                ulidGenerator.next(),
                username,
                validated.nickname,
                passwordEncoder.encode(password),
            )

        try {
            val saved = userRepository.saveAndFlush(user)
            return toResponse(saved)
        } catch (ex: DataIntegrityViolationException) {
            throw conflict(ApiErrorKey.REGISTRATION_CONFLICT, ErrorField.USER)
        }
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): UserAuthResponse {
        val username = trimToNull(request.username) ?: throw invalidCredentials()
        val password =
            request.password
                ?.takeIf { it.isNotBlank() }
                ?: throw invalidCredentials()

        val user =
            userRepository
                .findByUsernameAndDeletedAtIsNull(username)
                .filter { account -> passwordEncoder.matches(password, account.passwordHash) }
                .orElseThrow { invalidCredentials() }

        return toResponse(user)
    }

    @Transactional
    fun logout(
        authorization: String?,
        request: LogoutRequest,
    ) {
        val accessToken = accessTokenFrom(authorization)
        val accessClaims =
            try {
                jwtService.validateToken(accessToken, JwtTokenType.ACCESS)
            } catch (ex: JwtException) {
                throw unauthorized()
            }

        val refreshToken =
            trimToNull(request.refreshToken)
                ?: throw invalidRefreshToken()

        val refreshClaims =
            try {
                jwtService.validateToken(refreshToken, JwtTokenType.REFRESH)
            } catch (ex: JwtException) {
                throw invalidRefreshToken()
            }
        if (accessClaims.subject != refreshClaims.subject) {
            throw invalidRefreshToken()
        }

        userRepository
            .findByIdAndDeletedAtIsNull(accessClaims.subject)
            .orElseThrow { unauthorized() }
        revokedTokenRecorder.record(accessClaims)
        revokedTokenRecorder.record(refreshClaims)
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): TokenPairResponse {
        val refreshToken =
            trimToNull(request.refreshToken)
                ?: throw invalidRefreshToken()

        val refreshClaims =
            try {
                jwtService.validateRefreshTokenClaims(refreshToken)
            } catch (ex: JwtException) {
                throw invalidRefreshToken()
            }
        val userId = refreshClaims.subject ?: throw invalidRefreshToken()

        val user =
            userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow { invalidRefreshToken() }
        val id = checkNotNull(user.id)
        val accessToken = jwtService.issueAccessToken(id)
        val newRefreshToken = jwtService.issueRefreshToken(id)

        if (!revokedTokenRecorder.consumeForRefresh(refreshClaims)) {
            throw invalidRefreshToken()
        }

        return TokenPairResponse(
            accessToken,
            newRefreshToken,
        )
    }

    private fun toResponse(user: UserAccount): UserAuthResponse {
        val id = checkNotNull(user.id)
        return UserAuthResponse(
            id,
            checkNotNull(user.username),
            user.nickname,
            checkNotNull(user.createdAt),
            checkNotNull(user.updatedAt),
            jwtService.issueAccessToken(id),
            jwtService.issueRefreshToken(id),
        )
    }

    private fun accessTokenFrom(authorization: String?): String {
        val bearerPrefix = AuthConstants.BEARER_AUTHENTICATION_SCHEME + " "
        if (authorization == null || !authorization.startsWith(bearerPrefix)) {
            throw unauthorized()
        }
        return trimToNull(authorization.substring(bearerPrefix.length))
            ?: throw unauthorized()
    }
}
