package com.example.demo.user.account

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserAccount, String> {
    fun findByUsername(username: String?): Optional<UserAccount>

    fun findByUsernameAndDeletedAtIsNull(username: String?): Optional<UserAccount>

    fun findByIdAndDeletedAtIsNull(id: String?): Optional<UserAccount>

    fun findByActiveNicknameKey(activeNicknameKey: String?): Optional<UserAccount>

    fun existsByActiveUsernameKey(activeUsernameKey: String?): Boolean

    fun existsByActiveNicknameKey(activeNicknameKey: String?): Boolean
}
