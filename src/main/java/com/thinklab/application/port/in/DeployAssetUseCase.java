package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.DeployAssetCommand;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for the deployment of IT Assets to users or locations.
 * This interface defines the mission-critical contract for transitioning an asset from
 * READY_FOR_DEPLOY to the DEPLOYED state, establishing authoritative ownership.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>State Transition Sovereignty:</b> Enforces the transition into operational
 * use through the aggregate root's Finite State Machine.</li>
 * <li><b>Reactive Atomicity:</b> Ensures that asset allocation and forensic audit
 * registration are handled in a single non-blocking pipeline.</li>
 * <li><b>Defensive Boundary:</b> Strictly consumes the validated {@link DeployAssetCommand}
 * to prevent illegal lifecycle mutations.</li>
 * </ul>
 */
public interface DeployAssetUseCase {

    /**
     * Executes the deployment orchestration for an existing IT Asset.
     *
     * @param command The validated {@link DeployAssetCommand} containing the target
     *                asset, deployment destination (User/Location), and compliance metadata.
     * @return A {@link Mono} emitting the updated {@link Asset} in DEPLOYED status.
     * @throws NullPointerException if the provided command is null.
     * @apiNote Signals an {@code InvalidAssetStatusException} if the asset is not currently
     *          in the READY_FOR_DEPLOY state.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull DeployAssetCommand command);
}