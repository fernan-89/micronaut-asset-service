package com.thinklab.infrastructure.adapter.in.web.dto.response;

import com.thinklab.domain.model.Asset;
import com.thinklab.domain.valueobject.AssetCategory;
import com.thinklab.domain.valueobject.AssetStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure DTO: Public-facing projection of an IT Asset aggregate.
 */
@Serdeable
@Introspected
@Schema(
        name = "AssetResponse",
        description = "Standardized response payload representing the current state and technical profile of an IT asset."
)
public record AssetResponse(
        @Nonnull
        @Schema(description = "System-generated unique identifier", example = "79e83370-d699-4d28-817f-acc401490ed5")
        UUID id,

        @Nonnull
        @Schema(description = "Isolated organization identifier (Tenant ID)", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID tenantId,

        @Nonnull
        @Schema(description = "Display name for the hardware", example = "MacBook Pro 16 M3 Max")
        String name,

        @Nonnull
        @Schema(description = "Hardware category classification")
        AssetCategory category,

        @Nonnull
        @Schema(description = "Current lifecycle operational status")
        AssetStatus status,

        @Nonnull
        @Schema(description = "Authoritative manufacturer serial number", example = "SN-NASA-2026-X")
        String serialNumber,

        @Nonnull
        @Schema(description = "Technical specifications dictionary")
        Map<String, Object> specifications, // Corrigido para bater com o Domínio

        @Nullable
        @Schema(description = "UUID of the user assigned to this asset, if applicable")
        UUID assignedToUserId,

        @Nullable
        @Schema(description = "Physical or logical site identifier")
        UUID locationId, // Corrigido para UUID para bater com o Domínio

        @Nonnull
        @Schema(description = "UTC timestamp of initial provisioning")
        Instant createdAt,

        @Nonnull
        @Schema(description = "UTC timestamp of the last administrative mutation")
        Instant updatedAt,

        @Nonnull
        @Schema(description = "Optimistic locking version for state consistency tracking")
        Long version
) {

    @Nonnull
    public static AssetResponse fromDomain(@Nonnull Asset domain) {
        return new AssetResponse(
                domain.id(),
                domain.tenantId(),
                domain.name(),
                domain.category(),
                domain.status(),
                domain.serialNumber(),
                domain.specifications(),
                domain.assignedTo(), // Corrigido (antes era assignedToUserId())
                domain.locationId(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.version()
        );
    }
}