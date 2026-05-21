package com.example.demo

import com.example.demo.auth.jwt.JwtService
import com.example.demo.auth.revocation.RevokedTokenRepository
import com.example.demo.user.account.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
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
internal class PocDataInspectionApiTest @Autowired constructor(
    mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val revokedTokenRepository: RevokedTokenRepository,
    private val jwtService: JwtService,
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
    fun listsAllUsersWithoutAuthentication() {
        helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD)
        helper.register(BOB_USERNAME, BOB_NICKNAME, PASSWORD)

        val body = helper.getPocUsers()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_TWO_ITEM_COUNT))
            .andReturn()
            .response
            .contentAsString

        val alice = helper.findItem(objectMapper.readTree(body), USERNAME_FIELD, ALICE_USERNAME)
        assertThat(alice.get(NICKNAME_FIELD).asString()).isEqualTo(ALICE_NICKNAME)
        assertThat(alice.get(PASSWORD_HASH_FIELD).asString()).isNotBlank()
        assertThat(alice.get(ID_FIELD).asString()).isNotBlank()
        assertThat(alice.get(CREATED_AT_FIELD).asString()).isNotBlank()
        assertThat(alice.get(UPDATED_AT_FIELD).asString()).isNotBlank()
    }

    @Test
    @Throws(Exception::class)
    fun userInspectionIncludesDeletionAndActiveUniqueKeyState() {
        helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD)
        helper.register(BOB_USERNAME, null, PASSWORD)
        val chris = helper.register(CHRIS_USERNAME, CHRIS_NICKNAME, PASSWORD)
        helper.deleteAccount(
            chris.get(ACCESS_TOKEN_FIELD).asString(),
            helper.accountDeletionJson(PASSWORD, chris.get(REFRESH_TOKEN_FIELD).asString())
        ).andExpect(status().isNoContent())

        val body = helper.getPocUsers()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_THREE_ITEM_COUNT))
            .andReturn()
            .response
            .contentAsString

        val users = objectMapper.readTree(body)
        val alice = helper.findItem(users, USERNAME_FIELD, ALICE_USERNAME)
        assertThat(alice.has(DELETED_AT_FIELD)).isTrue()
        assertThat(alice.get(DELETED_AT_FIELD).isNull()).isTrue()
        assertThat(alice.get(ACTIVE_USERNAME_KEY_FIELD).asString()).isEqualTo(ALICE_USERNAME)
        assertThat(alice.get(ACTIVE_NICKNAME_KEY_FIELD).asString()).isEqualTo(ALICE_NICKNAME)

        val bob = helper.findItem(users, USERNAME_FIELD, BOB_USERNAME)
        assertThat(bob.has(NICKNAME_FIELD)).isTrue()
        assertThat(bob.get(NICKNAME_FIELD).isNull()).isTrue()
        assertThat(bob.get(ACTIVE_USERNAME_KEY_FIELD).asString()).isEqualTo(BOB_USERNAME)
        assertThat(bob.has(ACTIVE_NICKNAME_KEY_FIELD)).isTrue()
        assertThat(bob.get(ACTIVE_NICKNAME_KEY_FIELD).isNull()).isTrue()

        val deletedChris = helper.findItem(users, USERNAME_FIELD, CHRIS_USERNAME)
        assertThat(deletedChris.get(DELETED_AT_FIELD).asString()).isNotBlank()
        assertThat(deletedChris.has(ACTIVE_USERNAME_KEY_FIELD)).isTrue()
        assertThat(deletedChris.get(ACTIVE_USERNAME_KEY_FIELD).isNull()).isTrue()
        assertThat(deletedChris.has(ACTIVE_NICKNAME_KEY_FIELD)).isTrue()
        assertThat(deletedChris.get(ACTIVE_NICKNAME_KEY_FIELD).isNull()).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun userInspectionReturnsEmptyCollection() {
        helper.getPocUsers()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_EMPTY_COUNT))
    }

    @Test
    @Throws(Exception::class)
    fun listsAllRevokedTokensWithoutAuthentication() {
        val response = helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD)
        val accessToken = response.get(ACCESS_TOKEN_FIELD).asString()
        val refreshToken = response.get(REFRESH_TOKEN_FIELD).asString()
        helper.logout(accessToken, refreshToken)

        val body = helper.getPocRevokedTokens()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_TWO_ITEM_COUNT))
            .andReturn()
            .response
            .contentAsString

        val revokedToken = objectMapper.readTree(body).get(0)
        assertThat(revokedToken.get(TOKEN_ID_FIELD).asString()).isNotBlank()
        assertThat(revokedToken.get(USER_ID_FIELD).asString()).isEqualTo(response.get(ID_FIELD).asString())
        assertThat(revokedToken.get(TOKEN_TYPE_FIELD).asString()).isNotBlank()
        assertThat(revokedToken.get(EXPIRES_AT_FIELD).asString()).isNotBlank()
        assertThat(revokedToken.get(REVOKED_AT_FIELD).asString()).isNotBlank()
    }

    @Test
    @Throws(Exception::class)
    fun revokedTokenInspectionReturnsEmptyCollection() {
        helper.getPocRevokedTokens()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_EMPTY_COUNT))
    }

    @Test
    @Throws(Exception::class)
    fun inspectionIgnoresAuthorizationHeader() {
        helper.getPocUsersWithBearer(INVALID_TOKEN)
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_EMPTY_COUNT))

        helper.getPocRevokedTokensWithBearer(INVALID_TOKEN)
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_EMPTY_COUNT))
    }

    @Test
    @Throws(Exception::class)
    fun inspectionDoesNotChangeExistingAuthContracts() {
        val registration = helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD)
        val login = helper.login(ALICE_USERNAME, PASSWORD)
        val refresh = helper.refresh(login.get(REFRESH_TOKEN_FIELD).asString())

        helper.logout(login.get(ACCESS_TOKEN_FIELD).asString(), login.get(REFRESH_TOKEN_FIELD).asString())

        assertThat(registration.get(USERNAME_FIELD).asString()).isEqualTo(ALICE_USERNAME)
        assertThat(login.get(ACCESS_TOKEN_FIELD).asString()).isNotBlank()
        assertThat(refresh.get(ACCESS_TOKEN_FIELD).asString()).isNotBlank()
        assertThat(refresh.has(USERNAME_FIELD)).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun inspectedPasswordHashMatchesPersistedUserState() {
        helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD)
        val user = userRepository.findByUsername(ALICE_USERNAME).orElseThrow()

        val body = helper.getPocUsers()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_SINGLE_ITEM_COUNT))
            .andReturn()
            .response
            .contentAsString

        val inspected = objectMapper.readTree(body).get(0)
        assertThat(inspected.get(PASSWORD_HASH_FIELD).asString()).isEqualTo(user.passwordHash)
        assertThat(passwordEncoder.matches(PASSWORD, inspected.get(PASSWORD_HASH_FIELD).asString())).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun inspectedRevokedTokensMatchPersistedTokenState() {
        val response = helper.register(ALICE_USERNAME, ALICE_NICKNAME, PASSWORD)
        val accessToken = response.get(ACCESS_TOKEN_FIELD).asString()
        val refreshToken = response.get(REFRESH_TOKEN_FIELD).asString()
        helper.logout(accessToken, refreshToken)

        val body = helper.getPocRevokedTokens()
            .andExpect(status().isOk())
            .andExpect(hasItemCount(EXPECTED_TWO_ITEM_COUNT))
            .andReturn()
            .response
            .contentAsString

        val revokedTokens = objectMapper.readTree(body)
        assertThat(revokedTokens.get(0).get(TOKEN_ID_FIELD).asString())
            .isIn(jwtService.tokenId(accessToken), jwtService.tokenId(refreshToken))
        assertThat(revokedTokens.get(1).get(TOKEN_ID_FIELD).asString())
            .isIn(jwtService.tokenId(accessToken), jwtService.tokenId(refreshToken))
    }

    private fun hasItemCount(expectedCount: Int) =
        jsonPath<MutableCollection<*>>("$", hasSize<Any?>(expectedCount))

    companion object {
        private const val PASSWORD = "Password1!"
        private const val INVALID_TOKEN = "invalid-token"
        private const val ALICE_USERNAME = "alice_001"
        private const val ALICE_NICKNAME = "Ace"
        private const val BOB_USERNAME = "bob_0001"
        private const val BOB_NICKNAME = "Bee"
        private const val CHRIS_USERNAME = "chris_01"
        private const val CHRIS_NICKNAME = "Cee"
        private const val ID_FIELD = "id"
        private const val USERNAME_FIELD = "username"
        private const val NICKNAME_FIELD = "nickname"
        private const val PASSWORD_HASH_FIELD = "password_hash"
        private const val CREATED_AT_FIELD = "created_at"
        private const val UPDATED_AT_FIELD = "updated_at"
        private const val DELETED_AT_FIELD = "deleted_at"
        private const val ACTIVE_USERNAME_KEY_FIELD = "active_username_key"
        private const val ACTIVE_NICKNAME_KEY_FIELD = "active_nickname_key"
        private const val ACCESS_TOKEN_FIELD = "access_token"
        private const val REFRESH_TOKEN_FIELD = "refresh_token"
        private const val TOKEN_ID_FIELD = "token_id"
        private const val USER_ID_FIELD = "user_id"
        private const val TOKEN_TYPE_FIELD = "token_type"
        private const val EXPIRES_AT_FIELD = "expires_at"
        private const val REVOKED_AT_FIELD = "revoked_at"
        private const val EXPECTED_EMPTY_COUNT = 0
        private const val EXPECTED_SINGLE_ITEM_COUNT = 1
        private const val EXPECTED_TWO_ITEM_COUNT = 2
        private const val EXPECTED_THREE_ITEM_COUNT = 3
    }
}
