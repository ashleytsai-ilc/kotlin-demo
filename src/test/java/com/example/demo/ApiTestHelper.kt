package com.example.demo

import com.example.demo.auth.AuthConstants
import com.example.demo.auth.jwt.JwtTokenType
import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.poc.inspection.PocInspectionRoutes
import com.example.demo.user.auth.UserAuthRoutes
import com.example.demo.user.profile.UserProfileRoutes
import org.hamcrest.Matchers
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

internal class ApiTestHelper(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val jwtEncoder: JwtEncoder
) {
    @Throws(Exception::class)
    fun register(username: String?, nickname: String?, password: String?): JsonNode {
        val body = mockMvc.perform(
            MockMvcRequestBuilders.post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson(username, nickname, password))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readTree(body)
    }

    @Throws(Exception::class)
    fun login(username: String?, password: String?): JsonNode {
        val body = mockMvc.perform(
            MockMvcRequestBuilders.post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(username, password))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readTree(body)
    }

    @Throws(Exception::class)
    fun refresh(refreshToken: String?): JsonNode {
        val body = mockMvc.perform(
            MockMvcRequestBuilders.post(REFRESH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson(refreshToken))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readTree(body)
    }

    @Throws(Exception::class)
    fun logout(accessToken: String?, refreshToken: String?) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(LOGOUT_PATH)
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutJson(refreshToken))
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent())
            .andExpect(MockMvcResultMatchers.content().string(""))
    }

    @Throws(Exception::class)
    fun deleteAccount(accessToken: String?, body: String): ResultActions {
        return mockMvc.perform(
            authenticatedDelete(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
    }

    @Throws(Exception::class)
    fun patchProfile(accessToken: String?, body: String): ResultActions {
        return mockMvc.perform(
            authenticatedPatch(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
    }

    @Throws(Exception::class)
    fun getPocUsers(): ResultActions {
        return mockMvc.perform(MockMvcRequestBuilders.get(POC_USERS_PATH))
    }

    @Throws(Exception::class)
    fun getPocRevokedTokens(): ResultActions {
        return mockMvc.perform(MockMvcRequestBuilders.get(POC_REVOKED_TOKENS_PATH))
    }

    @Throws(Exception::class)
    fun getPocUsersWithBearer(bearerToken: String?): ResultActions {
        return mockMvc.perform(MockMvcRequestBuilders.get(POC_USERS_PATH).header("Authorization", bearer(bearerToken)))
    }

    @Throws(Exception::class)
    fun getPocRevokedTokensWithBearer(bearerToken: String?): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.get(POC_REVOKED_TOKENS_PATH).header("Authorization", bearer(bearerToken))
        )
    }

    @Throws(Exception::class)
    fun assertBadRegistrationRequest(body: String) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.REGISTRATION_VALIDATION))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.message")
                    .value(ApiErrorCatalog.message(ApiErrorKey.REGISTRATION_VALIDATION))
            )
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertBadRegistrationRequestWithoutBody() {
        mockMvc.perform(
            MockMvcRequestBuilders.post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
    }

    @Throws(Exception::class)
    fun assertInvalidCredentials(body: String) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_CREDENTIALS))
            )
            .andExpect(MockMvcResultMatchers.jsonPath<String>("$.message", Matchers.not("")))
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertInvalidCredentialsWithoutBody() {
        mockMvc.perform(
            MockMvcRequestBuilders.post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
    }

    @Throws(Exception::class)
    fun assertInvalidRefreshToken(body: String) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(REFRESH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_REFRESH_TOKEN))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.message")
                    .value(ApiErrorCatalog.message(ApiErrorKey.INVALID_REFRESH_TOKEN))
            )
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertInvalidRefreshTokenWithoutBody() {
        mockMvc.perform(
            MockMvcRequestBuilders.post(REFRESH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
    }

    @Throws(Exception::class)
    fun assertUnauthorizedLogout(bearerToken: String?, body: String?) {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.post(LOGOUT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
        if (bearerToken != null) {
            request.header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + bearerToken)
        }
        if (body != null) {
            request.content(body)
        }
        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
            .andExpect(MockMvcResultMatchers.jsonPath<String>("$.message", Matchers.not("")))
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertInvalidLogoutRefreshToken(accessToken: String?, body: String) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(LOGOUT_PATH)
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_REFRESH_TOKEN))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.message")
                    .value(ApiErrorCatalog.message(ApiErrorKey.INVALID_REFRESH_TOKEN))
            )
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertInvalidLogoutRefreshTokenWithoutBody(accessToken: String?) {
        mockMvc.perform(
            MockMvcRequestBuilders.post(LOGOUT_PATH)
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
    }

    @Throws(Exception::class)
    fun assertInvalidJsonDeletionWithoutBody(accessToken: String?) {
        mockMvc.perform(
            authenticatedDelete(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
    }

    @Throws(Exception::class)
    fun assertUnauthorizedDeletion(bearerToken: String?, body: String?) {
        val request = authenticatedDelete(bearerToken)
            .contentType(MediaType.APPLICATION_JSON)
        if (body != null) {
            request.content(body)
        }
        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
            .andExpect(MockMvcResultMatchers.jsonPath<String>("$.message", Matchers.not("")))
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertInvalidAccountDeletionCredentials(accessToken: String?, body: String) {
        deleteAccount(accessToken, body)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_CREDENTIALS))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.message")
                    .value(ApiErrorCatalog.message(ApiErrorKey.INVALID_CREDENTIALS))
            )
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun assertInvalidJsonProfileUpdateWithoutBody(accessToken: String?) {
        mockMvc.perform(
            authenticatedPatch(accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.INVALID_JSON)))
    }

    @Throws(Exception::class)
    fun assertUnauthorizedProfileUpdate(accessToken: String?, body: String) {
        patchProfile(accessToken, body)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
            .andExpect(MockMvcResultMatchers.jsonPath<String>("$.message", Matchers.not("")))
            .andExpect(MockMvcResultMatchers.jsonPath<Any>("$.details", Matchers.notNullValue()))
    }

    @Throws(Exception::class)
    fun registrationJson(username: String?, nickname: String?, password: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["username"] = username
        body["nickname"] = nickname
        body["password"] = password
        return objectMapper.writeValueAsString(body)
    }

    @Throws(Exception::class)
    fun loginJson(username: String?, password: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["username"] = username
        body["password"] = password
        return objectMapper.writeValueAsString(body)
    }

    @Throws(Exception::class)
    fun refreshJson(refreshToken: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["refresh_token"] = refreshToken
        return objectMapper.writeValueAsString(body)
    }

    @Throws(Exception::class)
    fun logoutJson(refreshToken: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["refresh_token"] = refreshToken
        return objectMapper.writeValueAsString(body)
    }

    @Throws(Exception::class)
    fun accountDeletionJson(password: String?, refreshToken: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["password"] = password
        body["refresh_token"] = refreshToken
        return objectMapper.writeValueAsString(body)
    }

    @Throws(Exception::class)
    fun passwordOnlyJson(password: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["password"] = password
        return objectMapper.writeValueAsString(body)
    }

    @Throws(Exception::class)
    fun profileJson(nickname: String?): String {
        val body = linkedMapOf<String, Any?>()
        body["nickname"] = nickname
        return objectMapper.writeValueAsString(body)
    }

    fun profileJsonWithNullNickname(): String = "{\"nickname\":null}"

    @Throws(Exception::class)
    fun json(body: Map<String, Any?>): String {
        return objectMapper.writeValueAsString(body)
    }

    fun findItem(items: JsonNode, field: String, value: String): JsonNode {
        for (index in 0..<items.size()) {
            val item = items.get(index)
            if (value == item.get(field).asString()) {
                return item
            }
        }
        throw AssertionError("Expected item was not found.")
    }

    fun refreshToken(subject: String, issuedAt: Instant, expiresAt: Instant): String {
        return token(subject, JwtTokenType.REFRESH, issuedAt, expiresAt)
    }

    fun accessToken(subject: String, issuedAt: Instant, expiresAt: Instant): String {
        return token(subject, JwtTokenType.ACCESS, issuedAt, expiresAt)
    }

    fun authenticatedDelete(accessToken: String?): MockHttpServletRequestBuilder {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(CURRENT_USER_PATH)
        if (accessToken != null) {
            request.header("Authorization", bearer(accessToken))
        }
        return request
    }

    fun authenticatedPatch(accessToken: String?): MockHttpServletRequestBuilder {
        val request: MockHttpServletRequestBuilder = MockMvcRequestBuilders.patch(CURRENT_USER_PATH)
        if (accessToken != null) {
            request.header("Authorization", bearer(accessToken))
        }
        return request
    }

    fun bearer(token: String?): String {
        return AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + token
    }

    private fun token(subject: String, tokenType: JwtTokenType, issuedAt: Instant, expiresAt: Instant): String {
        val claims = JwtClaimsSet.builder()
            .subject(subject)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim(JwtTokenType.ID_CLAIM, UUID.randomUUID().toString())
            .claim(JwtTokenType.CLAIM, tokenType.value())
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

    companion object {
        private val REGISTER_PATH = UserAuthRoutes.REGISTER_PATH
        private val LOGIN_PATH = UserAuthRoutes.LOGIN_PATH
        private val REFRESH_PATH = UserAuthRoutes.REFRESH_PATH
        private val LOGOUT_PATH = UserAuthRoutes.LOGOUT_PATH
        private val CURRENT_USER_PATH = UserProfileRoutes.ME_PATH
        private val POC_USERS_PATH = PocInspectionRoutes.USERS_PATH
        private val POC_REVOKED_TOKENS_PATH = PocInspectionRoutes.REVOKED_TOKENS_PATH
    }
}
