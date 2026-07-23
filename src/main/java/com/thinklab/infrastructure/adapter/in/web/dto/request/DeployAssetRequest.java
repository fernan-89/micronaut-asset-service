package com.thinklab.infrastructure.adapter.in.web.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Request DTO: Payload for deploying an IT asset to a user or physical location.
 */
@Serdeable
@Introspected
@Schema(name = "DeployAssetRequest", description = "Payload required to deploy an asset, assigning ownership and location context.")
public record DeployAssetRequest(
        @Nullable
        @Schema(description = "UUID of the user taking ownership of the hardware", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID assignedToUserId,

        @Nullable
        @Schema(description = "Identifier of the physical location or rack", example = "DC-SPO-RACK-04")
        String locationId,

        @NotBlank(message = "Executor identification is mandatory")
        @Schema(description = "Identifier of the executor performing the deployment", example = "staff-engineer-01")
        String executorId,

        @NotBlank
        @Schema(description = "Mandatory justification for compliance tracking", example = "Routine hardware allocation for engineering team upgrade")
        String reason
) {
    /**
     * Converts the web request DTO into an application-layer command.
     *
     * @param assetId The target asset UUID from the path variable.
     * @return The corresponding command object.
     */
    public com.thinklab.application.usecase.command.DeployAssetCommand toCommand(@Nonnull UUID assetId) {
        // A ordem dos parâmetros foi ajustada para casar com a assinatura do domínio:
        // (assetId, executorId, assignedToUserId, locationId, reason)
        return new com.thinklab.application.usecase.command.DeployAssetCommand(
                assetId,
                executorId,
                assignedToUserId,
                locationId,
                reason
        );
    }
}