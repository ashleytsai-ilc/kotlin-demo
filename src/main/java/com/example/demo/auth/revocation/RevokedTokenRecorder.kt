package com.example.demo.auth.revocation

import com.example.demo.auth.jwt.JwtTokenClaims
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant

@Component
class RevokedTokenRecorder(
    private val revokedTokenRepository: RevokedTokenRepository,
    transactionManager: PlatformTransactionManager,
) {
    private val revocationTransaction: TransactionTemplate

    init {
        revocationTransaction = TransactionTemplate(transactionManager)
        revocationTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    }

    fun record(token: JwtTokenClaims) {
        try {
            revocationTransaction.executeWithoutResult {
                revokedTokenRepository.saveAndFlush(revokedToken(token))
            }
        } catch (ex: RuntimeException) {
            if (!revokedTokenRepository.existsById(token.tokenId)) {
                throw ex
            }
        }
    }

    fun consumeForRefresh(token: JwtTokenClaims): Boolean {
        val userId = token.subject ?: return false
        val expiresAt = token.expiresAt ?: return false

        return try {
            revokedTokenRepository.insertIfAbsent(
                token.tokenId,
                userId,
                token.tokenType.value(),
                expiresAt,
                Instant.now(),
            ) == INSERTED_ROW_COUNT
        } catch (ex: DataIntegrityViolationException) {
            false
        }
    }

    private fun revokedToken(token: JwtTokenClaims): RevokedToken =
        RevokedToken(
            token.tokenId,
            token.subject,
            token.tokenType,
            token.expiresAt,
            Instant.now(),
        )

    companion object {
        private const val INSERTED_ROW_COUNT = 1
    }
}
