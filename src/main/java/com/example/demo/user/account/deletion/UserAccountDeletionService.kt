package com.example.demo.user.account.deletion

import com.example.demo.auth.jwt.JwtService
import com.example.demo.auth.jwt.JwtTokenClaims
import com.example.demo.auth.revocation.RevokedTokenRecorder
import com.example.demo.common.ApiExceptions
import com.example.demo.common.Strings
import com.example.demo.user.account.UserRepository
import com.example.demo.user.account.deletion.dto.AccountDeletionRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserAccountDeletionService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val revokedTokenRecorder: RevokedTokenRecorder,
) {
    @Transactional
    fun deleteCurrentUser(
        accessClaims: JwtTokenClaims,
        request: AccountDeletionRequest,
    ) {
        val user =
            userRepository
                .findByIdAndDeletedAtIsNull(accessClaims.subject)
                .orElseThrow { ApiExceptions.unauthorized() }

        val password =
            request.password
                ?.takeIf { it.isNotBlank() }
                ?: throw ApiExceptions.invalidCredentials()

        val refreshToken =
            Strings.trimToNull(request.refreshToken)
                ?: throw ApiExceptions.invalidCredentials()

        val refreshClaims =
            try {
                jwtService.validateRefreshTokenClaims(refreshToken)
            } catch (ex: JwtException) {
                throw ApiExceptions.invalidCredentials()
            }
        if (accessClaims.subject != refreshClaims.subject) {
            throw ApiExceptions.invalidCredentials()
        }

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw ApiExceptions.invalidCredentials()
        }

        user.softDelete(Instant.now())
        userRepository.saveAndFlush(user)
        revokedTokenRecorder.record(accessClaims)
        revokedTokenRecorder.record(refreshClaims)
    }
}
