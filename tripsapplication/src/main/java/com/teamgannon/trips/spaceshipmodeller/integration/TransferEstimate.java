package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * The result of a first-order orbital-transfer estimate between two circular, coplanar orbits.
 * <p>
 * All figures are estimates intended to gauge feasibility, not to fly a mission: a two-impulse Hohmann
 * transfer, the rocket equation for propellant, and a constant-thrust approximation for burn time. A real
 * Transfer Planner would refine these.
 *
 * @param originAxisAu            origin orbital radius, in AU
 * @param destinationAxisAu       destination orbital radius, in AU
 * @param centralMassSolar        central body mass, in solar masses
 * @param requiredDeltaVKmps      Hohmann delta-V required, in km/s
 * @param shipDeltaVKmps          the ship's available delta-V budget, in km/s ({@code NaN} if reactionless)
 * @param transferTimeDays        Hohmann transfer (coast) time, in days
 * @param propellantRequiredTons  propellant needed for the manoeuvre, in tonnes ({@code NaN} if not derivable)
 * @param propellantAvailableTons propellant the ship actually carries, in tonnes
 * @param burnTimeSeconds         rough constant-thrust burn time, in seconds ({@code NaN} if no thrust)
 * @param feasible                {@code true} if the ship's delta-V budget covers the requirement
 */
public record TransferEstimate(
        double originAxisAu,
        double destinationAxisAu,
        double centralMassSolar,
        double requiredDeltaVKmps,
        double shipDeltaVKmps,
        double transferTimeDays,
        double propellantRequiredTons,
        double propellantAvailableTons,
        double burnTimeSeconds,
        boolean feasible
) {

    /** @return delta-V budget remaining after the transfer, in km/s ({@code NaN} if the ship Δv is unknown) */
    public double deltaVMarginKmps() {
        return shipDeltaVKmps - requiredDeltaVKmps;
    }

    /** @return {@code true} if the ship carries enough propellant for the manoeuvre */
    public boolean propellantSufficient() {
        return !Double.isNaN(propellantRequiredTons) && propellantAvailableTons >= propellantRequiredTons;
    }
}
