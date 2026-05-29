package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.TechLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
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
 * JPA persistence form of a station catalog entry.
 * <p>
 * Mirrors the shape of {@link SpaceshipEntity}: a flat, persistence-friendly mirror of the
 * immutable domain {@link com.terranrepublic.assets.StationDesign} record. Conversion is the job
 * of {@link StationDesignMapper}; nothing else should touch this class directly.
 * <p>
 * Design choices follow the existing TRIPS entity conventions:
 * <ul>
 *   <li>{@code String} UUID primary key, defended with {@link PrePersist};</li>
 *   <li>enums persisted as {@link EnumType#STRING} columns so the repository can filter on them;</li>
 *   <li>the carried-craft and armament lists are stored as JSON {@link Lob} strings, mirroring
 *       {@code SpaceshipEntity.carriedCraftJson}. This is the existing collection-level LOB
 *       pattern, not a whole-entity {@code payload_json} CLOB; the latter would re-trip the
 *       Issue 46 LazyInitializationException constraint documented in v2 §4;</li>
 *   <li>{@code @DynamicUpdate} and an {@code id}-based {@code equals}/{@code hashCode} using
 *       {@link Hibernate#getClass(Object)}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description", "carriedCraftJson", "armamentsJson", "secondaryFunctionsJson"})
@DynamicUpdate
@Entity(name = "STATION_DESIGN")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "stationType"),
        @Index(columnList = "mobility"),
        @Index(columnList = "faction")
})
public class StationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationType stationType;

    private String source;

    private String faction;

    @Column(nullable = false)
    private boolean concealed;

    /**
     * The faction this station is currently loyal to. Defaults to {@code faction} via the domain
     * record's compact constructor when blank, but persisted independently so the difference
     * (e.g. a captured installation) survives a round-trip.
     */
    private String allegiance;

    @Column(length = 4000)
    private String description;

    // --- geometry / mass ---
    private double overallSpanMeters;
    private double interiorSpanMeters;
    private double dryMassTons;
    private double armourThicknessMeters;

    // --- crew / volume ---
    private int crewCapacity;
    private int crewComplement;
    private double pressurizedVolumeM3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mobility mobility;

    /**
     * Nullable: only valid when {@code mobility != FIXED}; enforced at the mapper boundary
     * (matches the domain record's compact constructor invariant).
     */
    @Enumerated(EnumType.STRING)
    private DriveType auxiliaryDrive;

    /** Serialised list of carried craft; see {@link StationDesignMapper}. */
    @Lob
    private String carriedCraftJson;

    /** Serialised list of armaments; see {@link StationDesignMapper}. */
    @Lob
    private String armamentsJson;

    private double hangarVolumeM3;

    @Column(nullable = false)
    private boolean carrierCapable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechLevel techLevel;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationalState operationalState;

    private Instant createdAt;

    private Instant modifiedAt;

    // ----------------------------------------- v2 Phase D.6 function + provenance axes

    /**
     * Primary functional role of the station. {@link com.terranrepublic.assets.StationFunction}'s
     * 30 values cover the documented SF taxonomy; defaults to {@code UNKNOWN}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private StationFunction primaryFunction;

    /**
     * JSON-serialised set of secondary functions; see {@link StationDesignMapper}. Empty set may
     * persist as null or {@code "[]"}; the mapper's null-safe read returns {@code Set.of()} for
     * either.
     */
    @Lob
    private String secondaryFunctionsJson;

    /** Real / Proposed / Science Fiction / Unknown (the catalog-provenance axis). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceType provenanceSourceType;

    /** Universe label (e.g. "Real / Proposed", "Troy Rising", "The Expanse"); never null, may be empty string. */
    @Column(nullable = false)
    private String provenanceSourceUniverse;

    /** Optional title of the specific work; may be null. */
    private String provenanceSourceWork;

    /** Catalog lifecycle status — HISTORIC / ACTIVE / PLANNED / CANCELLED / FICTIONAL / UNKNOWN. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CatalogOperationalStatus provenanceStatus;

    /**
     * Creates an entity with a freshly generated id and the not-null defaults that match the
     * {@link com.terranrepublic.assets.StationDesign} compact constructor.
     *
     * @param name display name
     */
    public StationEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.concealed = false;
        this.mobility = Mobility.FIXED;
        this.carrierCapable = false;
        this.techLevel = TechLevel.UNKNOWN;
        this.operationalState = OperationalState.OPERATIONAL;
        // v2 Phase D.6 defaults — match the NOT NULL DEFAULT clauses in V10.
        this.primaryFunction = StationFunction.UNKNOWN;
        this.provenanceSourceType = SourceType.UNKNOWN;
        this.provenanceSourceUniverse = "";
        this.provenanceStatus = CatalogOperationalStatus.UNKNOWN;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.mobility == null) {
            this.mobility = Mobility.FIXED;
        }
        if (this.techLevel == null) {
            this.techLevel = TechLevel.UNKNOWN;
        }
        if (this.operationalState == null) {
            this.operationalState = OperationalState.OPERATIONAL;
        }
        // v2 Phase D.6: belt-and-braces defaults for the NOT NULL columns introduced in V10.
        // Mirrors the Phase A0 pattern for operationalState — the column-level constraint
        // fires before V10 has run on stale dev DBs, so the entity layer defaults too.
        if (this.primaryFunction == null) {
            this.primaryFunction = StationFunction.UNKNOWN;
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        StationEntity that = (StationEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
