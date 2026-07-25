/**
 * Trust Network bounded context.
 *
 * <p>Manages SOS contacts and dispatches emergency alerts and notifications.
 * Listens to {@code UserRegisteredEvent} (IAM) to set up initial trust network
 * and to {@code IncidentActivatedEvent} to dispatch SMS/push notifications.
 *
 * TODO: implement after hackathon
 */
@org.springframework.modulith.ApplicationModule(displayName = "Trust Network")
package com.aura.trustnetwork;
