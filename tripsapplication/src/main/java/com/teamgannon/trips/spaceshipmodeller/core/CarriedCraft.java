package com.teamgannon.trips.spaceshipmodeller.core;

/**
 * A complement of identical smaller craft carried aboard a mothership.
 * <p>
 * Rather than embedding a full {@link SpaceshipDesign} for every fighter in a wing, a carried complement is
 * summarised by its class, count and per-unit mass. A future revision may link this to a full design by id.
 *
 * @param name         display name of the craft type (e.g. {@code "Mk II Interceptor"})
 * @param shipClass    the class of the carried craft; must be {@link ShipClass#carriable()}
 * @param count        how many of this craft are carried (at least 1)
 * @param unitMassTons mass of a single craft, in metric tonnes
 * @param role         short description of the craft's role aboard the mothership
 * @author TRIPS Spaceship Modeller
 */
public record CarriedCraft(String name, ShipClass shipClass, int count, double unitMassTons, String role) {

    /**
     * Compact constructor enforcing basic invariants.
     */
    public CarriedCraft {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("CarriedCraft name must be provided");
        }
        if (shipClass == null) {
            throw new IllegalArgumentException("CarriedCraft shipClass must be provided");
        }
        if (count < 1) {
            throw new IllegalArgumentException("CarriedCraft count must be at least 1");
        }
        if (unitMassTons < 0) {
            throw new IllegalArgumentException("CarriedCraft unitMassTons must not be negative");
        }
        role = role == null ? "" : role;
    }

    /** @return combined mass of the whole complement, in tonnes */
    public double totalMassTons() {
        return count * unitMassTons;
    }
}
