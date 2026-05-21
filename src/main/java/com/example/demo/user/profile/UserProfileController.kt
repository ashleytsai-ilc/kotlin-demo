package com.example.demo.user.profile

import com.example.demo.user.profile.dto.UpdateUserProfileRequest
import com.example.demo.user.profile.dto.UserProfileResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserProfileRoutes.BASE_PATH)
class UserProfileController(
    private val userProfileService: UserProfileService,
) {
    @PatchMapping(UserProfileRoutes.ME_ROUTE)
    fun updateCurrentUser(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UpdateUserProfileRequest,
    ): UserProfileResponse = userProfileService.updateCurrentUser(jwt.subject, request)
}
