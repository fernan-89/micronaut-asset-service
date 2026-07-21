package com.thinklab.domain.exception;

import jakarta.annotation.Nonnull;
import java.util.UUID;

/**
 * Domain Exception: Specialized error signaling that an IT Asset with the 
 * same unique identifier (Serial Number) already exists within the tenant context.
 *
 * <p>This exception enforces business idempotency at the domain level, 
 * preventing hardware registry collisions and ensuring that the forensic 
 * audit trail identifies clear duplication attempts.</p>
 *
 * <p><b>Architectural Principles (Mission-Critical Pattern):</b></p>
 * <ul>
 * <li><b>Semantic Signaling:</b> Carries a specific business code to be 
 * translated into HTTP 409 (Conflict) at the infrastructure edge.</li>
 * <li><b>NASA Standard Traceability:</b> Captures the conflicting Serial Number 
 * and Tenant ID for precise forensic analysis.</li>
 * <li><b>Pure Domain:</b> Zero dependencies on external frameworks, 
 * remaining strictly within the domain boundary.</li>
 * </ul>
 */
public class AssetAlreadyExistsException extends BusinessException {

    private static final String ERROR_CODE = "ASSET_DUPLICATE_IDENTIFIER";

    private final UUID tenantId;
    private final String serialNumber;

    /**
     * Constructs the exception with full forensic context.
     *
     * @param tenantId     The organization context where the collision occurred.
     * @param serialNumber The hardware serial number that triggered the duplication.
     */
    public AssetAlreadyExistsException(@Nonnull UUID tenantId, @Nonnull String serialNumber) {
        super(ERROR_CODE, String.format(
                "Provisioning Violation: Asset with Serial Number [%s] is already registered for Tenant [%s].",
                serialNumber, tenantId
        ));
        this.tenantId = tenantId;
        this.serialNumber = serialNumber;
    }

    /**
     * @return The tenant identity associated with the collision.
     */
    public UUID getTenantId() {
        return tenantId;
    }

    /**
     * @return The serial number that caused the conflict.
     */
    public String getSerialNumber() {
        return serialNumber;
    }
}