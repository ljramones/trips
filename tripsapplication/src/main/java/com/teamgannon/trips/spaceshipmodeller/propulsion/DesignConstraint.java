package com.teamgannon.trips.spaceshipmodeller.propulsion;

/**
 * An inherent engineering or operational constraint that a {@link DriveType} imposes on any ship that
 * uses it.
 * <p>
 * Constraints are descriptive metadata attached to a drive (for example, "exhaust is radioactive" or
 * "requires an external beam"). The rules engine reads them when validating a {@link
 * com.terranrepublic.assets.SpaceshipDesign}. A {@code blocking} constraint represents a
 * hard physical limitation (it will typically produce an error if the design is used in a way that
 * violates it); a non-blocking constraint is advisory.
 *
 * @param code        stable machine-readable identifier (e.g. {@code "RADIOACTIVE_EXHAUST"})
 * @param description human-readable explanation of the constraint
 * @param blocking    {@code true} if the constraint represents a hard limitation rather than mere advice
 * @author TRIPS Spaceship Modeller
 */
public record DesignConstraint(String code, String description, boolean blocking) {

    /**
     * Compact constructor enforcing that the identifying fields are present.
     */
    public DesignConstraint {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("DesignConstraint code must be provided");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("DesignConstraint description must be provided");
        }
    }

    /**
     * Factory for a hard, blocking constraint.
     *
     * @param code        stable identifier
     * @param description human-readable explanation
     * @return a blocking constraint
     */
    public static DesignConstraint blocking(String code, String description) {
        return new DesignConstraint(code, description, true);
    }

    /**
     * Factory for an advisory, non-blocking constraint.
     *
     * @param code        stable identifier
     * @param description human-readable explanation
     * @return an advisory constraint
     */
    public static DesignConstraint advisory(String code, String description) {
        return new DesignConstraint(code, description, false);
    }
}
