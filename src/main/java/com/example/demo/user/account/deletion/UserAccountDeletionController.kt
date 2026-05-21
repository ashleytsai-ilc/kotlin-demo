package com.example.demo.user.account.deletion

import com.example.demo.auth.jwt.JwtTokenClaims
import com.example.demo.auth.jwt.JwtTokenType
import com.example.demo.common.ApiExceptions
import com.example.demo.user.account.deletion.dto.AccountDeletionRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserAccountDeletionRoutes.BASE_PATH)
class UserAccountDeletionController(
    private val userAccountDeletionService: UserAccountDeletionService,
) {
    @DeleteMapping(UserAccountDeletionRoutes.ME_ROUTE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCurrentUser(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestBody request: AccountDeletionRequest,
    ) {
        userAccountDeletionService.deleteCurrentUser(toClaims(jwt), request)
    }

    private fun toClaims(jwt: Jwt?): JwtTokenClaims {
        val authenticatedJwt = jwt ?: throw ApiExceptions.unauthorized()
        val tokenId =
            authenticatedJwt.id
                ?.takeIf { it.isNotBlank() }
                ?: throw ApiExceptions.unauthorized()

        return JwtTokenClaims(
            tokenId,
            authenticatedJwt.subject,
            JwtTokenType.ACCESS,
            authenticatedJwt.issuedAt,
            authenticatedJwt.expiresAt,
        )
    }
}
