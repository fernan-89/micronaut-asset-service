package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.MarkForMaintenanceCommand;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for transitioning IT Assets into maintenance.
 * This interface defines the contract for orchestrating the withdrawal of an asset
 * from operational use, ensuring that the state transition is validated by the
 * domain's Finite State Machine (FSM) and forensics are recorded.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>State Integrity:</b> Strictly governs the transition from DEPLOYED or
 * READY_FOR_DEPLOY to the UNDER_MAINTENANCE state.</li>
 * <li><b>Reactive Atomicity:</b> Ensures the state update and the forensic audit
 * registration are handled in a single non-blocking pipeline.</li>
 * <li><b>Defensive Boundary:</b> Mandates a validated {@link MarkForMaintenanceCommand}
 * to prevent illegal lifecycle mutations.</li>
 * </ul>
 */
public interface MarkForMaintenanceUseCase {

    /**
     * Executes the maintenance transition orchestration for an existing asset.
     *
     * @param command The validated {@link MarkForMaintenanceCommand} containing the
     *                asset identity, technical executor, and maintenance reason.
     * @return A {@link Mono} emitting the updated {@link Asset} in UNDER_MAINTENANCE status.
     * @throws NullPointerException if the provided command is null.
     * @apiNote Signals an {@code InvalidAssetStatusException} if the asset is in a terminal
     *          state (e.g., DECOMMISSIONED) that prevents maintenance.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull MarkForMaintenanceCommand command);
}

