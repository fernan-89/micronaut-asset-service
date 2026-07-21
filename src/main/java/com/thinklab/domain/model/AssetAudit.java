package com.thinklab.domain.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Model: Immutable aggregate representing a forensic audit event for IT Assets.
 * This model serves as the irrefutable source of truth for all lifecycle mutations,
 * ensuring strict compliance and traceability across the global ecosystem.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Append-Only Integrity:</b> Designed to be persisted in a non-mutable ledger.</li>
 * <li><b>Domain Portability:</b> Zero framework dependencies (Pure Java 21).</li>
 * <li><b>Defensive Construction:</b> Validation of all invariants at the point of instantiation.</li>
 * </ul>
 *
 * @param id         The unique identifier for this specific audit entry.
 * @param txId       The correlation identifier for the encompassing transaction (Correlation ID).
 * @param tenantId   The unique identifier of the organization owning the asset.
 * @param assetId    The unique identifier of the IT Asset (Aggregate Root) being audited.
 * @param operation  The business operation type (e.g., ASSET_PROVISIONING, ASSET_DEPLOYMENT).
 * @param status     The outcome of the operation (e.g., SUCCESS, FAILURE).
 * @param executorId The identifier of the agent (user or system) authorizing the action.
 * @param timestamp  The UTC instant when the event was recorded.
 * @param metadata   Contextual data providing additional details for forensic analysis.
 */
public record AssetAudit(
        @Nonnull UUID id,
        @Nonnull String txId,
        @Nonnull UUID tenantId,
        @Nonnull UUID assetId,
        @Nonnull String operation,
        @Nonnull String status,
        @Nonnull String executorId,
        @Nonnull Instant timestamp,
        @Nonnull Map<String, Object> metadata
) {

    /**
     * Compact constructor for input sanitization and domain invariant protection.
     */
    public AssetAudit {
        Objects.requireNonNull(id, "Audit ID is mandatory");
        Objects.requireNonNull(txId, "Transaction ID is mandatory");
        Objects.requireNonNull(tenantId, "Tenant ID is mandatory");
        Objects.requireNonNull(assetId, "Asset ID is mandatory");
        Objects.requireNonNull(operation, "Operation name is mandatory");
        Objects.requireNonNull(status, "Status outcome is mandatory");
        Objects.requireNonNull(executorId, "Executor identification is mandatory");
        Objects.requireNonNull(timestamp, "Event timestamp is mandatory");

        operation = operation.trim().toUpperCase();
        status = status.trim().toUpperCase();
        executorId = executorId.trim();
        txId = txId.trim();
        metadata = metadata != null ? Map.copyOf(metadata) : Collections.emptyMap();
    }

    /**
     * Factory method for creating a new forensic audit record.
     * Automatically handles ID generation and timestamping.
     *
     * @param tenantId   The organization ID.
     * @param assetId    The target asset ID.
     * @param operation  The business action name.
     * @param status     The outcome status.
     * @param executorId The agent performing the action.
     * @param metadata   Additional contextual metadata (Optional).
     * @return A fully initialized, immutable {@link AssetAudit} instance.
     */
    @Nonnull
    public static AssetAudit create(
            @Nonnull UUID tenantId,
            @Nonnull UUID assetId,
            @Nonnull String operation,
            @Nonnull String status,
            @Nonnull String executorId,
            @Nullable Map<String, Object> metadata
    ) {
        return new AssetAudit(
                UUID.randomUUID(),
                UUID.randomUUID().toString(), // Should ideally be sourced from MDC/Tracing Context
                tenantId,
                assetId,
                operation,
                status,
                executorId,
                Instant.now(),
                metadata
        );
    }
}