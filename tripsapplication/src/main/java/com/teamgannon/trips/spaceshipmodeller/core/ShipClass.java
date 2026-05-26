package com.teamgannon.trips.spaceshipmodeller.core;

/**
 * The role/size class of a spaceship.
 * <p>
 * Each class records two structural facts the rules engine cares about:
 * <ul>
 *   <li>{@code carrierCapable} &mdash; whether the class is a "mothership" that can carry smaller craft;</li>
 *   <li>{@code carriable} &mdash; whether the class is small enough to be carried by a mothership.</li>
 * </ul>
 * It also records {@code routinelyLands}, used to require landing-capable propulsion on classes that are
 * expected to make planetfall.
 *
 * @author TRIPS Spaceship Modeller
 */
public enum ShipClass {

    /** Small crewed craft for short hops between ships, stations and surfaces. */
    SHUTTLE("Shuttle", true, false, true, "Short-range crew/cargo transfer"),

    /** Dedicated surface-to-orbit lander. */
    LANDER("Lander", true, false, true, "Surface landing and ascent"),

    /** Armoured craft that delivers troops or cargo to a surface under fire. */
    DROPSHIP("Dropship", true, false, true, "Combat surface insertion"),

    /** Small, fast combat craft operating from a carrier. */
    FIGHTER("Fighter", true, false, false, "Space superiority / interception"),

    /** Smallest independent warship. */
    CORVETTE("Corvette", false, false, false, "Patrol and escort"),

    /** Mid-weight warship. */
    FRIGATE("Frigate", false, false, false, "Escort and independent operations"),

    /** Flagship or fleet-control vessel with enhanced command facilities. */
    COMMAND_SHIP("Command Ship", false, true, false, "Fleet command and control"),

    /** Heavy warship; large enough to carry a few small craft (pinnaces, assault shuttles). */
    CRUISER("Cruiser", false, true, false, "Line combatant"),

    /** Very heavy capital warship built around maximum battle endurance. */
    DREADNOUGHT("Dreadnought", false, true, false, "Capital ship line combatant"),

    /** Heavy defensive combatant optimized for system or orbital defence. */
    MONITOR("Monitor", false, false, false, "System defence heavy combatant"),

    /** Bulk cargo hauler. */
    FREIGHTER("Freighter", false, false, false, "Bulk cargo transport"),

    /** Warship whose primary armament is its embarked fighter wing. */
    CARRIER("Carrier", false, true, false, "Fighter and small-craft operations"),

    /** Large vessel that carries a complement of smaller craft as an integrated capability. */
    MOTHERSHIP("Mothership", false, true, false, "Carries shuttles, landers and fighters"),

    /** Self-contained colonisation vessel carrying landers and surface equipment. */
    COLONY_SHIP("Colony Ship", false, true, false, "Interstellar colonisation; carries landers"),

    /** Uncrewed orbital platform or spacecraft bus. */
    SATELLITE("Satellite", true, false, false, "Uncrewed orbital platform"),

    /** Uncrewed exploration or survey craft. */
    PROBE("Probe", true, false, false, "Uncrewed exploration and survey"),

    /** Utility vessel for towing, docking and close manoeuvre work. */
    TUG("Tug", false, false, false, "Towing and orbital utility work"),

    /** Cargo vessel optimized for bulk transport. */
    HAULER("Hauler", false, false, false, "Bulk transport"),

    /** Fuel or reaction-mass transport vessel. */
    COLLIER("Collier", false, false, false, "Fuel and reaction-mass transport");

    private final String label;
    private final boolean carriable;
    private final boolean carrierCapable;
    private final boolean routinelyLands;
    private final String role;

    ShipClass(String label, boolean carriable, boolean carrierCapable, boolean routinelyLands, String role) {
        this.label = label;
        this.carriable = carriable;
        this.carrierCapable = carrierCapable;
        this.routinelyLands = routinelyLands;
        this.role = role;
    }

    /** @return short human-readable name */
    public String label() {
        return label;
    }

    /** @return {@code true} if a mothership may carry this class */
    public boolean carriable() {
        return carriable;
    }

    /** @return {@code true} if this class can itself carry smaller craft */
    public boolean carrierCapable() {
        return carrierCapable;
    }

    /** @return {@code true} if this class is expected to perform planetary landings */
    public boolean routinelyLands() {
        return routinelyLands;
    }

    /** @return one-line description of the class's purpose */
    public String role() {
        return role;
    }
}
