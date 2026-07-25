package com.aura.iam.application.dto;

/**
 * Command DTO for registering a new user.
 */
public record RegisterUserCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber
) {}
