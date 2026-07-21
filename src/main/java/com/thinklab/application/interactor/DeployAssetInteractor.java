package com.thinklab.application.interactor;

import com.thinklab.application.port.in.DeployAssetUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.command.DeployAssetCommand;
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
import java.util.UUID;

/**
 * Application Interactor: Implementation of the {@link DeployAssetUseCase} input port.
 * This service orchestrates the high-assurance deployment process for IT Assets,
 * ensuring that hardware is correctly assigned to users or locations while
 * maintaining a strictly audited state transition.
 */
@Singleton
public class DeployAssetInteractor implements DeployAssetUseCase {

    // Declaração explícita do Logger substituindo o @Slf4j
    private static final Logger log = LoggerFactory.getLogger(DeployAssetInteractor.class);

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    @Inject
    public DeployAssetInteractor(
            AssetRepositoryPort assetRepository,
            AssetAuditRepositoryPort auditRepository
    ) {
        this.assetRepository = assetRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull DeployAssetCommand command) {
        Objects.requireNonNull(command, "DeployAssetCommand cannot be null");

        return assetRepository.findById(command.assetId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[ACTION: DEPLOY_ASSET] [ID: {}] - Deployment halted: Asset not found.", command.assetId());
                    return Mono.error(new AssetNotFoundException(command.assetId()));
                }))
                .map(asset -> {
                    // Conversão segura da String do Command para o UUID que o Domínio espera
                    UUID locationUuid = (command.locationId() != null && !command.locationId().isBlank())
                            ? UUID.fromString(command.locationId())
                            : null;

                    // Domain FSM handles validation and state change
                    return asset.deploy(command.assignedToUserId(), locationUuid);
                })
                .flatMap(updatedAsset -> assetRepository.update(updatedAsset)) // Referência corrigida para evitar inferência de Object
                .flatMap(deployedAsset -> registerForensicAudit(deployedAsset, command.executorId(), command.reason())
                        .thenReturn(deployedAsset))
                .doOnSubscribe(s -> log.info("[ACTION: DEPLOY_ASSET] [ID: {}] - Initiating deployment for [USER: {}] [LOC: {}].",
                        command.assetId(), command.assignedToUserId(), command.locationId()))
                .doOnSuccess(asset -> {
                    if (asset != null) {
                        log.info("[ACTION: DEPLOY_ASSET] [ID: {}] - Deployment successfully finalized and audited.", asset.id());
                    }
                })
                .doOnError(err -> {
                    if (!(err instanceof AssetNotFoundException || err instanceof InvalidAssetStatusException)) {
                        log.error("[ACTION: DEPLOY_ASSET] [ID: {}] - Critical system failure: {}", command.assetId(), err.getMessage());
                    }
                });
    }

    /**
     * Records the deployment event in the immutable audit trail.
     */
    private Mono<AssetAudit> registerForensicAudit(Asset asset, String executorId, String reason) {
        AssetAudit auditEntry = AssetAudit.create(
                asset.tenantId(),
                asset.id(),
                "ASSET_DEPLOYMENT",
                "SUCCESS",
                executorId,
                Map.of(
                        // Corrigido: o Domínio usa apenas assignedTo()
                        "assignedToUserId", Objects.toString(asset.assignedTo(), "N/A"),
                        "locationId", Objects.toString(asset.locationId(), "N/A"),
                        "reason", reason,
                        "traceabilityTier", "3"
                )
        );
        return auditRepository.save(auditEntry);
    }
}