package com.teamgannon.trips.spaceshipmodeller.planner;

import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A persisted transfer plan: a computed {@link TransferPlan} plus the saved-library context (id, links to a
 * ship and solar system, status, creation time). Origin/destination are flattened to name + AU for storage.
 * <p>
 * This is the domain object the {@link TransferPlanService} returns; the JPA {@code TransferPlanEntity} never
 * leaves the persistence layer.
 *
 * @param id                   stable id
 * @param shipId               id of the ship the plan is for (may be {@code null})
 * @param shipName             ship display name
 * @param solarSystemId        id of the solar system (may be {@code null})
 * @param transferType         transfer type
 * @param originName           origin body name
 * @param originAu             origin orbital radius (AU)
 * @param destinationName      destination body name
 * @param destinationAu        destination orbital radius (AU)
 * @param centralMassSolar     central star mass used (solar masses)
 * @param nodes                maneuver nodes
 * @param totalDeltaVKmps      total delta-V (km/s)
 * @param totalPropellantTons  total propellant (tonnes; {@code NaN} if not derivable)
 * @param transferTimeDays     transfer time (days)
 * @param shipDeltaVKmps       ship delta-V budget (km/s; {@code NaN} if reactionless)
 * @param feasible             whether the ship's delta-V covers the requirement
 * @param propellantSufficient whether the ship carries enough propellant
 * @param status               derived feasibility status
 * @param createdAt            creation timestamp
 */
public record SavedTransferPlan(
        String id,
        String shipId,
        String shipName,
        String solarSystemId,
        TransferType transferType,
        String originName,
        double originAu,
        String destinationName,
        double destinationAu,
        double centralMassSolar,
        List<ManeuverNode> nodes,
        double totalDeltaVKmps,
        double totalPropellantTons,
        double transferTimeDays,
        double shipDeltaVKmps,
        boolean feasible,
        boolean propellantSufficient,
        TransferPlanStatus status,
        Instant createdAt
) {

    public SavedTransferPlan {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
    }

    /** @return "Origin → Destination" for display */
    public String route() {
        return originName + " → " + destinationName;
    }

    /**
     * Builds a savable plan from a freshly computed one, assigning a new id, creation time and derived
     * status.
     *
     * @param plan             the computed plan
     * @param shipId           the ship id (may be {@code null})
     * @param solarSystemId    the solar system id (may be {@code null})
     * @param centralMassSolar the central star mass used
     * @return a new saved plan ready to persist
     */
    public static SavedTransferPlan fromComputed(TransferPlan plan, String shipId,
                                                 String solarSystemId, double centralMassSolar) {
        TransferPlanStatus status;
        if (!plan.feasible()) {
            status = TransferPlanStatus.INSUFFICIENT_DELTA_V;
        } else if (!plan.propellantSufficient()) {
            status = TransferPlanStatus.INSUFFICIENT_PROPELLANT;
        } else {
            status = TransferPlanStatus.FEASIBLE;
        }
        return new SavedTransferPlan(
                UUID.randomUUID().toString(), shipId, plan.shipName(), solarSystemId, plan.type(),
                plan.origin().name(), plan.origin().semiMajorAxisAu(),
                plan.destination().name(), plan.destination().semiMajorAxisAu(),
                centralMassSolar, plan.nodes(), plan.totalDeltaVKmps(), plan.totalPropellantTons(),
                plan.transferTimeDays(), plan.shipDeltaVKmps(), plan.feasible(),
                plan.propellantSufficient(), status, Instant.now());
    }
}
