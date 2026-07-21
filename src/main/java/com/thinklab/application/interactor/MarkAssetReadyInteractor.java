package com.thinklab.application.interactor;

import com.thinklab.application.port.in.MarkAssetReadyUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.command.MarkAssetReadyCommand;
import com.thinklab.domain.exception.AssetNotFoundException;
import com.thinklab.domain.exception.InvalidAssetStatusException;
import com.thinklab.domain.model.Asset;
import com.thinklab.domain.model.AssetAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link MarkAssetReadyUseCase} input port.
 * This service orchestrates the transition of an IT Asset to the operational readiness state,
 * ensuring that lifecycle governance and forensic audit requirements are strictly met.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>State Sovereignty:</b> Delegates transition validation to the Asset Aggregate Root.</li>
 * <li><b>Reactive Atomicity:</b> Chains retrieval, mutation, and auditing in a non-blocking flow.</li>
 * <li><b>Explicit DI:</b> Utilizes constructor injection for AoT compatibility and testability.</li>
 * </ul>
 */
@Slf4j
@Singleton
public class MarkAssetReadyInteractor implements MarkAssetReadyUseCase {

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    @Inject
    public MarkAssetReadyInteractor(
            AssetRepositoryPort assetRepository,
            AssetAuditRepositoryPort auditRepository
    ) {
        this.assetRepository = assetRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Executes the readiness transition orchestration.
     *
     * @param command The validated {@link MarkAssetReadyCommand}.
     * @return A {@link Mono} emitting the updated {@link Asset}.
     * @throws AssetNotFoundException if the asset does not exist.
     * @throws InvalidAssetStatusException if the state transition is illegal.
     */
    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull MarkAssetReadyCommand command) {
        Objects.requireNonNull(command, "MarkAssetReadyCommand cannot be null");

        return assetRepository.findById(command.assetId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: MARK_ASSET_READY] [ID: {}] - Transition halted: Asset not found.", command.assetId());
                    return Mono.error(new AssetNotFoundException(command.assetId()));
                }))
                .map(asset -> asset.markAsReady()) // Domain logic handles validation
                .flatMap(assetRepository::update)
                .flatMap(updatedAsset -> registerForensicAudit(updatedAsset, command.executorId())
                        .thenReturn(updatedAsset))
                .doOnSubscribe(s -> log.info("[ACTION: MARK_ASSET_READY] [ID: {}] - Initiating readiness transition.", command.assetId()))
                .doOnSuccess(asset -> {
                    if (asset != null) {
                        log.info("[ACTION: MARK_ASSET_READY] [ID: {}] - Asset marked as READY and audited.", asset.id());
                    }
                })
                .doOnError(err -> {
                    if (!(err instanceof AssetNotFoundException || err instanceof InvalidAssetStatusException)) {
                        log.error("[ACTION: MARK_ASSET_READY] [ID: {}] - Critical failure: {}", command.assetId(), err.getMessage());
                    }
                });
    }

    /**
     * Internal dispatcher to persist the immutable audit record.
     */
    private Mono<AssetAudit> registerForensicAudit(Asset asset, String executorId) {
        AssetAudit auditEntry = AssetAudit.create(
                asset.tenantId(),
                asset.id(),
                "ASSET_READINESS_CONFIRMED",
                "SUCCESS",
                executorId,
                Map.of(
                        "newStatus", asset.status().name(),
                        "version", String.valueOf(asset.version()),
                        "traceabilityTier", "3"
                )
        );
        return auditRepository.save(auditEntry);
    }
}