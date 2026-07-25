/**
 * Privacy bounded context.
 *
 * <p>Manages data retention policies, auto-destruction schedules, and
 * field-level encryption configuration for sensitive evidence metadata.
 *
 * <p>Listens to {@code AccountDeletionRequestedEvent} (IAM) to trigger
 * data purging pipelines per configured retention policies.
 *
 * TODO: implement after hackathon
 */
@org.springframework.modulith.ApplicationModule(displayName = "Privacy")
package com.aura.privacy;
