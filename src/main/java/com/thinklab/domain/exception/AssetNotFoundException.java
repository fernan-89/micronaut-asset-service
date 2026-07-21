package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;
import java.util.UUID;

/**
 * Domain Exception: Thrown when a requested IT Asset cannot be located within the
 * specific tenant context or global registry.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>Semantic Error Mapping:</b> Inherits from BusinessException to enforce
 * the "ASSET_NOT_FOUND" error code, mapping to HTTP 404.</li>
 * <li><b>Type-Safe Context:</b> Captures the missing Asset ID to enrich audit logs
 * and debugging information.</li>
 * </ul>
 */
public class AssetNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "ASSET_NOT_FOUND";

    /**
     * Constructs a new exception identifying exactly which asset was not found.
     *
     * @param assetId The unique system identifier (UUID) that triggered the lookup failure.
     */
    public AssetNotFoundException(@Nonnull UUID assetId) {
        super(ERROR_CODE, String.format("Asset with identifier [%s] was not found in the current context.", assetId));
    }

    /**
     * Secondary constructor for searches based on identification strings (like Serial Number).
     *
     * @param identifier The string identifier (Serial/Tag) used in the failed lookup.
     */
    public AssetNotFoundException(@Nonnull String identifier) {
        super(ERROR_CODE, String.format("Asset with identification [%s] does not exist.", identifier));
    }
}