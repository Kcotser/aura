package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.UserProfileResult;
import com.aura.iam.application.mapper.UserDomainMapper;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Retrieves the profile of the currently authenticated user.
 */
@Service
public class GetUserProfileUseCase {

    private final UserRepository userRepository;

    public GetUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResult execute(String userId) {
        return userRepository.findById(UserId.of(userId))
                .map(UserDomainMapper::toProfileResult)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
