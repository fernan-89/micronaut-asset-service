package com.thinklab.infrastructure.adapter.out.mongo;

import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.domain.model.AssetAudit;
import com.thinklab.infrastructure.adapter.out.mongo.entity.AssetAuditEntity;
import com.thinklab.infrastructure.adapter.out.mongo.repository.MongoAssetAuditRepository;
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
 * Persistence Adapter: Implementation of the {@link AssetAuditRepositoryPort} for MongoDB.
 */
@Singleton
public class AssetAuditRepositoryAdapter implements AssetAuditRepositoryPort {

    // Substituímos o @Slf4j do Lombok por uma instância explícita para evitar conflitos de anotação com o Micronaut
    private static final Logger log = LoggerFactory.getLogger(AssetAuditRepositoryAdapter.class);

    private final MongoAssetAuditRepository repository;

    /**
     * Explicit constructor injection mandated by ADR 001 for reliable proxy generation.
     */
    @Inject
    public AssetAuditRepositoryAdapter(MongoAssetAuditRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Mongo Asset Audit repository is mandatory.");
    }

    /**
     * Persists an immutable forensic record to the ledger.
     */
    @Override
    @Nonnull
    public Mono<AssetAudit> save(@Nonnull AssetAudit audit) {
        Objects.requireNonNull(audit, "AssetAudit aggregate cannot be null for persistence.");

        return repository.save(AssetAuditEntity.fromDomain(audit))
                .map(AssetAuditEntity::toDomain)
                .doOnSuccess(a -> log.debug("[ACTION: PERSIST_AUDIT] [ID: {}] [OP: {}] - Forensic record committed successfully.", a.id(), a.operation()))
                .doOnError(e -> log.error("[ACTION: PERSIST_AUDIT] [ASSET: {}] - Critical persistence failure: {}", audit.assetId(), e.getMessage()));
    }

    /**
     * Retrieves the chronological historical trail for a specific asset.
     */
    @Override
    @Nonnull
    public Flux<AssetAudit> findByAssetId(@Nonnull UUID assetId) {
        Objects.requireNonNull(assetId, "Asset ID is mandatory for forensic retrieval.");

        return repository.findByAssetIdOrderByTimestampDesc(assetId)
                .map(AssetAuditEntity::toDomain)
                .doOnSubscribe(s -> log.debug("[ACTION: RETRIEVE_AUDIT_TRAIL] [ASSET: {}] - Initiating historical reconstruction.", assetId));
    }

    /**
     * Retrieves all organization-scoped logs for compliance reporting.
     */
    @Override
    @Nonnull
    public Flux<AssetAudit> findByTenantId(@Nonnull UUID tenantId) {
        Objects.requireNonNull(tenantId, "Tenant context is mandatory for compliance discovery.");

        return repository.findByTenantIdOrderByTimestampDesc(tenantId)
                .map(AssetAuditEntity::toDomain);
    }

    /**
     * Retrieves logs associated with a specific agent/executor.
     */
    @Override
    @Nonnull
    public Flux<AssetAudit> findByExecutorId(@Nonnull String executorId) {
        return repository.findByExecutorId(Objects.requireNonNull(executorId))
                .map(AssetAuditEntity::toDomain);
    }

    /**
     * Implementação do método exigido pela AssetAuditRepositoryPort.
     */
    @Override
    @Nonnull
    public Flux<AssetAudit> findByTxId(@Nonnull String txId) { // <--- AQUI: mude de Mono para Flux
        Objects.requireNonNull(txId, "Transaction ID is mandatory.");
        return repository.findByTxId(txId)
                .map(AssetAuditEntity::toDomain);
    }
}