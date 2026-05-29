package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.InteriorGravityType;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationFunction;
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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA persistence form of a megastructure catalog entry. Mirrors the shape of
 * {@link StationEntity}: a flat, persistence-friendly mirror of the immutable
 * domain {@link com.terranrepublic.assets.Megastructure} record. Conversion is
 * the job of {@link MegastructureDesignMapper}; nothing else should touch this
 * class directly.
 * <p>
 * Design choices mirror v2 Phase A → Phase D.6 conventions:
 * <ul>
 *   <li>{@code String} UUID primary key, defended with {@link PrePersist};</li>
 *   <li>enums persisted as {@link EnumType#STRING} columns so repositories can
 *       filter on them (the archetype, origin_type, provenance_source_universe
 *       indexes in V11 target the filter dimensions);</li>
 *   <li>{@code secondary_functions_json} and {@code armaments_json} stored as
 *       JSON LOBs, matching the StationEntity collection-LOB pattern;</li>
 *   <li>provenance decomposed into four flat columns (source_type, source_universe,
 *       source_work, status) per the v2 Phase D.6 precedent;</li>
 *   <li>{@code dry_mass_megatons} carries mass in megatons (10^6 tons) — the
 *       megastructure-scale unit. The domain record's {@code dryMassTons()}
 *       override derives the ton-scale value at read time;</li>
 *   <li>{@code @DynamicUpdate} and an {@code id}-based {@code equals}/{@code hashCode}
 *       using {@link Hibernate#getClass(Object)};</li>
 *   <li>{@code @Cache(usage = READ_WRITE)} for L2 caching per design §5.1 —
 *       mirrors the {@code SolarSystem} L2-cache pattern (megastructure catalog
 *       entries are read-heavy and rarely mutated, the same access shape as
 *       SolarSystem reads).</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description", "notes", "secondaryFunctionsJson", "armamentsJson"})
@DynamicUpdate
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE,
        region = "com.terranrepublic.assets.Megastructure")
@Entity(name = "MEGASTRUCTURE")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "archetype"),
        @Index(columnList = "originType"),
        @Index(columnList = "provenanceSourceUniverse")
})
public class MegastructureEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String designation;

    @Column(length = 4000)
    private String description;

    private String category;

    @Column(length = 4000)
    private String notes;

    // ------------------------------------------------------------ structural

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MegastructureArchetype archetype;

    @Column(nullable = false)
    private double dimensionsKm;

    /** Mass in megatons (1 MT = 10^6 tons). The domain {@code dryMassTons()} override scales × 10^6. */
    @Column(nullable = false)
    private double dryMassMegatons;

    @Column(nullable = false)
    private double internalVolumeKm3;

    // ------------------------------------------------------------ mobility

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Mobility mobility;

    /**
     * Nullable: not all megastructures move under their own power. The pre-D.7 StationDesign
     * invariant "FIXED forbids auxiliaryDrive" does NOT carry over to megastructures by design
     * (D.7 Resolution A); a megastructure with mobility=FIXED + non-null auxiliaryDrive is
     * semantically odd but legal.
     */
    @Enumerated(EnumType.STRING)
    private DriveType auxiliaryDrive;

    // ------------------------------------------------------------ origin

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MegastructureOriginType originType;

    private String builderPolity;

    /** Year of discovery, when known. Nullable. */
    private Integer discoveryYear;

    /** Year of construction, when known. Nullable. */
    private Integer constructionYear;

    // ------------------------------------------------------------ function (D.6 axis)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private StationFunction primaryFunction;

    /** JSON-serialised set of secondary functions; see {@link MegastructureDesignMapper}. */
    @Lob
    private String secondaryFunctionsJson;

    // ------------------------------------------------------------ interior

    @Column(nullable = false)
    private boolean hasInteriorSetting;

    @Column(nullable = false)
    private long interiorPopulation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InteriorGravityType interiorGravity;

    // ------------------------------------------------------------ operational

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private OperationalState operationalState;

    @Column(nullable = false)
    private boolean concealed;

    // ------------------------------------------------------------ armament

    /** JSON-serialised list of armaments; see {@link MegastructureDesignMapper}. */
    @Lob
    private String armamentsJson;

    // ------------------------------------------------------------ provenance (D.6 axis)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceType provenanceSourceType;

    /** Universe label (e.g. "Troy Rising", "Star Wars"); never null, may be empty string. */
    @Column(nullable = false)
    private String provenanceSourceUniverse;

    /** Optional title of the specific work; may be null. */
    private String provenanceSourceWork;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CatalogOperationalStatus provenanceStatus;

    // ------------------------------------------------------------ faction / allegiance

    private String faction;

    private String allegiance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private TechLevel techLevel;

    // ------------------------------------------------------------ timestamps

    private Instant createdAt;

    private Instant modifiedAt;

    /**
     * Creates an entity with a freshly generated id and the not-null defaults that match the
     * {@link com.terranrepublic.assets.Megastructure} compact constructor and the V11 column
     * defaults.
     *
     * @param name display name
     */
    public MegastructureEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.archetype = MegastructureArchetype.UNKNOWN;
        this.mobility = Mobility.STATIONKEEPING;
        this.originType = MegastructureOriginType.UNKNOWN;
        this.primaryFunction = StationFunction.UNKNOWN;
        this.hasInteriorSetting = false;
        this.interiorPopulation = 0L;
        this.interiorGravity = InteriorGravityType.UNKNOWN;
        this.operationalState = OperationalState.OPERATIONAL;
        this.concealed = false;
        this.provenanceSourceType = SourceType.UNKNOWN;
        this.provenanceSourceUniverse = "";
        this.provenanceStatus = CatalogOperationalStatus.UNKNOWN;
        this.techLevel = TechLevel.UNKNOWN;
    }

    @PrePersist
    private void ensureDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.archetype == null) {
            this.archetype = MegastructureArchetype.UNKNOWN;
        }
        if (this.mobility == null) {
            this.mobility = Mobility.STATIONKEEPING;
        }
        if (this.originType == null) {
            this.originType = MegastructureOriginType.UNKNOWN;
        }
        if (this.primaryFunction == null) {
            this.primaryFunction = StationFunction.UNKNOWN;
        }
        if (this.interiorGravity == null) {
            this.interiorGravity = InteriorGravityType.UNKNOWN;
        }
        if (this.operationalState == null) {
            this.operationalState = OperationalState.OPERATIONAL;
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
        if (this.techLevel == null) {
            this.techLevel = TechLevel.UNKNOWN;
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
        MegastructureEntity that = (MegastructureEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
