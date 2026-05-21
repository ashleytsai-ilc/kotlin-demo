package com.example.demo.auth.jwt

import com.example.demo.auth.revocation.RevokedTokenChecker
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class JwtService(
    private val properties: JwtProperties,
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
    private val revokedTokenChecker: RevokedTokenChecker,
) {
    fun issueAccessToken(subject: String): String = issueToken(subject, JwtTokenType.ACCESS, properties.accessTokenExpiration)

    fun issueRefreshToken(subject: String): String = issueToken(subject, JwtTokenType.REFRESH, properties.refreshTokenExpiration)

    private fun issueToken(
        subject: String,
        tokenType: JwtTokenType,
        expiration: Duration,
    ): String {
        val now = Instant.now()
        val claims =
            JwtClaimsSet
                .builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .id(UUID.randomUUID().toString())
                .claim(JwtTokenType.CLAIM, tokenType.value())
                .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

    fun validateRefreshToken(token: String?): String? = validateRefreshTokenClaims(token).subject

    fun validateRefreshTokenClaims(token: String?): JwtTokenClaims {
        val claims = validateToken(token, JwtTokenType.REFRESH)
        if (revokedTokenChecker.isRevoked(claims.tokenId)) {
            throw JwtException("JWT has been revoked.")
        }
        return claims
    }

    fun subject(token: String?): String? = decode(token).subject

    fun tokenType(token: String?): String? = tokenType(decode(token))

    fun tokenId(token: String?): String = decode(token).id

    fun issuedAt(token: String?): Instant? = decode(token).issuedAt

    fun expiresAt(token: String?): Instant? = decode(token).expiresAt

    private fun decode(token: String?): Jwt = jwtDecoder.decode(token)

    fun validateToken(
        token: String?,
        expectedType: JwtTokenType,
    ): JwtTokenClaims {
        val jwt = decode(token)
        if (expectedType.value() != tokenType(jwt)) {
            throw JwtException("JWT token type is invalid.")
        }
        val tokenId = jwt.id
        if (tokenId == null || tokenId.isBlank()) {
            throw JwtException("JWT token id is missing.")
        }
        return JwtTokenClaims(
            tokenId,
            jwt.subject,
            expectedType,
            jwt.issuedAt,
            jwt.expiresAt,
        )
    }

    private fun tokenType(jwt: Jwt): String? = jwt.getClaimAsString(JwtTokenType.CLAIM)
}
