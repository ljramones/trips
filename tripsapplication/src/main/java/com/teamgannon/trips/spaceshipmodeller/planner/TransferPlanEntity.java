package com.teamgannon.trips.spaceshipmodeller.planner;

import com.teamgannon.trips.spaceshipmodeller.integration.TransferType;
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
 * JPA persistence form of a {@link SavedTransferPlan}. Maneuver nodes are stored as a JSON {@link Lob}
 * (see {@code TransferPlanMapper}); the transfer type and status are {@link EnumType#STRING} columns.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"maneuverNodesJson"})
@DynamicUpdate
@Entity(name = "TRANSFER_PLAN")
@Table(indexes = {
        @Index(columnList = "shipId"),
        @Index(columnList = "solarSystemId"),
        @Index(columnList = "createdAt")
})
public class TransferPlanEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String shipId;
    private String shipName;
    private String solarSystemId;

    @Enumerated(EnumType.STRING)
    private TransferType transferType;

    private String originName;
    private double originAu;
    private String destinationName;
    private double destinationAu;
    private double centralMassSolar;

    @Lob
    private String maneuverNodesJson;

    private double totalDeltaVKmps;
    private double totalPropellantTons;
    private double transferTimeDays;
    private double shipDeltaVKmps;
    private boolean feasible;
    private boolean propellantSufficient;

    @Enumerated(EnumType.STRING)
    private TransferPlanStatus status;

    private Instant createdAt;

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
        TransferPlanEntity that = (TransferPlanEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
