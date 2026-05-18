package com.example.demo.user.auth;

import com.example.demo.auth.AuthConstants;
import com.example.demo.auth.jwt.JwtTokenClaims;
import com.example.demo.auth.revocation.RevokedTokenRecorder;
import com.example.demo.auth.jwt.JwtService;
import com.example.demo.auth.jwt.JwtTokenType;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.common.ApiExceptions;
import com.example.demo.common.ErrorField;
import com.example.demo.common.Strings;
import com.example.demo.user.account.UlidGenerator;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import com.example.demo.user.auth.dto.LoginRequest;
import com.example.demo.user.auth.dto.LogoutRequest;
import com.example.demo.user.auth.dto.RefreshTokenRequest;
import com.example.demo.user.auth.dto.RegistrationRequest;
import com.example.demo.user.auth.dto.TokenPairResponse;
import com.example.demo.user.auth.dto.UserAuthResponse;
import com.example.demo.user.auth.validation.RegistrationValidator;
import com.example.demo.user.auth.validation.ValidatedRegistration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final UlidGenerator ulidGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegistrationValidator registrationValidator;
    private final RevokedTokenRecorder revokedTokenRecorder;

    public UserAuthService(
            UserRepository userRepository,
            UlidGenerator ulidGenerator,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RegistrationValidator registrationValidator,
            RevokedTokenRecorder revokedTokenRecorder
    ) {
        this.userRepository = userRepository;
        this.ulidGenerator = ulidGenerator;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registrationValidator = registrationValidator;
        this.revokedTokenRecorder = revokedTokenRecorder;
    }

    @Transactional
    public UserAuthResponse register(RegistrationRequest request) {
        ValidatedRegistration validated = registrationValidator.validate(request);
        if (userRepository.existsByActiveUsernameKey(validated.username())) {
            throw ApiExceptions.conflict(ApiErrorKey.USERNAME_ALREADY_EXISTS, ErrorField.USERNAME);
        }
        if (validated.nickname() != null && userRepository.existsByActiveNicknameKey(validated.nickname())) {
            throw ApiExceptions.conflict(ApiErrorKey.NICKNAME_ALREADY_EXISTS, ErrorField.NICKNAME);
        }

        UserAccount user = new UserAccount(
                ulidGenerator.next(),
                validated.username(),
                validated.nickname(),
                passwordEncoder.encode(validated.password())
        );

        try {
            UserAccount saved = userRepository.saveAndFlush(user);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw ApiExceptions.conflict(ApiErrorKey.REGISTRATION_CONFLICT, ErrorField.USER);
        }
    }

    @Transactional(readOnly = true)
    public UserAuthResponse login(LoginRequest request) {
        String username = Strings.trimToNull(request.username());
        String password = request.password();
        if (username == null || password == null || password.isBlank()) {
            throw ApiExceptions.invalidCredentials();
        }

        UserAccount user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
                .orElseThrow(ApiExceptions::invalidCredentials);

        return toResponse(user);
    }

    @Transactional
    public void logout(String authorization, LogoutRequest request) {
        String accessToken = accessTokenFrom(authorization);
        JwtTokenClaims accessClaims;
        try {
            accessClaims = jwtService.validateToken(accessToken, JwtTokenType.ACCESS);
        } catch (JwtException ex) {
            throw ApiExceptions.unauthorized();
        }

        String refreshToken = Strings.trimToNull(request.refreshToken());
        if (refreshToken == null) {
            throw ApiExceptions.invalidRefreshToken();
        }

        JwtTokenClaims refreshClaims;
        try {
            refreshClaims = jwtService.validateToken(refreshToken, JwtTokenType.REFRESH);
        } catch (JwtException ex) {
            throw ApiExceptions.invalidRefreshToken();
        }
        if (!accessClaims.subject().equals(refreshClaims.subject())) {
            throw ApiExceptions.invalidRefreshToken();
        }

        userRepository.findByIdAndDeletedAtIsNull(accessClaims.subject()).orElseThrow(ApiExceptions::unauthorized);
        revokedTokenRecorder.record(accessClaims);
        revokedTokenRecorder.record(refreshClaims);
    }

    @Transactional(readOnly = true)
    public TokenPairResponse refresh(RefreshTokenRequest request) {
        String refreshToken = Strings.trimToNull(request.refreshToken());
        if (refreshToken == null) {
            throw ApiExceptions.invalidRefreshToken();
        }

        String userId;
        try {
            userId = jwtService.validateRefreshToken(refreshToken);
        } catch (JwtException ex) {
            throw ApiExceptions.invalidRefreshToken();
        }

        UserAccount user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(ApiExceptions::invalidRefreshToken);

        return new TokenPairResponse(
                jwtService.issueAccessToken(user.getId()),
                jwtService.issueRefreshToken(user.getId())
        );
    }

    private UserAuthResponse toResponse(UserAccount user) {
        return new UserAuthResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                jwtService.issueAccessToken(user.getId()),
                jwtService.issueRefreshToken(user.getId())
        );
    }

    private String accessTokenFrom(String authorization) {
        String bearerPrefix = AuthConstants.BEARER_AUTHENTICATION_SCHEME + " ";
        if (authorization == null || !authorization.startsWith(bearerPrefix)) {
            throw ApiExceptions.unauthorized();
        }
        String token = Strings.trimToNull(authorization.substring(bearerPrefix.length()));
        if (token == null) {
            throw ApiExceptions.unauthorized();
        }
        return token;
    }
}
