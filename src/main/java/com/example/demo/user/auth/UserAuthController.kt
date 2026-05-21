package com.example.demo.user.auth;

import com.example.demo.user.auth.dto.LoginRequest;
import com.example.demo.user.auth.dto.LogoutRequest;
import com.example.demo.user.auth.dto.RefreshTokenRequest;
import com.example.demo.user.auth.dto.RegistrationRequest;
import com.example.demo.user.auth.dto.TokenPairResponse;
import com.example.demo.user.auth.dto.UserAuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthRoutes.BASE_PATH)
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping(UserAuthRoutes.REGISTER_ROUTE)
    @ResponseStatus(HttpStatus.CREATED)
    UserAuthResponse register(@RequestBody RegistrationRequest request) {
        return userAuthService.register(request);
    }

    @PostMapping(UserAuthRoutes.LOGIN_ROUTE)
    UserAuthResponse login(@RequestBody LoginRequest request) {
        return userAuthService.login(request);
    }

    @PostMapping(UserAuthRoutes.LOGOUT_ROUTE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody LogoutRequest request
    ) {
        userAuthService.logout(authorization, request);
    }

    @PostMapping(UserAuthRoutes.REFRESH_ROUTE)
    TokenPairResponse refresh(@RequestBody RefreshTokenRequest request) {
        return userAuthService.refresh(request);
    }
}
