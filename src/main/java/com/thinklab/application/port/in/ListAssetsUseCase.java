package com.thinklab.application.port.in;

import com.thinklab.application.usecase.query.ListAssetsQuery;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Flux;

/**
 * Application Port: Input boundary for the paginated discovery of IT Assets.
 * Following the CQRS principle, this interface defines a high-performance,
 * read-only stream contract for retrieving assets filtered by business
 * criteria and strictly scoped to a tenant context.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Reactive Streaming:</b> Utilizes {@link Flux} to support backpressure and
 * efficient memory management during large inventory exports.</li>
 * <li><b>Multi-Tenant Sovereignty:</b> Enforces that all implementations respect
 * the tenant isolation defined in the query object.</li>
 * <li><b>Non-Blocking I/O:</b> Designed for end-to-end asynchronous execution.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface ListAssetsUseCase {

    /**
     * Executes the reactive retrieval of a paginated list of assets.
     *
     * @param query The validated {@link ListAssetsQuery} containing filters and limits.
     * @return A {@link Flux} streaming the matching {@link Asset} aggregates.
     * @throws NullPointerException if the provided query is null, preserving pipeline integrity.
     */
    @Nonnull
    Flux<Asset> execute(@Nonnull ListAssetsQuery query);
}