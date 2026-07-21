package com.thinklab.domain.exception;

import com.thinklab.domain.valueobject.AssetStatus;
import jakarta.annotation.Nonnull;

/**
 * Domain Exception: Thrown when an illegal lifecycle transition is attempted 
 * within the Asset state machine.
 *
 * <p>This exception acts as a business guard, ensuring that IT Assets follow
 * a strictly governed path from provisioning to decommissioning, satisfying
 * enterprise compliance and forensic audit requirements.</p>
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>FSM Integrity:</b> Prevents corrupted operational states in the aggregate root.</li>
 * <li><b>Semantic Error Mapping:</b> Inherits from BusinessException to enforce 
 * the "INVALID_ASSET_STATUS" error code, typically mapping to HTTP 422.</li>
 * </ul>
 */
public class InvalidAssetStatusException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_ASSET_STATUS";

    /**
     * Constructs a new exception detailing the illegal transition attempt.
     *
     * @param current The current status of the asset.
     * @param target  The illegal target status requested.
     */
    public InvalidAssetStatusException(@Nonnull AssetStatus current, @Nonnull AssetStatus target) {
        super(ERROR_CODE, String.format(
                "Compliance Violation: Illegal asset transition attempt from [%s] to [%s].",
                current.name(), target.name()
        ));
    }
}