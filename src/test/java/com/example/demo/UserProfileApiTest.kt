package com.example.demo

import com.example.demo.auth.revocation.RevokedTokenRepository
import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.user.account.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
internal class UserProfileApiTest @Autowired constructor(
    mockMvc: MockMvc,
    objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val revokedTokenRepository: RevokedTokenRepository,
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
    @Throws(Exception::class)
    fun updatesNicknameAndReturnsProfileFields() {
        val registration = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = registration.get(ACCESS_TOKEN_FIELD).asString()

        val body = helper.patchProfile(accessToken, helper.profileJson("Captain"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(registration.get("id").asString()))
            .andExpect(jsonPath("$.username").value("alice_001"))
            .andExpect(jsonPath("$.nickname").value("Captain"))
            .andExpect(jsonPath<Any>("$.created_at", notNullValue()))
            .andExpect(jsonPath<Any>("$.updated_at", notNullValue()))
            .andReturn()
            .response
            .contentAsString

        assertThat(body).doesNotContainIgnoringCase("password")
        assertThat(body).doesNotContain(ACCESS_TOKEN_FIELD, REFRESH_TOKEN_FIELD)
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname)
            .isEqualTo("Captain")
    }

    @Test
    @Throws(Exception::class)
    fun updatesOnlyTheBearerTokenUser() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        helper.register("bob_0001", "Bee", PASSWORD)

        helper.patchProfile(alice.get(ACCESS_TOKEN_FIELD).asString(), helper.profileJson("Captain"))
            .andExpect(status().isOk())

        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname)
            .isEqualTo("Captain")
        assertThat(userRepository.findByUsername("bob_0001").orElseThrow().nickname).isEqualTo("Bee")
    }

    @Test
    @Throws(Exception::class)
    fun rejectsMissingInvalidRefreshRevokedAndMissingSubjectAccessTokens() {
        val registration = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = registration.get(ACCESS_TOKEN_FIELD).asString()
        val refreshToken = registration.get(REFRESH_TOKEN_FIELD).asString()

        helper.assertUnauthorizedProfileUpdate(null, helper.profileJson("Captain"))
        helper.assertUnauthorizedProfileUpdate(INVALID_TOKEN, helper.profileJson("Captain"))
        helper.assertUnauthorizedProfileUpdate(refreshToken, helper.profileJson("Captain"))

        helper.logout(accessToken, refreshToken)
        helper.assertUnauthorizedProfileUpdate(accessToken, helper.profileJson("Captain"))

        val bob = helper.register("bob_0001", "Bee", PASSWORD)
        val deletedUserToken = bob.get(ACCESS_TOKEN_FIELD).asString()
        userRepository.deleteById(bob.get("id").asString())
        helper.assertUnauthorizedProfileUpdate(deletedUserToken, helper.profileJson("Captain"))
    }

    @Test
    @Throws(Exception::class)
    fun ignoresUnknownAndImmutableFields() {
        val registration = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = registration.get(ACCESS_TOKEN_FIELD).asString()
        val before = userRepository.findByUsername("alice_001").orElseThrow()
        val originalPasswordHash = before.passwordHash
        val body = linkedMapOf<String, Any?>()
        body["id"] = "different-id"
        body["username"] = "mallory_1"
        body["password"] = "NewPassword1!"
        body[ACCESS_TOKEN_FIELD] = "new-access"
        body[REFRESH_TOKEN_FIELD] = "new-refresh"
        body["unknown_field"] = "ignored"
        body["nickname"] = "Captain"

        val response = helper.patchProfile(accessToken, helper.json(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(registration.get("id").asString()))
            .andExpect(jsonPath("$.username").value("alice_001"))
            .andExpect(jsonPath("$.nickname").value("Captain"))
            .andReturn()
            .response
            .contentAsString

        val after = userRepository.findById(registration.get("id").asString()).orElseThrow()
        assertThat(after.username).isEqualTo("alice_001")
        assertThat(after.passwordHash).isEqualTo(originalPasswordHash)
        assertThat(passwordEncoder.matches("NewPassword1!", after.passwordHash)).isFalse()
        assertThat(response).doesNotContainIgnoringCase("password")
        assertThat(response).doesNotContain(ACCESS_TOKEN_FIELD, REFRESH_TOKEN_FIELD, "unknown_field")
    }

    @Test
    @Throws(Exception::class)
    fun handlesMissingClearedAndRequiredNicknameRequests() {
        val registration = helper.register("alice_001", "Ace", PASSWORD)
        val accessToken = registration.get(ACCESS_TOKEN_FIELD).asString()

        helper.assertInvalidJsonProfileUpdateWithoutBody(accessToken)

        helper.patchProfile(accessToken, EMPTY_JSON)
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.USER_PROFILE_VALIDATION))
            )
            .andExpect(jsonPath<Any>("$.details", notNullValue()))
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname).isEqualTo("Ace")

        helper.patchProfile(accessToken, helper.profileJsonWithNullNickname())
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.USER_PROFILE_VALIDATION))
            )
            .andExpect(jsonPath<Any>("$.details", notNullValue()))
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname).isEqualTo("Ace")

        helper.patchProfile(accessToken, helper.profileJson(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value(""))
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname).isNull()

        helper.patchProfile(accessToken, helper.profileJson("Ace"))
            .andExpect(status().isOk())
        helper.patchProfile(accessToken, helper.profileJson("   "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value(""))
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname).isNull()

        helper.patchProfile(accessToken, helper.profileJson("Ace"))
            .andExpect(status().isOk())
    }

    @Test
    @Throws(Exception::class)
    fun validatesNicknameLengthUniquenessNoOpAndMultipleUsersWithoutNickname() {
        val alice = helper.register("alice_001", "Ace", PASSWORD)
        helper.register("bob_0001", "Bee", PASSWORD)
        val accessToken = alice.get(ACCESS_TOKEN_FIELD).asString()

        helper.patchProfile(accessToken, helper.profileJson("1234567890123456789012345678901"))
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.USER_PROFILE_VALIDATION))
            )
            .andExpect(
                jsonPath("$.message")
                    .value(ApiErrorCatalog.message(ApiErrorKey.USER_PROFILE_VALIDATION))
            )
            .andExpect(jsonPath<Any>("$.details", notNullValue()))
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname).isEqualTo("Ace")

        helper.patchProfile(accessToken, helper.profileJson("Bee"))
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value(ApiErrorCatalog.code(ApiErrorKey.NICKNAME_ALREADY_EXISTS))
            )
            .andExpect(jsonPath<String>("$.message", not("")))
            .andExpect(jsonPath<Any>("$.details", notNullValue()))
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().nickname).isEqualTo("Ace")

        val beforeSameNickname = userRepository.findByUsername("alice_001").orElseThrow()
        helper.patchProfile(accessToken, helper.profileJson("Ace"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("Ace"))
            .andExpect(
                jsonPath("$.updated_at").value(beforeSameNickname.updatedAt.toString())
            )
        assertThat(userRepository.findByUsername("alice_001").orElseThrow().updatedAt)
            .isEqualTo(beforeSameNickname.updatedAt)

        helper.patchProfile(accessToken, helper.profileJson(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value(""))
        val bobAccessToken = helper.login("bob_0001", PASSWORD).get(ACCESS_TOKEN_FIELD).asString()
        helper.patchProfile(bobAccessToken, helper.profileJson(""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value(""))
    }

    companion object {
        private const val PASSWORD = "Password1!"
        private const val INVALID_TOKEN = "invalid-token"
        private const val EMPTY_JSON = "{}"
        private const val ACCESS_TOKEN_FIELD = "access_token"
        private const val REFRESH_TOKEN_FIELD = "refresh_token"
    }
}
