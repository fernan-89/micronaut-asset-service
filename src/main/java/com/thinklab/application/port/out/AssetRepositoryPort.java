package com.thinklab.application.port.out;

import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AssetRepositoryPort {
    @Nonnull Mono<Asset> save(@Nonnull Asset asset);
    @Nonnull Mono<Asset> update(@Nonnull Asset asset);
    @Nonnull Mono<Asset> findById(@Nonnull UUID id);
    @Nonnull Mono<Boolean> existsById(@Nonnull UUID id);
    @Nonnull Mono<Boolean> existsByTenantIdAndSerialNumber(@Nonnull UUID tenantId, @Nonnull String serialNumber);
    @Nonnull Flux<Asset> findAllByTenantId(@Nonnull UUID tenantId);
    @Nonnull Flux<Asset> findByAssignedTo(@Nonnull UUID assignedTo);
    @Nonnull Flux<Asset> findByLocationId(@Nonnull UUID locationId);
}