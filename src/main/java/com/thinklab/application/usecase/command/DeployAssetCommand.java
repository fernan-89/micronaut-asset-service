package com.thinklab.application.usecase.command;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Command: Encapsulates the intent to deploy an IT Asset to a specific 
 * user or physical/logical location.
 *
 * This record enforces strict boundary validation and defensive sanitization, 
 * ensuring that lifecycle mutations are properly targeted and authorized for 
 * forensic audit compliance.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Assigned Sovereignty:</b> Mandates at least one deployment target 
 * (User or Location) to prevent "floating" deployments.</li>
 * <li><b>Input Integrity:</b> Utilizes Jakarta Validation to perform fail-fast 
 * detection of malformed request data.</li>
 * <li><b>Audit Compliance:</b> Carries mandatory executor and reason fields 
 * for the permanent forensic trail.</li>
 * </ul>
 *
 * @param assetId          The unique system identifier of the hardware to be deployed.
 * @param executorId       The identity of the administrative agent authorizing the deployment.
 * @param assignedToUserId Optional UUID of the employee/user receiving the asset.
 * @param locationId       Optional identifier of the rack, room, or branch receiving the asset.
 * @param reason           Business justification for the deployment (Max 500 characters).
 */
@Introspected
public record DeployAssetCommand(
        @NotNull(message = "Asset ID is mandatory for deployment")
        UUID assetId,

        @NotBlank(message = "Executor identification is mandatory")
        String executorId,

        @Nullable
        UUID assignedToUserId,

        @Nullable
        @Size(max = 100, message = "Location ID exceeds length limit")
        String locationId,

        @NotBlank(message = "Deployment reason is mandatory for compliance")
        @Size(max = 500, message = "Reason exceeds maximum allowed length")
        String reason
) {

    /**
     * Compact constructor for cross-field validation and sanitization.
     */
    public DeployAssetCommand {
        Objects.requireNonNull(assetId, "Asset ID cannot be null");
        Objects.requireNonNull(executorId, "Executor ID cannot be null");
        Objects.requireNonNull(reason, "Deployment reason cannot be null");

        if (assignedToUserId == null && (locationId == null || locationId.isBlank())) {
            throw new IllegalArgumentException("Deployment Target Violation: Either assignedToUserId or locationId must be provided.");
        }

        executorId = executorId.trim();
        reason = reason.trim();
        locationId = locationId != null ? locationId.trim() : null;
    }
}