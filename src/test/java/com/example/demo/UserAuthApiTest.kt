package com.example.demo;

import com.example.demo.auth.AuthConstants;
import com.example.demo.auth.jwt.JwtProperties;
import com.example.demo.auth.jwt.JwtService;
import com.example.demo.auth.jwt.JwtTokenType;
import com.example.demo.auth.revocation.RevokedTokenRepository;
import com.example.demo.common.ApiErrorCatalog;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import com.example.demo.user.auth.UserAuthRoutes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthApiTest {

    private static final String REGISTER_PATH = UserAuthRoutes.REGISTER_PATH;
    private static final String LOGIN_PATH = UserAuthRoutes.LOGIN_PATH;
    private static final String REFRESH_PATH = UserAuthRoutes.REFRESH_PATH;
    private static final String PROTECTED_PATH = "/api/test/protected";
    private static final String EMPTY_JSON = "{}";
    private static final String PASSWORD = "Password1!";
    private static final String BLANK_CREDENTIAL = "   ";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String UNKNOWN_USER_ID = "missing-user";
    private static final String ULID_PATTERN = "[0-9A-HJKMNP-TV-Z]{26}";

    private final MockMvc mockMvc;
    private final ApiTestHelper helper;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserAuthApiTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            JwtService jwtService,
            JwtProperties jwtProperties,
            JwtEncoder jwtEncoder,
            PasswordEncoder passwordEncoder
    ) {
        this.mockMvc = mockMvc;
        this.helper = new ApiTestHelper(mockMvc, objectMapper, jwtEncoder);
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registersUserWithProfileAndAccessToken() throws Exception {
        String body = mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("alice_001", "Ace", PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username").value("alice_001"))
                .andExpect(jsonPath("$.nickname").value("Ace"))
                .andExpect(jsonPath("$.created_at", notNullValue()))
                .andExpect(jsonPath("$.updated_at", notNullValue()))
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).doesNotContainIgnoringCase("password");
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("alice_001", "Bee", PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.USERNAME_ALREADY_EXISTS)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    void rejectsDuplicateNonEmptyNickname() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("bob_0001", "Ace", PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    void acceptsOmittedAndBlankNickname() throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("alice_001", null, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").doesNotExist());

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("bob_0001", "   ", PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").doesNotExist());
    }

    @Test
    void validatesRegistrationInput() throws Exception {
        helper.assertBadRegistrationRequestWithoutBody();
        helper.assertBadRegistrationRequest(helper.registrationJson(null, "Ace", PASSWORD));
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001", "Ace", null));
        helper.assertBadRegistrationRequest(helper.registrationJson("alice-001", "Ace", PASSWORD));
        helper.assertBadRegistrationRequest(helper.registrationJson("short", "Ace", PASSWORD));
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001_longer", "Ace", PASSWORD));
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001", "1234567890123456789012345678901", PASSWORD));
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001", "Ace", "password1!"));
    }

    @Test
    void storesPasswordHashOnly() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        UserAccount user = userRepository.findByUsername("alice_001").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).isTrue();
    }

    @Test
    void generatesUlidUserId() throws Exception {
        String userId = helper.register("alice_001", "Ace", PASSWORD).get("id").asString();

        assertThat(userId).matches(ULID_PATTERN);
    }

    @Test
    void setsCreatedAtAndUpdatedAtOnRegistration() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        UserAccount user = userRepository.findByUsername("alice_001").orElseThrow();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    void issuesJwtForRegisteredUserWithSubjectAndConfiguredExpiry() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);

        String accessToken = response.get("access_token").asString();
        String refreshToken = response.get("refresh_token").asString();
        UserAccount user = userRepository.findByUsername("alice_001").orElseThrow();

        assertThat(jwtService.subject(accessToken)).isEqualTo(user.getId());
        assertThat(jwtService.subject(refreshToken)).isEqualTo(user.getId());
        assertThat(jwtService.tokenId(accessToken)).isNotBlank();
        assertThat(jwtService.tokenId(refreshToken)).isNotBlank();
        assertThat(jwtService.tokenType(accessToken)).isEqualTo(JwtTokenType.ACCESS.value());
        assertThat(jwtService.tokenType(refreshToken)).isEqualTo(JwtTokenType.REFRESH.value());
        Duration tokenLifetime = Duration.between(
                jwtService.issuedAt(accessToken),
                jwtService.expiresAt(accessToken)
        );
        assertThat(tokenLifetime).isEqualTo(jwtProperties.accessTokenExpiration());
        Duration refreshTokenLifetime = Duration.between(
                jwtService.issuedAt(refreshToken),
                jwtService.expiresAt(refreshToken)
        );
        assertThat(refreshTokenLifetime).isEqualTo(jwtProperties.refreshTokenExpiration());
    }

    @Test
    void authenticatesProtectedApiWithValidBearerToken() throws Exception {
        String accessToken = helper.register("alice_001", "Ace", PASSWORD).get("access_token").asString();

        mockMvc.perform(get(PROTECTED_PATH)
                        .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void rejectsMissingOrInvalidBearerTokenForProtectedApi() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get(PROTECTED_PATH)
                        .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + INVALID_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    void rejectsRefreshTokenForProtectedApi() throws Exception {
        String refreshToken = helper.register("alice_001", "Ace", PASSWORD).get("refresh_token").asString();

        mockMvc.perform(get(PROTECTED_PATH)
                        .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    void logsInUserWithProfileAndAccessToken() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        String body = mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.loginJson("alice_001", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username").value("alice_001"))
                .andExpect(jsonPath("$.nickname").value("Ace"))
                .andExpect(jsonPath("$.created_at", notNullValue()))
                .andExpect(jsonPath("$.updated_at", notNullValue()))
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).doesNotContainIgnoringCase("password");
    }

    @Test
    void rejectsInvalidLoginCredentials() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        helper.assertInvalidCredentials(helper.loginJson("missing_001", PASSWORD));
        helper.assertInvalidCredentials(helper.loginJson("alice_001", "Wrongpass1!"));
    }

    @Test
    void rejectsMissingOrBlankLoginCredentials() throws Exception {
        helper.assertInvalidCredentialsWithoutBody();
        helper.assertInvalidCredentials(helper.loginJson(null, PASSWORD));
        helper.assertInvalidCredentials(helper.loginJson("alice_001", null));
        helper.assertInvalidCredentials(helper.loginJson(BLANK_CREDENTIAL, PASSWORD));
        helper.assertInvalidCredentials(helper.loginJson("alice_001", BLANK_CREDENTIAL));
    }

    @Test
    void loginIsPublic() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.loginJson("alice_001", PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void issuesJwtForLoggedInUserWithSubjectAndConfiguredExpiry() throws Exception {
        helper.register("alice_001", "Ace", PASSWORD);

        JsonNode response = helper.login("alice_001", PASSWORD);
        String accessToken = response.get("access_token").asString();
        String refreshToken = response.get("refresh_token").asString();
        UserAccount user = userRepository.findByUsername("alice_001").orElseThrow();

        assertThat(jwtService.subject(accessToken)).isEqualTo(user.getId());
        assertThat(jwtService.subject(refreshToken)).isEqualTo(user.getId());
        assertThat(jwtService.tokenId(accessToken)).isNotBlank();
        assertThat(jwtService.tokenId(refreshToken)).isNotBlank();
        assertThat(jwtService.tokenType(accessToken)).isEqualTo(JwtTokenType.ACCESS.value());
        assertThat(jwtService.tokenType(refreshToken)).isEqualTo(JwtTokenType.REFRESH.value());
        Duration tokenLifetime = Duration.between(
                jwtService.issuedAt(accessToken),
                jwtService.expiresAt(accessToken)
        );
        assertThat(tokenLifetime).isEqualTo(jwtProperties.accessTokenExpiration());
        Duration refreshTokenLifetime = Duration.between(
                jwtService.issuedAt(refreshToken),
                jwtService.expiresAt(refreshToken)
        );
        assertThat(refreshTokenLifetime).isEqualTo(jwtProperties.refreshTokenExpiration());
    }

    @Test
    void refreshesTokenPairWithValidRefreshToken() throws Exception {
        JsonNode loginResponse = helper.register("alice_001", "Ace", PASSWORD);
        String originalRefreshToken = loginResponse.get("refresh_token").asString();

        JsonNode refreshResponse = helper.refresh(originalRefreshToken);

        assertThat(refreshResponse.get("access_token").asString()).isNotBlank();
        assertThat(refreshResponse.get("refresh_token").asString()).isNotBlank();
        assertThat(jwtService.tokenId(refreshResponse.get("access_token").asString())).isNotBlank();
        assertThat(jwtService.tokenId(refreshResponse.get("refresh_token").asString())).isNotBlank();
        assertThat(refreshResponse.has("id")).isFalse();
        assertThat(refreshResponse.has("username")).isFalse();
        assertThat(refreshResponse.has("nickname")).isFalse();
        assertThat(refreshResponse.has("created_at")).isFalse();
        assertThat(refreshResponse.has("updated_at")).isFalse();

        JsonNode secondRefreshResponse = helper.refresh(originalRefreshToken);
        assertThat(secondRefreshResponse.get("access_token").asString()).isNotBlank();
        assertThat(secondRefreshResponse.get("refresh_token").asString()).isNotBlank();
    }

    @Test
    void rejectsMissingBlankInvalidAndAccessTokenForRefresh() throws Exception {
        String accessToken = helper.register("alice_001", "Ace", PASSWORD).get("access_token").asString();

        helper.assertInvalidRefreshTokenWithoutBody();
        helper.assertInvalidRefreshToken(helper.refreshJson(null));
        helper.assertInvalidRefreshToken(helper.refreshJson(BLANK_CREDENTIAL));
        helper.assertInvalidRefreshToken(helper.refreshJson(INVALID_TOKEN));
        helper.assertInvalidRefreshToken(helper.refreshJson(accessToken));
    }

    @Test
    void rejectsExpiredRefreshToken() throws Exception {
        Instant expiresAt = Instant.now().minus(jwtProperties.accessTokenExpiration());
        Instant issuedAt = expiresAt.minus(jwtProperties.refreshTokenExpiration());
        String expiredRefreshToken = helper.refreshToken(UNKNOWN_USER_ID, issuedAt, expiresAt);

        helper.assertInvalidRefreshToken(helper.refreshJson(expiredRefreshToken));
    }

    @Test
    void rejectsRefreshTokenWhenUserDoesNotExist() throws Exception {
        String refreshToken = helper.register("alice_001", "Ace", PASSWORD).get("refresh_token").asString();
        userRepository.deleteAll();

        helper.assertInvalidRefreshToken(helper.refreshJson(refreshToken));
    }

    @Test
    void refreshEndpointIsPublic() throws Exception {
        JsonNode registerResponse = helper.register("alice_001", "Ace", PASSWORD);
        String refreshToken = registerResponse.get("refresh_token").asString();

        mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.refreshJson(refreshToken)))
                .andExpect(status().isOk());
    }

    @Test
    void refreshIgnoresExpiredAccessTokenAuthorizationHeader() throws Exception {
        JsonNode registerResponse = helper.register("alice_001", "Ace", PASSWORD);
        String refreshToken = registerResponse.get("refresh_token").asString();
        Instant expiresAt = Instant.now().minus(jwtProperties.accessTokenExpiration());
        Instant issuedAt = expiresAt.minus(jwtProperties.accessTokenExpiration());
        String expiredAccessToken = helper.accessToken(registerResponse.get("id").asString(), issuedAt, expiresAt);

        mockMvc.perform(post(REFRESH_PATH)
                        .header(HttpHeaders.AUTHORIZATION, AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + expiredAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.refreshJson(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()));
    }

    @Test
    void logsOutCurrentTokenPair() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = response.get("access_token").asString();
        String refreshToken = response.get("refresh_token").asString();

        helper.logout(accessToken, refreshToken);
    }

    @Test
    void logoutIsIdempotent() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = response.get("access_token").asString();
        String refreshToken = response.get("refresh_token").asString();

        helper.logout(accessToken, refreshToken);
        helper.logout(accessToken, refreshToken);
    }

    @Test
    void rejectsInvalidAccessTokenForLogout() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);
        String refreshToken = response.get("refresh_token").asString();

        helper.assertUnauthorizedLogout(null, helper.logoutJson(refreshToken));
        helper.assertUnauthorizedLogout(INVALID_TOKEN, helper.logoutJson(refreshToken));
        helper.assertUnauthorizedLogout(refreshToken, helper.logoutJson(refreshToken));
    }

    @Test
    void rejectsInvalidRefreshTokenForLogout() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = response.get("access_token").asString();

        helper.assertInvalidLogoutRefreshTokenWithoutBody(accessToken);
        helper.assertInvalidLogoutRefreshToken(accessToken, EMPTY_JSON);
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(null));
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(BLANK_CREDENTIAL));
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(INVALID_TOKEN));

        Instant expiresAt = Instant.now().minus(jwtProperties.accessTokenExpiration());
        Instant issuedAt = expiresAt.minus(jwtProperties.refreshTokenExpiration());
        String expiredRefreshToken = helper.refreshToken(UNKNOWN_USER_ID, issuedAt, expiresAt);
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(expiredRefreshToken));
    }

    @Test
    void rejectsLogoutWhenTokensBelongToDifferentUsers() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        JsonNode bob = helper.register("bob_0001", "Bee", PASSWORD);

        helper.assertInvalidLogoutRefreshToken(
                alice.get("access_token").asString(),
                helper.logoutJson(bob.get("refresh_token").asString())
        );
    }

    @Test
    void revokedAccessTokenCannotAuthenticateProtectedApi() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = response.get("access_token").asString();
        String refreshToken = response.get("refresh_token").asString();

        helper.logout(accessToken, refreshToken);

        mockMvc.perform(get(PROTECTED_PATH)
                        .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    void revokedRefreshTokenCannotRefresh() throws Exception {
        JsonNode response = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = response.get("access_token").asString();
        String refreshToken = response.get("refresh_token").asString();

        helper.logout(accessToken, refreshToken);

        helper.assertInvalidRefreshToken(helper.refreshJson(refreshToken));
    }

    @TestConfiguration
    static class ProtectedTestEndpointConfiguration {

        @Bean
        ProtectedTestController protectedTestController() {
            return new ProtectedTestController();
        }
    }

    @RestController
    static class ProtectedTestController {

        @GetMapping("/api/test/protected")
        Map<String, String> protectedEndpoint() {
            return Map.of("status", "ok");
        }
    }
}
