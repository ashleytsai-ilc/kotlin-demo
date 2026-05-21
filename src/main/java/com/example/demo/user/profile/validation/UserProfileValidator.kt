package com.example.demo.user.profile.validation;

import com.example.demo.common.ApiErrorCatalog;
import com.example.demo.common.ApiErrorKey;
import com.example.demo.common.ApiException;
import com.example.demo.common.ErrorDetail;
import com.example.demo.common.Strings;
import com.example.demo.user.account.UserAccountConstraints;
import com.example.demo.user.profile.dto.UpdateUserProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserProfileValidator {

    public ValidatedUserProfileUpdate validate(UpdateUserProfileRequest request) {
        List<ErrorDetail> errors = new ArrayList<>();
        if (!request.nicknameProvided() || request.nickname() == null) {
            errors.add(UserProfileValidationError.NICKNAME_REQUIRED.toDetail());
            throw validationError(errors);
        }

        String nickname = Strings.trimToNull(request.nickname());
        if (nickname != null && nickname.length() > UserAccountConstraints.NICKNAME_MAX_LENGTH) {
            errors.add(UserProfileValidationError.NICKNAME_TOO_LONG.toDetail());
        }

        if (!errors.isEmpty()) {
            throw validationError(errors);
        }
        return new ValidatedUserProfileUpdate(nickname);
    }

    private ApiException validationError(List<ErrorDetail> errors) {
        return ApiErrorCatalog.exception(
                HttpStatus.BAD_REQUEST,
                ApiErrorKey.USER_PROFILE_VALIDATION,
                errors
        );
    }
}
