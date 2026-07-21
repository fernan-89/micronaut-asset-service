package com.thinklab.application.usecase.command;

import com.thinklab.domain.valueobject.AssetCategory;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Command: Encapsulates the intent to provision a new IT Asset.
 * This record enforces strict boundary validation and defensive sanitization, acting
 * as the primary shield for the Domain Layer against malformed or malicious data.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Immutability:</b> Leverages Java Records to ensure side-effect-free processing.</li>
 * <li><b>Edge Observability:</b> Integrated logging for immediate detection of validation breaches.</li>
 * <li><b>Deterministic Integrity:</b> Uses native UUIDs for tenant and identity isolation.</li>
 * <li><b>AOT Optimized:</b> Compiled with introspection for high-performance reflection-free execution.</li>
 * </ul>
 */
@Slf4j
@Introspected
public record CreateAssetCommand(
        @NotNull(message = "Tenant context is mandatory for multi-tenant isolation")
        UUID tenantId,

        @NotBlank(message = "Asset name is mandatory")
        @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
        String name,

        @NotNull(message = "Asset category classification is mandatory")
        AssetCategory category,

        @NotBlank(message = "Serial number is mandatory for deterministic hardware tracking")
        @Size(max = 100, message = "Serial number exceeds the safety limit")
        String serialNumber,

        @NotNull(message = "Specifications map cannot be null")
        Map<String, String> specifications,

        @NotBlank(message = "Executor identification is mandatory for the forensic audit trail")
        String executorId
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     * Enforces that no blank strings or invalid states bypass the boundary.
     */
    public CreateAssetCommand {
        // Defensive Logging for edge violations
        if (tenantId == null || name == null || name.isBlank() || serialNumber == null || serialNumber.isBlank()) {
            log.error("[ACTION: ASSET_PROVISIONING_VALIDATION] - Integrity breach: Mandatory identity fields are missing.");
        }

        Objects.requireNonNull(tenantId, "Tenant ID cannot be null.");
        Objects.requireNonNull(name, "Asset name cannot be null.");
        Objects.requireNonNull(category, "Category cannot be null.");
        Objects.requireNonNull(serialNumber, "Serial number cannot be null.");
        Objects.requireNonNull(specifications, "Specifications cannot be null.");
        Objects.requireNonNull(executorId, "Executor ID cannot be null.");

        // Sanitization & Normalization
        name = name.trim();
        serialNumber = serialNumber.trim().toUpperCase(); // Normalization for uniqueness checks
        executorId = executorId.trim();
        specifications = Map.copyOf(specifications); // Force immutability of the metadata
    }
}