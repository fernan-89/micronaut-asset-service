package com.thinklab.infrastructure.adapter.out.mongo.repository;

import com.thinklab.infrastructure.adapter.out.mongo.entity.AssetAuditEntity;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import java.util.UUID;

/**
 * Infrastructure Repository: Reactive persistence interface for {@link AssetAuditEntity}.
 */
@MongoRepository
public interface MongoAssetAuditRepository extends ReactorCrudRepository<AssetAuditEntity, UUID> {

    @Nonnull
    Flux<AssetAuditEntity> findByAssetIdOrderByTimestampDesc(@Nonnull UUID assetId);

    @Nonnull
    Flux<AssetAuditEntity> findByTenantIdOrderByTimestampDesc(@Nonnull UUID tenantId);

    @Nonnull
    Flux<AssetAuditEntity> findByExecutorId(@Nonnull String executorId);

    @Nonnull
    Flux<AssetAuditEntity> findByTxId(@Nonnull String txId);
}