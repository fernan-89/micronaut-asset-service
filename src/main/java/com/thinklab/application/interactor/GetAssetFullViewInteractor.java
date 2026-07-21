package com.thinklab.application.interactor;

import com.thinklab.application.port.in.GetAssetFullViewUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.domain.exception.AssetNotFoundException;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Interactor: Implementation of the {@link GetAssetFullViewUseCase} input port.
 * This service orchestrates the high-fidelity retrieval of an Asset's current state
 * alongside its complete forensic audit trail. It utilizes reactive composition to
 * consolidate data from multiple persistence sources in parallel.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Parallel Composition:</b> Uses {@link Mono#zip} to fetch history and state simultaneously.</li>
 * <li><b>Reactive Integrity:</b> Ensures non-blocking execution throughout the retrieval pipeline.</li>
 * <li><b>Semantic Error Handling:</b> Signals specialized domain exceptions for missing records.</li>
 * </ul>
 *
 * @version 1.0.0
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class GetAssetFullViewInteractor implements GetAssetFullViewUseCase {

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    /**
     * Executes the comprehensive retrieval and composition of the Asset Full View.
     *
     * @param assetId The unique system identifier of the organization.
     * @return A {@link Mono} emitting the consolidated {@link AssetFullView}.
     * @throws AssetNotFoundException if the asset is not found.
     */
    @Override
    @Nonnull
    public Mono<AssetFullView> execute(@Nonnull UUID assetId) {
        Objects.requireNonNull(assetId, "Asset ID is mandatory for full view retrieval.");

        return assetRepository.findById(assetId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: GET_ASSET_FULL_VIEW] [ID: {}] - Orchestration halted: Entity not found.", assetId);
                    return Mono.error(new AssetNotFoundException(assetId));
                }))
                .flatMap(this::composeWithAuditTrail)
                .doOnSubscribe(s -> log.info("[ACTION: GET_ASSET_FULL_VIEW] [ID: {}] - Initiating 360-degree forensic retrieval.", assetId))
                .doOnSuccess(view -> log.info("[ACTION: GET_ASSET_FULL_VIEW] [ID: {}] - Consolidated view successfully projected with [{}] audit entries.",
                        assetId, view.auditLogs().size()))
                .doOnError(err -> {
                    if (!(err instanceof AssetNotFoundException)) {
                        log.error("[ACTION: GET_ASSET_FULL_VIEW] [ID: {}] - Critical failure during data composition: {}",
                                assetId, err.getMessage());
                    }
                });
    }

    /**
     * Internal parallel orchestrator to fetch audits once asset existence is confirmed.
     */
    private Mono<AssetFullView> composeWithAuditTrail(com.thinklab.domain.model.Asset asset) {
        return auditRepository.findByAssetId(asset.id())
                .collectList()
                .map(logs -> new AssetFullView(asset, logs));
    }
}