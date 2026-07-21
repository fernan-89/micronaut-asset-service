package com.thinklab.infrastructure.adapter.out.mongo.repository;

import com.thinklab.domain.valueobject.AssetCategory;
import com.thinklab.domain.valueobject.AssetStatus;
import com.thinklab.infrastructure.adapter.out.mongo.entity.AssetEntity;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactorPageableRepository;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Infrastructure Repository: Reactive persistence interface for {@link AssetEntity}.
 */
@MongoRepository
public interface MongoAssetRepository extends ReactorPageableRepository<AssetEntity, UUID> {

    @Nonnull
    Mono<Boolean> existsByTenantIdAndSerialNumber(@Nonnull UUID tenantId, @Nonnull String serialNumber);

    @Nonnull
    Flux<AssetEntity> findAllByTenantId(@Nonnull UUID tenantId, @Nonnull Pageable pageable);

    @Nonnull
    Flux<AssetEntity> findByTenantIdAndStatus(@Nonnull UUID tenantId, @Nonnull AssetStatus status, @Nonnull Pageable pageable);

    @Nonnull
    Flux<AssetEntity> findByTenantIdAndCategory(@Nonnull UUID tenantId, @Nonnull AssetCategory category, @Nonnull Pageable pageable);

    // Corrigido para bater com o nome da propriedade 'assignedTo' no Domínio/Entity
    @Nonnull
    Flux<AssetEntity> findByAssignedTo(@Nonnull UUID assignedTo);

    // Corrigido para o tipo UUID (antes era String)
    @Nonnull
    Flux<AssetEntity> findByLocationId(@Nonnull UUID locationId);
}