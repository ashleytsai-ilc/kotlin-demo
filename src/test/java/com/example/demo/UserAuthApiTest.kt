package com.example.demo

import com.example.demo.auth.AuthConstants
import com.example.demo.auth.jwt.JwtProperties
import com.example.demo.auth.jwt.JwtService
import com.example.demo.auth.jwt.JwtTokenType
import com.example.demo.auth.revocation.RevokedTokenRepository
import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.user.account.UserAccount
import com.example.demo.user.account.UserRepository
import com.example.demo.user.auth.UserAuthRoutes
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthApiTest @Autowired constructor(
    private val mockMvc: MockMvc,
    objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val revokedTokenRepository: RevokedTokenRepository,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    jwtEncoder: JwtEncoder,
    private val passwordEncoder: PasswordEncoder
) {
    private val helper = ApiTestHelper(mockMvc, objectMapper, jwtEncoder)

    @BeforeEach
    fun setUp() {
        revokedTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun registersUserWithProfileAndAccessToken() {
        val body = mockMvc.perform(
            post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("alice_001", "Ace", PASSWORD))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.username").value("alice_001"))
            .andExpect(jsonPath("$.nickname").value("Ace"))
            .andExpect(jsonPath("$.created_at", notNullValue()))
            .andExpect(jsonPath("$.updated_at", notNullValue()))
            .andExpect(jsonPath("$.access_token", notNullValue()))
            .andExpect(jsonPath("$.refresh_token", notNullValue()))
            .andReturn()
            .response
            .contentAsString

        assertThat(body).doesNotContainIgnoringCase("password")
    }

    @Test
    fun rejectsDuplicateUsername() {
        helper.register("alice_001", "Ace", PASSWORD)

        mockMvc.perform(
            post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("alice_001", "Bee", PASSWORD))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.USERNAME_ALREADY_EXISTS)))
            .andExpect(jsonPath("$.message", not("")))
            .andExpect(jsonPath("$.details", notNullValue()))
    }

    @Test
    fun rejectsDuplicateNonEmptyNickname() {
        helper.register("alice_001", "Ace", PASSWORD)

        mockMvc.perform(
            post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("bob_0001", "Ace", PASSWORD))
        )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS)))
            .andExpect(jsonPath("$.message", not("")))
            .andExpect(jsonPath("$.details", notNullValue()))
    }

    @Test
    fun acceptsOmittedAndBlankNickname() {
        mockMvc.perform(
            post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("alice_001", null, PASSWORD))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nickname").doesNotExist())

        mockMvc.perform(
            post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("bob_0001", "   ", PASSWORD))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nickname").doesNotExist())
    }

    @Test
    fun validatesRegistrationInput() {
        helper.assertBadRegistrationRequestWithoutBody()
        helper.assertBadRegistrationRequest(helper.registrationJson(null, "Ace", PASSWORD))
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001", "Ace", null))
        helper.assertBadRegistrationRequest(helper.registrationJson("alice-001", "Ace", PASSWORD))
        helper.assertBadRegistrationRequest(helper.registrationJson("short", "Ace", PASSWORD))
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001_longer", "Ace", PASSWORD))
        helper.assertBadRegistrationRequest(
            helper.registrationJson("alice_001", "1234567890123456789012345678901", PASSWORD)
        )
        helper.assertBadRegistrationRequest(helper.registrationJson("alice_001", "Ace", "password1!"))
    }

    @Test
    fun storesPasswordHashOnly() {
        helper.register("alice_001", "Ace", PASSWORD)

        val user: UserAccount = userRepository.findByUsername("alice_001").orElseThrow()
        assertThat(user.passwordHash).isNotEqualTo(PASSWORD)
        assertThat(passwordEncoder.matches(PASSWORD, user.passwordHash)).isTrue()
    }

    @Test
    fun generatesUlidUserId() {
        val userId = helper.register("alice_001", "Ace", PASSWORD).get("id").asString()

        assertThat(userId).matches(ULID_PATTERN)
    }

    @Test
    fun setsCreatedAtAndUpdatedAtOnRegistration() {
        helper.register("alice_001", "Ace", PASSWORD)

        val user: UserAccount = userRepository.findByUsername("alice_001").orElseThrow()
        assertThat(user.createdAt).isNotNull()
        assertThat(user.updatedAt).isNotNull()
        assertThat(user.updatedAt).isEqualTo(user.createdAt)
    }

    @Test
    fun issuesJwtForRegisteredUserWithSubjectAndConfiguredExpiry() {
        val response = helper.register("alice_001", "Ace", PASSWORD)

        val accessToken = response.get("access_token").asString()
        val refreshToken = response.get("refresh_token").asString()
        val user: UserAccount = userRepository.findByUsername("alice_001").orElseThrow()

        assertThat(jwtService.subject(accessToken)).isEqualTo(user.id)
        assertThat(jwtService.subject(refreshToken)).isEqualTo(user.id)
        assertThat(jwtService.tokenId(accessToken)).isNotBlank()
        assertThat(jwtService.tokenId(refreshToken)).isNotBlank()
        assertThat(jwtService.tokenType(accessToken)).isEqualTo(JwtTokenType.ACCESS.value())
        assertThat(jwtService.tokenType(refreshToken)).isEqualTo(JwtTokenType.REFRESH.value())
        val tokenLifetime = Duration.between(
            jwtService.issuedAt(accessToken),
            jwtService.expiresAt(accessToken)
        )
        assertThat(tokenLifetime).isEqualTo(jwtProperties.accessTokenExpiration)
        val refreshTokenLifetime = Duration.between(
            jwtService.issuedAt(refreshToken),
            jwtService.expiresAt(refreshToken)
        )
        assertThat(refreshTokenLifetime).isEqualTo(jwtProperties.refreshTokenExpiration)
    }

    @Test
    fun authenticatesProtectedApiWithValidBearerToken() {
        val accessToken = helper.register("alice_001", "Ace", PASSWORD).get("access_token").asString()

        mockMvc.perform(
            get(PROTECTED_PATH)
                .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + accessToken)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
    }

    @Test
    fun rejectsMissingOrInvalidBearerTokenForProtectedApi() {
        mockMvc.perform(get(PROTECTED_PATH))
            .andExpect(status().isUnauthorized())

        mockMvc.perform(
            get(PROTECTED_PATH)
                .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + INVALID_TOKEN)
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
            .andExpect(jsonPath("$.message", not("")))
            .andExpect(jsonPath("$.details", notNullValue()))
    }

    @Test
    fun rejectsRefreshTokenForProtectedApi() {
        val refreshToken = helper.register("alice_001", "Ace", PASSWORD).get("refresh_token").asString()

        mockMvc.perform(
            get(PROTECTED_PATH)
                .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + refreshToken)
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
            .andExpect(jsonPath("$.message", not("")))
            .andExpect(jsonPath("$.details", notNullValue()))
    }

    @Test
    fun logsInUserWithProfileAndAccessToken() {
        helper.register("alice_001", "Ace", PASSWORD)

        val body = mockMvc.perform(
            post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.loginJson("alice_001", PASSWORD))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.username").value("alice_001"))
            .andExpect(jsonPath("$.nickname").value("Ace"))
            .andExpect(jsonPath("$.created_at", notNullValue()))
            .andExpect(jsonPath("$.updated_at", notNullValue()))
            .andExpect(jsonPath("$.access_token", notNullValue()))
            .andExpect(jsonPath("$.refresh_token", notNullValue()))
            .andReturn()
            .response
            .contentAsString

        assertThat(body).doesNotContainIgnoringCase("password")
    }

    @Test
    fun rejectsInvalidLoginCredentials() {
        helper.register("alice_001", "Ace", PASSWORD)

        helper.assertInvalidCredentials(helper.loginJson("missing_001", PASSWORD))
        helper.assertInvalidCredentials(helper.loginJson("alice_001", "Wrongpass1!"))
    }

    @Test
    fun rejectsMissingOrBlankLoginCredentials() {
        helper.assertInvalidCredentialsWithoutBody()
        helper.assertInvalidCredentials(helper.loginJson(null, PASSWORD))
        helper.assertInvalidCredentials(helper.loginJson("alice_001", null))
        helper.assertInvalidCredentials(helper.loginJson(BLANK_CREDENTIAL, PASSWORD))
        helper.assertInvalidCredentials(helper.loginJson("alice_001", BLANK_CREDENTIAL))
    }

    @Test
    fun loginIsPublic() {
        helper.register("alice_001", "Ace", PASSWORD)

        mockMvc.perform(
            post(LOGIN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.loginJson("alice_001", PASSWORD))
        )
            .andExpect(status().isOk())
    }

    @Test
    fun issuesJwtForLoggedInUserWithSubjectAndConfiguredExpiry() {
        helper.register("alice_001", "Ace", PASSWORD)

        val response = helper.login("alice_001", PASSWORD)
        val accessToken = response.get("access_token").asString()
        val refreshToken = response.get("refresh_token").asString()
        val user: UserAccount = userRepository.findByUsername("alice_001").orElseThrow()

        assertThat(jwtService.subject(accessToken)).isEqualTo(user.id)
        assertThat(jwtService.subject(refreshToken)).isEqualTo(user.id)
        assertThat(jwtService.tokenId(accessToken)).isNotBlank()
        assertThat(jwtService.tokenId(refreshToken)).isNotBlank()
        assertThat(jwtService.tokenType(accessToken)).isEqualTo(JwtTokenType.ACCESS.value())
        assertThat(jwtService.tokenType(refreshToken)).isEqualTo(JwtTokenType.REFRESH.value())
        val tokenLifetime = Duration.between(
            jwtService.issuedAt(accessToken),
            jwtService.expiresAt(accessToken)
        )
        assertThat(tokenLifetime).isEqualTo(jwtProperties.accessTokenExpiration)
        val refreshTokenLifetime = Duration.between(
            jwtService.issuedAt(refreshToken),
            jwtService.expiresAt(refreshToken)
        )
        assertThat(refreshTokenLifetime).isEqualTo(jwtProperties.refreshTokenExpiration)
    }

    @Test
    fun refreshesTokenPairWithValidRefreshToken() {
        val loginResponse = helper.register("alice_001", "Ace", PASSWORD)
        val originalRefreshToken = loginResponse.get("refresh_token").asString()

        val refreshResponse = helper.refresh(originalRefreshToken)

        assertThat(refreshResponse.get("access_token").asString()).isNotBlank()
        assertThat(refreshResponse.get("refresh_token").asString()).isNotBlank()
        assertThat(jwtService.tokenId(refreshResponse.get("access_token").asString())).isNotBlank()
        assertThat(jwtService.tokenId(refreshResponse.get("refresh_token").asString())).isNotBlank()
        assertThat(refreshResponse.has("id")).isFalse()
        assertThat(refreshResponse.has("username")).isFalse()
        assertThat(refreshResponse.has("nickname")).isFalse()
        assertThat(refreshResponse.has("created_at")).isFalse()
        assertThat(refreshResponse.has("updated_at")).isFalse()

        val secondRefreshResponse = helper.refresh(originalRefreshToken)
        assertThat(secondRefreshResponse.get("access_token").asString()).isNotBlank()
        assertThat(secondRefreshResponse.get("refresh_token").asString()).isNotBlank()
    }

    @Test
    fun rejectsMissingBlankInvalidAndAccessTokenForRefresh() {
        val accessToken = helper.register("alice_001", "Ace", PASSWORD).get("access_token").asString()

        helper.assertInvalidRefreshTokenWithoutBody()
        helper.assertInvalidRefreshToken(helper.refreshJson(null))
        helper.assertInvalidRefreshToken(helper.refreshJson(BLANK_CREDENTIAL))
        helper.assertInvalidRefreshToken(helper.refreshJson(INVALID_TOKEN))
        helper.assertInvalidRefreshToken(helper.refreshJson(accessToken))
    }

    @Test
    fun rejectsExpiredRefreshToken() {
        val expiresAt = Instant.now().minus(jwtProperties.accessTokenExpiration)
        val issuedAt = expiresAt.minus(jwtProperties.refreshTokenExpiration)
        val expiredRefreshToken = helper.refreshToken(UNKNOWN_USER_ID, issuedAt, expiresAt)

        helper.assertInvalidRefreshToken(helper.refreshJson(expiredRefreshToken))
    }

    @Test
    fun rejectsRefreshTokenWhenUserDoesNotExist() {
        val refreshToken = helper.register("alice_001", "Ace", PASSWORD).get("refresh_token").asString()
        userRepository.deleteAll()

        helper.assertInvalidRefreshToken(helper.refreshJson(refreshToken))
    }

    @Test
    fun refreshEndpointIsPublic() {
        val registerResponse = helper.register("alice_001", "Ace", PASSWORD)
        val refreshToken = registerResponse.get("refresh_token").asString()

        mockMvc.perform(
            post(REFRESH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.refreshJson(refreshToken))
        )
            .andExpect(status().isOk())
    }

    @Test
    fun refreshIgnoresExpiredAccessTokenAuthorizationHeader() {
        val registerResponse = helper.register("alice_001", "Ace", PASSWORD)
        val refreshToken = registerResponse.get("refresh_token").asString()
        val expiresAt = Instant.now().minus(jwtProperties.accessTokenExpiration)
        val issuedAt = expiresAt.minus(jwtProperties.accessTokenExpiration)
        val expiredAccessToken = helper.accessToken(registerResponse.get("id").asString(), issuedAt, expiresAt)

        mockMvc.perform(
            post(REFRESH_PATH)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + expiredAccessToken
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.refreshJson(refreshToken))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token", notNullValue()))
            .andExpect(jsonPath("$.refresh_token", notNullValue()))
    }

    @Test
    fun logsOutCurrentTokenPair() {
        val response = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = response.get("access_token").asString()
        val refreshToken = response.get("refresh_token").asString()

        helper.logout(accessToken, refreshToken)
    }

    @Test
    fun logoutIsIdempotent() {
        val response = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = response.get("access_token").asString()
        val refreshToken = response.get("refresh_token").asString()

        helper.logout(accessToken, refreshToken)
        helper.logout(accessToken, refreshToken)
    }

    @Test
    fun rejectsInvalidAccessTokenForLogout() {
        val response = helper.register("alice_001", "Ace", PASSWORD)
        val refreshToken = response.get("refresh_token").asString()

        helper.assertUnauthorizedLogout(null, helper.logoutJson(refreshToken))
        helper.assertUnauthorizedLogout(INVALID_TOKEN, helper.logoutJson(refreshToken))
        helper.assertUnauthorizedLogout(refreshToken, helper.logoutJson(refreshToken))
    }

    @Test
    fun rejectsInvalidRefreshTokenForLogout() {
        val response = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = response.get("access_token").asString()

        helper.assertInvalidLogoutRefreshTokenWithoutBody(accessToken)
        helper.assertInvalidLogoutRefreshToken(accessToken, EMPTY_JSON)
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(null))
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(BLANK_CREDENTIAL))
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(INVALID_TOKEN))

        val expiresAt = Instant.now().minus(jwtProperties.accessTokenExpiration)
        val issuedAt = expiresAt.minus(jwtProperties.refreshTokenExpiration)
        val expiredRefreshToken = helper.refreshToken(UNKNOWN_USER_ID, issuedAt, expiresAt)
        helper.assertInvalidLogoutRefreshToken(accessToken, helper.logoutJson(expiredRefreshToken))
    }

    @Test
    fun rejectsLogoutWhenTokensBelongToDifferentUsers() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        val bob = helper.register("bob_0001", "Bee", PASSWORD)

        helper.assertInvalidLogoutRefreshToken(
            alice.get("access_token").asString(),
            helper.logoutJson(bob.get("refresh_token").asString())
        )
    }

    @Test
    fun revokedAccessTokenCannotAuthenticateProtectedApi() {
        val response = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = response.get("access_token").asString()
        val refreshToken = response.get("refresh_token").asString()

        helper.logout(accessToken, refreshToken)

        mockMvc.perform(
            get(PROTECTED_PATH)
                .header("Authorization", AuthConstants.BEARER_AUTHENTICATION_SCHEME + " " + accessToken)
        )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ApiErrorCatalog.code(ApiErrorKey.UNAUTHORIZED)))
            .andExpect(jsonPath("$.message", not("")))
            .andExpect(jsonPath("$.details", notNullValue()))
    }

    @Test
    fun revokedRefreshTokenCannotRefresh() {
        val response = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = response.get("access_token").asString()
        val refreshToken = response.get("refresh_token").asString()

        helper.logout(accessToken, refreshToken)

        helper.assertInvalidRefreshToken(helper.refreshJson(refreshToken))
    }

    @TestConfiguration
    class ProtectedTestEndpointConfiguration {
        @Bean
        fun protectedTestController() = ProtectedTestController()
    }

    @RestController
    class ProtectedTestController {
        @GetMapping(PROTECTED_PATH)
        fun protectedEndpoint(): Map<String, String> = mapOf("status" to "ok")
    }

    companion object {
        private val REGISTER_PATH: String = UserAuthRoutes.REGISTER_PATH
        private val LOGIN_PATH: String = UserAuthRoutes.LOGIN_PATH
        private val REFRESH_PATH: String = UserAuthRoutes.REFRESH_PATH
        private const val PROTECTED_PATH = "/api/test/protected"
        private const val EMPTY_JSON = "{}"
        private const val PASSWORD = "Password1!"
        private const val BLANK_CREDENTIAL = "   "
        private const val INVALID_TOKEN = "invalid-token"
        private const val UNKNOWN_USER_ID = "missing-user"
        private const val ULID_PATTERN = "[0-9A-HJKMNP-TV-Z]{26}"
    }
}
