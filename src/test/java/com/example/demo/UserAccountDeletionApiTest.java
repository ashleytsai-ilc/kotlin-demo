package com.example.demo;

import com.example.demo.auth.jwt.JwtService;
import com.example.demo.auth.revocation.RevokedTokenRepository;
import com.example.demo.common.ApiErrorCatalog;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAccountDeletionApiTest {

    private static final String PASSWORD = "Password1!";
    private static final String WRONG_PASSWORD = "Wrongpass1!";
    private static final String BLANK_CREDENTIAL = "   ";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String UNKNOWN_USER_ID = "missing-user";
    private static final String EMPTY_JSON = "{}";
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String REFRESH_TOKEN_FIELD = "refresh_token";

    private final MockMvc mockMvc;
    private final ApiTestHelper helper;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    @Autowired
    UserAccountDeletionApiTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            JwtEncoder jwtEncoder,
            JwtService jwtService
    ) {
        this.mockMvc = mockMvc;
        this.helper = new ApiTestHelper(mockMvc, objectMapper, jwtEncoder);
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
    }

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void deletesCurrentUserSoftlyAndRevokesSubmittedTokens() throws Exception {
        JsonNode registration = helper.register("alice_001", "Ace", PASSWORD);
        String userId = registration.get("id").asString();
        String accessToken = registration.get(ACCESS_TOKEN_FIELD).asString();
        String refreshToken = registration.get(REFRESH_TOKEN_FIELD).asString();

        helper.deleteAccount(accessToken, helper.accountDeletionJson(PASSWORD, refreshToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        UserAccount deletedUser = userRepository.findById(userId).orElseThrow();
        assertThat(deletedUser.getDeletedAt()).isNotNull();
        assertThat(deletedUser.getUsername()).isEqualTo("alice_001");
        assertThat(deletedUser.getNickname()).isEqualTo("Ace");
        assertThat(deletedUser.getActiveUsernameKey()).isNull();
        assertThat(deletedUser.getActiveNicknameKey()).isNull();
        assertThat(revokedTokenRepository.existsById(jwtService.tokenId(accessToken))).isTrue();
        assertThat(revokedTokenRepository.existsById(jwtService.tokenId(refreshToken))).isTrue();
    }

    @Test
    void rejectsInvalidAccessTokenCasesForAccountDeletion() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = alice.get(ACCESS_TOKEN_FIELD).asString();
        String refreshToken = alice.get(REFRESH_TOKEN_FIELD).asString();
        String body = helper.accountDeletionJson(PASSWORD, refreshToken);

        helper.assertUnauthorizedDeletion(null, body);
        helper.assertUnauthorizedDeletion(INVALID_TOKEN, body);
        helper.assertUnauthorizedDeletion(refreshToken, body);

        JsonNode replacementPair = helper.login("alice_001", PASSWORD);
        helper.logout(accessToken, refreshToken);
        helper.assertUnauthorizedDeletion(accessToken, helper.accountDeletionJson(PASSWORD, refreshToken));

        JsonNode bob = helper.register("bob_0001", "Bee", PASSWORD);
        JsonNode bobSecondPair = helper.login("bob_0001", PASSWORD);
        helper.deleteAccount(
                bob.get(ACCESS_TOKEN_FIELD).asString(),
                helper.accountDeletionJson(PASSWORD, bob.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent());

        helper.assertUnauthorizedDeletion(
                bobSecondPair.get(ACCESS_TOKEN_FIELD).asString(),
                helper.accountDeletionJson(PASSWORD, bobSecondPair.get(REFRESH_TOKEN_FIELD).asString())
        );
        helper.assertUnauthorizedDeletion(bobSecondPair.get(ACCESS_TOKEN_FIELD).asString(), EMPTY_JSON);
        assertThat(replacementPair.get(ACCESS_TOKEN_FIELD).asString()).isNotBlank();
    }

    @Test
    void rejectsMissingBodyAndInvalidPasswordWithoutDeletingUser() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = alice.get(ACCESS_TOKEN_FIELD).asString();
        String refreshToken = alice.get(REFRESH_TOKEN_FIELD).asString();

        helper.assertInvalidJsonDeletionWithoutBody(accessToken);

        helper.assertInvalidAccountDeletionCredentials(accessToken, EMPTY_JSON);
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(null, refreshToken));
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(BLANK_CREDENTIAL, refreshToken));
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(WRONG_PASSWORD, refreshToken));

        UserAccount user = userRepository.findById(alice.get("id").asString()).orElseThrow();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getActiveUsernameKey()).isEqualTo("alice_001");
        assertThat(user.getActiveNicknameKey()).isEqualTo("Ace");
    }

    @Test
    void rejectsInvalidRefreshTokenCasesAsInvalidCredentialsForAccountDeletion() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        JsonNode aliceSecondPair = helper.login("alice_001", PASSWORD);
        JsonNode bob = helper.register("bob_0001", "Bee", PASSWORD);
        String accessToken = aliceSecondPair.get(ACCESS_TOKEN_FIELD).asString();

        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.passwordOnlyJson(PASSWORD));
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(PASSWORD, null));
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(PASSWORD, BLANK_CREDENTIAL));
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(PASSWORD, INVALID_TOKEN));
        helper.assertInvalidAccountDeletionCredentials(
                accessToken,
                helper.accountDeletionJson(PASSWORD, alice.get(ACCESS_TOKEN_FIELD).asString())
        );
        helper.assertInvalidAccountDeletionCredentials(
                accessToken,
                helper.accountDeletionJson(PASSWORD, expiredRefreshToken())
        );

        helper.logout(alice.get(ACCESS_TOKEN_FIELD).asString(), alice.get(REFRESH_TOKEN_FIELD).asString());
        helper.assertInvalidAccountDeletionCredentials(
                accessToken,
                helper.accountDeletionJson(PASSWORD, alice.get(REFRESH_TOKEN_FIELD).asString())
        );
        helper.assertInvalidAccountDeletionCredentials(
                accessToken,
                helper.accountDeletionJson(PASSWORD, bob.get(REFRESH_TOKEN_FIELD).asString())
        );
    }

    @Test
    void deletedUserCannotUseExistingAuthAndProfileFlows() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        JsonNode secondPair = helper.login("alice_001", PASSWORD);

        helper.deleteAccount(
                alice.get(ACCESS_TOKEN_FIELD).asString(),
                helper.accountDeletionJson(PASSWORD, alice.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent());

        helper.assertInvalidCredentials(helper.loginJson("alice_001", PASSWORD));
        helper.assertInvalidRefreshToken(helper.refreshJson(alice.get(REFRESH_TOKEN_FIELD).asString()));
        helper.assertUnauthorizedLogout(
                alice.get(ACCESS_TOKEN_FIELD).asString(),
                helper.logoutJson(alice.get(REFRESH_TOKEN_FIELD).asString())
        );
        helper.assertUnauthorizedProfileUpdate(secondPair.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Captain"));
        helper.assertUnauthorizedProfileUpdate(secondPair.get(ACCESS_TOKEN_FIELD).asString(), EMPTY_JSON);
    }

    @Test
    void deletedUsernameAndNicknameCanBeReusedButActiveValuesRemainUnique() throws Exception {
        JsonNode deletedAlice = helper.register("alice_001", "Ace", PASSWORD);
        helper.deleteAccount(
                deletedAlice.get(ACCESS_TOKEN_FIELD).asString(),
                helper.accountDeletionJson(PASSWORD, deletedAlice.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent());

        helper.register("alice_001", "Ace", PASSWORD);

        mockMvc.perform(post(com.example.demo.user.auth.UserAuthRoutes.REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("alice_001", "Bee", PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.USERNAME_ALREADY_EXISTS)));
        mockMvc.perform(post(com.example.demo.user.auth.UserAuthRoutes.REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.registrationJson("bob_0001", "Ace", PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS)));
    }

    @Test
    void deletedNicknameCanBeReusedOnProfileUpdateButActiveNicknameRemainsUnique() throws Exception {
        JsonNode deletedAlice = helper.register("alice_001", "Ace", PASSWORD);
        JsonNode bob = helper.register("bob_0001", "Bee", PASSWORD);
        JsonNode chris = helper.register("chris_01", "Cee", PASSWORD);
        helper.deleteAccount(
                deletedAlice.get(ACCESS_TOKEN_FIELD).asString(),
                helper.accountDeletionJson(PASSWORD, deletedAlice.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent());

        helper.patchProfile(bob.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Ace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Ace"))
                .andExpect(jsonPath("$.deleted_at").doesNotExist())
                .andExpect(jsonPath("$.active_username_key").doesNotExist())
                .andExpect(jsonPath("$.active_nickname_key").doesNotExist());

        helper.patchProfile(chris.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Ace"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS)));
    }

    @Test
    void formalUserResponsesDoNotExposeDeletionOrActiveKeyFields() throws Exception {
        JsonNode registration = helper.register("alice_001", "Ace", PASSWORD);
        assertNoDeletionOrActiveKeyFields(registration);

        JsonNode login = helper.login("alice_001", PASSWORD);
        assertNoDeletionOrActiveKeyFields(login);

        JsonNode refresh = helper.refresh(login.get(REFRESH_TOKEN_FIELD).asString());
        assertNoDeletionOrActiveKeyFields(refresh);

        String profileResponse = helper.patchProfile(login.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Captain"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(profileResponse).doesNotContain("deleted_at", "active_username_key", "active_nickname_key");
    }

    private String expiredRefreshToken() {
        Instant expiresAt = Instant.now().minusSeconds(60);
        Instant issuedAt = expiresAt.minusSeconds(60);
        return helper.refreshToken(UNKNOWN_USER_ID, issuedAt, expiresAt);
    }

    private void assertNoDeletionOrActiveKeyFields(JsonNode response) {
        assertThat(response.has("deleted_at")).isFalse();
        assertThat(response.has("active_username_key")).isFalse();
        assertThat(response.has("active_nickname_key")).isFalse();
    }
}
