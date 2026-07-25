/**
 * Emergency Activation bounded context.
 *
 * <p>Manages the state machine of a safety incident:
 * Activated → Recording → Uploaded → Processing → DraftReady → Approved → Closed.
 *
 * <p>Listens for an activation signal from the mobile app and orchestrates
 * the evidence capture and upload pipeline.
 *
 * TODO: implement after hackathon
 */
@org.springframework.modulith.ApplicationModule(displayName = "Emergency Activation")
package com.aura.emergencyactivation;
