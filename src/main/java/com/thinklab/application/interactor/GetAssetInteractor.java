package com.thinklab.application.interactor;

import com.thinklab.application.port.in.GetAssetUseCase;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.domain.exception.AssetNotFoundException;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Interactor: Implementation of the {@link GetAssetUseCase} input port.
 * This service provides high-performance, read-only access to the current state
 * of an IT Asset. It strictly adheres to the CQRS principle by separating
 * direct retrieval from complex historical projections or state mutations.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Lean Retrieval:</b> Focused on direct aggregate access without audit overhead.</li>
 * <li><b>Reactive Guard:</b> Utilizes {@link Mono#switchIfEmpty} to signal domain exceptions reatively.</li>
 * <li><b>Thread-Safe Orchestration:</b> Stateless implementation safe for high-concurrency environments.</li>
 * </ul>
 *
 * @version 1.0.0
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class GetAssetInteractor implements GetAssetUseCase {

    private final AssetRepositoryPort assetRepository;

    /**
     * Executes the direct retrieval of an Asset by its system identifier.
     *
     * @param assetId The unique system UUID of the target asset.
     * @return A {@link Mono} emitting the requested {@link Asset} aggregate.
     * @throws AssetNotFoundException if the record is missing from the repository.
     */
    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull UUID assetId) {
        Objects.requireNonNull(assetId, "Asset ID is mandatory for retrieval.");

        return assetRepository.findById(assetId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: GET_ASSET] [ID: {}] - Discovery halted: Record not found in persistence.", assetId);
                    return Mono.error(new AssetNotFoundException(assetId));
                }))
                .doOnSubscribe(s -> log.info("[ACTION: GET_ASSET] [ID: {}] - Initiating direct state lookup.", assetId))
                .doOnSuccess(asset -> {
                    if (asset != null) {
                        log.debug("[ACTION: GET_ASSET] [ID: {}] - Record successfully located and projected.", asset.id());
                    }
                })
                .doOnError(err -> {
                    if (!(err instanceof AssetNotFoundException)) {
                        log.error("[ACTION: GET_ASSET] [ID: {}] - Critical system failure during retrieval: {}",
                                assetId, err.getMessage());
                    }
                });
    }
}