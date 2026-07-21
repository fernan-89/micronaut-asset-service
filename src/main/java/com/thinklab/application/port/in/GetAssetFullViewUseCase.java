package com.thinklab.application.port.in;

import com.thinklab.domain.model.Asset;
import com.thinklab.domain.model.AssetAudit;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Application Port: Input boundary for retrieving a comprehensive 360-degree view of an IT Asset.
 * Following the CQRS principle for read-side operations, this use case orchestrates the
 * simultaneous retrieval of an Asset's current state and its complete immutable
 * forensic audit trail (lifecycle history).
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Data Consolidation:</b> Aggregates state and forensics to reduce API round-trips.</li>
 * <li><b>Reactive Parallelism:</b> Designed for non-blocking composition of multiple data sources.</li>
 * <li><b>Immutability:</b> Leverages Java Records to guarantee thread-safe data projection.</li>
 * </ul>
 */
public interface GetAssetFullViewUseCase {

    /**
     * Data structure representing the consolidated 360-degree view of an IT Asset.
     * Engineered for high-performance serialization without runtime reflection.
     */
    @Serdeable
    @Introspected
    record AssetFullView(
            @Nonnull Asset asset,
            @Nonnull List<AssetAudit> auditLogs
    ) {}

    /**
     * Executes the high-fidelity retrieval and composition of the Asset Full View.
     *
     * @param assetId The unique system identifier (UUID) of the target IT Asset.
     * @return A {@link Mono} emitting the {@link AssetFullView} containing the aggregate
     *         and its forensic timeline.
     * @throws NullPointerException if the provided assetId is null.
     * @apiNote Signals an {@code AssetNotFoundException} through the reactive pipeline
     *          if the record is missing in the authoritative registry.
     */
    @Nonnull
    Mono<AssetFullView> execute(@Nonnull java.util.UUID assetId);
}