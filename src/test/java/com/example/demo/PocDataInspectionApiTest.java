package com.example.demo;

import com.example.demo.auth.jwt.JwtService;
import com.example.demo.auth.revocation.RevokedTokenRepository;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PocDataInspectionApiTest {

    private static final String PASSWORD = "Password1!";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String ALICE_USERNAME = "alice_001";
    private static final String ALICE_NICKNAME = "Ace";
    private static final String BOB_USERNAME = "bob_0001";
    private static final String BOB_NICKNAME = "Bee";
    private static final String CHRIS_USERNAME = "chris_01";
    private static final String CHRIS_NICKNAME = "Cee";
    private static final String ID_FIELD = "id";
    private static final String USERNAME_FIELD = "username";
    private static final String NICKNAME_FIELD = "nickname";
    private static final String PASSWORD_HASH_FIELD = "password_hash";
    private static final String CREATED_AT_FIELD = "created_at";
    private static final String UPDATED_AT_FIELD = "updated_at";
    private static final String DELETED_AT_FIELD = "deleted_at";
    private static final String ACTIVE_USERNAME_KEY_FIELD = "active_username_key";
    private static final String ACTIVE_NICKNAME_KEY_FIELD = "active_nickname_key";
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String REFRESH_TOKEN_FIELD = "refresh_token";
    private static final String TOKEN_ID_FIELD = "token_id";
    private static final String USER_ID_FIELD = "user_id";
    private static final String TOKEN_TYPE_FIELD = "token_type";
    private static final String EXPIRES_AT_FIELD = "expires_at";
    private static final String REVOKED_AT_FIELD = "revoked_at";
    private static final int EXPECTED_EMPTY_COUNT = 0;
    private static final int EXPECTED_SINGLE_ITEM_COUNT = 1;
    private static final int EXPECTED_TWO_ITEM_COUNT = 2;

    private final ObjectMapper objectMapper;
    private final ApiTestHelper helper;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    PocDataInspectionApiTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            JwtService jwtService,
            JwtEncoder jwtEncoder,
            PasswordEncoder passwordEncoder
    ) {
        this.objectMapper = objectMapper;
        this.helper = new ApiTestHelper(mockMvc, objectMapper, jwtEncoder);
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void listsAllUsersWithoutAuthentication() throws Exception {
        helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD);
        helper.register(BOB_USERNAME, BOB_NICKNAME, PASSWORD);

        String body = helper.getPocUsers()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_TWO_ITEM_COUNT)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode alice = helper.findItem(objectMapper.readTree(body), USERNAME_FIELD, ALICE_USERNAME);
        assertThat(alice.get(NICKNAME_FIELD).asString()).isEqualTo(ALICE_NICKNAME);
        assertThat(alice.get(PASSWORD_HASH_FIELD).asString()).isNotBlank();
        assertThat(alice.get(ID_FIELD).asString()).isNotBlank();
        assertThat(alice.get(CREATED_AT_FIELD).asString()).isNotBlank();
        assertThat(alice.get(UPDATED_AT_FIELD).asString()).isNotBlank();
    }

    @Test
    void userInspectionIncludesDeletionAndActiveUniqueKeyState() throws Exception {
        helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD);
        helper.register(BOB_USERNAME, null, PASSWORD);
        JsonNode chris = helper.register(CHRIS_USERNAME, CHRIS_NICKNAME, PASSWORD);
        helper.deleteAccount(
                chris.get(ACCESS_TOKEN_FIELD).asString(),
                helper.accountDeletionJson(PASSWORD, chris.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent());

        String body = helper.getPocUsers()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode users = objectMapper.readTree(body);
        JsonNode alice = helper.findItem(users, USERNAME_FIELD, ALICE_USERNAME);
        assertThat(alice.has(DELETED_AT_FIELD)).isTrue();
        assertThat(alice.get(DELETED_AT_FIELD).isNull()).isTrue();
        assertThat(alice.get(ACTIVE_USERNAME_KEY_FIELD).asString()).isEqualTo(ALICE_USERNAME);
        assertThat(alice.get(ACTIVE_NICKNAME_KEY_FIELD).asString()).isEqualTo(ALICE_NICKNAME);

        JsonNode bob = helper.findItem(users, USERNAME_FIELD, BOB_USERNAME);
        assertThat(bob.has(NICKNAME_FIELD)).isTrue();
        assertThat(bob.get(NICKNAME_FIELD).isNull()).isTrue();
        assertThat(bob.get(ACTIVE_USERNAME_KEY_FIELD).asString()).isEqualTo(BOB_USERNAME);
        assertThat(bob.has(ACTIVE_NICKNAME_KEY_FIELD)).isTrue();
        assertThat(bob.get(ACTIVE_NICKNAME_KEY_FIELD).isNull()).isTrue();

        JsonNode deletedChris = helper.findItem(users, USERNAME_FIELD, CHRIS_USERNAME);
        assertThat(deletedChris.get(DELETED_AT_FIELD).asString()).isNotBlank();
        assertThat(deletedChris.has(ACTIVE_USERNAME_KEY_FIELD)).isTrue();
        assertThat(deletedChris.get(ACTIVE_USERNAME_KEY_FIELD).isNull()).isTrue();
        assertThat(deletedChris.has(ACTIVE_NICKNAME_KEY_FIELD)).isTrue();
        assertThat(deletedChris.get(ACTIVE_NICKNAME_KEY_FIELD).isNull()).isTrue();
    }

    @Test
    void userInspectionReturnsEmptyCollection() throws Exception {
        helper.getPocUsers()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_EMPTY_COUNT)));
    }

    @Test
    void listsAllRevokedTokensWithoutAuthentication() throws Exception {
        JsonNode response = helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD);
        String accessToken = response.get(ACCESS_TOKEN_FIELD).asString();
        String refreshToken = response.get(REFRESH_TOKEN_FIELD).asString();
        helper.logout(accessToken, refreshToken);

        String body = helper.getPocRevokedTokens()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_TWO_ITEM_COUNT)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode revokedToken = objectMapper.readTree(body).get(0);
        assertThat(revokedToken.get(TOKEN_ID_FIELD).asString()).isNotBlank();
        assertThat(revokedToken.get(USER_ID_FIELD).asString()).isEqualTo(response.get(ID_FIELD).asString());
        assertThat(revokedToken.get(TOKEN_TYPE_FIELD).asString()).isNotBlank();
        assertThat(revokedToken.get(EXPIRES_AT_FIELD).asString()).isNotBlank();
        assertThat(revokedToken.get(REVOKED_AT_FIELD).asString()).isNotBlank();
    }

    @Test
    void revokedTokenInspectionReturnsEmptyCollection() throws Exception {
        helper.getPocRevokedTokens()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_EMPTY_COUNT)));
    }

    @Test
    void inspectionIgnoresAuthorizationHeader() throws Exception {
        helper.getPocUsersWithBearer(INVALID_TOKEN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_EMPTY_COUNT)));

        helper.getPocRevokedTokensWithBearer(INVALID_TOKEN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_EMPTY_COUNT)));
    }

    @Test
    void inspectionDoesNotChangeExistingAuthContracts() throws Exception {
        JsonNode registration = helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD);
        JsonNode login = helper.login(ALICE_USERNAME, PASSWORD);
        JsonNode refresh = helper.refresh(login.get(REFRESH_TOKEN_FIELD).asString());

        helper.logout(login.get(ACCESS_TOKEN_FIELD).asString(), login.get(REFRESH_TOKEN_FIELD).asString());

        assertThat(registration.get(USERNAME_FIELD).asString()).isEqualTo(ALICE_USERNAME);
        assertThat(login.get(ACCESS_TOKEN_FIELD).asString()).isNotBlank();
        assertThat(refresh.get(ACCESS_TOKEN_FIELD).asString()).isNotBlank();
        assertThat(refresh.has(USERNAME_FIELD)).isFalse();
    }

    @Test
    void inspectedPasswordHashMatchesPersistedUserState() throws Exception {
        helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD);
        UserAccount user = userRepository.findByUsername(ALICE_USERNAME).orElseThrow();

        String body = helper.getPocUsers()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_SINGLE_ITEM_COUNT)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode inspected = objectMapper.readTree(body).get(0);
        assertThat(inspected.get(PASSWORD_HASH_FIELD).asString()).isEqualTo(user.getPasswordHash());
        assertThat(passwordEncoder.matches(PASSWORD, inspected.get(PASSWORD_HASH_FIELD).asString())).isTrue();
    }

    @Test
    void inspectedRevokedTokensMatchPersistedTokenState() throws Exception {
        JsonNode response = helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD);
        String accessToken = response.get(ACCESS_TOKEN_FIELD).asString();
        String refreshToken = response.get(REFRESH_TOKEN_FIELD).asString();
        helper.logout(accessToken, refreshToken);

        String body = helper.getPocRevokedTokens()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(EXPECTED_TWO_ITEM_COUNT)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode revokedTokens = objectMapper.readTree(body);
        assertThat(revokedTokens.get(0).get(TOKEN_ID_FIELD).asString())
                .isIn(jwtService.tokenId(accessToken), jwtService.tokenId(refreshToken));
        assertThat(revokedTokens.get(1).get(TOKEN_ID_FIELD).asString())
                .isIn(jwtService.tokenId(accessToken), jwtService.tokenId(refreshToken));
    }
}
