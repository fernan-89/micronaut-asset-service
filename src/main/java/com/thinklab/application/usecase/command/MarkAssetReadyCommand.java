package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Command: Encapsulates the intent to transition an Asset to the READY_FOR_DEPLOY state.
 * This record enforces strict boundary validation using Jakarta Bean Validation, ensuring
 * that lifecycle mutations are authorized and properly targeted.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Input Integrity:</b> Guarantees that the target Asset UUID is valid before reaching the Interactor.</li>
 * <li><b>Audit Compliance:</b> Mandates an executor identity to satisfy Tier 3 forensic requirements.</li>
 * <li><b>Immutability:</b> Leverages Java Records to prevent state mutation during the reactive pipeline.</li>
 * </ul>
 *
 * @param assetId    The unique system identifier of the target asset.
 * @param executorId The identity of the technical agent authorizing the readiness state.
 */
@Introspected
public record MarkAssetReadyCommand(
        @NotNull(message = "Asset ID is mandatory for lifecycle transitions")
        UUID assetId,

        @NotBlank(message = "Executor identification is mandatory for forensic auditing")
        String executorId
) {

    /**
     * Compact constructor for input sanitization and defensive integrity checks.
     */
    public MarkAssetReadyCommand {
        Objects.requireNonNull(assetId, "Asset ID cannot be null");
        Objects.requireNonNull(executorId, "Executor ID cannot be null");

        executorId = executorId.trim();
    }
}