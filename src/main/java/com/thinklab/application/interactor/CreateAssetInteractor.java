package com.thinklab.application.interactor;

import com.thinklab.application.port.in.CreateAssetUseCase;
import com.thinklab.application.port.out.AssetAuditRepositoryPort;
import com.thinklab.application.port.out.AssetRepositoryPort;
import com.thinklab.application.usecase.command.CreateAssetCommand;
import com.thinklab.domain.exception.AssetAlreadyExistsException;
import com.thinklab.domain.model.Asset;
import com.thinklab.domain.model.AssetAudit;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Application Interactor: Implementation of the {@link CreateAssetUseCase} input port.
 */
@Singleton
public class CreateAssetInteractor implements CreateAssetUseCase {

    // Substituímos o @Slf4j pela declaração explícita para evitar o erro "cannot find symbol variable log"
    private static final Logger log = LoggerFactory.getLogger(CreateAssetInteractor.class);

    private final AssetRepositoryPort assetRepository;
    private final AssetAuditRepositoryPort auditRepository;

    @Inject
    public CreateAssetInteractor(
            AssetRepositoryPort assetRepository,
            AssetAuditRepositoryPort auditRepository
    ) {
        this.assetRepository = Objects.requireNonNull(assetRepository, "Asset repository port is mandatory");
        this.auditRepository = Objects.requireNonNull(auditRepository, "Audit repository port is mandatory");
    }

    @Override
    @Nonnull
    public Mono<Asset> execute(@Nonnull CreateAssetCommand command) {
        Objects.requireNonNull(command, "CreateAssetCommand cannot be null");

        return assetRepository.existsByTenantIdAndSerialNumber(command.tenantId(), command.serialNumber())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("[ACTION: PROVISION_ASSET] [TENANT: {}] [SERIAL: {}] - Orchestration halted: Duplicate serial number detected.",
                                command.tenantId(), command.serialNumber());
                        return Mono.error(new AssetAlreadyExistsException(command.tenantId(), command.serialNumber()));
                    }
                    return performProvisioning(command);
                })
                .doOnSubscribe(s -> log.info("[ACTION: PROVISION_ASSET] [TENANT: {}] - Initiating hardware registration sequence.", command.tenantId()))
                .doOnError(err -> {
                    if (!(err instanceof AssetAlreadyExistsException)) {
                        log.error("[ACTION: PROVISION_ASSET] [TENANT: {}] - Critical system failure: {}", command.tenantId(), err.getMessage());
                    }
                });
    }

    private Mono<Asset> performProvisioning(CreateAssetCommand command) {
        // Correção do erro de Map<String, String> para Map<String, Object>
        Map<String, Object> specsAsObject = new HashMap<>(command.specifications());

        // Domain Logic: Instantiate the Aggregate Root
        Asset newAsset = Asset.provision(
                command.tenantId(),
                command.name(),
                command.category(),
                command.serialNumber(),
                specsAsObject
        );

        return assetRepository.save(newAsset) // ADR 002: Use save() for initial provisioning
                .flatMap(savedAsset -> registerForensicAudit(savedAsset, command.executorId())
                        .thenReturn(savedAsset))
                .doOnSuccess(asset -> {
                    if (asset != null) {
                        log.info("[ACTION: PROVISION_ASSET] [ID: {}] - Asset successfully established and audited.", asset.id());
                    }
                });
    }

    private Mono<AssetAudit> registerForensicAudit(Asset asset, String executorId) {
        AssetAudit auditEntry = AssetAudit.create(
                asset.tenantId(),
                asset.id(),
                "ASSET_INITIAL_PROVISIONING",
                "SUCCESS",
                executorId,
                Map.of(
                        "serialNumber", asset.serialNumber(),
                        "category", asset.category().name(),
                        "traceabilityTier", "3"
                )
        );
        return auditRepository.save(auditEntry);
    }
}