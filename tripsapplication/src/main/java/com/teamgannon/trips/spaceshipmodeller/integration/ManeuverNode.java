package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * A single impulsive burn within a {@link TransferPlan}.
 *
 * @param name              what the burn does (e.g. "Departure burn")
 * @param deltaVKmps        the burn's delta-V, in km/s
 * @param timeFromStartDays when the burn occurs, in days after departure
 * @param propellantTons    propellant consumed by this burn, in tonnes ({@code NaN} if not derivable)
 * @param burnTimeSeconds   rough constant-thrust burn duration, in seconds ({@code NaN} if no thrust)
 */
public record ManeuverNode(
        String name,
        double deltaVKmps,
        double timeFromStartDays,
        double propellantTons,
        double burnTimeSeconds
) {
}
