package com.example.demo.user.account

import com.github.f4b6a3.ulid.UlidCreator
import org.springframework.stereotype.Component

@Component
class UlidGenerator {
    fun next(): String = UlidCreator.getMonotonicUlid().toString()
}
