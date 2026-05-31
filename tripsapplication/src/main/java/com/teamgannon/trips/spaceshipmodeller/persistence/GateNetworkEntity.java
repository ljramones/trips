package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.GateNetworkLifecycle;
import com.terranrepublic.assets.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA persistence form of a {@link com.terranrepublic.assets.GateNetwork} catalog entry.
 * Mirrors the shape of {@link MegastructureEntity} but without the L2 cache (no read pressure
 * predicted at E.1 scale) and without JSON LOB columns (no collection fields on
 * {@code GateNetwork}).
 *
 * <p>v2 Phase E.1 §5 — GateNetwork is the first persisted catalog entity outside the
 * SpaceAsset/SpaceInfrastructure sealed hierarchies. Conversion is the job of
 * {@link GateNetworkMapper}; nothing else should touch this class directly.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description", "notes"})
@DynamicUpdate
@Entity(name = "GATE_NETWORK")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "lifecycle"),
        @Index(columnList = "builderPolity"),
        @Index(columnList = "provenanceSourceUniverse")
})
public class GateNetworkEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String builderPolity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GateNetworkLifecycle lifecycle;

    private String transponderName;

    @Column(length = 4000)
    private String description;

    @Column(length = 4000)
    private String notes;

    private String category;

    // ----------------------------------------------- provenance (D.6 axis, flattened)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceType provenanceSourceType;

    @Column(nullable = false)
    private String provenanceSourceUniverse;

    private String provenanceSourceWork;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CatalogOperationalStatus provenanceStatus;

    // ----------------------------------------------- timestamps

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant modifiedAt;

    /**
     * Creates an entity with a freshly generated id and NOT NULL defaults that match the V13
     * column defaults + {@link com.terranrepublic.assets.GateNetwork} compact constructor.
     *
     * @param name display name
     */
    public GateNetworkEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.lifecycle = GateNetworkLifecycle.ACTIVE;
        this.provenanceSourceType = SourceType.UNKNOWN;
        this.provenanceSourceUniverse = "";
        this.provenanceStatus = CatalogOperationalStatus.UNKNOWN;
        Instant now = Instant.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }

    @PrePersist
    private void ensureDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.lifecycle == null) {
            this.lifecycle = GateNetworkLifecycle.ACTIVE;
        }
        if (this.provenanceSourceType == null) {
            this.provenanceSourceType = SourceType.UNKNOWN;
        }
        if (this.provenanceSourceUniverse == null) {
            this.provenanceSourceUniverse = "";
        }
        if (this.provenanceStatus == null) {
            this.provenanceStatus = CatalogOperationalStatus.UNKNOWN;
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.modifiedAt == null) {
            this.modifiedAt = this.createdAt;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        GateNetworkEntity that = (GateNetworkEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
