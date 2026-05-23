package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * The kind of orbital transfer to plan.
 * <p>
 * Only {@link #HOHMANN} is computed today; the type is carried through the planning API as an extension
 * point for bi-elliptic or low-thrust spiral transfers later.
 */
public enum TransferType {

    /** Two-impulse minimum-energy transfer between circular, coplanar orbits. */
    HOHMANN("Hohmann transfer");

    private final String label;

    TransferType(String label) {
        this.label = label;
    }

    /** @return human-readable name */
    public String label() {
        return label;
    }
}
