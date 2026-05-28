package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.OperationalState;
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
 * JPA persistence form of a standalone weapon installation catalog entry.
 * <p>
 * Mirrors the shape of {@link SpaceshipEntity} / {@link StationEntity}: a flat,
 * persistence-friendly mirror of the immutable domain {@link com.terranrepublic.assets.WeaponInstallation}
 * record. Conversion is the job of {@link WeaponInstallationMapper}; nothing else should touch
 * this class directly.
 * <p>
 * Note the table declaration: {@code @Table(name = "weapon_installation")} sets the physical
 * table name explicitly, distinct from the {@code @Entity(name = ...)} pattern used by
 * {@link SpaceshipEntity} and {@link StationEntity} which sets the JPQL entity name and lets the
 * naming strategy derive the table. Phase B prompt called for this explicit form; either works
 * for {@code ddl-auto=validate} so the choice is purely stylistic.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description", "armamentsJson"})
@DynamicUpdate
@Entity
@Table(name = "weapon_installation", indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "installationType"),
        @Index(columnList = "emplacement"),
        @Index(columnList = "faction")
})
public class WeaponInstallationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallationType installationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Emplacement emplacement;

    private String source;

    private String faction;

    @Column(nullable = false)
    private boolean concealed;

    @Column(length = 4000)
    private String description;

    private double dryMassTons;
    private double footprintSpanMeters;

    @Column(nullable = false)
    private boolean mobile;

    private int crewComplement;

    /** Serialised list of armaments; see {@link WeaponInstallationMapper}. */
    @Lob
    private String armamentsJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechLevel techLevel;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationalState operationalState;

    private Instant createdAt;

    private Instant modifiedAt;

    public WeaponInstallationEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.concealed = false;
        this.mobile = false;
        this.techLevel = TechLevel.UNKNOWN;
        this.operationalState = OperationalState.OPERATIONAL;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.techLevel == null) {
            this.techLevel = TechLevel.UNKNOWN;
        }
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
        WeaponInstallationEntity that = (WeaponInstallationEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
