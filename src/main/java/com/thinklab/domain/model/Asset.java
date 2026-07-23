package com.thinklab.domain.model;

import com.thinklab.domain.valueobject.AssetCategory;
import com.thinklab.domain.valueobject.AssetStatus;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Represents a physical or logical IT Asset within the enterprise ecosystem.
 * This record ensures absolute immutability and protects business invariants related
 * to the hardware lifecycle, multi-tenant isolation, and technical specifications.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>Deterministic Identity:</b> IDs are generated via Name-based UUID (v3) to ensure idempotency.</li>
 * <li><b>Functional Mutation:</b> State transitions return new instances, preserving historical consistency.</li>
 * <li><b>Glocal Governance:</b> Mandatory binding to a Tenant and optional binding to Users or Locations.</li>
 * </ul>
 *
 * @param id             The deterministic unique identifier of the asset.
 * @param tenantId       The identifier of the organization owning the asset.
 * @param name           The human-readable name/label of the asset.
 * @param category       The functional classification (e.g., SERVER, NOTEBOOK, IOT_SENSOR).
 * @param status         The current operational state governed by the Finite State Machine (FSM).
 * @param serialNumber   The manufacturer's serial number (used as seed for the ID).
 * @param specifications A map of technical attributes (RAM, CPU, IP, etc.) specific to the category.
 * @param assignedTo     The identifier of the User currently responsible for the asset (optional).
 * @param locationId     The identifier of the physical Site or Rack where the asset resides (optional).
 * @param createdAt      The instant when the asset was first provisioned.
 * @param updatedAt      The instant of the last state or metadata mutation.
 * @param version        Concurrency control version for optimistic locking.
 */
public record Asset(
        @Nonnull UUID id,
        @Nonnull UUID tenantId,
        @Nonnull String name,
        @Nonnull AssetCategory category,
        @Nonnull AssetStatus status,
        @Nonnull String serialNumber,
        @Nonnull Map<String, Object> specifications,
        @Nullable UUID assignedTo,
        @Nullable UUID locationId,
        @Nonnull Instant createdAt,
        @Nonnull Instant updatedAt,
        @Nonnull Long version
) {

    /**
     * Compact constructor for invariant validation and data sanitization.
     */
    public Asset {
        Objects.requireNonNull(id, "Asset ID is mandatory");
        Objects.requireNonNull(tenantId, "Tenant ID is mandatory");
        Objects.requireNonNull(name, "Asset name is mandatory");
        Objects.requireNonNull(category, "Asset category is mandatory");
        Objects.requireNonNull(status, "Asset status is mandatory");
        Objects.requireNonNull(serialNumber, "Serial number is mandatory");
        Objects.requireNonNull(specifications, "Specifications map cannot be null");
        Objects.requireNonNull(createdAt, "Creation timestamp is mandatory");
        Objects.requireNonNull(updatedAt, "Update timestamp is mandatory");
        Objects.requireNonNull(version, "Version is mandatory");

        name = name.trim();
        serialNumber = serialNumber.trim().toUpperCase();
        specifications = Map.copyOf(specifications);
    }

    /**
     * Factory method to provision a new asset in the system.
     * Generates a deterministic ID based on the tenant and serial number to prevent duplicates.
     */
    public static Asset provision(
            @Nonnull UUID tenantId,
            @Nonnull String name,
            @Nonnull AssetCategory category,
            @Nonnull String serialNumber,
            @Nonnull Map<String, Object> specifications
    ) {
        String seed = tenantId.toString() + "|" + serialNumber.trim().toUpperCase();
        UUID deterministicId = UUID.nameUUIDFromBytes(seed.getBytes());

        return new Asset(
                deterministicId,
                tenantId,
                name,
                category,
                AssetStatus.PROVISIONED,
                serialNumber,
                specifications,
                null,
                null,
                Instant.now(),
                Instant.now(),
                0L
        );
    }

    /**
     * Transitions the asset to READY_FOR_DEPLOY state after initial configuration.
     */
    public Asset markAsReady() {
        this.status.validateTransitionTo(AssetStatus.READY_FOR_DEPLOY);
        // Retornando 'version' sem o '+ 1'
        return new Asset(id, tenantId, name, category, AssetStatus.READY_FOR_DEPLOY,
                serialNumber, specifications, assignedTo, locationId, createdAt, Instant.now(), version);
    }

    /**
     * Deploys the asset to a specific user or location.
     */
    public Asset deploy(@Nullable UUID userId, @Nullable UUID locationId) {
        this.status.validateTransitionTo(AssetStatus.DEPLOYED);
        // Retornando 'version' sem o '+ 1'
        return new Asset(id, tenantId, name, category, AssetStatus.DEPLOYED,
                serialNumber, specifications, userId, locationId, createdAt, Instant.now(), version);
    }

    /**
     * Sends the asset for technical maintenance.
     */
    public Asset sendToMaintenance() {
        this.status.validateTransitionTo(AssetStatus.UNDER_MAINTENANCE);
        // Retornando 'version' sem o '+ 1'
        return new Asset(id, tenantId, name, category, AssetStatus.UNDER_MAINTENANCE,
                serialNumber, specifications, assignedTo, locationId, createdAt, Instant.now(), version);
    }

    /**
     * Permanently decommissions the asset (Terminal State).
     */
    public Asset decommission() {
        this.status.validateTransitionTo(AssetStatus.DECOMMISSIONED);
        // Retornando 'version' sem o '+ 1'
        return new Asset(id, tenantId, name, category, AssetStatus.DECOMMISSIONED,
                serialNumber, specifications, null, null, createdAt, Instant.now(), version);
    }
}