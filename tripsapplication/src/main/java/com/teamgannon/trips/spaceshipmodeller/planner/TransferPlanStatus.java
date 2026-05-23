package com.teamgannon.trips.spaceshipmodeller.planner;

/**
 * Feasibility status of a saved transfer plan, derived from the computed plan.
 */
public enum TransferPlanStatus {

    /** The ship has enough delta-V and propellant for the burns. */
    FEASIBLE("Feasible"),

    /** The ship's delta-V budget is below the transfer requirement. */
    INSUFFICIENT_DELTA_V("Insufficient Δv"),

    /** Delta-V is sufficient, but the ship does not carry enough propellant. */
    INSUFFICIENT_PROPELLANT("Insufficient propellant");

    private final String label;

    TransferPlanStatus(String label) {
        this.label = label;
    }

    /** @return human-readable label */
    public String label() {
        return label;
    }
}
