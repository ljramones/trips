package com.teamgannon.trips.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a feature or structure within a solar system.
 * Can be natural (asteroid belts, debris disks) or artificial (stations, gates).
 *
 * <p>Features are rendered in the solar system view using the ring system
 * for belt/disk structures or as point objects for stations and gates.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@DynamicUpdate
@Entity(name = "SOLAR_SYSTEM_FEATURE")
@Table(indexes = {
        @Index(columnList = "solarSystemId"),
        @Index(columnList = "featureType"),
        @Index(columnList = "featureCategory")
})
public class SolarSystemFeature implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /**
     * Foreign key to the parent SolarSystem
     */
    @Column(nullable = false)
    private String solarSystemId;

    /**
     * Display name for this feature
     */
    private String name;

    // ==================== Type Classification ====================

    /**
     * The type of feature. Valid values:
     *
     * Natural:
     * - ASTEROID_BELT: Rocky/metallic bodies (Main Belt style)
     * - KUIPER_BELT: Distant icy bodies
     * - DEBRIS_DISK: Dust and planetesimals
     * - OORT_CLOUD: Spherical shell of comets
     * - ZODIACAL_DUST: Inner system dust
     * - TROJAN_CLUSTER: Bodies at Lagrange points
     * - COMET_TRAIL: Debris from comets
     *
     * Artificial:
     * - DYSON_SWARM: Energy collectors around star
     * - ORBITAL_HABITAT: Space station or colony
     * - DEFENSE_PERIMETER: Military installations
     * - MINING_OPERATION: Industrial extraction
     * - JUMP_GATE: FTL transit infrastructure
     * - SHIPYARD: Construction facility
     * - SENSOR_NETWORK: Detection/communication array
     * - RESEARCH_STATION: Scientific outpost
     */
    @Column(nullable = false)
    private String featureType;

    /**
     * Category: NATURAL or ARTIFICIAL
     */
    @Column(nullable = false)
    private String featureCategory;

    // ==================== Spatial Properties (Belt/Disk) ====================

    /**
     * Inner radius in AU (for belt/disk structures)
     */
    @Column(nullable = true)
    private Double innerRadiusAU;

    /**
     * Outer radius in AU (for belt/disk structures)
     */
    @Column(nullable = true)
    private Double outerRadiusAU;

    /**
     * Thickness of the feature as a ratio (0.01 = thin disk, 0.5 = thick torus)
     */
    @Column(nullable = true)
    private Double thickness;

    /**
     * Inclination from the ecliptic plane in degrees
     */
    @Column(nullable = true)
    private Double inclinationDeg;

    /**
     * Eccentricity of the feature's shape (0 = circular, >0 = elliptical)
     */
    @Column(nullable = true)
    private Double eccentricity;

    // ==================== Spatial Properties (Point Features) ====================

    /**
     * Orbital radius in AU (for point features like stations, gates)
     */
    @Column(nullable = true)
    private Double orbitalRadiusAU;

    /**
     * Position on orbit in degrees (0 = toward reference direction)
     */
    @Column(nullable = true)
    private Double orbitalAngleDeg;

    /**
     * Height above/below ecliptic in AU
     */
    @Column(nullable = true)
    private Double orbitalHeightAU;

    /**
     * Associated planet ID for Lagrange point features (Trojans, etc.)
     */
    private String associatedPlanetId;

    /**
     * Lagrange point (L1, L2, L3, L4, L5) if associated with a planet
     */
    private String lagrangePoint;

    // ==================== Visual Properties ====================

    /**
     * Number of particles for rendered belt/disk features
     */
    @Column(nullable = true)
    private Integer particleCount;

    /**
     * Minimum particle size for rendering
     */
    @Column(nullable = true)
    private Double minParticleSize;

    /**
     * Maximum particle size for rendering
     */
    @Column(nullable = true)
    private Double maxParticleSize;

    /**
     * Primary color as hex string (e.g., "#8C8278")
     */
    private String primaryColor;

    /**
     * Secondary color as hex string
     */
    private String secondaryColor;

    /**
     * Opacity/density for rendering (0.0 to 1.0)
     */
    @Column(nullable = true)
    private Double opacity;

    /**
     * Whether to animate this feature (rotating belts, orbiting stations)
     */
    private Boolean animated;

    /**
     * Animation speed multiplier (1.0 = normal Keplerian speed)
     */
    @Column(nullable = true)
    private Double animationSpeed;

    // ==================== Science Fiction Properties ====================

    /**
     * Polity or faction controlling this feature
     */
    private String controllingPolity;

    /**
     * Population (for habitats, stations)
     */
    @Column(nullable = true)
    private Long population;

    /**
     * Primary purpose or function
     */
    private String purpose;

    /**
     * Technology level (1-10 scale)
     */
    @Column(nullable = true)
    private Integer techLevel;

    /**
     * Year constructed/discovered
     */
    @Column(nullable = true)
    private Integer yearEstablished;

    /**
     * Operational status (Active, Abandoned, Under Construction, Destroyed)
     */
    private String status;

    /**
     * Strategic importance rating (1-10)
     */
    @Column(nullable = true)
    private Integer strategicImportance;

    /**
     * Defensive capability rating (1-10, for military features)
     */
    @Column(nullable = true)
    private Integer defensiveRating;

    /**
     * Production capacity (for shipyards, mining operations)
     */
    private String productionCapacity;

    /**
     * Transit destinations (for jump gates) - comma-separated system names
     */
    @Column(length = 1000)
    private String transitDestinations;

    /**
     * General notes about this feature
     */
    @Column(length = 4000)
    private String notes;

    // ==================== Hazard Properties ====================

    /**
     * Whether this feature is hazardous to navigation
     */
    private Boolean navigationHazard;

    /**
     * Hazard type (Radiation, Debris, Gravitational, etc.)
     */
    private String hazardType;

    /**
     * Hazard severity (1-10)
     */
    @Column(nullable = true)
    private Integer hazardSeverity;

    // ==================== Constructors ====================

    public SolarSystemFeature(String name, String featureType, String featureCategory) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.featureType = featureType;
        this.featureCategory = featureCategory;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    // ==================== Convenience Methods ====================

    /**
     * Check if this is a belt/disk type feature (rendered with particles)
     */
    public boolean isBeltType() {
        return featureType != null && (
                featureType.equals(FeatureType.ASTEROID_BELT) ||
                featureType.equals(FeatureType.KUIPER_BELT) ||
                featureType.equals(FeatureType.DEBRIS_DISK) ||
                featureType.equals(FeatureType.OORT_CLOUD) ||
                featureType.equals(FeatureType.ZODIACAL_DUST) ||
                featureType.equals(FeatureType.DYSON_SWARM) ||
                featureType.equals(FeatureType.DEFENSE_PERIMETER) ||
                featureType.equals(FeatureType.SENSOR_NETWORK)
        );
    }

    /**
     * Check if this is a point/localized feature
     */
    public boolean isPointType() {
        return featureType != null && (
                featureType.equals(FeatureType.ORBITAL_HABITAT) ||
                featureType.equals(FeatureType.JUMP_GATE) ||
                featureType.equals(FeatureType.SHIPYARD) ||
                featureType.equals(FeatureType.RESEARCH_STATION) ||
                featureType.equals(FeatureType.MINING_OPERATION) ||
                featureType.equals(FeatureType.TROJAN_CLUSTER)
        );
    }

    /**
     * Check if this is a natural feature
     */
    public boolean isNatural() {
        return FeatureCategory.NATURAL.equals(featureCategory);
    }

    /**
     * Check if this is an artificial feature
     */
    public boolean isArtificial() {
        return FeatureCategory.ARTIFICIAL.equals(featureCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolarSystemFeature that = (SolarSystemFeature) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ==================== Constants ====================

    /**
     * Feature type constants
     */
    public static final class FeatureType {
        // Natural
        public static final String ASTEROID_BELT = "ASTEROID_BELT";
        public static final String KUIPER_BELT = "KUIPER_BELT";
        public static final String DEBRIS_DISK = "DEBRIS_DISK";
        public static final String OORT_CLOUD = "OORT_CLOUD";
        public static final String ZODIACAL_DUST = "ZODIACAL_DUST";
        public static final String TROJAN_CLUSTER = "TROJAN_CLUSTER";
        public static final String COMET_TRAIL = "COMET_TRAIL";

        // Artificial
        public static final String DYSON_SWARM = "DYSON_SWARM";
        public static final String ORBITAL_HABITAT = "ORBITAL_HABITAT";
        public static final String DEFENSE_PERIMETER = "DEFENSE_PERIMETER";
        public static final String MINING_OPERATION = "MINING_OPERATION";
        public static final String JUMP_GATE = "JUMP_GATE";
        public static final String SHIPYARD = "SHIPYARD";
        public static final String SENSOR_NETWORK = "SENSOR_NETWORK";
        public static final String RESEARCH_STATION = "RESEARCH_STATION";

        private FeatureType() {}
    }

    /**
     * Feature category constants
     */
    public static final class FeatureCategory {
        public static final String NATURAL = "NATURAL";
        public static final String ARTIFICIAL = "ARTIFICIAL";

        private FeatureCategory() {}
    }

    /**
     * Feature status constants
     */
    public static final class FeatureStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String ABANDONED = "ABANDONED";
        public static final String UNDER_CONSTRUCTION = "UNDER_CONSTRUCTION";
        public static final String DESTROYED = "DESTROYED";
        public static final String UNKNOWN = "UNKNOWN";

        private FeatureStatus() {}
    }
}
