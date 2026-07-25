/**
 * IAM (Identity & Access Management) bounded context.
 *
 * <p>Responsible for user registration, authentication (JWT), session management,
 * backup PIN configuration, and account lifecycle events.
 *
 * <p>This is the only bounded context fully implemented for the hackathon demo.
 * All other contexts receive and react to domain events published by this module.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Identity & Access Management")
package com.aura.iam;
