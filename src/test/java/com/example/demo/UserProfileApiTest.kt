package com.example.demo;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileApiTest {

    private static final String PASSWORD = "Password1!";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String EMPTY_JSON = "{}";
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String REFRESH_TOKEN_FIELD = "refresh_token";

    private final ApiTestHelper helper;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserProfileApiTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            JwtEncoder jwtEncoder,
            PasswordEncoder passwordEncoder
    ) {
        this.helper = new ApiTestHelper(mockMvc, objectMapper, jwtEncoder);
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void updatesNicknameAndReturnsProfileFields() throws Exception {
        JsonNode registration = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = registration.get(ACCESS_TOKEN_FIELD).asString();

        String body = helper.patchProfile(accessToken, helper.profileJson("Captain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registration.get("id").asString()))
                .andExpect(jsonPath("$.username").value("alice_001"))
                .andExpect(jsonPath("$.nickname").value("Captain"))
                .andExpect(jsonPath("$.created_at", notNullValue()))
                .andExpect(jsonPath("$.updated_at", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).doesNotContainIgnoringCase("password");
        assertThat(body).doesNotContain(ACCESS_TOKEN_FIELD, REFRESH_TOKEN_FIELD);
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isEqualTo("Captain");
    }

    @Test
    void updatesOnlyTheBearerTokenUser() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        helper.register("bob_0001", "Bee", PASSWORD);

        helper.patchProfile(alice.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Captain"))
                .andExpect(status().isOk());

        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isEqualTo("Captain");
        assertThat(userRepository.findByUsername("bob_0001").orElseThrow().getNickname()).isEqualTo("Bee");
    }

    @Test
    void rejectsMissingInvalidRefreshRevokedAndMissingSubjectAccessTokens() throws Exception {
        JsonNode registration = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = registration.get(ACCESS_TOKEN_FIELD).asString();
        String refreshToken = registration.get(REFRESH_TOKEN_FIELD).asString();

        helper.assertUnauthorizedProfileUpdate(null, helper.profileJson("Captain"));
        helper.assertUnauthorizedProfileUpdate(INVALID_TOKEN, helper.profileJson("Captain"));
        helper.assertUnauthorizedProfileUpdate(refreshToken, helper.profileJson("Captain"));

        helper.logout(accessToken, refreshToken);
        helper.assertUnauthorizedProfileUpdate(accessToken, helper.profileJson("Captain"));

        JsonNode bob = helper.register("bob_0001", "Bee", PASSWORD);
        String deletedUserToken = bob.get(ACCESS_TOKEN_FIELD).asString();
        userRepository.deleteById(bob.get("id").asString());
        helper.assertUnauthorizedProfileUpdate(deletedUserToken, helper.profileJson("Captain"));
    }

    @Test
    void ignoresUnknownAndImmutableFields() throws Exception {
        JsonNode registration = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = registration.get(ACCESS_TOKEN_FIELD).asString();
        UserAccount before = userRepository.findByUsername("alice_001").orElseThrow();
        String originalPasswordHash = before.getPasswordHash();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", "different-id");
        body.put("username", "mallory_1");
        body.put("password", "NewPassword1!");
        body.put(ACCESS_TOKEN_FIELD, "new-access");
        body.put(REFRESH_TOKEN_FIELD, "new-refresh");
        body.put("unknown_field", "ignored");
        body.put("nickname", "Captain");

        String response = helper.patchProfile(accessToken, helper.json(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registration.get("id").asString()))
                .andExpect(jsonPath("$.username").value("alice_001"))
                .andExpect(jsonPath("$.nickname").value("Captain"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserAccount after = userRepository.findById(registration.get("id").asString()).orElseThrow();
        assertThat(after.getUsername()).isEqualTo("alice_001");
        assertThat(after.getPasswordHash()).isEqualTo(originalPasswordHash);
        assertThat(passwordEncoder.matches("NewPassword1!", after.getPasswordHash())).isFalse();
        assertThat(response).doesNotContainIgnoringCase("password");
        assertThat(response).doesNotContain(ACCESS_TOKEN_FIELD, REFRESH_TOKEN_FIELD, "unknown_field");
    }

    @Test
    void handlesMissingClearedAndRequiredNicknameRequests() throws Exception {
        JsonNode registration = helper.register("alice_001", "Ace", PASSWORD);
        String accessToken = registration.get(ACCESS_TOKEN_FIELD).asString();

        helper.assertInvalidJsonProfileUpdateWithoutBody(accessToken);

        helper.patchProfile(accessToken, EMPTY_JSON)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.USER_PROFILE_VALIDATION)))
                .andExpect(jsonPath("$.details", notNullValue()));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isEqualTo("Ace");

        helper.patchProfile(accessToken, helper.profileJsonWithNullNickname())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.USER_PROFILE_VALIDATION)))
                .andExpect(jsonPath("$.details", notNullValue()));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isEqualTo("Ace");

        helper.patchProfile(accessToken, helper.profileJson(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(""));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isNull();

        helper.patchProfile(accessToken, helper.profileJson("Ace"))
                .andExpect(status().isOk());
        helper.patchProfile(accessToken, helper.profileJson("   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(""));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isNull();

        helper.patchProfile(accessToken, helper.profileJson("Ace"))
                .andExpect(status().isOk());
    }

    @Test
    void validatesNicknameLengthUniquenessNoOpAndMultipleUsersWithoutNickname() throws Exception {
        JsonNode alice = helper.register("alice_001", "Ace", PASSWORD);
        helper.register("bob_0001", "Bee", PASSWORD);
        String accessToken = alice.get(ACCESS_TOKEN_FIELD).asString();

        helper.patchProfile(accessToken, helper.profileJson("1234567890123456789012345678901"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.USER_PROFILE_VALIDATION)))
                .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.USER_PROFILE_VALIDATION)))
                .andExpect(jsonPath("$.details", notNullValue()));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isEqualTo("Ace");

        helper.patchProfile(accessToken, helper.profileJson("Bee"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getNickname()).isEqualTo("Ace");

        UserAccount beforeSameNickname = userRepository.findByUsername("alice_001").orElseThrow();
        helper.patchProfile(accessToken, helper.profileJson("Ace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Ace"))
                .andExpect(jsonPath("$.updated_at").value(beforeSameNickname.getUpdatedAt().toString()));
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().getUpdatedAt())
                .isEqualTo(beforeSameNickname.getUpdatedAt());

        helper.patchProfile(accessToken, helper.profileJson(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(""));
        String bobAccessToken = helper.login("bob_0001", PASSWORD).get(ACCESS_TOKEN_FIELD).asString();
        helper.patchProfile(bobAccessToken, helper.profileJson(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(""));
    }
}
