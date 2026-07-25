package com.aura.iam.application.usecase;

import com.aura.iam.domain.event.AccountDeletionRequestedEvent;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.DeviceSessionRepository;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.shared.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles an account deletion request.
 *
 * <p>Deactivates the user, revokes all sessions, and publishes
 * {@link AccountDeletionRequestedEvent} so the {@code privacy} context
 * can schedule data purging per retention policies.
 */
@Service
public class RequestAccountDeletionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestAccountDeletionUseCase.class);

    private final UserRepository userRepository;
    private final DeviceSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RequestAccountDeletionUseCase(
            UserRepository userRepository,
            DeviceSessionRepository sessionRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(String userId) {
        UserId uid = UserId.of(userId);
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.deactivate();
        userRepository.save(user);

        // Revoke all active sessions
        var sessions = sessionRepository.findAllByUserId(uid);
        sessions.forEach(s -> s.revoke());
        sessionRepository.saveAll(sessions);

        log.info("Account deletion requested for userId={}", userId);

        eventPublisher.publishEvent(
                new AccountDeletionRequestedEvent(user.getId(), user.getEmail().value())
        );
    }
}
