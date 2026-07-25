/**
 * Shared kernel for the Aura platform.
 *
 * <p>Contains cross-cutting concerns reused across all bounded contexts:
 * base exceptions, the generic {@code ApiResponse<T>} envelope, reusable
 * Value Objects (Email), audit metadata, and the global exception handler.
 *
 * <p><strong>Rule:</strong> This module must not contain any business logic.
 * It provides purely technical building blocks.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Shared Kernel")
package com.aura.shared;
