package com.example.demo.user.auth

object UserAuthRoutes {
    const val BASE_PATH: String = "/api/users"
    const val REGISTER_ROUTE: String = "/register"
    const val LOGIN_ROUTE: String = "/login"
    const val LOGOUT_ROUTE: String = "/logout"
    const val REFRESH_ROUTE: String = "/tokens/refresh"
    const val REGISTER_PATH: String = BASE_PATH + REGISTER_ROUTE
    const val LOGIN_PATH: String = BASE_PATH + LOGIN_ROUTE
    const val LOGOUT_PATH: String = BASE_PATH + LOGOUT_ROUTE
    const val REFRESH_PATH: String = BASE_PATH + REFRESH_ROUTE
}
