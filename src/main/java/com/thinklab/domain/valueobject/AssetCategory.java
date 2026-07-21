package com.thinklab.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Value Object: Type-Safe Enumeration representing the functional categories of IT Assets.
 * This enum governs the data taxonomy across the ecosystem, supporting everything from
 * industrial Edge/IoT devices to virtualized remote work infrastructure.
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>Glocal Governance:</b> Supports both localized physical hardware and global logical resources.</li>
 * <li><b>AOT Optimized:</b> Uses Micronaut Serde for reflection-free serialization.</li>
 * <li><b>Strong Typing:</b> Prevents raw string leakage into the domain core.</li>
 * </ul>
 */
@Introspected
@Serdeable
public enum AssetCategory {

    // --- Computing ---
    SERVER("SERVER"),
    WORKSTATION("WORKSTATION"),
    NOTEBOOK("NOTEBOOK"),
    VIRTUAL_MACHINE("VIRTUAL_MACHINE"),

    // --- Networking ---
    SWITCH("SWITCH"),
    ROUTER("ROUTER"),
    FIREWALL("FIREWALL"),
    ACCESS_POINT("ACCESS_POINT"),
    VPN_GATEWAY("VPN_GATEWAY"),

    // --- Edge & IoT (Novelis/Industrial Context) ---
    EDGE_CONTROLLER("EDGE_CONTROLLER"),
    IOT_SENSOR("IOT_SENSOR"),
    GATEWAY_IOT("GATEWAY_IOT"),

    // --- Power & Infrastructure ---
    UPS("UPS"), // Nobreaks
    PDU("PDU"),
    RACK("RACK"),
    STORAGE_ARRAY("STORAGE_ARRAY"),

    // --- Peripherals & Remote Work ---
    MONITOR("MONITOR"),
    DOCKING_STATION("DOCKING_STATION"),
    PERIPHERAL("PERIPHERAL"),
    REMOTE_KIT("REMOTE_KIT"),

    // --- Mobile & Communication ---
    SMARTPHONE("SMARTPHONE"),
    TABLET("TABLET"),
    PHYSICAL_TOKEN("PHYSICAL_TOKEN"),

    // --- Logical & SaaS ---
    SOFTWARE_LICENSE("SOFTWARE_LICENSE"),
    CLOUD_RESOURCE("CLOUD_RESOURCE"),
    VIRTUAL_DESKTOP("VIRTUAL_DESKTOP");

    private final String standardName;

    private static final Map<String, AssetCategory> LOOKUP_MAP = Arrays.stream(values())
            .collect(Collectors.collectingAndThen(
                    Collectors.toMap(s -> s.standardName.toUpperCase(), Function.identity()),
                    Collections::unmodifiableMap
            ));

    AssetCategory(String standardName) {
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
     * @return The corresponding {@link AssetCategory}.
     * @throws IllegalArgumentException if the category is unsupported.
     */
    @JsonCreator
    @Nonnull
    public static AssetCategory fromString(@Nonnull String value) {
        AssetCategory category = LOOKUP_MAP.get(value.trim().toUpperCase());
        if (category == null) {
            throw new IllegalArgumentException("Unsupported asset category: " + value);
        }
        return category;
    }
}