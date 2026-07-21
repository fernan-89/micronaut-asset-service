package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Command: Encapsulates the intent to transition an IT Asset into
 * a maintenance lifecycle state.
 *
 * This record enforces strict boundary validation and edge-case logging, ensuring
 * that technical withdrawals from service are authorized and carries mandatory
 * forensic metadata for compliance audits.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Operational Accountability:</b> Mandates a business reason to document
 * the technical failure or scheduled service.</li>
 * <li><b>Input Boundary Defense:</b> Utilizes Jakarta Validation to perform
 * fail-fast detection of malformed requests.</li>
 * <li><b>Edge Observability:</b> Integrated constructor logging for
 * immediate detection of validation violations.</li>
 * </ul>
 *
 * @param assetId    The unique system identifier of the target asset.
 * @param executorId The identity of the technical agent authorizing the maintenance.
 * @param reason     Business justification or fault description (Max 500 characters).
 */
@Slf4j
@Introspected
public record MarkForMaintenanceCommand(
        @NotNull(message = "Asset ID is mandatory for lifecycle transitions")
        UUID assetId,

        @NotBlank(message = "Executor identification is mandatory for forensic auditing")
        String executorId,

        @NotBlank(message = "Maintenance reason is mandatory for compliance tracking")
        @Size(max = 500, message = "Reason exceeds maximum allowed length")
        String reason
) {

    /**
     * Compact constructor for input sanitization and defensive integrity checks.
     * Acts as the final gatekeeper before the command reaches the Application Interactor.
     */
    public MarkForMaintenanceCommand {
        if (assetId == null || executorId == null || executorId.isBlank() || reason == null || reason.isBlank()) {
            log.error("[ACTION: MAINTENANCE_VALIDATION] - Integrity violation: Mandatory fields missing for Asset [{}].", assetId);
        }

        Objects.requireNonNull(assetId, "Asset ID cannot be null");
        Objects.requireNonNull(executorId, "Executor ID cannot be null");
        Objects.requireNonNull(reason, "Maintenance reason cannot be null");

        executorId = executorId.trim();
        reason = reason.trim();
    }
}