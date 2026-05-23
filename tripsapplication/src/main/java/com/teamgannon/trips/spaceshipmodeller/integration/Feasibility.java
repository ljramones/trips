package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * Three-level feasibility of a transfer for a given ship.
 * <p>
 * Ordered by severity: {@code FEASIBLE} (0) &lt; {@code MARGINAL} (1) &lt; {@code INSUFFICIENT} (2), so the
 * worse of two statuses is the one with the larger ordinal.
 */
public enum Feasibility {

    /** Comfortable margin (UI: green). */
    FEASIBLE("Feasible"),

    /** Exactly enough or very tight (UI: orange). */
    MARGINAL("Marginal"),

    /** Clearly not enough (UI: red). */
    INSUFFICIENT("Insufficient");

    private final String label;

    Feasibility(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
