package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
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
 * {@link com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign} record. Conversion between the two is
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
@ToString(exclude = {"description", "carriedCraftJson"})
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

    private String iconPath;

    @Column(length = 4000)
    private String description;

    /** Provenance: real, proposed or science fiction. */
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    /** Franchise/program name (e.g. "The Expanse", "NASA / JPL"); free text. */
    private String series;

    private Instant createdAt;

    /**
     * Creates an entity with a freshly generated id.
     *
     * @param name display name
     */
    public SpaceshipEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
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
