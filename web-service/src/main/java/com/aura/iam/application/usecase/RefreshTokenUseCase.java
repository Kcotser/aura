package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.AuthTokenResult;
import com.aura.iam.application.dto.RefreshTokenCommand;
import com.aura.iam.domain.model.DeviceSession;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.model.UserId;
import com.aura.iam.domain.repository.DeviceSessionRepository;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.iam.application.port.TokenProvider;
import com.aura.shared.domain.exception.AuthenticationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Handles refresh token exchange for a new access token.
 *
 * <p>The incoming refresh token is matched against all stored session hashes
 * for the user extracted from the JWT claims. On success, the old session is
 * revoked and a new one is issued (token rotation).
 */
@Service
public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final DeviceSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider jwtTokenProvider;

    public RefreshTokenUseCase(
            DeviceSessionRepository sessionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider jwtTokenProvider
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthTokenResult execute(RefreshTokenCommand command) {
        // Extract userId from the refresh token's JWT claims
        String userId = jwtTokenProvider.extractSubjectFromRefreshToken(command.refreshToken());

        // Find all sessions for this user and match by hash
        List<DeviceSession> sessions = sessionRepository.findAllByUserId(UserId.of(userId));

        DeviceSession matchedSession = sessions.stream()
                .filter(DeviceSession::isValid)
                .filter(s -> passwordEncoder.matches(command.refreshToken(), s.getRefreshTokenHash()))
                .findFirst()
                .orElseThrow(() -> new AuthenticationFailedException("Invalid or expired refresh token"));

        User user = userRepository.findById(matchedSession.getUserId())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        if (!user.isActive()) {
            throw new AuthenticationFailedException("Account is inactive");
        }

        // Revoke old session (token rotation)
        matchedSession.revoke();
        sessionRepository.save(matchedSession);

        // Issue new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId().value(), user.getEmail().value());
        String newRawRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId().value());
        String newRefreshTokenHash = passwordEncoder.encode(newRawRefreshToken);

        long refreshDays = jwtTokenProvider.getRefreshTokenExpirationDays();
        Instant expiresAt = Instant.now().plus(refreshDays, ChronoUnit.DAYS);

        DeviceSession newSession = DeviceSession.create(
                user.getId(),
                newRefreshTokenHash,
                command.deviceId() != null ? command.deviceId() : matchedSession.getDeviceId(),
                expiresAt
        );
        sessionRepository.save(newSession);

        log.info("Token refreshed: userId={}", user.getId());

        long expiresInSeconds = jwtTokenProvider.getAccessTokenExpirationMinutes() * 60L;
        return new AuthTokenResult(newAccessToken, newRawRefreshToken, expiresInSeconds);
    }
}
