package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.DeployAssetCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Infrastructure DTO: Represents the HTTP request payload for deploying an IT Asset.
 */
@Serdeable
@Introspected
@Schema(
        name = "DeployAssetRequest",
        description = "Payload required to assign an asset to a user and physical location."
)
public record DeployAssetRequest(
        @NotNull(message = "Target user ID is mandatory for deployment")
        @Schema(description = "UUID of the assigned user", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID assignedToUserId,

        @NotBlank(message = "Location identifier is mandatory for inventory tracking")
        @Schema(description = "Site or Rack identifier", example = "SPO-DC-01-RACK-04")
        String locationId,

        @NotBlank(message = "Executor identification is mandatory for forensic auditing")
        @Schema(description = "Authorized agent ID", example = "admin-sys-01")
        String executorId,

        @NotBlank(message = "Deployment reason is mandatory for compliance")
        @Schema(description = "Business justification for this deployment", example = "New employee onboarding")
        String reason
) {

    /**
     * Mapper Pattern: Projects the Infrastructure DTO into a validated Application Command.
     *
     * @param assetId The system-generated UUID of the target asset.
     * @return A validated {@link DeployAssetCommand} instance.
     */
    public DeployAssetCommand toCommand(UUID assetId) {
        return new DeployAssetCommand(
                assetId,
                this.executorId,
                this.assignedToUserId,
                this.locationId,
                this.reason
        );
    }
}