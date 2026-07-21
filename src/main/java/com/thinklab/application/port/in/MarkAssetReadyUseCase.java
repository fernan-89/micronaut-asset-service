package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.MarkAssetReadyCommand;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for confirming the readiness of an IT Asset.
 * This interface defines the contract for transitioning an asset from its initial
 * PROVISIONED state to the READY_FOR_DEPLOY state, signifying that all 
 * baseline configurations and inspections are complete.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>State Machine Integrity:</b> Enforces strict lifecycle transitions through 
 * the aggregate's internal logic.</li>
 * <li><b>Reactive Non-Blocking:</b> Ensures the transition and its mandatory audit 
 * trail are processed asynchronously.</li>
 * <li><b>Fail-Fast Validation:</b> Mandates a validated command to prevent illegal 
 * state mutations.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface MarkAssetReadyUseCase {

    /**
     * Executes the business logic to mark an asset as ready for deployment.
     *
     * @param command The validated {@link MarkAssetReadyCommand} containing the 
     *                asset identity and execution context.
     * @return A {@link Mono} emitting the updated {@link Asset} aggregate.
     * @throws NullPointerException if the provided command is null.
     * @apiNote Signals an {@code InvalidAssetStatusException} if the asset is not in PROVISIONED state.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull MarkAssetReadyCommand command);
}