package com.example.demo.user.profile;

import com.example.demo.common.ApiErrorKey;
import com.example.demo.common.ApiExceptions;
import com.example.demo.common.ErrorField;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import com.example.demo.user.profile.dto.UpdateUserProfileRequest;
import com.example.demo.user.profile.dto.UserProfileResponse;
import com.example.demo.user.profile.validation.UserProfileValidator;
import com.example.demo.user.profile.validation.ValidatedUserProfileUpdate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileValidator userProfileValidator;

    public UserProfileService(UserRepository userRepository, UserProfileValidator userProfileValidator) {
        this.userRepository = userRepository;
        this.userProfileValidator = userProfileValidator;
    }

    @Transactional
    public UserProfileResponse updateCurrentUser(String userId, UpdateUserProfileRequest request) {
        UserAccount user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(ApiExceptions::unauthorized);
        ValidatedUserProfileUpdate validated = userProfileValidator.validate(request);

        if (Objects.equals(user.getNickname(), validated.nickname())) {
            return toResponse(user);
        }

        if (validated.nickname() != null && nicknameBelongsToAnotherUser(validated.nickname(), user.getId())) {
            throw ApiExceptions.conflict(ApiErrorKey.NICKNAME_ALREADY_EXISTS, ErrorField.NICKNAME);
        }

        user.updateNickname(validated.nickname());
        try {
            return toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw ApiExceptions.conflict(ApiErrorKey.USER_PROFILE_CONFLICT, ErrorField.USER);
        }
    }

    private boolean nicknameBelongsToAnotherUser(String nickname, String userId) {
        return userRepository.findByActiveNicknameKey(nickname)
                .map(existing -> !existing.getId().equals(userId))
                .orElse(false);
    }

    private UserProfileResponse toResponse(UserAccount user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
