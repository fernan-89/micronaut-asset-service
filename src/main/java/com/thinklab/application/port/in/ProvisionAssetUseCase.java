package com.thinklab.application.port.in;

import com.thinklab.application.usecase.command.ProvisionAssetCommand;
import com.thinklab.domain.model.Asset;
import jakarta.annotation.Nonnull;
import reactor.core.publisher.Mono;

/**
 * Application Port: Input boundary for the provisioning of new IT Assets.
 * This interface defines the formalized contract that implementation services (Interactors)
 * must satisfy to establish a new physical or logical asset within the enterprise registry.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Input Boundary Isolation:</b> Strictly uses a validated Command object to shield the core logic.</li>
 * <li><b>Reactive Non-Blocking:</b> Enforces asynchronous execution using {@link Mono} to preserve event-loop responsiveness.</li>
 * <li><b>Forensic Sovereignty:</b> Mandates that implementers orchestrate both entity persistence and audit trail registration.</li>
 * </ul>
 *
 * @version 1.0.0
 */
public interface ProvisionAssetUseCase {

    /**
     * Orchestrates the creation, deterministic identity generation, and atomic persistence
     * of a new {@link Asset} aggregate.
     *
     * @param command The validated {@link ProvisionAssetCommand} containing the tenant context,
     *                hardware classification, and technical specifications.
     * @return A {@link Mono} emitting the successfully provisioned and audited {@link Asset}.
     * @throws NullPointerException if the provided command is null, ensuring fail-fast integrity.
     */
    @Nonnull
    Mono<Asset> execute(@Nonnull ProvisionAssetCommand command);
}