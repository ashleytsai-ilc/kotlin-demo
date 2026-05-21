package com.example.demo.auth.revocation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RevokedTokenChecker(
    private val revokedTokenRepository: RevokedTokenRepository,
) {
    @Transactional(readOnly = true)
    fun isRevoked(tokenId: String?): Boolean = !tokenId.isNullOrBlank() && revokedTokenRepository.existsById(tokenId)
}
