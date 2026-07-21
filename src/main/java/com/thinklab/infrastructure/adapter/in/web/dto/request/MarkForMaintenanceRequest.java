package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.MarkForMaintenanceCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Infrastructure DTO: Represents the HTTP request payload to transition an asset
 * to the UNDER_MAINTENANCE status.
 *
 * This object ensures that hardware withdrawals for technical reasons are
 * syntax-validated at the system boundary and properly justified for
 * organizational compliance.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Forensic Accountability:</b> Mandates a business reason to document
 * the technical necessity of the maintenance.</li>
 * <li><b>Fail-Fast Validation:</b> Rejects malformed requests at the protocol
 * edge using Jakarta constraints.</li>
 * <li><b>AOT Ready:</b> Compiled for reflection-free serialization with
 * Micronaut Serde.</li>
 * </ul>
 *
 * @param executorId The identity of the technical agent authorizing the maintenance.
 * @param reason     The mandatory justification for the maintenance window (Max 500 chars).
 */
@Serdeable
@Introspected
@Schema(
        name = "MarkForMaintenanceRequest",
        description = "Payload required to authorize the withdrawal of an asset for technical maintenance."
)
public record MarkForMaintenanceRequest(
        @NotBlank(message = "Executor identification is mandatory for maintenance logs")
        @Schema(description = "Authorized technical agent ID", example = "hardware-lab-04")
        String executorId,

        @NotBlank(message = "A valid reason is required to withdraw an asset from production")
        @Size(max = 500, message = "Reason exceeds the maximum allowed length for audit logs")
        @Schema(description = "Business justification for the maintenance", example = "Periodic thermal paste replacement and stress test.")
        String reason
) {

    /**
     * Mapper Pattern: Projects the Web DTO into a validated Application Command.
     *
     * @param assetId The system-generated UUID of the target asset.
     * @return A validated {@link MarkForMaintenanceCommand} instance.
     */
    public MarkForMaintenanceCommand toCommand(UUID assetId) {
        return new MarkForMaintenanceCommand(
                assetId,
                this.executorId,
                this.reason
        );
    }
}