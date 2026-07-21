package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.CreateAssetCommand;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for the provisioning of new IT Assets.
 * This interface defines the authoritative contract for registering hardware within
 * the organizational inventory. It ensures that the lifecycle begins in a valid
 * PROVISIONED state and triggers the initial forensic audit trail.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Domain Integrity:</b> Enforces that only validated commands can initiate
 * the creation of an Asset aggregate root.</li>
 * <li><b>Reactive Non-Blocking:</b> Mandates an asynchronous pipeline using {@link Mono}
 * to maintain high throughput during bulk ingestion.</li>
 * <li><b>Fail-Fast Validation:</b> Strictly protected by Jakarta constraints and
 * nullability guards to prevent corrupted state entry.</li>
 * </ul>
 */
public interface CreateAssetUseCase {

    /**
     * Executes the orchestration logic to provision a new IT Asset.
     *
     * @param command The validated {@link CreateAssetCommand} containing
     *                specification, identity, and execution context.
     * @return A {@link Mono} emitting the successfully persisted {@link Asset} aggregate.
     * @throws NullPointerException if the provided command is null.
     * @apiNote Emits an {@code AssetAlreadyExistsException} if the serial number
     *          is already registered within the same tenant context.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull CreateAssetCommand command);
}