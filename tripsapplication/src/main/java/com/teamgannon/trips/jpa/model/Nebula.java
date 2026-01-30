package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a nebula in the interstellar view.
 * <p>
 * Nebulae are rendered as particle clouds using procedural generation.
 * This entity stores the parameters needed to regenerate the nebula
 * deterministically, rather than storing individual particle positions.
 * <p>
 * Supports various nebula types: emission, dark, reflection, planetary,
 * and supernova remnants. Can be user-defined or imported from astronomical
 * catalogs (Messier, NGC).
 */
@Slf4j
@Getter
@Setter
@ToString(exclude = {"notes"})
@DynamicUpdate
@Entity(name = "NEBULA")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "dataSetName ASC"),
        @Index(columnList = "type"),
        @Index(columnList = "catalogId"),
        @Index(columnList = "centerX, centerY, centerZ")
})
public class Nebula implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== Identity ====================

    /**
     * Unique identifier for this nebula (UUID)
     */
    @Id
    private String id;

    /**
     * Human-readable name for the nebula (e.g., "Orion Nebula", "Horsehead Nebula")
     */
    @Column(nullable = false)
    private String name;

    /**
     * The dataset this nebula belongs to (each dataset = different universe)
     */
    @Column(nullable = false)
    private String dataSetName;

    // ==================== Position ====================

    /**
     * X coordinate of nebula center in light-years from Sol
     */
    private double centerX;

    /**
     * Y coordinate of nebula center in light-years from Sol
     */
    private double centerY;

    /**
     * Z coordinate of nebula center in light-years from Sol
     */
    private double centerZ;

    // ==================== Shape ====================

    /**
     * Inner radius in light-years.
     * Set to 0 for filled nebulae, > 0 for shell-like structures (planetary nebulae).
     */
    private double innerRadius = 0.0;

    /**
     * Outer radius in light-years (the overall extent of the nebula)
     */
    private double outerRadius = 10.0;

    // ==================== Type & Generation ====================

    /**
     * The type of nebula (affects visual appearance and default parameters)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NebulaType type = NebulaType.EMISSION;

    /**
     * Random seed for reproducible procedural generation.
     * Same seed = same particle positions every time.
     */
    private long seed;

    // ==================== Density & Structure ====================

    /**
     * Base particle density in particles per cubic light-year.
     * Combined with volume to calculate total particle count.
     */
    private double particleDensity = 0.005;

    /**
     * User override for particle count.
     * If set (non-null), bypasses density * volume calculation.
     */
    private Integer numElementsOverride;

    /**
     * Radial power for density falloff.
     * < 0.5 = dense core with gradual falloff
     * = 0.5 = uniform distribution
     * > 0.5 = shell-like (hollow center)
     */
    private double radialPower = 0.4;

    /**
     * Noise strength for filamentary structure (0.0 - 1.0).
     * Higher values create more wispy, turbulent appearance.
     */
    private double noiseStrength = 0.3;

    /**
     * Number of noise octaves for detail (2-4 typical).
     * More octaves = finer detail but more computation.
     */
    private int noiseOctaves = 3;

    // ==================== Appearance ====================

    /**
     * Primary color as hex string (e.g., "#FF6496" for H-alpha pink)
     */
    private String primaryColor;

    /**
     * Secondary color as hex string for gradients/variation
     */
    private String secondaryColor;

    /**
     * Base opacity (0.0 - 1.0)
     */
    private double opacity = 0.7;

    // ==================== Animation ====================

    /**
     * Whether to enable slow turbulent animation
     */
    private boolean enableAnimation = true;

    /**
     * Base angular speed for particle motion (radians per frame * multiplier)
     */
    private double baseAngularSpeed = 0.0003;

    // ==================== Catalog Tracking ====================

    /**
     * Source catalog name (e.g., "Messier", "NGC", "User-defined")
     */
    private String sourceCatalog = "User-defined";

    /**
     * Catalog ID (e.g., "M42", "NGC 1976", "IC 434")
     */
    private String catalogId;

    // ==================== Notes ====================

    /**
     * Optional notes or description
     */
    @Lob
    private String notes;

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA
     */
    public Nebula() {
        this.id = UUID.randomUUID().toString();
        this.seed = System.currentTimeMillis();
    }

    /**
     * Creates a new nebula with the given name and type.
     * Applies default colors and parameters based on type.
     */
    public Nebula(String name, NebulaType type, String dataSetName) {
        this();
        this.name = name;
        this.type = type;
        this.dataSetName = dataSetName;
        applyTypeDefaults();
    }

    /**
     * Creates a new nebula at the specified position.
     */
    public Nebula(String name, NebulaType type, String dataSetName,
                  double centerX, double centerY, double centerZ, double outerRadius) {
        this(name, type, dataSetName);
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.outerRadius = outerRadius;
    }

    // ==================== Helper Methods ====================

    /**
     * Apply default values based on nebula type.
     */
    public void applyTypeDefaults() {
        if (type != null) {
            if (primaryColor == null) {
                primaryColor = type.getDefaultPrimaryColor();
            }
            if (secondaryColor == null) {
                secondaryColor = type.getDefaultSecondaryColor();
            }
            radialPower = type.getDefaultRadialPower();
            noiseStrength = type.getDefaultNoiseStrength();
            particleDensity = type.getDefaultParticleDensity();
            enableAnimation = type.isAnimationEnabledByDefault();
        }
    }

    /**
     * Calculate the volume of the nebula in cubic light-years.
     */
    public double getVolume() {
        return (4.0 / 3.0) * Math.PI *
                (Math.pow(outerRadius, 3) - Math.pow(innerRadius, 3));
    }

    /**
     * Calculate the base particle count from density and volume.
     * Does not apply LOD scaling.
     */
    public int calculateBaseParticleCount() {
        if (numElementsOverride != null) {
            return numElementsOverride;
        }
        int count = (int) (particleDensity * getVolume());
        return Math.max(1000, Math.min(500_000, count));
    }

    /**
     * Calculate distance from this nebula's center to a point.
     */
    public double distanceTo(double x, double y, double z) {
        double dx = centerX - x;
        double dy = centerY - y;
        double dz = centerZ - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Check if this nebula intersects with a spherical plot region.
     * Returns true if any part of the nebula is within the plot radius.
     */
    public boolean intersectsPlotRegion(double plotCenterX, double plotCenterY,
                                         double plotCenterZ, double plotRadius) {
        double distance = distanceTo(plotCenterX, plotCenterY, plotCenterZ);
        return (distance - outerRadius) <= plotRadius;
    }

    // ==================== Equals & HashCode ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nebula nebula = (Nebula) o;
        return Objects.equals(id, nebula.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
