package com.teamgannon.trips.spaceshipmodeller.propulsion;

/**
 * Qualitative thrust band for a drive, ordered from weakest to strongest.
 * <p>
 * The ordinal ordering is meaningful: {@code NEGLIGIBLE.ordinal() < EXTREME.ordinal()}. The rules engine
 * uses this to flag, for example, a {@link #NEGLIGIBLE} drive bolted to a heavy mothership.
 *
 * @author TRIPS Spaceship Modeller
 */
public enum ThrustLevel {

    /** Micronewtons to millinewtons. Photon sails, station-keeping ion thrusters. */
    NEGLIGIBLE("Negligible", "micronewtons to millinewtons"),

    /** Millinewtons to a few newtons. Solar sails, small electric thrusters. */
    VERY_LOW("Very Low", "millinewtons to newtons"),

    /** Newtons to kilonewtons. Hall and gridded ion arrays. */
    LOW("Low", "newtons to kilonewtons"),

    /** Kilonewtons. Nuclear-electric and fusion-pulse drives. */
    MODERATE("Moderate", "kilonewtons"),

    /** Tens to hundreds of kilonewtons. Nuclear thermal, gas-core. */
    HIGH("High", "tens to hundreds of kilonewtons"),

    /** Meganewtons. Chemical engines and fusion torches. */
    VERY_HIGH("Very High", "meganewtons"),

    /** Tens of meganewtons sustained, or pulsed giganewton spikes. Orion-style pulse drives. */
    EXTREME("Extreme", "tens of meganewtons sustained or pulsed giganewton spikes");

    private final String label;
    private final String magnitude;

    ThrustLevel(String label, String magnitude) {
        this.label = label;
        this.magnitude = magnitude;
    }

    /** @return short human-readable name */
    public String label() {
        return label;
    }

    /** @return informal description of the represented force magnitude */
    public String magnitude() {
        return magnitude;
    }

    /** @return {@code true} if this band is at least as strong as {@code other} */
    public boolean atLeast(ThrustLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
