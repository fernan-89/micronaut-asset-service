package com.thinklab.application.interactor;

import com.thinklab.application.port.in.DecommissionAssetUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.command.DecommissionAssetCommand;
import com.thinklab.domain.exception.AssetNotFoundException;
import com.thinklab.domain.exception.InvalidAssetStatusException;
import com.thinklab.domain.model.Asset;
import com.thinklab.domain.model.AssetAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link DecommissionAssetUseCase} input port.
 * This service orchestrates the irreversible decommissioning workflow for IT Assets,
 * ensuring strict adherence to lifecycle governance, state machine validation,
 * and mandatory forensic auditability.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Terminal State Sovereignty:</b> Delegates the final state transition
 * logic to the Aggregate Root to prevent business rule leakage.</li>
 * <li><b>Atomic Persistence:</b> Chains state mutation and audit trail in a
 * single non-blocking pipeline using {@link Mono#flatMap}.</li>
 * <li><b>Explicit DI:</b> Uses constructor injection for AoT compatibility
 * and clear dependency graphing.</li>
 * </ul>
 */
@Slf4j
@Singleton
public class DecommissionAssetInteractor implements DecommissionAssetUseCase {

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    /**
     * Explicit constructor injection mandated by ADR 001 for Micronaut beans.
     */
    @Inject
    public DecommissionAssetInteractor(
            AssetRepositoryPort assetRepository,
            AssetAuditRepositoryPort auditRepository
    ) {
        this.assetRepository = Objects.requireNonNull(assetRepository, "Asset repository port is mandatory");
        this.auditRepository = Objects.requireNonNull(auditRepository, "Audit repository port is mandatory");
    }

    /**
     * Executes the irreversible decommissioning orchestration.
     *
     * @param command The validated {@link DecommissionAssetCommand}.
     * @return A {@link Mono} emitting the decommissioned {@link Asset} aggregate.
     * @throws AssetNotFoundException if the target asset does not exist.
     * @throws InvalidAssetStatusException if the FSM blocks the transition (e.g., already decommissioned).
     */
    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull DecommissionAssetCommand command) {
        Objects.requireNonNull(command, "DecommissionAssetCommand cannot be null");

        return assetRepository.findById(command.assetId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: DECOMMISSION_ASSET] [ID: {}] - Orchestration halted: Entity not found.",
                            command.assetId());
                    return Mono.error(new AssetNotFoundException(command.assetId()));
                }))
                .map(asset -> asset.decommission()) // Domain FSM logic handles the state change
                .flatMap(assetRepository::update) // ADR 002: Always use update for state mutations
                .flatMap(decommissionedAsset -> registerForensicAudit(decommissionedAsset, command.executorId(), command.reason())
                        .thenReturn(decommissionedAsset))
                .doOnSubscribe(s -> log.warn("[ACTION: DECOMMISSION_ASSET] [ID: {}] [EXECUTOR: {}] - CRITICAL: Initiating irreversible entity termination.",
                        command.assetId(), command.executorId()))
                .doOnSuccess(asset -> {
                    if (asset != null) {
                        log.warn("[ACTION: DECOMMISSION_ASSET] [ID: {}] - CRITICAL: Entity permanently transitioned to terminal DECOMMISSIONED state.",
                                asset.id());
                    }
                })
                .doOnError(err -> {
                    if (!(err instanceof AssetNotFoundException || err instanceof InvalidAssetStatusException)) {
                        log.error("[ACTION: DECOMMISSION_ASSET] [ID: {}] - CRITICAL: Termination protocol failed due to system exception: {}",
                                command.assetId(), err.getMessage());
                    }
                });
    }

    /**
     * Records the permanent disposal event in the immutable audit trail.
     */
    private Mono<AssetAudit> registerForensicAudit(Asset asset, String executorId, String reason) {
        AssetAudit auditEntry = AssetAudit.create(
                asset.tenantId(),
                asset.id(),
                "ASSET_DECOMMISSIONING",
                "SUCCESS",
                executorId,
                Map.of(
                        "reason", reason,
                        "terminalAction", "TRUE",
                        "finalStatus", asset.status().name(),
                        "traceabilityTier", "3"
                )
        );
        return auditRepository.save(auditEntry);
    }
}