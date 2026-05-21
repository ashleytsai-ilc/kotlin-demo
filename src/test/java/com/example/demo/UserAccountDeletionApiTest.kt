package com.example.demo

import com.example.demo.auth.jwt.JwtService
import com.example.demo.auth.revocation.RevokedTokenRepository
import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.user.account.UserRepository
import com.example.demo.user.auth.UserAuthRoutes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
internal class UserAccountDeletionApiTest @Autowired constructor(
    private val mockMvc: MockMvc,
    objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val revokedTokenRepository: RevokedTokenRepository,
    jwtEncoder: JwtEncoder,
    private val jwtService: JwtService
) {
    private val helper = ApiTestHelper(mockMvc, objectMapper, jwtEncoder)

    @BeforeEach
    fun setUp() {
        revokedTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @Throws(Exception::class)
    fun deletesCurrentUserSoftlyAndRevokesSubmittedTokens() {
        val registration = helper.register("alice_001", "Ace", PASSWORD)
        val userId = registration.get("id").asString()
        val accessToken = registration.get(ACCESS_TOKEN_FIELD).asString()
        val refreshToken = registration.get(REFRESH_TOKEN_FIELD).asString()

        helper.deleteAccount(accessToken, helper.accountDeletionJson(PASSWORD, refreshToken))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))

        val deletedUser = userRepository.findById(userId).orElseThrow()
        assertThat(deletedUser.deletedAt).isNotNull()
        assertThat(deletedUser.username).isEqualTo("alice_001")
        assertThat(deletedUser.nickname).isEqualTo("Ace")
        assertThat(deletedUser.activeUsernameKey).isNull()
        assertThat(deletedUser.activeNicknameKey).isNull()
        assertThat(revokedTokenRepository.existsById(jwtService.tokenId(accessToken))).isTrue()
        assertThat(revokedTokenRepository.existsById(jwtService.tokenId(refreshToken))).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun rejectsInvalidAccessTokenCasesForAccountDeletion() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = alice.get(ACCESS_TOKEN_FIELD).asString()
        val refreshToken = alice.get(REFRESH_TOKEN_FIELD).asString()
        val body = helper.accountDeletionJson(PASSWORD, refreshToken)

        helper.assertUnauthorizedDeletion(null, body)
        helper.assertUnauthorizedDeletion(INVALID_TOKEN, body)
        helper.assertUnauthorizedDeletion(refreshToken, body)

        val replacementPair = helper.login("alice_001", PASSWORD)
        helper.logout(accessToken, refreshToken)
        helper.assertUnauthorizedDeletion(accessToken, helper.accountDeletionJson(PASSWORD, refreshToken))

        val bob = helper.register("bob_0001", "Bee", PASSWORD)
        val bobSecondPair = helper.login("bob_0001", PASSWORD)
        helper.deleteAccount(
            bob.get(ACCESS_TOKEN_FIELD).asString(),
            helper.accountDeletionJson(PASSWORD, bob.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent())

        helper.assertUnauthorizedDeletion(
            bobSecondPair.get(ACCESS_TOKEN_FIELD).asString(),
            helper.accountDeletionJson(PASSWORD, bobSecondPair.get(REFRESH_TOKEN_FIELD).asString())
        )
        helper.assertUnauthorizedDeletion(bobSecondPair.get(ACCESS_TOKEN_FIELD).asString(), EMPTY_JSON)
        assertThat(replacementPair.get(ACCESS_TOKEN_FIELD).asString()).isNotBlank()
    }

    @Test
    @Throws(Exception::class)
    fun rejectsMissingBodyAndInvalidPasswordWithoutDeletingUser() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = alice.get(ACCESS_TOKEN_FIELD).asString()
        val refreshToken = alice.get(REFRESH_TOKEN_FIELD).asString()

        helper.assertInvalidJsonDeletionWithoutBody(accessToken)

        helper.assertInvalidAccountDeletionCredentials(accessToken, EMPTY_JSON)
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(null, refreshToken))
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(BLANK_CREDENTIAL, refreshToken)
        )
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(WRONG_PASSWORD, refreshToken)
        )

        val user = userRepository.findById(alice.get("id").asString()).orElseThrow()
        assertThat(user.deletedAt).isNull()
        assertThat(user.activeUsernameKey).isEqualTo("alice_001")
        assertThat(user.activeNicknameKey).isEqualTo("Ace")
    }

    @Test
    @Throws(Exception::class)
    fun rejectsInvalidRefreshTokenCasesAsInvalidCredentialsForAccountDeletion() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        val aliceSecondPair = helper.login("alice_001", PASSWORD)
        val bob = helper.register("bob_0001", "Bee", PASSWORD)
        val accessToken = aliceSecondPair.get(ACCESS_TOKEN_FIELD).asString()

        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.passwordOnlyJson(PASSWORD))
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(PASSWORD, null))
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(PASSWORD, BLANK_CREDENTIAL)
        )
        helper.assertInvalidAccountDeletionCredentials(accessToken, helper.accountDeletionJson(PASSWORD, INVALID_TOKEN))
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(PASSWORD, alice.get(ACCESS_TOKEN_FIELD).asString())
        )
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(PASSWORD, expiredRefreshToken())
        )

        helper.logout(alice.get(ACCESS_TOKEN_FIELD).asString(), alice.get(REFRESH_TOKEN_FIELD).asString())
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(PASSWORD, alice.get(REFRESH_TOKEN_FIELD).asString())
        )
        helper.assertInvalidAccountDeletionCredentials(
            accessToken,
            helper.accountDeletionJson(PASSWORD, bob.get(REFRESH_TOKEN_FIELD).asString())
        )
    }

    @Test
    @Throws(Exception::class)
    fun deletedUserCannotUseExistingAuthAndProfileFlows() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        val secondPair = helper.login("alice_001", PASSWORD)

        helper.deleteAccount(
            alice.get(ACCESS_TOKEN_FIELD).asString(),
            helper.accountDeletionJson(PASSWORD, alice.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent())

        helper.assertInvalidCredentials(helper.loginJson("alice_001", PASSWORD))
        helper.assertInvalidRefreshToken(helper.refreshJson(alice.get(REFRESH_TOKEN_FIELD).asString()))
        helper.assertUnauthorizedLogout(
            alice.get(ACCESS_TOKEN_FIELD).asString(),
            helper.logoutJson(alice.get(REFRESH_TOKEN_FIELD).asString())
        )
        helper.assertUnauthorizedProfileUpdate(
            secondPair.get(ACCESS_TOKEN_FIELD).asString(),
            helper.profileJson("Captain")
        )
        helper.assertUnauthorizedProfileUpdate(secondPair.get(ACCESS_TOKEN_FIELD).asString(), EMPTY_JSON)
    }

    @Test
    @Throws(Exception::class)
    fun deletedUsernameAndNicknameCanBeReusedButActiveValuesRemainUnique() {
        val deletedAlice = helper.register("alice_001", "Ace", PASSWORD)
        helper.deleteAccount(
            deletedAlice.get(ACCESS_TOKEN_FIELD).asString(),
            helper.accountDeletionJson(PASSWORD, deletedAlice.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent())

        helper.register("alice_001", "Ace", PASSWORD)

        mockMvc.perform(
            post(UserAuthRoutes.REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("alice_001", "Bee", PASSWORD))
        )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.USERNAME_ALREADY_EXISTS))
            )
        mockMvc.perform(
            post(UserAuthRoutes.REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.registrationJson("bob_0001", "Ace", PASSWORD))
        )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS))
            )
    }

    @Test
    @Throws(Exception::class)
    fun deletedNicknameCanBeReusedOnProfileUpdateButActiveNicknameRemainsUnique() {
        val deletedAlice = helper.register("alice_001", "Ace", PASSWORD)
        val bob = helper.register("bob_0001", "Bee", PASSWORD)
        val chris = helper.register("chris_01", "Cee", PASSWORD)
        helper.deleteAccount(
            deletedAlice.get(ACCESS_TOKEN_FIELD).asString(),
            helper.accountDeletionJson(PASSWORD, deletedAlice.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent())

        helper.patchProfile(bob.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Ace"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("Ace"))
            .andExpect(jsonPath("$.deleted_at").doesNotExist())
            .andExpect(jsonPath("$.active_username_key").doesNotExist())
            .andExpect(jsonPath("$.active_nickname_key").doesNotExist())

        helper.patchProfile(chris.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Ace"))
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS))
            )
    }

    @Test
    @Throws(Exception::class)
    fun formalUserResponsesDoNotExposeDeletionOrActiveKeyFields() {
        val registration = helper.register("alice_001", "Ace", PASSWORD)
        assertNoDeletionOrActiveKeyFields(registration)

        val login = helper.login("alice_001", PASSWORD)
        assertNoDeletionOrActiveKeyFields(login)

        val refresh = helper.refresh(login.get(REFRESH_TOKEN_FIELD).asString())
        assertNoDeletionOrActiveKeyFields(refresh)

        val profileResponse =
            helper.patchProfile(login.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Captain"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString
        assertThat(profileResponse)
            .doesNotContain("deleted_at", "active_username_key", "active_nickname_key")
    }

    private fun expiredRefreshToken(): String {
        val expiresAt = Instant.now().minusSeconds(60)
        val issuedAt = expiresAt.minusSeconds(60)
        return helper.refreshToken(UNKNOWN_USER_ID, issuedAt, expiresAt)
    }

    private fun assertNoDeletionOrActiveKeyFields(response: JsonNode) {
        assertThat(response.has("deleted_at")).isFalse()
        assertThat(response.has("active_username_key")).isFalse()
        assertThat(response.has("active_nickname_key")).isFalse()
    }

    companion object {
        private const val PASSWORD = "Password1!"
        private const val WRONG_PASSWORD = "Wrongpass1!"
        private const val BLANK_CREDENTIAL = "   "
        private const val INVALID_TOKEN = "invalid-token"
        private const val UNKNOWN_USER_ID = "missing-user"
        private const val EMPTY_JSON = "{}"
        private const val ACCESS_TOKEN_FIELD = "access_token"
        private const val REFRESH_TOKEN_FIELD = "refresh_token"
    }
}
