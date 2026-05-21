package com.example.demo.user.profile.validation

import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.common.ApiException
import com.example.demo.common.ErrorDetail
import com.example.demo.common.Strings.trimToNull
import com.example.demo.user.account.UserAccountConstraints
import com.example.demo.user.profile.dto.UpdateUserProfileRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserProfileValidator {
    fun validate(request: UpdateUserProfileRequest): ValidatedUserProfileUpdate {
        val errors = mutableListOf<ErrorDetail>()
        if (!request.nicknameProvided() || request.nickname() == null) {
            errors.add(UserProfileValidationError.NICKNAME_REQUIRED.toDetail())
            throw validationError(errors)
        }

        val nickname = trimToNull(request.nickname())
        if (nickname != null && nickname.length > UserAccountConstraints.NICKNAME_MAX_LENGTH) {
            errors.add(UserProfileValidationError.NICKNAME_TOO_LONG.toDetail())
        }

        if (errors.isNotEmpty()) {
            throw validationError(errors)
        }
        return ValidatedUserProfileUpdate(nickname)
    }

    private fun validationError(errors: List<ErrorDetail>): ApiException =
        ApiErrorCatalog.exception(
            HttpStatus.BAD_REQUEST,
            ApiErrorKey.USER_PROFILE_VALIDATION,
            errors
        )
}
