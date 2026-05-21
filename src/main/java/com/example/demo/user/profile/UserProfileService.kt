package com.example.demo.user.profile

import com.example.demo.common.ApiErrorKey
import com.example.demo.common.ApiExceptions
import com.example.demo.common.ApiExceptions.conflict
import com.example.demo.common.ErrorField
import com.example.demo.user.account.UserAccount
import com.example.demo.user.account.UserRepository
import com.example.demo.user.profile.dto.UpdateUserProfileRequest
import com.example.demo.user.profile.dto.UserProfileResponse
import com.example.demo.user.profile.validation.UserProfileValidator
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserProfileService(
    private val userRepository: UserRepository,
    private val userProfileValidator: UserProfileValidator,
) {
    @Transactional
    fun updateCurrentUser(
        userId: String?,
        request: UpdateUserProfileRequest,
    ): UserProfileResponse {
        val user =
            userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow { ApiExceptions.unauthorized() }
        val validated = userProfileValidator.validate(request)

        if (user.nickname == validated.nickname) {
            return toResponse(user)
        }

        if (validated.nickname != null && nicknameBelongsToAnotherUser(validated.nickname, user.id)) {
            throw conflict(ApiErrorKey.NICKNAME_ALREADY_EXISTS, ErrorField.NICKNAME)
        }

        user.updateNickname(validated.nickname)
        try {
            return toResponse(userRepository.saveAndFlush(user))
        } catch (ex: DataIntegrityViolationException) {
            throw conflict(ApiErrorKey.USER_PROFILE_CONFLICT, ErrorField.USER)
        }
    }

    private fun nicknameBelongsToAnotherUser(
        nickname: String,
        userId: String?,
    ): Boolean {
        val existing = userRepository.findByActiveNicknameKey(nickname).orElse(null)
        return existing != null && existing.id != userId
    }

    private fun toResponse(user: UserAccount): UserProfileResponse {
        val id = checkNotNull(user.id)
        return UserProfileResponse(
            id,
            checkNotNull(user.username),
            user.nickname,
            checkNotNull(user.createdAt),
            checkNotNull(user.updatedAt),
        )
    }
}
