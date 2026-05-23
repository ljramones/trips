package com.teamgannon.trips.spaceshipmodeller.propulsion;

/**
 * How much dedicated waste-heat radiator capacity a drive demands, ordered from none to overwhelming.
 * <p>
 * High-efficiency drives convert a large fraction of their power into waste heat that must be radiated to
 * space; for the most extreme drives the radiators dominate the vehicle's dry mass. The rules engine uses
 * {@link #requiresDedicatedRadiators()} to insist that a design actually allocates radiator mass.
 *
 * @author TRIPS Spaceship Modeller
 */
public enum RadiatorLevel {

    /** No dedicated radiators; waste heat is carried away by the exhaust. */
    NONE("None", "No dedicated radiators required; waste heat leaves with the exhaust"),

    /** Small panels sufficient. */
    MINIMAL("Minimal", "Modest panels sufficient for housekeeping heat loads"),

    /** Substantial radiator wings are a real part of the design. */
    MODERATE("Moderate", "Substantial radiator wings form part of the structure"),

    /** Radiators are a major structural element and mass driver. */
    EXTENSIVE("Extensive", "Radiators are a major structural element and mass driver"),

    /** Radiators dominate the vehicle's dry mass and surface area. */
    MASSIVE("Massive", "Radiators dominate the vehicle's dry mass and surface area");

    private final String label;
    private final String description;

    RadiatorLevel(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /** @return short human-readable name */
    public String label() {
        return label;
    }

    /** @return one-line summary of the heat-rejection demand */
    public String description() {
        return description;
    }

    /**
     * @return {@code true} once the level reaches {@link #MODERATE}, meaning the design must carry
     * dedicated radiator mass to be physically plausible
     */
    public boolean requiresDedicatedRadiators() {
        return this.ordinal() >= MODERATE.ordinal();
    }
}
