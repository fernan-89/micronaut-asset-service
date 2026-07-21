package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * Domain Exception: Base class for all business rule violations within the Asset domain.
 * This exception serves as the primary signaling mechanism between the Core Domain
 * and the external Adapters, allowing for specialized error mapping (e.g., HTTP 4xx).
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>Semantic Decoupling:</b> Uses internal error codes instead of HTTP status codes.</li>
 * <li><b>Defensive Construction:</b> Enforces mandatory error codes and messages via null-checks.</li>
 * <li><b>Type-Safety:</b> Designed to be extended by granular, intent-based exceptions.</li>
 * </ul>
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new business exception with a mandatory error code.
     *
     * @param errorCode A unique, standardized string identifying the business failure.
     * @param message   A descriptive human-readable message explaining the violation.
     */
    public BusinessException(@Nonnull String errorCode, @Nonnull String message) {
        super(Objects.requireNonNull(message, "Exception message cannot be null"));
        this.errorCode = Objects.requireNonNull(errorCode, "Business error code is mandatory");
    }

    /**
     * Retrieves the specific business error code for downstream processing.
     * @return The standardized error identifier.
     */
    @Nonnull
    public String getErrorCode() {
        return errorCode;
    }
}