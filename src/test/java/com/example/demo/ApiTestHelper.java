package com.example.demo;

import com.example.demo.auth.AuthConstants;
import com.example.demo.auth.jwt.JwtTokenType;
import com.example.demo.common.ApiErrorCatalog;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.poc.inspection.PocInspectionRoutes;
import com.example.demo.user.auth.UserAuthRoutes;
import com.example.demo.user.profile.UserProfileRoutes;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class ApiTestHelper {

    private static final String REGISTER_PATH = UserAuthRoutes.REGISTER_PATH;
    private static final String LOGIN_PATH = UserAuthRoutes.LOGIN_PATH;
    private static final String REFRESH_PATH = UserAuthRoutes.REFRESH_PATH;
    private static final String LOGOUT_PATH = UserAuthRoutes.LOGOUT_PATH;
    private static final String CURRENT_USER_PATH = UserProfileRoutes.ME_PATH;
    private static final String POC_USERS_PATH = PocInspectionRoutes.USERS_PATH;
    private static final String POC_REVOKED_TOKENS_PATH = PocInspectionRoutes.REVOKED_TOKENS_PATH;
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final JwtEncoder jwtEncoder;

    ApiTestHelper(MockMvc mockMvc, ObjectMapper objectMapper, JwtEncoder jwtEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.jwtEncoder = jwtEncoder;
    }

    JsonNode register(String username, String nickname, String password) throws Exception {
        String body = mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(username, nickname, password)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    JsonNode login(String username, String password) throws Exception {
        String body = mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    JsonNode refresh(String refreshToken) throws Exception {
        String body = mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson(refreshToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    void logout(String accessToken, String refreshToken) throws Exception {
        mockMvc.perform(post(LOGOUT_PATH)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutJson(refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    org.springframework.test.web.servlet.ResultActions deleteAccount(String accessToken, String body) throws Exception {
        return mockMvc.perform(authenticatedDelete(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    org.springframework.test.web.servlet.ResultActions patchProfile(String accessToken, String body) throws Exception {
        return mockMvc.perform(authenticatedPatch(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    org.springframework.test.web.servlet.ResultActions getPocUsers() throws Exception {
        return mockMvc.perform(get(POC_USERS_PATH));
    }

    org.springframework.test.web.servlet.ResultActions getPocRevokedTokens() throws Exception {
        return mockMvc.perform(get(POC_REVOKED_TOKENS_PATH));
    }

    org.springframework.test.web.servlet.ResultActions getPocUsersWithBearer(String bearerToken) throws Exception {
        return mockMvc.perform(get(POC_USERS_PATH).header("Authorization", bearer(bearerToken)));
    }

    org.springframework.test.web.servlet.ResultActions getPocRevokedTokensWithBearer(String bearerToken) throws Exception {
        return mockMvc.perform(get(POC_REVOKED_TOKENS_PATH).header("Authorization", bearer(bearerToken)));
    }

    void assertBadRegistrationRequest(String body) throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.REGISTRATION_VALIDATION)))
                .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.REGISTRATION_VALIDATION)))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertBadRegistrationRequestWithoutBody() throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)));
    }

    void assertInvalidCredentials(String body) throws Exception {
        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_CREDENTIALS)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertInvalidCredentialsWithoutBody() throws Exception {
        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)));
    }

    void assertInvalidRefreshToken(String body) throws Exception {
        mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_REFRESH_TOKEN)))
                .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.INVALID_REFRESH_TOKEN)))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertInvalidRefreshTokenWithoutBody() throws Exception {
        mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)));
    }

    void assertUnauthorizedLogout(String bearerToken, String body) throws Exception {
        MockHttpServletRequestBuilder request = post(LOGOUT_PATH)
                .contentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            request.header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + bearerToken);
        }
        if (body != null) {
            request.content(body);
        }
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertInvalidLogoutRefreshToken(String accessToken, String body) throws Exception {
        mockMvc.perform(post(LOGOUT_PATH)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_REFRESH_TOKEN)))
                .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.INVALID_REFRESH_TOKEN)))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertInvalidLogoutRefreshTokenWithoutBody(String accessToken) throws Exception {
        mockMvc.perform(post(LOGOUT_PATH)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)));
    }

    void assertInvalidJsonDeletionWithoutBody(String accessToken) throws Exception {
        mockMvc.perform(authenticatedDelete(accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)));
    }

    void assertUnauthorizedDeletion(String bearerToken, String body) throws Exception {
        MockHttpServletRequestBuilder request = authenticatedDelete(bearerToken)
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            request.content(body);
        }
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertInvalidAccountDeletionCredentials(String accessToken, String body) throws Exception {
        deleteAccount(accessToken, body)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_CREDENTIALS)))
                .andExpect(jsonPath("$.message").value(ApiErrorCatalog.message(ApiErrorKey.INVALID_CREDENTIALS)))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    void assertInvalidJsonProfileUpdateWithoutBody(String accessToken) throws Exception {
        mockMvc.perform(authenticatedPatch(accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)));
    }

    void assertUnauthorizedProfileUpdate(String accessToken, String body) throws Exception {
        patchProfile(accessToken, body)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
                .andExpect(jsonPath("$.message", not("")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    String registrationJson(String username, String nickname, String password) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("nickname", nickname);
        body.put("password", password);
        return objectMapper.writeValueAsString(body);
    }

    String loginJson(String username, String password) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", password);
        return objectMapper.writeValueAsString(body);
    }

    String refreshJson(String refreshToken) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("refresh_token", refreshToken);
        return objectMapper.writeValueAsString(body);
    }

    String logoutJson(String refreshToken) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("refresh_token", refreshToken);
        return objectMapper.writeValueAsString(body);
    }

    String accountDeletionJson(String password, String refreshToken) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", password);
        body.put("refresh_token", refreshToken);
        return objectMapper.writeValueAsString(body);
    }

    String passwordOnlyJson(String password) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", password);
        return objectMapper.writeValueAsString(body);
    }

    String profileJson(String nickname) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nickname", nickname);
        return objectMapper.writeValueAsString(body);
    }

    String profileJsonWithNullNickname() {
        return "{\"nickname\":null}";
    }

    String json(Map<String, Object> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    JsonNode findItem(JsonNode items, String field, String value) {
        for (int index = 0; index < items.size(); index++) {
            JsonNode item = items.get(index);
            if (value.equals(item.get(field).asString())) {
                return item;
            }
        }
        throw new AssertionError("Expected item was not found.");
    }

    String refreshToken(String subject, Instant issuedAt, Instant expiresAt) {
        return token(subject, JwtTokenType.REFRESH, issuedAt, expiresAt);
    }

    String accessToken(String subject, Instant issuedAt, Instant expiresAt) {
        return token(subject, JwtTokenType.ACCESS, issuedAt, expiresAt);
    }

    MockHttpServletRequestBuilder authenticatedDelete(String accessToken) {
        MockHttpServletRequestBuilder request = delete(CURRENT_USER_PATH);
        if (accessToken != null) {
            request.header("Authorization", bearer(accessToken));
        }
        return request;
    }

    MockHttpServletRequestBuilder authenticatedPatch(String accessToken) {
        MockHttpServletRequestBuilder request = patch(CURRENT_USER_PATH);
        if (accessToken != null) {
            request.header("Authorization", bearer(accessToken));
        }
        return request;
    }

    String bearer(String token) {
        return AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + token;
    }

    private String token(String subject, JwtTokenType tokenType, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtTokenType.ID_CLAIM, UUID.randomUUID().toString())
                .claim(JwtTokenType.CLAIM, tokenType.value())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
