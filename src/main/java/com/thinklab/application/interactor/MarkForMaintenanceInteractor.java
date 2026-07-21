package com.thinklab.application.interactor;

import com.thinklab.application.port.in.MarkForMaintenanceUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.command.MarkForMaintenanceCommand;
import com.thinklab.domain.exception.AssetNotFoundException;
import com.thinklab.domain.exception.InvalidAssetStatusException;
import com.thinklab.domain.model.Asset;
import com.thinklab.domain.model.AssetAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link MarkForMaintenanceUseCase} input port.
 * This service orchestrates the transition of an IT Asset into a maintenance state,
 * ensuring strict adherence to lifecycle governance and forensic audit requirements.
 */
@Singleton
public class MarkForMaintenanceInteractor implements MarkForMaintenanceUseCase {

    // Substituímos o @Slf4j pela declaração explícita
    private static final Logger log = LoggerFactory.getLogger(MarkForMaintenanceInteractor.class);

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    @Inject
    public MarkForMaintenanceInteractor(
            AssetRepositoryPort assetRepository,
            AssetAuditRepositoryPort auditRepository
    ) {
        this.assetRepository = Objects.requireNonNull(assetRepository, "Asset repository is mandatory");
        this.auditRepository = Objects.requireNonNull(auditRepository, "Audit repository is mandatory");
    }

    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull MarkForMaintenanceCommand command) {
        Objects.requireNonNull(command, "MarkForMaintenanceCommand cannot be null");

        return assetRepository.findById(command.assetId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: MARK_ASSET_MAINTENANCE] [ID: {}] - Orchestration halted: Asset not found.",
                            command.assetId());
                    return Mono.error(new AssetNotFoundException(command.assetId()));
                }))
                .map(asset -> asset.sendToMaintenance()) // Corrigido para o método real no Domínio
                .flatMap(updatedAsset -> assetRepository.update(updatedAsset)) // Referência corrigida
                .flatMap(updatedAsset -> registerForensicAudit(updatedAsset, command.executorId(), command.reason())
                        .thenReturn(updatedAsset))
                .doOnSubscribe(s -> log.info("[ACTION: MARK_ASSET_MAINTENANCE] [ID: {}] - Initiating maintenance withdrawal.",
                        command.assetId()))
                .doOnSuccess(asset -> {
                    if (asset != null) {
                        log.info("[ACTION: MARK_ASSET_MAINTENANCE] [ID: {}] - Asset successfully transitioned to UNDER_MAINTENANCE.",
                                asset.id());
                    }
                })
                .doOnError(err -> {
                    if (!(err instanceof AssetNotFoundException || err instanceof InvalidAssetStatusException)) {
                        log.error("[ACTION: MARK_ASSET_MAINTENANCE] [ID: {}] - Critical system failure: {}",
                                command.assetId(), err.getMessage());
                    }
                });
    }

    private Mono<AssetAudit> registerForensicAudit(Asset asset, String executorId, String reason) {
        AssetAudit auditEntry = AssetAudit.create(
                asset.tenantId(),
                asset.id(),
                "ASSET_MAINTENANCE_START",
                "SUCCESS",
                executorId,
                Map.of(
                        "reason", reason,
                        "status", asset.status().name(),
                        "traceabilityTier", "3"
                )
        );
        return auditRepository.save(auditEntry);
    }
}