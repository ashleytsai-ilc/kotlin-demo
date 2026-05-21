package com.example.demo.user.profile;

import com.example.demo.user.profile.dto.UpdateUserProfileRequest;
import com.example.demo.user.profile.dto.UserProfileResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserProfileRoutes.BASE_PATH)
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PatchMapping(UserProfileRoutes.ME_ROUTE)
    UserProfileResponse updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateUserProfileRequest request
    ) {
        return userProfileService.updateCurrentUser(jwt.getSubject(), request);
    }
}
