package com.example.demo.user.profile.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateUserProfileRequest {
    private var nicknameProvided = false
    private var nickname: String? = null

    fun nicknameProvided(): Boolean = nicknameProvided

    fun nickname(): String? = nickname

    @JsonProperty("nickname")
    fun setNickname(nickname: String?) {
        this.nicknameProvided = true
        this.nickname = nickname
    }
}
