package com.example.demo.user.auth;

public final class UserAuthRoutes {

    public static final String BASE_PATH = "/api/users";
    public static final String REGISTER_ROUTE = "/register";
    public static final String LOGIN_ROUTE = "/login";
    public static final String LOGOUT_ROUTE = "/logout";
    public static final String REFRESH_ROUTE = "/tokens/refresh";
    public static final String REGISTER_PATH = BASE_PATH + REGISTER_ROUTE;
    public static final String LOGIN_PATH = BASE_PATH + LOGIN_ROUTE;
    public static final String LOGOUT_PATH = BASE_PATH + LOGOUT_ROUTE;
    public static final String REFRESH_PATH = BASE_PATH + REFRESH_ROUTE;

    private UserAuthRoutes() {
    }
}
