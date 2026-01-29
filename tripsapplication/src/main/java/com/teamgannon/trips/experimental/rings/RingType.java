package com.teamgannon.trips.experimental.rings;

/**
 * Categorizes different types of ring/particle field systems.
 */
public enum RingType {

    /**
     * Planetary ring (Saturn-like): extremely thin, dense, small icy/rocky particles,
     * nearly circular orbits with very low inclination, fast Keplerian rotation.
     */
    PLANETARY_RING("Planetary Ring"),

    /**
     * Asteroid belt (Main belt-like): thick vertical distribution, sparse,
     * large rocky bodies, eccentric and inclined orbits.
     */
    ASTEROID_BELT("Asteroid Belt"),

    /**
     * Debris disk (protoplanetary or collision remnants): moderate thickness,
     * mix of dust and planetesimals, some structure/gaps possible.
     */
    DEBRIS_DISK("Debris Disk"),

    /**
     * Dust cloud / nebula: three-dimensional distribution (not a flat ring),
     * very diffuse, slow turbulent motion rather than orbital.
     */
    DUST_CLOUD("Dust Cloud"),

    /**
     * Accretion disk (around compact objects): thin but very hot/fast,
     * density increases toward center, visible temperature gradient.
     */
    ACCRETION_DISK("Accretion Disk");

    private final String displayName;

    RingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
