package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * A named orbiting body usable as a transfer origin or destination: just the bits the transfer maths needs
 * (a display name and a circular orbital radius in AU).
 * <p>
 * Decouples transfer planning from any particular celestial-body model — the Solar System view builds these
 * from its {@code PlanetDescription}s, while the Spaceship Modeller offers Solar-System presets.
 *
 * @param name             display name
 * @param semiMajorAxisAu  orbital radius, in AU
 */
public record TransferBody(String name, double semiMajorAxisAu) {

    public TransferBody {
        if (name == null || name.isBlank()) {
            name = "Body";
        }
    }

    @Override
    public String toString() {
        return name + " (" + semiMajorAxisAu + " AU)";
    }
}
