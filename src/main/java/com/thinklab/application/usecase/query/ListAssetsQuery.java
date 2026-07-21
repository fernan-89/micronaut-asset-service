package com.thinklab.application.usecase.query;

import com.thinklab.domain.valueobject.AssetCategory;
import com.thinklab.domain.valueobject.AssetStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Query: Encapsulates the criteria for paginated asset discovery.
 * This record ensures that list requests are sanitized, strictly scoped to a
 * tenant context, and limited at the boundary to prevent excessive resource
 * consumption (DoS protection).
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Multi-Tenant Isolation:</b> Mandatory tenantId prevents cross-tenant data leakage.</li>
 * <li><b>Resource Protection:</b> Enforces strict pagination limits (Max 100 elements).</li>
 * <li><b>Type-Safe Filtering:</b> Uses Domain Value Objects for category and status criteria.</li>
 * </ul>
 *
 * @param tenantId The unique identifier of the organization (Mandatory for isolation).
 * @param category Optional filter to restrict results by hardware classification.
 * @param status   Optional filter to restrict results by operational lifecycle state.
 * @param page     The zero-based page index (Default: 0).
 * @param size     The number of records per page (Default: 20, Max: 100).
 */
@Introspected
public record ListAssetsQuery(
        @NotNull(message = "Tenant context is mandatory for search isolation")
        UUID tenantId,

        @Nullable
        AssetCategory category,

        @Nullable
        AssetStatus status,

        @Min(value = 0, message = "Page index cannot be negative")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size exceeds the safety limit of 100")
        Integer size
) {

    /**
     * Compact constructor for input sanitization and default value assignment.
     */
    public ListAssetsQuery {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        page = (page == null) ? 0 : page;
        size = (size == null) ? 20 : size;
    }
}