package com.example.demo.user.auth.validation;

import com.example.demo.common.ApiErrorCatalog;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.common.ApiException;
import com.example.demo.common.ErrorDetail;
import com.example.demo.common.Strings;
import com.example.demo.user.account.UserAccountConstraints;
import com.example.demo.user.auth.dto.RegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RegistrationValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[^A-Za-z0-9]");

    public ValidatedRegistration validate(RegistrationRequest request) {
        List<ErrorDetail> errors = new ArrayList<>();
        String username = Strings.trimToNull(request.username());
        String nickname = Strings.trimToNull(request.nickname());
        String password = request.password();

        if (username == null) {
            errors.add(RegistrationValidationError.USERNAME_REQUIRED.toDetail());
        } else {
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                errors.add(RegistrationValidationError.USERNAME_INVALID_CHARACTERS.toDetail());
            }
            if (username.length() < RegistrationValidationRules.USERNAME_MIN_LENGTH) {
                errors.add(RegistrationValidationError.USERNAME_TOO_SHORT.toDetail());
            }
            if (username.length() > UserAccountConstraints.USERNAME_MAX_LENGTH) {
                errors.add(RegistrationValidationError.USERNAME_TOO_LONG.toDetail());
            }
        }

        if (nickname != null && nickname.length() > UserAccountConstraints.NICKNAME_MAX_LENGTH) {
            errors.add(RegistrationValidationError.NICKNAME_TOO_LONG.toDetail());
        }

        if (password == null || password.isBlank()) {
            errors.add(RegistrationValidationError.PASSWORD_REQUIRED.toDetail());
        } else {
            if (password.length() < RegistrationValidationRules.PASSWORD_MIN_LENGTH) {
                errors.add(RegistrationValidationError.PASSWORD_TOO_SHORT.toDetail());
            }
            if (!UPPERCASE_PATTERN.matcher(password).find()
                    || !LOWERCASE_PATTERN.matcher(password).find()
                    || !DIGIT_PATTERN.matcher(password).find()
                    || !SPECIAL_PATTERN.matcher(password).find()) {
                errors.add(RegistrationValidationError.PASSWORD_WEAK.toDetail());
            }
        }

        if (!errors.isEmpty()) {
            throw validationError(errors);
        }
        return new ValidatedRegistration(username, nickname, password);
    }

    private ApiException validationError(List<ErrorDetail> errors) {
        return ApiErrorCatalog.exception(
                HttpStatus.BAD_REQUEST,
                ApiErrorKey.REGISTRATION_VALIDATION,
                errors
        );
    }
}
