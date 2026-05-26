package com.teamgannon.trips.spaceshipmodeller.io;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.List;

/**
 * Stable JSON wire format for a {@link SpaceshipDesign}.
 * <p>
 * Why a DTO rather than serialising {@link SpaceshipDesign} directly: the domain record exposes derived
 * boolean accessors ({@code isMothership()}, {@code isSuitableForLanding()}) which Jackson would otherwise
 * emit as bogus {@code "mothership"}/{@code "suitableForLanding"} JSON fields. This record contains only the
 * design's actual data components, so the exported JSON is exactly the design's state and round-trips
 * cleanly. {@link MassBudget} and {@link CarriedCraft} have no getter-style ("get"/"is") accessors, so
 * they nest safely.
 *
 * @param id              stable id
 * @param name            display name
 * @param designation     hull designation
 * @param shipClass       role/size class
 * @param driveType       installed propulsion
 * @param massBudget      mass breakdown
 * @param crewComplement  crew head count
 * @param lengthMeters    overall length
 * @param carriedCraft    embarked smaller craft
 * @param armaments       mounted weapons and weapon batteries
 * @param iconPath        icon path/key
 * @param description     free-form description
 * @param sourceType      provenance (real, proposed or science fiction)
 * @param sourceUniverse  source universe / franchise
 * @param faction         in-universe faction / operator
 * @param concealed       whether the design is hidden in catalog views
 * @param era             era / timeframe
 * @param createdAt       creation timestamp
 */
public record SpaceshipDesignDto(
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

    /**
     * @param design the domain design
     * @return a DTO mirroring the design's data
     */
    public static SpaceshipDesignDto fromDomain(SpaceshipDesign design) {
        return new SpaceshipDesignDto(
                design.id(), design.name(), design.designation(), design.shipClass(), design.driveType(),
                design.massBudget(), design.crewComplement(), design.lengthMeters(), design.carriedCraft(),
                design.armaments(), design.iconPath(), design.description(), design.sourceType(), design.sourceUniverse(),
                design.faction(), design.concealed(), design.era(), design.createdAt());
    }

    /**
     * @return the domain design reconstructed from this DTO (a fresh creation time is supplied if absent)
     */
    public SpaceshipDesign toDomain() {
        return new SpaceshipDesign(
                id, name, designation, shipClass, driveType, massBudget, crewComplement, lengthMeters,
                carriedCraft, armaments, iconPath, description, sourceType, sourceUniverse, faction, concealed, era,
                createdAt != null ? createdAt : Instant.now());
    }
}
