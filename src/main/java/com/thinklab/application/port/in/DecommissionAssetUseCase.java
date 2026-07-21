package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.DecommissionAssetCommand;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for the permanent decommissioning of IT Assets.
 * This interface defines the mission-critical contract for transitioning an asset
 * to its terminal DECOMMISSIONED state, signifying end-of-life or disposal.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Terminal State Sovereignty:</b> Enforces that decommissioning is an 
 * irreversible operation governed by the Aggregate Root.</li>
 * <li><b>Reactive Non-Blocking:</b> Mandates asynchronous execution to maintain 
 * high throughput during bulk inventory liquidations.</li>
 * <li><b>Compliance Lock:</b> Ensures that every termination is backed by a 
 * validated {@link DecommissionAssetCommand} containing the mandatory forensic reason.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface DecommissionAssetUseCase {

    /**
     * Orchestrates the irreversible decommissioning workflow for an IT Asset.
     *
     * @param command The validated {@link DecommissionAssetCommand} containing 
     *                the asset identity, authorized agent, and disposal justification.
     * @return A {@link Mono} emitting the updated {@link Asset} in its terminal state.
     * @throws NullPointerException if the provided command is null.
     * @apiNote Once successfully decommissioned, the asset is considered logically 
     *          inactive and cannot transition to any other operational state.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull DecommissionAssetCommand command);
}