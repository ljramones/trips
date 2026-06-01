package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.UniverseLifecycle;
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
 * JPA persistence form of a {@link com.terranrepublic.assets.Universe} catalog entry.
 *
 * <p>v2 Phase F.1 §4.2 — mirrors {@link GateNetworkEntity}'s shape without the provenance
 * columns (Universe doesn't carry provenance — see Universe javadoc) and with an extra
 * {@code active} column for the per-universe activation state.
 *
 * <p>No L2 cache (no read pressure at F.1 scale). No JSON LOB columns (no collection fields on
 * Universe). Conversion is the job of {@link UniverseMapper}; nothing else should touch this
 * class directly.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description"})
@DynamicUpdate
@Entity(name = "UNIVERSE")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "lifecycle"),
        @Index(columnList = "active")
})
public class UniverseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 4000)
    private String description;

    private String sourceAuthor;

    @Column(nullable = false, length = 32)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UniverseLifecycle lifecycle;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant modifiedAt;

    /**
     * Creates an entity with a freshly generated id and NOT NULL defaults matching the V15
     * column defaults + {@link com.terranrepublic.assets.Universe} compact constructor.
     *
     * @param name display name
     */
    public UniverseEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = "";
        this.sourceAuthor = "";
        this.version = "1.0";
        this.lifecycle = UniverseLifecycle.AVAILABLE;
        this.active = false;
        Instant now = Instant.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }

    @PrePersist
    private void ensureDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.description == null) {
            this.description = "";
        }
        if (this.sourceAuthor == null) {
            this.sourceAuthor = "";
        }
        if (this.version == null || this.version.isBlank()) {
            this.version = "1.0";
        }
        if (this.lifecycle == null) {
            this.lifecycle = UniverseLifecycle.AVAILABLE;
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
        UniverseEntity that = (UniverseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
