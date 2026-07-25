package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.UpdateProfileCommand;
import com.aura.iam.application.dto.UserProfileResult;
import com.aura.iam.application.mapper.UserDomainMapper;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Updates profile fields for the authenticated user.
 */
@Service
public class UpdateUserProfileUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserProfileUseCase.class);

    private final UserRepository userRepository;

    public UpdateUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResult execute(UpdateProfileCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userId()));

        user.updateProfile(command.firstName(), command.lastName(), command.phoneNumber());
        User saved = userRepository.save(user);

        log.info("Profile updated for userId={}", command.userId());
        return UserDomainMapper.toProfileResult(saved);
    }
}
