package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Command: Encapsulates the intent to permanently decommission an IT Asset.
 *
 * This record enforces strict boundary validation for the terminal stage of the
 * asset lifecycle. Decommissioning is an irreversible action under the Zero Trust
 * principle, mandating a business justification for forensic audit compliance
 * and hardware disposal tracking.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Terminal Accountability:</b> Mandates a reason to document the hardware's
 * final disposal or end-of-life status.</li>
 * <li><b>Forensic Integrity:</b> Guarantees that the executor and asset identity
 * are verified before the domain state mutation.</li>
 * <li><b>Edge Observability:</b> Integrated constructor logging for immediate
 * detection of unauthorized or malformed disposal attempts.</li>
 * </ul>
 *
 * @param assetId    The unique system identifier of the asset to be decommissioned.
 * @param executorId The identity of the agent authorizing the permanent disposal.
 * @param reason     The mandatory business justification for decommissioning (Max 500 characters).
 */
@Slf4j
@Introspected
public record DecommissionAssetCommand(
        @NotNull(message = "Asset ID is mandatory for lifecycle termination")
        UUID assetId,

        @NotBlank(message = "Executor identification is mandatory for forensic auditing")
        String executorId,

        @NotBlank(message = "Decommissioning reason is mandatory for compliance")
        @Size(max = 500, message = "Reason exceeds maximum allowed length")
        String reason
) {

    /**
     * Compact constructor for input sanitization and defensive integrity checks.
     * Acts as the final gatekeeper for terminal lifecycle actions.
     */
    public DecommissionAssetCommand {
        if (assetId == null || executorId == null || executorId.isBlank() || reason == null || reason.isBlank()) {
            log.error("[ACTION: DECOMMISSION_VALIDATION] - Terminal integrity violation: Mandatory fields missing for Asset [{}].", assetId);
        }

        Objects.requireNonNull(assetId, "Asset ID cannot be null");
        Objects.requireNonNull(executorId, "Executor ID cannot be null");
        Objects.requireNonNull(reason, "Decommissioning reason cannot be null");

        executorId = executorId.trim();
        reason = reason.trim();
    }
}