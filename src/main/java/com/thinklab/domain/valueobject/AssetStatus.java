package com.thinklab.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Value Object: Type-Safe Enumeration representing the lifecycle states of a corporate IT Asset.
 * This Enum implements a Finite State Machine (FSM) to strictly govern transitions
 * between operational states, ensuring forensic integrity and preventing illegal
 * status assignments (e.g., transitioning from DECOMMISSIONED back to ACTIVE).
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Deterministic Transitions:</b> Only allowed paths defined in the VALID_TRANSITIONS map can be executed.</li>
 * <li><b>Immutable States:</b> Once an asset reaches a terminal state, no further operational mutations are permitted.</li>
 * <li><b>Standardized Deserialization:</b> Uses hifen-aware mapping for JSON/BSON compatibility.</li>
 * </ul>
 */
public enum AssetStatus {

    /** Asset has been purchased/registered but not yet physically received or configured. */
    PROVISIONED("PROVISIONED"),

    /** Asset is in inventory, configured with the base image, and ready for user allocation. */
    READY_FOR_DEPLOY("READY_FOR_DEPLOY"),

    /** Asset is currently assigned to a user or a specific production location. */
    DEPLOYED("DEPLOYED"),

    /** Asset is undergoing technical repair or hardware upgrade. */
    UNDER_MAINTENANCE("UNDER_MAINTENANCE"),

    /** Asset has reached the end of its lifecycle, been retired, or destroyed. (Terminal State). */
    DECOMMISSIONED("DECOMMISSIONED");

    private final String standardName;

    private static final Map<String, AssetStatus> LOOKUP_MAP = Arrays.stream(values())
            .collect(Collectors.collectingAndThen(
                    Collectors.toMap(s -> s.standardName.toUpperCase(), Function.identity()),
                    Collections::unmodifiableMap
            ));

    private static final Map<AssetStatus, Set<AssetStatus>> VALID_TRANSITIONS = Map.of(
            PROVISIONED, Set.of(READY_FOR_DEPLOY, DECOMMISSIONED),
            READY_FOR_DEPLOY, Set.of(DEPLOYED, UNDER_MAINTENANCE, DECOMMISSIONED),
            DEPLOYED, Set.of(UNDER_MAINTENANCE, DECOMMISSIONED),
            UNDER_MAINTENANCE, Set.of(READY_FOR_DEPLOY, DEPLOYED, DECOMMISSIONED),
            DECOMMISSIONED, Collections.emptySet()
    );

    AssetStatus(String standardName) {
        this.standardName = standardName;
    }

    @JsonValue
    @Nonnull
    public String getStandardName() {
        return standardName;
    }

    /**
     * Factory method for safe JSON/BSON to Enum conversion.
     * @param value The raw string representation.
     * @return The corresponding {@link AssetStatus}.
     * @throws IllegalArgumentException if the status is unsupported.
     */
    @JsonCreator
    @Nonnull
    public static AssetStatus fromString(@Nonnull String value) {
        AssetStatus status = LOOKUP_MAP.get(value.trim().toUpperCase());
        if (status == null) {
            throw new IllegalArgumentException("Unsupported asset status: " + value);
        }
        return status;
    }

    /**
     * Validates if the transition from the current state to the target state is allowed.
     * @param targetStatus The desired next state.
     * @throws IllegalStateException if the transition violates lifecycle rules.
     */
    public void validateTransitionTo(@Nonnull AssetStatus targetStatus) {
        if (this == targetStatus) return; // Idempotency check

        if (!VALID_TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(targetStatus)) {
            throw new IllegalStateException(String.format(
                    "Illegal state transition: Asset cannot move from [%s] to [%s].",
                    this.name(), targetStatus.name()
            ));
        }
    }
}