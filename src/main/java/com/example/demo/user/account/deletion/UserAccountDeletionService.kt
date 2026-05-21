package com.example.demo.user.account.deletion;

import com.example.demo.auth.jwt.JwtService;
import com.example.demo.auth.jwt.JwtTokenClaims;
import com.example.demo.auth.revocation.RevokedTokenRecorder;
import com.example.demo.common.ApiExceptions;
import com.example.demo.common.Strings;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import com.example.demo.user.account.deletion.dto.AccountDeletionRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserAccountDeletionService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenRecorder revokedTokenRecorder;

    public UserAccountDeletionService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RevokedTokenRecorder revokedTokenRecorder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.revokedTokenRecorder = revokedTokenRecorder;
    }

    @Transactional
    public void deleteCurrentUser(JwtTokenClaims accessClaims, AccountDeletionRequest request) {
        UserAccount user = userRepository.findByIdAndDeletedAtIsNull(accessClaims.subject())
                .orElseThrow(ApiExceptions::unauthorized);

        String password = request.password();
        if (password == null || password.isBlank()) {
            throw ApiExceptions.invalidCredentials();
        }

        String refreshToken = Strings.trimToNull(request.refreshToken());
        if (refreshToken == null) {
            throw ApiExceptions.invalidCredentials();
        }

        JwtTokenClaims refreshClaims;
        try {
            refreshClaims = jwtService.validateRefreshTokenClaims(refreshToken);
        } catch (JwtException ex) {
            throw ApiExceptions.invalidCredentials();
        }
        if (!accessClaims.subject().equals(refreshClaims.subject())) {
            throw ApiExceptions.invalidCredentials();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw ApiExceptions.invalidCredentials();
        }

        user.softDelete(Instant.now());
        userRepository.saveAndFlush(user);
        revokedTokenRecorder.record(accessClaims);
        revokedTokenRecorder.record(refreshClaims);
    }

}
