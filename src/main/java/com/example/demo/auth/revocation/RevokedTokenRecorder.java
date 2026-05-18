package com.example.demo.auth.revocation;

import com.example.demo.auth.jwt.JwtTokenClaims;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Component
public class RevokedTokenRecorder {

    private final RevokedTokenRepository revokedTokenRepository;
    private final TransactionTemplate revocationTransaction;

    public RevokedTokenRecorder(
            RevokedTokenRepository revokedTokenRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.revokedTokenRepository = revokedTokenRepository;
        this.revocationTransaction = new TransactionTemplate(transactionManager);
        this.revocationTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(JwtTokenClaims token) {
        try {
            revocationTransaction.executeWithoutResult(status -> revokedTokenRepository.saveAndFlush(new RevokedToken(
                            token.tokenId(),
                            token.subject(),
                            token.tokenType(),
                            token.expiresAt(),
                            Instant.now()
                    ))
            );
        } catch (RuntimeException ex) {
            if (!revokedTokenRepository.existsById(token.tokenId())) {
                throw ex;
            }
        }
    }
}
