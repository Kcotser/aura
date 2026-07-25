package com.aura.iam.application.dto;

/**
 * Command DTO for authenticating a user (login).
 */
public record AuthenticateUserCommand(
        String email,
        String password,
        String deviceId
) {}
