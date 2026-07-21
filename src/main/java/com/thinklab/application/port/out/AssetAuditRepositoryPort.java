package com.thinklab.application.port.out;

import com.thinklab.domain.model.AssetAudit;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Outbound Port: Contract for Asset Audit persistence operations.
 */
public interface AssetAuditRepositoryPort {

    @Nonnull
    Mono<AssetAudit> save(@Nonnull AssetAudit audit);

    @Nonnull
    Flux<AssetAudit> findByAssetId(@Nonnull UUID assetId);

    @Nonnull
    Flux<AssetAudit> findByTenantId(@Nonnull UUID tenantId);

    @Nonnull
    Flux<AssetAudit> findByExecutorId(@Nonnull String executorId);

    @Nonnull
    Flux<AssetAudit> findByTxId(@Nonnull String txId);
}