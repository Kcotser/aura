package com.aura.iam.application.usecase;

import com.aura.iam.domain.model.DeviceSession;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.DeviceSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Revokes the current device session on logout.
 */
@Service
public class LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogoutUseCase.class);

    private final DeviceSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public LogoutUseCase(DeviceSessionRepository sessionRepository, PasswordEncoder passwordEncoder) {
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Revokes the session associated with the given refresh token, if found.
     *
     * @param userId       the authenticated user's ID
     * @param refreshToken the raw refresh token to revoke (may be null if not provided)
     */
    public void execute(String userId, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            // Revoke all sessions for this user as a fallback
            List<DeviceSession> sessions = sessionRepository.findAllByUserId(UserId.of(userId));
            sessions.forEach(DeviceSession::revoke);
            sessionRepository.saveAll(sessions);
            log.info("All sessions revoked for userId={}", userId);
            return;
        }

        List<DeviceSession> sessions = sessionRepository.findAllByUserId(UserId.of(userId));
        sessions.stream()
                .filter(s -> !s.isRevoked())
                .filter(s -> passwordEncoder.matches(refreshToken, s.getRefreshTokenHash()))
                .findFirst()
                .ifPresent(session -> {
                    session.revoke();
                    sessionRepository.save(session);
                    log.info("Session revoked: sessionId={}, userId={}", session.getId(), userId);
                });
    }
}
