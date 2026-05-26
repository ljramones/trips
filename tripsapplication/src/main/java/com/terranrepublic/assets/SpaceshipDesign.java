package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.teamgannon.trips.spaceshipmodeller.propulsion.Category;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveSpecs;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.List;

/**
 * The aggregate root describing a complete spaceship design.
 * <p>
 * This is the immutable "Generic" design model: a name, a {@link ShipClass}, a {@link DriveType}, a
 * {@link MassBudget}, basic geometry, and an optional complement of {@link CarriedCraft}. The future
 * "Advanced 3D" branch is expected to attach richer geometry to the same identity.
 * <p>
 * Instances are normally produced through {@code SpaceshipBuilder} and checked by {@code ValidationEngine};
 * the helper methods here (delta-V estimate, landing suitability, carried-mass totals) are deliberately
 * dependency-free so the model can be reused by the transfer planner and the 3D view.
 *
 * @param id              stable unique identifier (UUID string)
 * @param name            display name
 * @param designation     hull designation/registry code (may be empty)
 * @param shipClass       role/size class
 * @param driveType       installed propulsion
 * @param massBudget      mass breakdown
 * @param crewComplement  number of crew aboard
 * @param lengthMeters    overall length, in metres
 * @param carriedCraft    embarked smaller craft (empty for non-motherships)
 * @param armaments       mounted weapons and weapon batteries
 * @param iconPath        path/key of the 2D icon for the generic branch (may be empty)
 * @param description     free-form description
 * @param createdAt       creation timestamp
 * @author TRIPS Spaceship Modeller
 */
public record SpaceshipDesign(
        String id,
        String name,
        String designation,
        ShipClass shipClass,
        DriveType driveType,
        MassBudget massBudget,
        int crewComplement,
        double lengthMeters,
        List<CarriedCraft> carriedCraft,
        List<Armament> armaments,
        String iconPath,
        String description,
        SourceType sourceType,
        String sourceUniverse,
        String faction,
        boolean concealed,
        OperationalState operationalState,
        String era,
        Instant createdAt
) implements SpaceAsset {

    /**
     * Backwards-compatible constructor for existing hidden/visible ship call sites.
     */
    public SpaceshipDesign(
            String id,
            String name,
            String designation,
            ShipClass shipClass,
            DriveType driveType,
            MassBudget massBudget,
            int crewComplement,
            double lengthMeters,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            String iconPath,
            String description,
            SourceType sourceType,
            String sourceUniverse,
            String faction,
            boolean concealed,
            String era,
            Instant createdAt
    ) {
        this(id, name, designation, shipClass, driveType, massBudget, crewComplement, lengthMeters,
                carriedCraft, armaments, iconPath, description, sourceType, sourceUniverse, faction, concealed,
                OperationalState.OPERATIONAL, era, createdAt);
    }

    /**
     * Backwards-compatible constructor for existing armed ship call sites.
     */
    public SpaceshipDesign(
            String id,
            String name,
            String designation,
            ShipClass shipClass,
            DriveType driveType,
            MassBudget massBudget,
            int crewComplement,
            double lengthMeters,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            String iconPath,
            String description,
            SourceType sourceType,
            String sourceUniverse,
            String faction,
            String era,
            Instant createdAt
    ) {
        this(id, name, designation, shipClass, driveType, massBudget, crewComplement, lengthMeters,
                carriedCraft, armaments, iconPath, description, sourceType, sourceUniverse, faction, false,
                OperationalState.OPERATIONAL, era, createdAt);
    }

    /**
     * Backwards-compatible constructor for existing ship-only call sites.
     */
    public SpaceshipDesign(
            String id,
            String name,
            String designation,
            ShipClass shipClass,
            DriveType driveType,
            MassBudget massBudget,
            int crewComplement,
            double lengthMeters,
            List<CarriedCraft> carriedCraft,
            String iconPath,
            String description,
            SourceType sourceType,
            String sourceUniverse,
            String faction,
            String era,
            Instant createdAt
    ) {
        this(id, name, designation, shipClass, driveType, massBudget, crewComplement, lengthMeters,
                carriedCraft, List.of(), iconPath, description, sourceType, sourceUniverse, faction, false,
                OperationalState.OPERATIONAL, era, createdAt);
    }

    /**
     * Compact constructor: validates required references and makes list fields immutable.
     */
    public SpaceshipDesign {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("SpaceshipDesign name must be provided");
        }
        if (shipClass == null) {
            throw new IllegalArgumentException("SpaceshipDesign shipClass must be provided");
        }
        if (driveType == null) {
            throw new IllegalArgumentException("SpaceshipDesign driveType must be provided");
        }
        if (massBudget == null) {
            throw new IllegalArgumentException("SpaceshipDesign massBudget must be provided");
        }
        if (crewComplement < 0) {
            throw new IllegalArgumentException("crewComplement must not be negative");
        }
        carriedCraft = carriedCraft == null ? List.of() : List.copyOf(carriedCraft);
        armaments = armaments == null ? List.of() : List.copyOf(armaments);
        designation = designation == null ? "" : designation;
        iconPath = iconPath == null ? "" : iconPath;
        description = description == null ? "" : description;
        sourceType = sourceType == null ? SourceType.UNKNOWN : sourceType;
        sourceUniverse = sourceUniverse == null ? "" : sourceUniverse;
        faction = faction == null || faction.isBlank() ? "Unknown" : faction;
        operationalState = operationalState == null ? OperationalState.OPERATIONAL : operationalState;
        era = era == null ? "" : era;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    /**
     * @return a one-line display label for this design's provenance: universe and faction where present
     * (e.g. "The Expanse - MCRN (~2350)"), otherwise the {@link SourceType} label, with the era appended
     * in parentheses when known
     */
    public String sourceLabel() {
        StringBuilder sb = new StringBuilder();
        if (!sourceUniverse.isBlank()) {
            sb.append(sourceUniverse);
        }
        if (!faction.isBlank() && !"Unknown".equals(faction)) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(faction);
        }
        if (sb.length() == 0) {
            sb.append(sourceType.label());
        }
        if (!era.isBlank()) {
            sb.append(" (").append(era).append(')');
        }
        return sb.toString();
    }

    @Override
    public String source() {
        return sourceLabel();
    }

    @Override
    public TechLevel techLevel() {
        return driveType.category() == Category.EXOTIC ? TechLevel.EXOTIC : TechLevel.UNKNOWN;
    }

    @Override
    public String category() {
        return shipClass.name();
    }

    @Override
    public Instant modifiedAt() {
        return createdAt;
    }

    @Override
    public double dryMassTons() {
        return massBudget.dryMassTons();
    }

    @Override
    public AssetKind kind() {
        return AssetKind.SHIP;
    }

    /** @return convenience accessor for the installed drive's performance envelope */
    public DriveSpecs driveSpecs() {
        return driveType.specs();
    }

    /** @return {@code true} if this design carries one or more smaller craft */
    public boolean carriesCraft() {
        return !carriedCraft.isEmpty();
    }

    /**
     * @return {@code true} if this design is acting as a mothership: a carrier-capable class that is
     * actually carrying craft
     */
    public boolean isMothership() {
        return shipClass.carrierCapable() && carriesCraft();
    }

    /** @return combined mass of all carried craft, in tonnes */
    public double totalCarriedMassTons() {
        return carriedCraft.stream().mapToDouble(CarriedCraft::totalMassTons).sum();
    }

    /** @return gross mass: the vessel's own wet mass plus all carried craft, in tonnes */
    public double grossMassTons() {
        return massBudget.wetMassTons() + totalCarriedMassTons();
    }

    /**
     * Estimates the ideal delta-V budget from the Tsiolkovsky rocket equation, {@code dv = Ve * ln(R)},
     * using the drive's average exhaust velocity and the budget's mass ratio.
     * <p>
     * This is a first-order estimate only: it ignores carried craft, gravity losses and staging. For
     * reaction-mass-free drives (sails) the rocket equation does not apply and this returns
     * {@link Double#NaN}.
     *
     * @return estimated delta-V in kilometres per second, or {@link Double#NaN} for reactionless drives
     */
    public double estimateDeltaVKmps() {
        if (driveSpecs().reactionless()) {
            return Double.NaN;
        }
        double massRatio = massBudget.massRatio();
        if (!Double.isFinite(massRatio) || massRatio <= 1.0) {
            return 0.0;
        }
        return driveSpecs().exhaustVelocityAverageKmps() * Math.log(massRatio);
    }

    /**
     * @return {@code true} if the installed drive could realistically land this ship (it is
     * landing-capable and can exceed a thrust-to-weight ratio of 1)
     * @see DriveSpecs#suitableForLanding()
     */
    public boolean isSuitableForLanding() {
        return driveSpecs().suitableForLanding();
    }
}
