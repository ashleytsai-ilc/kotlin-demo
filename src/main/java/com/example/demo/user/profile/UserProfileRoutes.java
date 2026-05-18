package com.example.demo.user.profile;

import com.example.demo.user.auth.UserAuthRoutes;

public final class UserProfileRoutes {

    public static final String BASE_PATH = UserAuthRoutes.BASE_PATH;
    public static final String ME_ROUTE = "/me";
    public static final String ME_PATH = BASE_PATH + ME_ROUTE;

    private UserProfileRoutes() {
    }
}
