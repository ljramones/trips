package com.teamgannon.trips.spaceshipmodeller.core;

/**
 * The mass breakdown of a spaceship, in metric tonnes.
 * <p>
 * Dry mass is everything except propellant; wet mass adds the propellant load. The
 * {@linkplain #massRatio() mass ratio} (wet / dry) is the key driver of achievable delta-V via the
 * Tsiolkovsky rocket equation.
 *
 * @param structureMassTons  hull, tankage and primary structure
 * @param engineMassTons     the drive(s) and associated machinery
 * @param propellantMassTons reaction mass carried (zero for reactionless drives)
 * @param payloadMassTons    cargo, weapons and carried craft allowance
 * @param crewMassTons       crew, life support and habitation
 * @param radiatorMassTons   dedicated waste-heat radiators
 * @author TRIPS Spaceship Modeller
 */
public record MassBudget(
        double structureMassTons,
        double engineMassTons,
        double propellantMassTons,
        double payloadMassTons,
        double crewMassTons,
        double radiatorMassTons
) {

    /**
     * Compact constructor rejecting negative masses.
     */
    public MassBudget {
        if (structureMassTons < 0 || engineMassTons < 0 || propellantMassTons < 0
                || payloadMassTons < 0 || crewMassTons < 0 || radiatorMassTons < 0) {
            throw new IllegalArgumentException("Mass components must not be negative");
        }
    }

    /** @return dry mass: everything except propellant, in tonnes */
    public double dryMassTons() {
        return structureMassTons + engineMassTons + payloadMassTons + crewMassTons + radiatorMassTons;
    }

    /** @return wet mass: dry mass plus propellant, in tonnes */
    public double wetMassTons() {
        return dryMassTons() + propellantMassTons;
    }

    /**
     * @return the mass ratio (wet / dry); {@link Double#POSITIVE_INFINITY} if the dry mass is zero, and
     * {@code 1.0} if no propellant is carried
     */
    public double massRatio() {
        double dry = dryMassTons();
        return dry > 0 ? wetMassTons() / dry : Double.POSITIVE_INFINITY;
    }

    /** @return propellant mass as a fraction of wet mass, in the range [0, 1] */
    public double propellantFraction() {
        double wet = wetMassTons();
        return wet > 0 ? propellantMassTons / wet : 0.0;
    }

    /** @return dry mass as a fraction of wet mass, in the range [0, 1] */
    public double dryMassFraction() {
        double wet = wetMassTons();
        return wet > 0 ? dryMassTons() / wet : 0.0;
    }

    /** @return dry mass as a percentage of wet mass, in the range [0, 100] */
    public double dryMassPercent() {
        return dryMassFraction() * 100.0;
    }
}
