package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.terranrepublic.assets.SourceType;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.OperationalState;
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
 * JPA persistence form of a spaceship design.
 * <p>
 * This is a deliberately flat, persistence-friendly mirror of the immutable domain
 * {@link com.terranrepublic.assets.SpaceshipDesign} record. Conversion between the two is
 * the job of {@link SpaceshipDesignMapper}; nothing else should touch this class directly.
 * <p>
 * Design choices follow existing TRIPS entity conventions:
 * <ul>
 *   <li>{@code String} UUID primary key, assigned in the constructor and defended with {@link PrePersist};</li>
 *   <li>enums persisted as {@link EnumType#STRING} columns so the repository can filter on them;</li>
 *   <li>the carried-craft list is stored as a JSON {@link Lob} string (see {@link SpaceshipDesignMapper}),
 *       mirroring how other TRIPS entities persist collections;</li>
 *   <li>{@code @DynamicUpdate} and an {@code id}-based {@code equals}/{@code hashCode} using
 *       {@link Hibernate#getClass(Object)}.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description", "carriedCraftJson", "armamentsJson", "defaultAccessibleNetworkIdsJson"})
@DynamicUpdate
@Entity(name = "SPACESHIP_DESIGN")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "shipClass"),
        @Index(columnList = "driveType"),
        @Index(columnList = "sourceType")
})
public class SpaceshipEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipClass shipClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriveType driveType;

    // --- flattened MassBudget (tonnes) ---
    private double structureMassTons;
    private double engineMassTons;
    private double propellantMassTons;
    private double payloadMassTons;
    private double crewMassTons;
    private double radiatorMassTons;

    private int crewComplement;
    private double lengthMeters;

    /** Serialised list of carried craft; see {@link SpaceshipDesignMapper}. */
    @Lob
    private String carriedCraftJson;

    /** Serialised list of armaments; see {@link SpaceshipDesignMapper}. */
    @Lob
    private String armamentsJson;

    /**
     * v2 Phase E.1 §5.4 — JSON-serialised {@code Set<String>} of {@code GateNetwork} ids that
     * ships of this design have transponder access to by default. Nullable; empty set on the
     * domain side persists as {@code null} here. The {@link SpaceshipDesignMapper} handles
     * round-trip via Jackson.
     */
    @Lob
    private String defaultAccessibleNetworkIdsJson;

    private String iconPath;

    @Column(length = 4000)
    private String description;

    /** Provenance: real, proposed or science fiction. */
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    /** Source universe / franchise (e.g. "The Expanse", "Caine Riordan"); free text. */
    private String sourceUniverse;

    /** In-universe faction / operator (e.g. "Terran Republic", "UN Navy"); free text. */
    private String faction;

    /** Era / timeframe (e.g. "2045", "Post-Contact"); free text. */
    private String era;

    /**
     * Whether this design is concealed from the catalog listing — mirrors
     * the {@code SpaceshipDesign.concealed()} accessor. Added in Phase A0
     * (Constructs feature) to close the round-trip-loss bug documented in
     * {@code constructs-existing-hierarchies.md §4.4}. NOT NULL with a
     * default of {@code false} so legacy rows materialised before V6
     * surface as visible designs.
     */
    @Column(nullable = false)
    private boolean concealed;

    /**
     * Current lifecycle state. Mirrors the {@code SpaceshipDesign
     * .operationalState()} accessor; persisted as the enum constant name
     * via {@code EnumType.STRING}. NOT NULL with a default of
     * {@code OPERATIONAL} so legacy rows materialised before V6 reflect
     * the same value the mapper used to silently reconstitute.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationalState operationalState;

    private Instant createdAt;

    /**
     * Creates an entity with a freshly generated id.
     *
     * @param name display name
     */
    public SpaceshipEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.concealed = false;
        this.operationalState = OperationalState.OPERATIONAL;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        // Phase A0: belt-and-braces default — the NOT NULL constraint on
        // operationalState fires before V6 has run on stale dev DBs.
        if (this.operationalState == null) {
            this.operationalState = OperationalState.OPERATIONAL;
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
        SpaceshipEntity that = (SpaceshipEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
