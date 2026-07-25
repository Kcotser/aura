package com.aura.iam.application.usecase;

import com.aura.iam.application.dto.AuthTokenResult;
import com.aura.iam.application.dto.AuthenticateUserCommand;
import com.aura.iam.domain.event.UserAuthenticatedEvent;
import com.aura.iam.domain.model.DeviceSession;
import com.aura.iam.domain.model.User;
import com.aura.iam.domain.repository.DeviceSessionRepository;
import com.aura.iam.domain.repository.UserRepository;
import com.aura.iam.application.port.TokenProvider;
import com.aura.shared.domain.exception.AuthenticationFailedException;
import com.aura.shared.domain.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Orchestrates the user authentication (login) flow.
 *
 * <p>Validates credentials, creates a DeviceSession with a hashed refresh token,
 * and returns both an access token and a refresh token.
 */
@Service
public class AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateUserUseCase.class);

    private final UserRepository userRepository;
    private final DeviceSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public AuthenticateUserUseCase(
            UserRepository userRepository,
            DeviceSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider jwtTokenProvider,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }

    public AuthTokenResult execute(AuthenticateUserCommand command) {
        Email email = Email.of(command.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AuthenticationFailedException("Account is inactive");
        }

        if (!passwordEncoder.matches(command.password(), user.getHashedPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().value(), user.getEmail().value());
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId().value());
        String refreshTokenHash = passwordEncoder.encode(rawRefreshToken);

        long refreshDays = jwtTokenProvider.getRefreshTokenExpirationDays();
        Instant expiresAt = Instant.now().plus(refreshDays, ChronoUnit.DAYS);

        DeviceSession session = DeviceSession.create(
                user.getId(),
                refreshTokenHash,
                command.deviceId() != null ? command.deviceId() : "unknown",
                expiresAt
        );
        sessionRepository.save(session);

        log.info("User authenticated: userId={}, deviceId={}", user.getId(), command.deviceId());

        eventPublisher.publishEvent(new UserAuthenticatedEvent(user.getId(), command.deviceId()));

        long expiresInSeconds = jwtTokenProvider.getAccessTokenExpirationMinutes() * 60L;
        return new AuthTokenResult(accessToken, rawRefreshToken, expiresInSeconds);
    }
}
