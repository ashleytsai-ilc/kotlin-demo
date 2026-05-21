package com.example.demo.user.profile

import com.example.demo.user.auth.UserAuthRoutes

object UserProfileRoutes {
    const val BASE_PATH: String = UserAuthRoutes.BASE_PATH
    const val ME_ROUTE: String = "/me"
    const val ME_PATH: String = BASE_PATH + ME_ROUTE
}
