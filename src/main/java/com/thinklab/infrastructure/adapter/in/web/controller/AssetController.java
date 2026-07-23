package com.thinklab.infrastructure.adapter.in.web.controller;

import com.thinklab.application.port.in.*;
import com.thinklab.application.usecase.query.ListAssetsQuery;
import com.thinklab.domain.valueobject.AssetCategory;
import com.thinklab.domain.valueobject.AssetStatus;
import com.thinklab.infrastructure.adapter.in.web.dto.request.*;
import com.thinklab.infrastructure.adapter.in.web.dto.response.AssetResponse;
import com.thinklab.infrastructure.adapter.in.web.dto.response.PagedAssetResponse;
import com.thinklab.application.port.in.GetAssetFullViewUseCase;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller: The primary inbound adapter for IT Asset Management.
 */
@Controller("/assets")
@Tag(name = "Asset Lifecycle API", description = "Authoritative endpoints for hardware provisioning, deployment, and decommissioning.")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);

    private final CreateAssetUseCase createAssetUseCase;
    private final ListAssetsUseCase listAssetsUseCase;
    private final MarkAssetReadyUseCase markReadyUseCase;
    private final DeployAssetUseCase deployUseCase;
    private final MarkForMaintenanceUseCase maintenanceUseCase;
    private final DecommissionAssetUseCase decommissionUseCase;
    private final GetAssetFullViewUseCase getAssetFullViewUseCase;

    @Inject
    public AssetController(
            CreateAssetUseCase createAssetUseCase,
            ListAssetsUseCase listAssetsUseCase,
            MarkAssetReadyUseCase markReadyUseCase,
            DeployAssetUseCase deployUseCase,
            MarkForMaintenanceUseCase maintenanceUseCase,
            DecommissionAssetUseCase decommissionUseCase,
            GetAssetFullViewUseCase getAssetFullViewUseCase
    ) {
        this.createAssetUseCase = createAssetUseCase;
        this.listAssetsUseCase = listAssetsUseCase;
        this.markReadyUseCase = markReadyUseCase;
        this.deployUseCase = deployUseCase;
        this.maintenanceUseCase = maintenanceUseCase;
        this.decommissionUseCase = decommissionUseCase;
        this.getAssetFullViewUseCase = getAssetFullViewUseCase;
    }

    @Post
    @Operation(summary = "Provision a new hardware asset", description = "Registers a new IT asset in the inventory with deterministic identity.")
    @ApiResponse(responseCode = "201", description = "Asset provisioned and audited successfully.")
    public Mono<MutableHttpResponse<AssetResponse>> provision(@Body @Valid ProvisionAssetRequest request) {
        return createAssetUseCase.execute(request.toCommand())
                .map(AssetResponse::fromDomain)
                .map(HttpResponse::created)
                .doOnSubscribe(s -> log.info("[ACTION: PROVISION_ASSET] [TENANT: {}] [SERIAL: {}] - Initiating sequence.", request.tenantId(), request.serialNumber()))
                .doOnSuccess(res -> log.info("[ACTION: PROVISION_ASSET] [ID: {}] - Completed. Status: 201.", res.body() != null ? res.body().id() : "N/A"));
    }

    @Get
    @Operation(summary = "Discover inventory assets", description = "Retrieves a paginated stream of assets scoped to the tenant context.")
    public Mono<MutableHttpResponse<PagedAssetResponse>> list(
            @Header("X-Tenant-Id") @NotBlank String tenantId,
            @QueryValue @Nullable AssetStatus status,
            @QueryValue @Nullable AssetCategory category,
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "20") int size) {

        var query = new ListAssetsQuery(UUID.fromString(tenantId), category, status, page, size);

        return listAssetsUseCase.execute(query)
                .map(AssetResponse::fromDomain)
                .collectList()
                .map(content -> PagedAssetResponse.of(content, page, size))
                .map(HttpResponse::ok)
                .doOnSubscribe(s -> log.info("[ACTION: LIST_ASSETS] [TENANT: {}] [PAGE: {}] - Executing discovery.", tenantId, page));
    }

    @Patch("/{id}/ready")
    @Operation(summary = "Mark asset as ready for deploy", description = "Transition from PROVISIONED to READY_FOR_DEPLOY state.")
    public Mono<MutableHttpResponse<AssetResponse>> markReady(
            @PathVariable UUID id,
            @Body @Valid MarkAssetReadyRequest request) {
        return markReadyUseCase.execute(request.toCommand(id))
                .map(AssetResponse::fromDomain)
                .map(HttpResponse::ok)
                .doOnSubscribe(s -> log.info("[ACTION: MARK_READY] [ID: {}] [EXECUTOR: {}] - Transitioning state.", id, request.executorId()));
    }

    @Patch("/{id}/deploy")
    @Operation(summary = "Deploy asset to user/location", description = "Assigns hardware ownership and transitions to DEPLOYED state.")
    public Mono<MutableHttpResponse<AssetResponse>> deploy(
            @PathVariable UUID id,
            @Body @Valid DeployAssetRequest request) {
        return deployUseCase.execute(request.toCommand(id))
                .map(AssetResponse::fromDomain)
                .map(HttpResponse::ok)
                .doOnSubscribe(s -> log.warn("[ACTION: DEPLOY_ASSET] [ID: {}] - Assigning hardware.", id));
    }

    @Patch("/{id}/maintenance")
    @Operation(summary = "Withdraw asset for maintenance", description = "Transition to UNDER_MAINTENANCE state with forensic reasoning.")
    public Mono<MutableHttpResponse<AssetResponse>> maintenance(
            @PathVariable UUID id,
            @Body @Valid MarkForMaintenanceRequest request) {
        return maintenanceUseCase.execute(request.toCommand(id))
                .map(AssetResponse::fromDomain)
                .map(HttpResponse::ok)
                .doOnSubscribe(s -> log.info("[ACTION: START_MAINTENANCE] [ID: {}] - Hardware withdrawal.", id));
    }

    @Delete("/{id}")
    @Operation(summary = "Decommission asset", description = "Irreversibly terminates hardware lifecycle under Zero Trust principles.")
    public Mono<MutableHttpResponse<AssetResponse>> decommission(
            @PathVariable UUID id,
            @Body @Valid DecommissionAssetRequest request) {
        return decommissionUseCase.execute(request.toCommand(id))
                .map(AssetResponse::fromDomain)
                .map(HttpResponse::ok)
                .doOnSubscribe(s -> log.warn("[ACTION: DECOMMISSION_ASSET] [ID: {}] - CRITICAL: Permanent termination sequence.", id))
                .doOnSuccess(res -> log.warn("[ACTION: DECOMMISSION_ASSET] [ID: {}] - CRITICAL: Hardware lifecycle terminated.", id));
    }

    /**
     * Retrieves the consolidated 360-degree view of an IT Asset.
     * Following the CQRS principle for read-side operations.
     *
     * @param id The unique system identifier (UUID) of the asset.
     * @return A Mono emitting the AssetFullViewResponse projection.
     */
    @Get("/{id}")
    @Operation(summary = "Get Asset 360° View", description = "Retrieves state and forensic audit trail in parallel.")
    @ApiResponse(responseCode = "200", description = "Consolidated view successfully projected.")
    @ApiResponse(responseCode = "404", description = "Asset not found in inventory.")
    public Mono<MutableHttpResponse<com.thinklab.infrastructure.adapter.in.web.dto.response.AssetFullViewResponse>> getFullView(@PathVariable UUID id) {
        return getAssetFullViewUseCase.execute(id)
                .map(com.thinklab.infrastructure.adapter.in.web.dto.response.AssetFullViewResponse::fromProjection)
                .map(HttpResponse::ok)
                .doOnSubscribe(s -> log.info("[ACTION: GET_ASSET_360] [ID: {}] - Initiating discovery.", id))
                .doOnError(err -> log.error("[ACTION: GET_ASSET_360] [ID: {}] - Discovery failed: {}", id, err.getMessage()));
    }
}