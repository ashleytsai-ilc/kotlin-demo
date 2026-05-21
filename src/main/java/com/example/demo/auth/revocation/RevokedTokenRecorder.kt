package com.example.demo.auth.revocation

import com.example.demo.auth.jwt.JwtTokenClaims
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
                revokedTokenRepository.saveAndFlush(
                    RevokedToken(
                        token.tokenId,
                        token.subject,
                        token.tokenType,
                        token.expiresAt,
                        Instant.now(),
                    ),
                )
            }
        } catch (ex: RuntimeException) {
            if (!revokedTokenRepository.existsById(token.tokenId)) {
                throw ex
            }
        }
    }
}
