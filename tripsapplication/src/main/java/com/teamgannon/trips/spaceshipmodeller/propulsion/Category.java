package com.teamgannon.trips.spaceshipmodeller.propulsion;

/**
 * High-level family a {@link DriveType} belongs to.
 * <p>
 * Categories group drives by their underlying physics so the UI and the rules engine can reason about
 * broad behaviour (for example, "all {@link #BEAMED} drives carry no reaction mass") without enumerating
 * every individual drive.
 *
 * @author TRIPS Spaceship Modeller
 */
public enum Category {

    /** Combustion of stored chemical propellant. Mature, very high thrust, modest efficiency. */
    CHEMICAL("Chemical", "Combustion of stored propellant; mature, high thrust, low efficiency"),

    /** Electrostatic or electromagnetic acceleration of propellant. High efficiency, very low thrust. */
    ELECTRIC("Electric", "Electrostatic/electromagnetic acceleration of propellant; high Isp, low thrust"),

    /** A fission reactor heats propellant directly. High thrust with moderate efficiency. */
    NUCLEAR_THERMAL("Nuclear Thermal", "Reactor heats propellant directly; high thrust, moderate Isp"),

    /** A fission reactor powers an electric thruster. Low thrust, high efficiency, large radiators. */
    NUCLEAR_ELECTRIC("Nuclear Electric", "Reactor powers an electric thruster; high Isp, large radiators"),

    /** Detonation of nuclear or fusion charges behind a pusher plate. Extreme pulsed thrust. */
    NUCLEAR_PULSE("Nuclear Pulse", "Detonation of nuclear charges behind a pusher plate; extreme thrust"),

    /** Controlled fusion of light nuclei for thrust and/or onboard power. */
    FUSION("Fusion", "Controlled fusion of light nuclei for thrust and power"),

    /** Matter-antimatter annihilation. Extreme energy density, severe storage and shielding demands. */
    ANTIMATTER("Antimatter", "Matter-antimatter annihilation; extreme energy density"),

    /** Momentum from an external photon/particle beam or from sunlight. Carries no reaction mass. */
    BEAMED("Beamed Power / Sail", "Momentum from an external beam or sunlight; reaction-mass-free"),

    /** Collects the interstellar medium as propellant and/or fusion fuel while underway. */
    INTERSTELLAR("Interstellar", "Collects the interstellar medium as propellant/fuel"),

    /** Speculative physics beyond known engineering. */
    EXOTIC("Exotic", "Speculative physics beyond known engineering");

    private final String label;
    private final String description;

    Category(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /** @return short human-readable name for menus and labels */
    public String label() {
        return label;
    }

    /** @return one-line summary of the category's physics */
    public String description() {
        return description;
    }
}
