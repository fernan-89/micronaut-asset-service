package com.thinklab.application.port.in;

import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Application Port: Input boundary for retrieving a single IT Asset.
 * Following the CQRS (Command Query Responsibility Segregation) principle, this interface
 * represents a pure read-only operation designed for high-performance retrieval
 * of an asset's current state and specifications.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Read-Only Optimization:</b> Focused strictly on current state, bypassing audit trail overhead.</li>
 * <li><b>Reactive Integrity:</b> Enforces non-blocking execution using {@link Mono} to preserve event-loop scalability.</li>
 * <li><b>Semantic Error Signaling:</b> Designed to emit business-specific signals (AssetNotFoundException) through the stream.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface GetAssetUseCase {

    /**
     * Executes the retrieval of an IT Asset by its unique deterministic identifier.
     *
     * @param assetId The unique system UUID of the target asset.
     * @return A {@link Mono} emitting the found {@link Asset} aggregate.
     * @throws NullPointerException if the provided assetId is null, preserving pipeline integrity.
     * @apiNote Emits an {@code AssetNotFoundException} signal through the reactive pipeline if the record is missing.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull UUID assetId);
}