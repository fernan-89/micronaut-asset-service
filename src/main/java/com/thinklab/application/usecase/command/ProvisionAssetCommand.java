package com.thinklab.application.usecase.command;

import com.thinklab.domain.valueobject.AssetCategory;
import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Command: Encapsulates the intent to provision a new IT Asset.
 * This record enforces strict input validation using Jakarta Bean Validation and
 * functional sanitization. It acts as a defensive shield, preventing malformed
 * hardware data from reaching the core domain.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Input Integrity:</b> Ensures mandatory fields like serial number and tenant context.</li>
 * <li><b>OOM Protection:</b> Limits specification map size to prevent memory exhaustion.</li>
 * <li><b>Deterministic Alignment:</b> Carries metadata required for ID generation.</li>
 * </ul>
 *
 * @param tenantId       The organizational identifier owning the asset.
 * @param name           The human-readable label for the hardware.
 * @param category       The functional classification (e.g., SERVER, NOTEBOOK).
 * @param serialNumber   The manufacturer's unique identifier (Seed for ID).
 * @param specifications Category-specific technical attributes (RAM, IP, Capacity).
 * @param executorId     The identity of the agent authorizing the provisioning.
 */
@Introspected
public record ProvisionAssetCommand(
        @NotNull(message = "Tenant ID is mandatory")
        UUID tenantId,

        @NotBlank(message = "Asset name is mandatory")
        @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
        String name,

        @NotNull(message = "Asset category is mandatory")
        AssetCategory category,

        @NotBlank(message = "Serial number is mandatory")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "Serial number contains invalid characters")
        String serialNumber,

        @NotNull(message = "Specifications map cannot be null")
        @Size(max = 50, message = "Specifications map exceeds security limit")
        Map<String, Object> specifications,

        @NotBlank(message = "Executor identification is mandatory")
        String executorId
) {

    /**
     * Compact constructor for functional sanitization and invariant protection.
     * Enforces that the command is in a valid state before being passed to an Interactor.
     */
    public ProvisionAssetCommand {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(category, "Category cannot be null");
        Objects.requireNonNull(specifications, "Specifications cannot be null");

        name = name.trim();
        serialNumber = serialNumber.trim().toUpperCase();
        executorId = executorId.trim();
        specifications = Map.copyOf(specifications);
    }
}