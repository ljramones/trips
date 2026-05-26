package com.teamgannon.trips.spaceshipmodeller.builder;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Fluent builder for {@link SpaceshipDesign}.
 * <p>
 * The builder accepts mass components individually (so callers need not assemble a {@link MassBudget}
 * up front) or as a complete budget. Identity ({@code id}) and {@code createdAt} are assigned at
 * {@link #build()} time. The builder performs only presence checks; physical plausibility is the job of
 * {@code ValidationEngine}.
 *
 * <pre>{@code
 * SpaceshipDesign d = SpaceshipBuilder.create("Rocinante")
 *         .designation("MCRN-X-T-001")
 *         .shipClass(ShipClass.FRIGATE)
 *         .driveType(DriveType.FUSION_TORCH)
 *         .structureTons(200).engineTons(150).propellantTons(400)
 *         .payloadTons(50).crewTons(20).radiatorTons(80)
 *         .crew(4).lengthMeters(46)
 *         .build();
 * }</pre>
 *
 * @author TRIPS Spaceship Modeller
 */
public final class SpaceshipBuilder {

    private String name;
    private String designation = "";
    private ShipClass shipClass;
    private DriveType driveType;

    private double structureMassTons;
    private double engineMassTons;
    private double propellantMassTons;
    private double payloadMassTons;
    private double crewMassTons;
    private double radiatorMassTons;

    private int crewComplement;
    private double lengthMeters;
    private final List<CarriedCraft> carriedCraft = new ArrayList<>();
    private String iconPath = "";
    private String description = "";
    private SourceType sourceType = SourceType.UNKNOWN;
    private String sourceUniverse = "";
    private String faction = "";
    private String era = "";

    private SpaceshipBuilder() {
    }

    /**
     * Starts a new builder.
     *
     * @param name display name of the design
     * @return a fresh builder
     */
    public static SpaceshipBuilder create(String name) {
        return new SpaceshipBuilder().name(name);
    }

    /** Sets the display name. */
    public SpaceshipBuilder name(String name) {
        this.name = name;
        return this;
    }

    /** Sets the hull designation/registry code. */
    public SpaceshipBuilder designation(String designation) {
        this.designation = designation;
        return this;
    }

    /** Sets the role/size class. */
    public SpaceshipBuilder shipClass(ShipClass shipClass) {
        this.shipClass = shipClass;
        return this;
    }

    /** Sets the installed propulsion. */
    public SpaceshipBuilder driveType(DriveType driveType) {
        this.driveType = driveType;
        return this;
    }

    /** Replaces all mass components with the values from the given budget. */
    public SpaceshipBuilder massBudget(MassBudget budget) {
        Objects.requireNonNull(budget, "budget");
        this.structureMassTons = budget.structureMassTons();
        this.engineMassTons = budget.engineMassTons();
        this.propellantMassTons = budget.propellantMassTons();
        this.payloadMassTons = budget.payloadMassTons();
        this.crewMassTons = budget.crewMassTons();
        this.radiatorMassTons = budget.radiatorMassTons();
        return this;
    }

    /** Sets the structure/hull mass, in tonnes. */
    public SpaceshipBuilder structureTons(double tons) {
        this.structureMassTons = tons;
        return this;
    }

    /** Sets the engine mass, in tonnes. */
    public SpaceshipBuilder engineTons(double tons) {
        this.engineMassTons = tons;
        return this;
    }

    /** Sets the propellant mass, in tonnes. */
    public SpaceshipBuilder propellantTons(double tons) {
        this.propellantMassTons = tons;
        return this;
    }

    /** Sets the payload mass, in tonnes. */
    public SpaceshipBuilder payloadTons(double tons) {
        this.payloadMassTons = tons;
        return this;
    }

    /** Sets the crew/life-support mass, in tonnes. */
    public SpaceshipBuilder crewTons(double tons) {
        this.crewMassTons = tons;
        return this;
    }

    /** Sets the radiator mass, in tonnes. */
    public SpaceshipBuilder radiatorTons(double tons) {
        this.radiatorMassTons = tons;
        return this;
    }

    /** Sets the crew complement (head count). */
    public SpaceshipBuilder crew(int crewComplement) {
        this.crewComplement = crewComplement;
        return this;
    }

    /** Sets the overall length, in metres. */
    public SpaceshipBuilder lengthMeters(double lengthMeters) {
        this.lengthMeters = lengthMeters;
        return this;
    }

    /** Adds a pre-built carried-craft complement. */
    public SpaceshipBuilder carry(CarriedCraft craft) {
        this.carriedCraft.add(Objects.requireNonNull(craft, "craft"));
        return this;
    }

    /**
     * Adds a carried-craft complement from its parts.
     *
     * @param name         craft type name
     * @param shipClass    class of the carried craft
     * @param count        number carried
     * @param unitMassTons mass per craft, in tonnes
     * @param role         role aboard the mothership
     * @return this builder
     */
    public SpaceshipBuilder carry(String name, ShipClass shipClass, int count, double unitMassTons, String role) {
        return carry(new CarriedCraft(name, shipClass, count, unitMassTons, role));
    }

    /** Sets the 2D icon path/key for the generic branch. */
    public SpaceshipBuilder icon(String iconPath) {
        this.iconPath = iconPath;
        return this;
    }

    /** Sets the free-form description. */
    public SpaceshipBuilder description(String description) {
        this.description = description;
        return this;
    }

    /** Sets the provenance type (real, proposed or science fiction). */
    public SpaceshipBuilder sourceType(SourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    /** Sets the source universe / franchise (e.g. "The Expanse", "Caine Riordan"). */
    public SpaceshipBuilder sourceUniverse(String sourceUniverse) {
        this.sourceUniverse = sourceUniverse;
        return this;
    }

    /** Sets the in-universe faction / operator (e.g. "Terran Republic", "UN Navy", "NASA / JPL"). */
    public SpaceshipBuilder faction(String faction) {
        this.faction = faction;
        return this;
    }

    /** Sets the era / timeframe (e.g. "2045", "Post-Contact", "1977"). */
    public SpaceshipBuilder era(String era) {
        this.era = era;
        return this;
    }

    /**
     * Assembles the immutable {@link SpaceshipDesign}, assigning a fresh id and creation timestamp.
     *
     * @return the completed design
     * @throws NullPointerException if name, ship class or drive type were not set
     */
    public SpaceshipDesign build() {
        Objects.requireNonNull(name, "name must be set");
        Objects.requireNonNull(shipClass, "shipClass must be set");
        Objects.requireNonNull(driveType, "driveType must be set");

        MassBudget budget = new MassBudget(
                structureMassTons, engineMassTons, propellantMassTons,
                payloadMassTons, crewMassTons, radiatorMassTons);

        return new SpaceshipDesign(
                UUID.randomUUID().toString(),
                name,
                designation,
                shipClass,
                driveType,
                budget,
                crewComplement,
                lengthMeters,
                carriedCraft,
                iconPath,
                description,
                sourceType,
                sourceUniverse,
                faction,
                era,
                Instant.now());
    }
}
