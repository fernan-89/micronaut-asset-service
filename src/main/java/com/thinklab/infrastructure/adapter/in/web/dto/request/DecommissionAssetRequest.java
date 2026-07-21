package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.DecommissionAssetCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Infrastructure DTO: Represents the HTTP request payload for permanent asset decommissioning.
 * This object ensures that terminal lifecycle events are syntax-validated and backed by
 * mandatory forensic justifications before being processed by the application core.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Terminal State Sovereignty:</b> Enforces that decommissioning is an irreversible
 * operation that requires high-quality audit metadata.</li>
 * <li><b>Zero Trust Boundary:</b> Mandates executor identification and reasoning at the
 * first point of contact.</li>
 * <li><b>AOT Ready:</b> Compiled for reflection-free serialization with Micronaut Serde.</li>
 * </ul>
 *
 * @param executorId The identity of the authorized agent requesting the decommissioning.
 * @param reason     The mandatory forensic justification for permanent hardware disposal (Max 500 chars).
 */
@Serdeable
@Introspected
@Schema(
        name = "DecommissionAssetRequest",
        description = "Payload required to permanently terminate the lifecycle of an IT asset."
)
public record DecommissionAssetRequest(
        @NotBlank(message = "Executor identification is mandatory for terminal lifecycle events")
        @Schema(description = "Authorized agent ID", example = "compliance-officer-01")
        String executorId,

        @NotBlank(message = "A business reason is mandatory for permanent decommissioning")
        @Size(max = 500, message = "Reason exceeds the maximum allowed length for forensic audit logs")
        @Schema(description = "Forensic justification for disposal", example = "Hardware end-of-life: obsolete board components.")
        String reason
) {

    /**
     * Mapper Pattern: Projects the Web DTO into a validated Application Command.
     * This method binds the resource identity from the URL to the business intent.
     *
     * @param assetId The system-generated UUID of the target asset.
     * @return A validated {@link DecommissionAssetCommand} instance.
     */
    public DecommissionAssetCommand toCommand(UUID assetId) {
        return new DecommissionAssetCommand(
                assetId,
                this.executorId,
                this.reason
        );
    }
}