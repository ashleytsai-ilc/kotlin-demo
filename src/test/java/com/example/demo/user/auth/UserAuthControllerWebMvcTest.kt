package com.example.demo.user.auth

import com.example.demo.user.auth.dto.LogoutRequest
import com.example.demo.user.auth.dto.RegistrationRequest
import com.example.demo.user.auth.dto.UserAuthResponse
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(controllers = [UserAuthController::class])
@AutoConfigureMockMvc(addFilters = false)
class UserAuthControllerWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userAuthService: UserAuthService

    @Test
    fun registerReturnsCreatedAuthResponseWithSnakeCaseJson() {
        given(userAuthService.register(RegistrationRequest(USERNAME, NICKNAME, PASSWORD)))
            .willReturn(authResponse())

        mockMvc.perform(
            post(UserAuthRoutes.REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"$USERNAME","nickname":"$NICKNAME","password":"$PASSWORD"}"""),
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(USER_ID))
            .andExpect(jsonPath("$.username").value(USERNAME))
            .andExpect(jsonPath("$.nickname").value(NICKNAME))
            .andExpect(jsonPath("$.created_at").value(NOW.toString()))
            .andExpect(jsonPath("$.updated_at").value(NOW.toString()))
            .andExpect(jsonPath("$.access_token").value(ACCESS_TOKEN))
            .andExpect(jsonPath("$.refresh_token").value(REFRESH_TOKEN))
            .andExpect(jsonPath("$.accessToken").doesNotExist())
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
    }

    @Test
    fun logoutReturnsNoContentAndPassesAuthorizationHeader() {
        mockMvc.perform(
            post(UserAuthRoutes.LOGOUT_PATH)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refresh_token":"$REFRESH_TOKEN"}"""),
        )
            .andExpect(status().isNoContent())

        verify(userAuthService).logout(AUTHORIZATION, LogoutRequest(REFRESH_TOKEN))
    }

    private fun authResponse(): UserAuthResponse =
        UserAuthResponse(
            USER_ID,
            USERNAME,
            NICKNAME,
            NOW,
            NOW,
            ACCESS_TOKEN,
            REFRESH_TOKEN,
        )

    companion object {
        private const val USER_ID = "01KTESTUSER000000000000000"
        private const val USERNAME = "alice_001"
        private const val NICKNAME = "Ace"
        private const val PASSWORD = "Password1!"
        private const val ACCESS_TOKEN = "access-token"
        private const val REFRESH_TOKEN = "refresh-token"
        private const val AUTHORIZATION = "Bearer access-token"
        private val NOW: Instant = Instant.parse("2026-05-23T00:00:00Z")
    }
}
