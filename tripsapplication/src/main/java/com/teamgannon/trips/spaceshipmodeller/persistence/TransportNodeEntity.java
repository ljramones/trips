package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.infrastructure.NodeType;
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
 * JPA persistence form of a {@link com.terranrepublic.infrastructure.TransportNode}.
 * <p>
 * First persisted subtype of the {@link com.terranrepublic.infrastructure.SpaceInfrastructure}
 * sealed hierarchy. The shape mirrors the SpaceAsset entities ({@link StationEntity},
 * {@link WeaponInstallationEntity}): flat scalar columns + JSON LOB for the only collection
 * field ({@code connectedNodeIds}).
 * <p>
 * The connected-node-ids column intentionally has no FK to {@code transport_node.id}. The
 * in-memory {@code GraphRegistry} keeps the dangling-id validation role for now; promoting it
 * to a database-level FK is deferred to a future phase. v2 Phase B prompt explicitly flagged
 * this as out of scope.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description", "connectedNodeIdsJson"})
@DynamicUpdate
@Entity
@Table(name = "transport_node", indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "type"),
        @Index(columnList = "faction")
})
public class TransportNodeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String source;

    private String faction;

    @Column(nullable = false)
    private boolean concealed;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeType type;

    private double positionX;
    private double positionY;
    private double positionZ;

    /** Serialised list of connected node ids; see {@link TransportNodeMapper}. */
    @Lob
    private String connectedNodeIdsJson;

    private double throughputTonsPerTick;

    @Column(nullable = false)
    private boolean instantaneousTransit;

    private double traversalTimeTicks;

    /**
     * v2 Phase F.1 §4.4 — worldbuilding universe affiliation. {@code null} means canonical/real;
     * non-null references {@code universe.id}. V16 migration adds the column.
     */
    private String universeId;

    private Instant createdAt;

    private Instant modifiedAt;

    public TransportNodeEntity(String name, NodeType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.concealed = false;
        this.instantaneousTransit = false;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.type == null) {
            // Belt-and-braces: the NOT NULL constraint fires before V9 has
            // run on stale dev DBs. Default to RELAY so callers see a
            // recognisable placeholder rather than a constraint violation.
            this.type = NodeType.RELAY;
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
        TransportNodeEntity that = (TransportNodeEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
