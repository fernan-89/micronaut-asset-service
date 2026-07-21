package com.thinklab.infrastructure.adapter.in.web.dto.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Infrastructure DTO: Standardized wrapper for paginated IT asset results.
 * This record follows the Collection Projection pattern to provide a consistent
 * structure for API consumers, including both the payload subset and the
 * pagination metadata required for high-performance UI navigation.
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Data Egress Sovereignty:</b> Aggregates streamed items into a structured envelope.</li>
 * <li><b>AOT Performance:</b> Annotated with {@link Serdeable} for reflection-free
 * serialization via Micronaut Serde.</li>
 * <li><b>Consumer Predictability:</b> Ensures all collection endpoints speak the
 * same metadata language.</li>
 * </ul>
 *
 * @param content  The sanitized list of IT assets for the requested page.
 * @param page     The current zero-indexed page number.
 * @param size     The total volume of records returned in this page.
 */
@Serdeable
@Introspected
@Schema(
        name = "PagedAssetResponse",
        description = "Paginated container representing a subset of the IT asset inventory with navigation metadata."
)
public record PagedAssetResponse(
        @Nonnull
        @Schema(description = "The list of projects assets for the current page")
        List<AssetResponse> content,

        @Schema(description = "Current zero-indexed page number", example = "0")
        int page,

        @Schema(description = "Number of elements in the current page subset", example = "20")
        int size
) {

    /**
     * Factory Method: Standardizes the creation of a paginated response.
     * Centralizes the assembly logic used by the Inbound Web Controller.
     *
     * @param content List of projected assets.
     * @param page    Current page index.
     * @param size    Current page size.
     * @return A standardized {@link PagedAssetResponse} instance.
     */
    @Nonnull
    public static PagedAssetResponse of(@Nonnull List<AssetResponse> content, int page, int size) {
        return new PagedAssetResponse(content, page, size);
    }
}