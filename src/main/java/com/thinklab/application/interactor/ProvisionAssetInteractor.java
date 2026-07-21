package com.thinklab.application.interactor;

import com.thinklab.application.port.in.ProvisionAssetUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.command.ProvisionAssetCommand;
import com.thinklab.domain.exception.AssetAlreadyProvisionedException;
import com.thinklab.domain.model.Asset;
import com.thinklab.domain.model.AssetAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link ProvisionAssetUseCase} input port.
 * This service orchestrates the high-assurance provisioning workflow for IT Assets,
 * ensuring deterministic identity generation, multi-tenant isolation, and
 * mandatory forensic auditability.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Idempotency Guard:</b> Uses deterministic UUIDs to prevent physical hardware duplication.</li>
 * <li><b>Reactive Atomicity:</b> Chains existence checks, persistence, and auditing in a non-blocking pipeline.</li>
 * <li><b>Domain Sovereignty:</b> Coordinates domain factory methods without leaking database specifics.</li>
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class ProvisionAssetInteractor implements ProvisionAssetUseCase {

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    /**
     * Executes the provisioning orchestration for a new asset.
     *
     * @param command The validated {@link ProvisionAssetCommand} intent.
     * @return A {@link Mono} emitting the successfully provisioned and audited {@link Asset}.
     * @throws AssetAlreadyProvisionedException if a duplicate deterministic ID is detected.
     */
    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull ProvisionAssetCommand command) {
        Objects.requireNonNull(command, "ProvisionAssetCommand cannot be null");

        // 1. Prepare domain object (generates deterministic ID internally)
        Asset newAsset = Asset.provision(
                command.tenantId(),
                command.name(),
                command.category(),
                command.serialNumber(),
                command.specifications()
        );

        return assetRepository.existsById(newAsset.id())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("[ACTION: PROVISION_ASSET] [TENANT: {}] [SN: {}] - Collision detected: Asset already exists.",
                                command.tenantId(), command.serialNumber());
                        return Mono.error(new AssetAlreadyProvisionedException(command.serialNumber()));
                    }
                    return performProvisioning(newAsset, command.executorId());
                })
                .doOnSubscribe(s -> log.info("[ACTION: PROVISION_ASSET] [TENANT: {}] [SN: {}] - Initiating provisioning orchestration.",
                        command.tenantId(), command.serialNumber()))
                .doOnError(err -> {
                    if (!(err instanceof AssetAlreadyProvisionedException)) {
                        log.error("[ACTION: PROVISION_ASSET] [TENANT: {}] - Critical failure during pipeline execution: {}",
                                command.tenantId(), err.getMessage());
                    }
                });
    }

    /**
     * Persists the asset and triggers the forensic audit registration.
     */
    private Mono<Asset> performProvisioning(Asset asset, String executorId) {
        return assetRepository.save(asset)
                .flatMap(savedAsset -> registerForensicAudit(savedAsset, executorId)
                        .thenReturn(savedAsset))
                .doOnSuccess(saved -> log.info("[ACTION: PROVISION_ASSET] [ID: {}] - Provisioning successfully completed and audited.",
                        saved.id()));
    }

    /**
     * Constructs and persists the immutable audit record.
     */
    private Mono<AssetAudit> registerForensicAudit(Asset asset, String executorId) {
        AssetAudit auditEntry = AssetAudit.create(
                asset.tenantId(),
                asset.id(),
                "ASSET_PROVISIONING",
                "SUCCESS",
                executorId,
                Map.of(
                        "category", asset.category().name(),
                        "serialNumber", asset.serialNumber(),
                        "traceabilityTier", "3"
                )
        );
        return auditRepository.save(auditEntry);
    }
}