package com.thinklab.infrastructure.adapter.out.mongo;

import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.domain.model.Asset;
import com.thinklab.infrastructure.adapter.out.mongo.entity.AssetEntity;
import com.thinklab.infrastructure.adapter.out.mongo.repository.MongoAssetRepository;
import io.micronaut.data.model.Pageable;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence Adapter: Implementation of the {@link AssetRepositoryPort} for MongoDB.
 */
@Singleton
public class AssetRepositoryAdapter implements AssetRepositoryPort {

    // Substituímos o @Slf4j do Lombok por uma declaração explícita
    private static final Logger log = LoggerFactory.getLogger(AssetRepositoryAdapter.class);

    private final MongoAssetRepository repository;

    @Inject
    public AssetRepositoryAdapter(MongoAssetRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Mongo repository dependency is mandatory.");
    }

    @Override
    @Nonnull
    public Mono<Asset> save(@Nonnull Asset asset) {
        Objects.requireNonNull(asset, "Asset aggregate cannot be null for persistence.");

        return repository.save(AssetEntity.fromDomain(asset))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("[ACTION: PERSIST_ASSET] [ID: {}] - Failure: Database returned empty signal on save.", asset.id());
                    return Mono.error(new IllegalStateException("Persistence failed: record was not inserted."));
                }))
                .map(AssetEntity::toDomain)
                .doOnSuccess(a -> log.info("[ACTION: PERSIST_ASSET] [ID: {}] - Deterministic identity successfully committed.", a.id()))
                .doOnError(e -> log.error("[ACTION: PERSIST_ASSET] [ID: {}] - Persistence violation: {}", asset.id(), e.getMessage()));
    }

    @Override
    @Nonnull
    public Mono<Asset> update(@Nonnull Asset asset) {
        Objects.requireNonNull(asset, "Asset aggregate cannot be null for synchronization.");

        return repository.update(AssetEntity.fromDomain(asset))
                .map(AssetEntity::toDomain)
                .doOnSuccess(a -> log.debug("[ACTION: SYNCHRONIZE_ASSET] [ID: {}] - Domain mutation successfully mirrored to persistence.", a.id()))
                .doOnError(e -> log.error("[ACTION: SYNCHRONIZE_ASSET] [ID: {}] - Critical synchronization failure: {}", asset.id(), e.getMessage()));
    }

    @Override
    @Nonnull
    public Mono<Asset> findById(@Nonnull UUID id) {
        return repository.findById(Objects.requireNonNull(id))
                .map(AssetEntity::toDomain)
                .doOnError(e -> log.error("[ACTION: FIND_ASSET_BY_ID] [ID: {}] - Retrieval error: {}", id, e.getMessage()));
    }

    // Método que a Porta exigia e estava faltando
    @Override
    @Nonnull
    public Mono<Boolean> existsById(@Nonnull UUID id) {
        return repository.existsById(Objects.requireNonNull(id));
    }

    @Override
    @Nonnull
    public Mono<Boolean> existsByTenantIdAndSerialNumber(@Nonnull UUID tenantId, @Nonnull String serialNumber) {
        return repository.existsByTenantIdAndSerialNumber(tenantId, serialNumber)
                .defaultIfEmpty(false);
    }

    @Override
    @Nonnull
    public Flux<Asset> findAllByTenantId(@Nonnull UUID tenantId) {
        return repository.findAllByTenantId(tenantId, Pageable.from(0, 100))
                .map(AssetEntity::toDomain);
    }

    // Alinhado para "assignedTo" (igual ao Domínio)
    @Override
    @Nonnull
    public Flux<Asset> findByAssignedTo(@Nonnull UUID assignedTo) {
        return repository.findByAssignedTo(assignedTo)
                .map(AssetEntity::toDomain);
    }

    // Alinhado para receber UUID em locationId
    @Override
    @Nonnull
    public Flux<Asset> findByLocationId(@Nonnull UUID locationId) {
        return repository.findByLocationId(locationId)
                .map(AssetEntity::toDomain);
    }
}