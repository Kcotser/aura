/**
 * Analysis bounded context.
 *
 * <p>Anti-Corruption Layer (ACL) for integrating with the Gemma 4 AI model.
 * Receives evidence references, calls Gemma 4's function calling API,
 * and publishes analysis result events.
 *
 * <p>First candidate for microservice extraction due to its compute-intensive
 * nature and distinct scaling profile (GPU/accelerator instances).
 *
 * TODO: implement after hackathon
 */
@org.springframework.modulith.ApplicationModule(displayName = "Analysis")
package com.aura.analysis;
