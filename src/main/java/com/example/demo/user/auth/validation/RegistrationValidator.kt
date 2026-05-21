package com.example.demo.user.auth.validation

import com.example.demo.common.ApiErrorCatalog
import com.example.demo.common.ApiErrorKey
import com.example.demo.common.ApiException
import com.example.demo.common.ErrorDetail
import com.example.demo.common.Strings.trimToNull
import com.example.demo.user.account.UserAccountConstraints
import com.example.demo.user.auth.dto.RegistrationRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class RegistrationValidator {
    fun validate(request: RegistrationRequest): ValidatedRegistration {
        val errors = mutableListOf<ErrorDetail>()
        val username = trimToNull(request.username)
        val nickname = trimToNull(request.nickname)
        val password = request.password

        if (username == null) {
            errors.add(RegistrationValidationError.USERNAME_REQUIRED.toDetail())
        } else {
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                errors.add(RegistrationValidationError.USERNAME_INVALID_CHARACTERS.toDetail())
            }
            if (username.length < RegistrationValidationRules.USERNAME_MIN_LENGTH) {
                errors.add(RegistrationValidationError.USERNAME_TOO_SHORT.toDetail())
            }
            if (username.length > UserAccountConstraints.USERNAME_MAX_LENGTH) {
                errors.add(RegistrationValidationError.USERNAME_TOO_LONG.toDetail())
            }
        }

        if (nickname != null && nickname.length > UserAccountConstraints.NICKNAME_MAX_LENGTH) {
            errors.add(RegistrationValidationError.NICKNAME_TOO_LONG.toDetail())
        }

        if (password == null || password.isBlank()) {
            errors.add(RegistrationValidationError.PASSWORD_REQUIRED.toDetail())
        } else {
            if (password.length < RegistrationValidationRules.PASSWORD_MIN_LENGTH) {
                errors.add(RegistrationValidationError.PASSWORD_TOO_SHORT.toDetail())
            }
            if (!UPPERCASE_PATTERN.matcher(password).find()
                || !LOWERCASE_PATTERN.matcher(password).find()
                || !DIGIT_PATTERN.matcher(password).find()
                || !SPECIAL_PATTERN.matcher(password).find()
            ) {
                errors.add(RegistrationValidationError.PASSWORD_WEAK.toDetail())
            }
        }

        if (errors.isNotEmpty()) {
            throw validationError(errors)
        }
        return ValidatedRegistration(username, nickname, password)
    }

    private fun validationError(errors: List<ErrorDetail>): ApiException =
        ApiErrorCatalog.exception(
            HttpStatus.BAD_REQUEST,
            ApiErrorKey.REGISTRATION_VALIDATION,
            errors
        )

    companion object {
        private val USERNAME_PATTERN: Pattern = Pattern.compile("^[A-Za-z0-9_]+$")
        private val UPPERCASE_PATTERN: Pattern = Pattern.compile("[A-Z]")
        private val LOWERCASE_PATTERN: Pattern = Pattern.compile("[a-z]")
        private val DIGIT_PATTERN: Pattern = Pattern.compile("\\d")
        private val SPECIAL_PATTERN: Pattern = Pattern.compile("[^A-Za-z0-9]")
    }
}
