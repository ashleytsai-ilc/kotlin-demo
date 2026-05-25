package com.example.demo.user.profile

import com.example.demo.user.profile.dto.UpdateUserProfileRequest
import com.example.demo.user.profile.dto.UserProfileResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Instant

@WebMvcTest(controllers = [UserProfileController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(UserProfileControllerSecurityTestConfig::class)
class UserProfileControllerWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userProfileService: UserProfileService

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun updateCurrentUserPassesJwtSubjectAndReturnsSnakeCaseJson() {
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt(USER_ID))
        given(userProfileService.updateCurrentUser(eq(USER_ID), any()))
            .willReturn(profileResponse())

        mockMvc.perform(
            patch(UserProfileRoutes.ME_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nickname":"Captain"}"""),
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(USER_ID))
            .andExpect(jsonPath("$.username").value(USERNAME))
            .andExpect(jsonPath("$.nickname").value(NICKNAME))
            .andExpect(jsonPath("$.created_at").value(NOW.toString()))
            .andExpect(jsonPath("$.updated_at").value(NOW.toString()))
            .andExpect(jsonPath("$.createdAt").doesNotExist())
            .andExpect(jsonPath("$.updatedAt").doesNotExist())

        val requestCaptor = argumentCaptor<UpdateUserProfileRequest>()
        verify(userProfileService).updateCurrentUser(eq(USER_ID), requestCaptor.capture())
        assertTrue(requestCaptor.firstValue.nicknameProvided())
        assertEquals(NICKNAME, requestCaptor.firstValue.nickname())
    }

    private fun profileResponse(): UserProfileResponse =
        UserProfileResponse(
            USER_ID,
            USERNAME,
            NICKNAME,
            NOW,
            NOW,
        )

    private fun jwt(subject: String): Jwt =
        Jwt
            .withTokenValue("test-token")
            .header("alg", "none")
            .subject(subject)
            .issuedAt(NOW)
            .expiresAt(NOW.plusSeconds(3600))
            .build()

    companion object {
        private const val USER_ID = "01KTESTUSER000000000000000"
        private const val USERNAME = "alice_001"
        private const val NICKNAME = "Captain"
        private val NOW: Instant = Instant.parse("2026-05-23T00:00:00Z")
    }
}

@TestConfiguration
class UserProfileControllerSecurityTestConfig : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(AuthenticationPrincipalArgumentResolver())
    }
}
