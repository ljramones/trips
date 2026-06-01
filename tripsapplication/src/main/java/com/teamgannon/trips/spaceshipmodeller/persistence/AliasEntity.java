package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.AliasTargetKind;
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
 * JPA persistence form of {@link com.terranrepublic.assets.Alias}.
 *
 * <p>Phase F.2 §4.3 — third top-level catalog entity outside the SpaceAsset/SpaceInfrastructure
 * sealed hierarchies. Mirrors the GateNetwork + Universe entity shapes: flat scalar columns,
 * no JSON LOBs, no L2 cache.
 *
 * <p>{@code @Index} annotations declared here are documentation/intent; the actual SQL DDL
 * lives in V18 (per the repo's Flyway discipline; {@code ddl-auto=validate} doesn't generate
 * indexes). The unique composite constraint on {@code (universe_id, target_kind, target_id)}
 * is enforced by V18's {@code uk_alias_universe_target} unique constraint.
 *
 * <p>FK to {@code universe(id)} with {@code ON DELETE CASCADE} (V18) — deleting a Universe
 * deletes all its aliases. Distinct from F.1's catalog tables (which use {@code SET NULL}
 * because catalog entries can exist canonically without a universe).
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"description"})
@DynamicUpdate
@Entity(name = "ALIAS")
@Table(indexes = {
        @Index(name = "idx_alias_universe", columnList = "universe_id"),
        @Index(name = "idx_alias_target", columnList = "target_kind, target_id"),
        @Index(name = "uk_alias_universe_target",
                columnList = "universe_id, target_kind, target_id",
                unique = true)
})
public class AliasEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "universe_id", nullable = false, length = 64)
    private String universeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_kind", nullable = false, length = 32)
    private AliasTargetKind targetKind;

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Column(name = "alias_text", nullable = false, length = 255)
    private String aliasText;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    /**
     * Convenience constructor for fresh entities — UUID id + timestamps set.
     */
    public AliasEntity(String universeId, AliasTargetKind targetKind, String targetId, String aliasText) {
        this.id = "catalog-alias-" + UUID.randomUUID();
        this.universeId = universeId;
        this.targetKind = targetKind;
        this.targetId = targetId;
        this.aliasText = aliasText;
        this.description = "";
        Instant now = Instant.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }

    @PrePersist
    private void ensureDefaults() {
        if (this.id == null) {
            this.id = "catalog-alias-" + UUID.randomUUID();
        }
        if (this.description == null) {
            this.description = "";
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
        AliasEntity that = (AliasEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
