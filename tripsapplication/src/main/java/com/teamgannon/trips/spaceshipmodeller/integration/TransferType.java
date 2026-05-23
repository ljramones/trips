package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * The kind of orbital transfer to plan.
 */
public enum TransferType {

    /** Two-impulse minimum-energy transfer between circular, coplanar orbits. */
    HOHMANN("Hohmann (2-burn)"),

    /** Three-impulse transfer via a high intermediate apoapsis; can beat Hohmann for large radius ratios. */
    BI_ELLIPTIC("Bi-elliptic (3-burn)"),

    /** Continuous low-thrust spiral approximation (for ion/Hall/VASIMR/fusion electric drives). */
    LOW_THRUST_APPROX("Low-thrust spiral");

    private final String label;

    TransferType(String label) {
        this.label = label;
    }

    /** @return human-readable name */
    public String label() {
        return label;
    }
}
