package com.thinklab.infrastructure.adapter.out.mongo.entity;

import com.thinklab.domain.model.Asset;
import com.thinklab.domain.valueobject.AssetCategory;
import com.thinklab.domain.valueobject.AssetStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Version;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Index;
import io.micronaut.data.annotation.Indexes;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure Entity: Persistence model for the Asset aggregate.
 */
@Serdeable
@Introspected
@MappedEntity("assets")
@Indexes({
        @Index(columns = {"tenantId", "serialNumber"}, unique = true),
        @Index(columns = {"tenantId", "status"}),
        @Index(columns = {"tenantId", "category"}),
        @Index(columns = {"assignedTo"}), // Alinhado com o Domínio
        @Index(columns = {"locationId"})
})
public record AssetEntity(
        @Id
        @Nonnull
        UUID id,

        @Nonnull
        UUID tenantId,

        @Nonnull
        String name,

        @Nonnull
        AssetCategory category,

        @Nonnull
        AssetStatus status,

        @Nonnull
        String serialNumber,

        @Nonnull
        Map<String, Object> specifications,

        @Nullable
        UUID assignedTo, // Alinhado com o Domínio (era assignedToUserId)

        @Nullable
        UUID locationId, // Alinhado com o Domínio (era String)

        @Nonnull
        Instant createdAt,

        @Nonnull
        Instant updatedAt,

        @Version
        @Nonnull
        Long version
) {

    @Nonnull
    public static AssetEntity fromDomain(@Nonnull Asset domain) {
        return new AssetEntity(
                domain.id(),
                domain.tenantId(),
                domain.name(),
                domain.category(),
                domain.status(),
                domain.serialNumber(),
                domain.specifications(),
                domain.assignedTo(), // Agora bate perfeitamente com o método do domínio
                domain.locationId(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.version()
        );
    }

    @Nonnull
    public Asset toDomain() {
        return new Asset(
                this.id,
                this.tenantId,
                this.name,
                this.category,
                this.status,
                this.serialNumber,
                this.specifications,
                this.assignedTo,
                this.locationId,
                this.createdAt,
                this.updatedAt,
                this.version
        );
    }
}