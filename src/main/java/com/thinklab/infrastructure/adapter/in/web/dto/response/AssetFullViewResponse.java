package com.thinklab.infrastructure.adapter.in.web.dto.response;

import com.thinklab.application.port.in.GetAssetFullViewUseCase.AssetFullView;
import com.thinklab.domain.model.AssetAudit;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO: Consolidated 360-degree view projection for an IT Asset.
 * Encapsulates both state representation and immutable forensic audit trail,
 * maintaining strict boundary segregation in accordance with ADR 004.
 */
@Serdeable
@Introspected
@Schema(name = "AssetFullViewResponse", description = "Consolidated 360-degree view projection containing asset status and audit history.")
public record AssetFullViewResponse(
        @Nonnull
        @Schema(description = "Core asset attributes and current operational state")
        AssetResponse asset,

        @Nonnull
        @Schema(description = "Chronological array of forensic audit records")
        List<AssetAuditResponse> auditTrail
) {

    /**
     * Factory method to convert an application-level projection into an immutable API response DTO.
     *
     * @param fullView The application projection emitting asset state and audit logs.
     * @return A fully populated {@link AssetFullViewResponse}.
     */
    @Nonnull
    public static AssetFullViewResponse fromProjection(@Nonnull AssetFullView fullView) {
        AssetResponse assetDto = AssetResponse.fromDomain(fullView.asset());

        List<AssetAuditResponse> auditDtos = fullView.auditLogs().stream()
                .map(AssetAuditResponse::fromDomain)
                .toList();

        return new AssetFullViewResponse(assetDto, auditDtos);
    }

    /**
     * Nested Response DTO representing an individual forensic audit log entry.
     */
    @Serdeable
    @Introspected
    @Schema(name = "AssetAuditResponse", description = "Immutable forensic audit trail record for lifecycle compliance.")
    public record AssetAuditResponse(
            @Nonnull
            @Schema(description = "Unique audit record identifier", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
            UUID id,

            @Nonnull
            @Schema(description = "Global trace transaction identifier", example = "tx-88392-audit")
            String txId,

            @Nonnull
            @Schema(description = "Target asset UUID identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            UUID assetId,

            @Nonnull
            @Schema(description = "Executed operational action", example = "PROVISION_ASSET")
            String operation,

            @Nonnull
            @Schema(description = "Resulting execution status", example = "SUCCESS")
            String status,

            @Nonnull
            @Schema(description = "System or user identity executing the action", example = "admin@thinklab.com")
            String executorId,

            @Nonnull
            @Schema(description = "UTC timestamp of the execution")
            Instant timestamp,

            @Schema(description = "Contextual metadata key-value pairs")
            Map<String, String> metadata
    ) {
        /**
         * Factory method mapping a domain {@link AssetAudit} model to its DTO representation.
         *
         * @param audit The domain audit entity.
         * @return An immutable {@link AssetAuditResponse}.
         */
        @Nonnull
        public static AssetAuditResponse fromDomain(@Nonnull AssetAudit audit) {
            // Converte o Map<String, Object> do domínio para Map<String, String> com segurança de nulos
            Map<String, String> convertedMetadata = audit.metadata() != null
                    ? audit.metadata().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                    ))
                    : Map.of();

            return new AssetAuditResponse(
                    audit.id(),
                    audit.txId(),
                    audit.assetId(),
                    audit.operation(),
                    audit.status(),
                    audit.executorId(),
                    audit.timestamp(),
                    convertedMetadata
            );
        }
    }
}