package com.thinklab.infrastructure.adapter.in.web.dto.request;

import com.thinklab.application.usecase.command.CreateAssetCommand;
import com.thinklab.domain.valueobject.AssetCategory;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure DTO: Represents the HTTP request payload for provisioning a new IT Asset.
 * This object acts as the primary entry boundary, enforcing strict syntactic validation
 * and providing metadata for automated OpenAPI documentation.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Fail-Fast Validation:</b> Utilizes Jakarta constraints to reject malformed
 * payloads before domain instantiation.</li>
 * <li><b>AOT Ready:</b> Annotated for compile-time introspection to ensure
 * reflection-free serialization.</li>
 * <li><b>Protocol Translation:</b> Decouples external JSON representation from
 * internal Application Commands.</li>
 * </ul>
 *
 * @param tenantId       The unique identifier of the organization (Tenant).
 * @param name           The human-readable name for the asset.
 * @param category       The hardware classification (e.g., LAPTOP, SERVER).
 * @param serialNumber   The authoritative hardware serial number.
 * @param specifications Key-value pairs of technical hardware specs.
 * @param executorId     The identity of the technical agent performing the action.
 */
@Serdeable
@Introspected
@Schema(
        name = "ProvisionAssetRequest",
        description = "Payload required to register a new IT asset into the organization inventory."
)
public record ProvisionAssetRequest(
        @NotNull(message = "Tenant context is mandatory for multi-tenant isolation")
        @Schema(description = "Isolated organization ID", example = "79e83370-d699-4d28-817f-acc401490ed5")
        UUID tenantId,

        @NotBlank(message = "Asset name is mandatory")
        @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
        @Schema(description = "Display name for the hardware", example = "MacBook Pro 16 M3")
        String name,

        @NotNull(message = "Hardware category is mandatory")
        @Schema(description = "Asset type classification")
        AssetCategory category,

        @NotBlank(message = "Serial number is mandatory for deterministic tracking")
        @Size(max = 100, message = "Serial number exceeds safety limit")
        @Schema(description = "Unique manufacturer serial number", example = "SN-NASA-2026-X")
        String serialNumber,

        @NotNull(message = "Specifications map cannot be null")
        @Schema(description = "Technical specifications dictionary")
        Map<String, String> specifications,

        @NotBlank(message = "Executor identification is mandatory for auditing")
        @Schema(description = "Authorized agent ID", example = "infra-bot-01")
        String executorId
) {

    /**
     * Mapper Pattern: Translates the Infrastructure DTO into an internal Application Command.
     * This decouples the web transport layer from the use case orchestration logic.
     *
     * @return A validated {@link CreateAssetCommand} instance.
     */
    public CreateAssetCommand toCommand() {
        return new CreateAssetCommand(
                this.tenantId,
                this.name,
                this.category,
                this.serialNumber,
                this.specifications,
                this.executorId
        );
    }
}