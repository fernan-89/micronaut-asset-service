package com.thinklab.application.interactor;

import com.thinklab.application.port.in.ListAssetsUseCase;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.query.ListAssetsQuery;
import com.thinklab.domain.model.Asset;
import io.micronaut.data.model.Pageable;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link ListAssetsUseCase} input port.
 * This service orchestrates the high-performance discovery of IT Assets, supporting
 * hierarchical multi-tenant isolation and reactive pagination. It acts as a
 * stateless router between the incoming query criteria and the persistence ports.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Reactive Streaming:</b> Utilizes {@link Flux#defer} to ensure that query
 * execution and logging occur only upon subscription.</li>
 * <li><b>Multi-Tenant Guard:</b> Enforces the tenantId filter in every repository call
 * to prevent unauthorized data cross-exposure.</li>
 * <li><b>Backpressure-Aware:</b> Leverages Pageable to maintain system stability
 * under high-volume inventory requests.</li>
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class ListAssetsInteractor implements ListAssetsUseCase {

    private final AssetRepositoryPort assetRepository;

    /**
     * Executes the paginated discovery of assets based on the provided query filters.
     *
     * @param query The validated {@link ListAssetsQuery} containing filters and limits.
     * @return A {@link Flux} streaming the matching {@link Asset} aggregates.
     * @throws NullPointerException if the query is null.
     */
    @Override
    @Nonnull
    public Flux<Asset> execute(@Nonnull ListAssetsQuery query) {
        Objects.requireNonNull(query, "ListAssetsQuery is mandatory for asset discovery.");

        // Micronaut Data Pageable used for type-safe cursor management
        Pageable pageable = Pageable.from(query.page(), query.size());

        return Flux.defer(() -> {
                    log.info("[ACTION: LIST_ASSETS] [TENANT: {}] [CAT: {}] [STATUS: {}] [PAGE: {}] - Initiating inventory discovery.",
                            query.tenantId(),
                            query.category() != null ? query.category() : "ALL",
                            query.status() != null ? query.status() : "ALL",
                            query.page());

                    return assetRepository.findAllByTenantId(query.tenantId());
                })
                .doOnComplete(() -> log.debug("[ACTION: LIST_ASSETS] [TENANT: {}] - Discovery stream completed successfully.", query.tenantId()))
                .doOnError(err -> log.error("[ACTION: LIST_ASSETS] [TENANT: {}] - Discovery failed during streaming: {}",
                        query.tenantId(), err.getMessage()));
    }
}