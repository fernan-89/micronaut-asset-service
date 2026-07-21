package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;

/**
 * Domain Exception: Thrown when an attempt is made to provision an IT Asset that
 * already exists within the tenant's registry, typically identified by a
 * collision in the deterministic UUID (v3) generated from Tenant ID and Serial Number.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>Semantic Error Mapping:</b> Inherits from BusinessException to enforce
 * the "ASSET_ALREADY_PROVISIONED" error code, mapping to HTTP 409 (Conflict).</li>
 * <li><b>Idempotency Guard:</b> Prevents duplicate physical hardware entries,
 * ensuring that the Forensic Audit Trail remains unique per physical unit.</li>
 * </ul>
 */
public class AssetAlreadyProvisionedException extends BusinessException {

    private static final String ERROR_CODE = "ASSET_ALREADY_PROVISIONED";

    /**
     * Constructs a new exception identifying the conflicting asset.
     *
     * @param serialNumber The manufacturer's serial number that triggered the conflict.
     */
    public AssetAlreadyProvisionedException(@Nonnull String serialNumber) {
        super(ERROR_CODE, String.format("Asset with serial number [%s] has already been provisioned for this tenant.", serialNumber));
    }
}