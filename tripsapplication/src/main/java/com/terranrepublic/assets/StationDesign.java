package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.List;

/**
 * Fixed or semi-fixed fortification/catalog asset without ship-only propellant or mass-ratio fields.
 */
public record StationDesign(
        String id,
        String name,
        String designation,
        StationType stationType,
        String source,
        String faction,
        boolean concealed,
        String allegiance,
        String description,
        double overallSpanMeters,
        double interiorSpanMeters,
        double dryMassTons,
        double armourThicknessMeters,
        int crewCapacity,
        int crewComplement,
        double pressurizedVolumeM3,
        Mobility mobility,
        DriveType auxiliaryDrive,
        List<CarriedCraft> carriedCraft,
        List<Armament> armaments,
        double hangarVolumeM3,
        boolean carrierCapable,
        TechLevel techLevel,
        String category,
        OperationalState operationalState,
        Instant createdAt,
        Instant modifiedAt
) implements SpaceAsset {

    /**
     * Backwards-compatible constructor for existing station call sites with faction/allegiance.
     */
    public StationDesign(
            String id,
            String name,
            String designation,
            StationType stationType,
            String source,
            String faction,
            boolean concealed,
            String allegiance,
            String description,
            double overallSpanMeters,
            double interiorSpanMeters,
            double dryMassTons,
            double armourThicknessMeters,
            int crewCapacity,
            int crewComplement,
            double pressurizedVolumeM3,
            Mobility mobility,
            DriveType auxiliaryDrive,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            double hangarVolumeM3,
            boolean carrierCapable,
            TechLevel techLevel,
            String category,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, stationType, source, faction, concealed, allegiance, description, overallSpanMeters,
                interiorSpanMeters, dryMassTons, armourThicknessMeters, crewCapacity, crewComplement,
                pressurizedVolumeM3, mobility, auxiliaryDrive, carriedCraft, armaments, hangarVolumeM3,
                carrierCapable, techLevel, category, OperationalState.OPERATIONAL, createdAt, modifiedAt);
    }

    /**
     * Backwards-compatible constructor for existing station catalog call sites.
     */
    public StationDesign(
            String id,
            String name,
            String designation,
            StationType stationType,
            String source,
            String description,
            double overallSpanMeters,
            double interiorSpanMeters,
            double dryMassTons,
            double armourThicknessMeters,
            int crewCapacity,
            int crewComplement,
            double pressurizedVolumeM3,
            Mobility mobility,
            DriveType auxiliaryDrive,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            double hangarVolumeM3,
            boolean carrierCapable,
            TechLevel techLevel,
            String category,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, stationType, source, "Unknown", false, null, description, overallSpanMeters,
                interiorSpanMeters, dryMassTons, armourThicknessMeters, crewCapacity, crewComplement,
                pressurizedVolumeM3, mobility, auxiliaryDrive, carriedCraft, armaments, hangarVolumeM3,
                carrierCapable, techLevel, category, OperationalState.OPERATIONAL, createdAt, modifiedAt);
    }

    public StationDesign {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("StationDesign name must be provided");
        }
        if (stationType == null) {
            throw new IllegalArgumentException("StationDesign stationType must be provided");
        }
        mobility = mobility == null ? Mobility.FIXED : mobility;
        if (mobility == Mobility.FIXED && auxiliaryDrive != null) {
            throw new IllegalArgumentException("auxiliaryDrive is only valid for non-fixed stations");
        }
        if (dryMassTons < 0 || overallSpanMeters < 0 || interiorSpanMeters < 0 || armourThicknessMeters < 0
                || crewCapacity < 0 || crewComplement < 0 || pressurizedVolumeM3 < 0 || hangarVolumeM3 < 0) {
            throw new IllegalArgumentException("Station numeric fields must not be negative");
        }
        designation = designation == null ? "" : designation;
        source = source == null ? "" : source;
        faction = faction == null || faction.isBlank() ? "Unknown" : faction;
        allegiance = allegiance == null || allegiance.isBlank() ? faction : allegiance;
        description = description == null ? "" : description;
        carriedCraft = carriedCraft == null ? List.of() : List.copyOf(carriedCraft);
        armaments = armaments == null ? List.of() : List.copyOf(armaments);
        techLevel = techLevel == null ? TechLevel.UNKNOWN : techLevel;
        category = category == null ? stationType.name() : category;
        operationalState = operationalState == null ? OperationalState.OPERATIONAL : operationalState;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    @Override
    public AssetKind kind() {
        return AssetKind.STATION;
    }
}
