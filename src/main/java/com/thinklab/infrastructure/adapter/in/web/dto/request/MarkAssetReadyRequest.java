package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.MarkAssetReadyCommand;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Infrastructure DTO: Represents the request payload to transition an asset
 * to the READY_FOR_DEPLOY status.
 *
 * This object enforces boundary integrity for assets that have completed
 * their initial technical setup and are waiting for deployment.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Fail-Fast Validation:</b> Ensures the executor's identity is
 * verified at the protocol level.</li>
 * <li><b>Reactive Immutability:</b> Modeled as a Java Record to prevent
 * state corruption during non-blocking transit.</li>
 * <li><b>Contract Decoupling:</b> Maintains isolation between the external
 * JSON representation and the internal Application Command.</li>
 * </ul>
 *
 * @param executorId The identity of the technical agent authorizing the state transition.
 */
@Serdeable
@Introspected
@Schema(
        name = "MarkAssetReadyRequest",
        description = "Payload required to authorize an asset for operational deployment."
)
public record MarkAssetReadyRequest(
        @NotBlank(message = "Executor identification is mandatory for lifecycle transitions")
        @Schema(description = "Authorized technical agent ID", example = "admin-sys-01")
        String executorId
) {

    /**
     * Mapper Pattern: Projects the Web DTO into a validated Application Command.
     *
     * @param assetId The system-generated UUID of the target asset.
     * @return A validated {@link MarkAssetReadyCommand}.
     */
    public MarkAssetReadyCommand toCommand(UUID assetId) {
        return new MarkAssetReadyCommand(
                assetId,
                this.executorId
        );
    }
}