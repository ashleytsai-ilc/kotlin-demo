package com.example.demo.poc.inspection

import com.example.demo.auth.revocation.RevokedToken
import com.example.demo.auth.revocation.RevokedTokenRepository
import com.example.demo.poc.inspection.dto.PocRevokedTokenInspectionResponse
import com.example.demo.poc.inspection.dto.PocUserInspectionResponse
import com.example.demo.user.account.UserAccount
import com.example.demo.user.account.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PocInspectionService(
    private val userRepository: UserRepository,
    private val revokedTokenRepository: RevokedTokenRepository,
) {
    @Transactional(readOnly = true)
    fun users(): List<PocUserInspectionResponse> = userRepository.findAll().map(::toUserResponse)

    @Transactional(readOnly = true)
    fun revokedTokens(): List<PocRevokedTokenInspectionResponse> = revokedTokenRepository.findAll().map(::toRevokedTokenResponse)

    private fun toUserResponse(user: UserAccount): PocUserInspectionResponse =
        PocUserInspectionResponse(
            checkNotNull(user.id),
            checkNotNull(user.username),
            user.nickname,
            checkNotNull(user.passwordHash),
            checkNotNull(user.createdAt),
            checkNotNull(user.updatedAt),
            user.deletedAt,
            user.activeUsernameKey,
            user.activeNicknameKey,
        )

    private fun toRevokedTokenResponse(revokedToken: RevokedToken): PocRevokedTokenInspectionResponse =
        PocRevokedTokenInspectionResponse(
            checkNotNull(revokedToken.tokenId),
            checkNotNull(revokedToken.userId),
            checkNotNull(revokedToken.tokenType),
            checkNotNull(revokedToken.expiresAt),
            checkNotNull(revokedToken.revokedAt),
        )
}
