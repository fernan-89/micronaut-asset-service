package com.thinklab.infrastructure.adapter.out.mongo.entity;

import com.thinklab.domain.model.AssetAudit;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Index;
import io.micronaut.data.annotation.Indexes;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure Entity: Persistence model for the Asset forensic audit trail.
 */
@Serdeable
@Introspected
@MappedEntity("asset_audits")
@Indexes({
        @Index(columns = {"txId"}), // Novo índice para busca por Transaction ID
        @Index(columns = {"tenantId", "assetId", "timestamp"}),
        @Index(columns = {"executorId", "timestamp"}),
        @Index(columns = {"tenantId", "operation", "timestamp"})
})
public record AssetAuditEntity(
        @Id
        @Nonnull
        UUID id,

        @Nonnull
        String txId, // Adicionado para bater com o Domínio

        @Nonnull
        UUID tenantId,

        @Nonnull
        UUID assetId,

        @Nonnull
        String operation,

        @Nonnull
        String status,

        @Nonnull
        String executorId,

        @Nonnull
        Instant timestamp,

        @Nonnull
        Map<String, Object> metadata
) {

    @Nonnull
    public static AssetAuditEntity fromDomain(@Nonnull AssetAudit domain) {
        return new AssetAuditEntity(
                domain.id(),
                domain.txId(),
                domain.tenantId(),
                domain.assetId(),
                domain.operation(),
                domain.status(),
                domain.executorId(),
                domain.timestamp(),
                domain.metadata()
        );
    }

    @Nonnull
    public AssetAudit toDomain() {
        return new AssetAudit(
                this.id,
                this.txId,
                this.tenantId,
                this.assetId,
                this.operation,
                this.status,
                this.executorId,
                this.timestamp,
                this.metadata
        );
    }
}