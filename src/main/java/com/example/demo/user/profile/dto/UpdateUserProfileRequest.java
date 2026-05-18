package com.example.demo.user.profile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserProfileRequest {

    private boolean nicknameProvided;
    private String nickname;

    public boolean nicknameProvided() {
        return nicknameProvided;
    }

    public String nickname() {
        return nickname;
    }

    @JsonProperty("nickname")
    public void setNickname(String nickname) {
        this.nicknameProvided = true;
        this.nickname = nickname;
    }
}
