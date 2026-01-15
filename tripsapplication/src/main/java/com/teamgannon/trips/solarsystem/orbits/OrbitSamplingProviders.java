package com.teamgannon.trips.solarsystem.orbits;

/**
 * Orekit seam: factory for swapping sampling providers without touching renderers.
 */
public final class OrbitSamplingProviders {

    private OrbitSamplingProviders() {
    }

    public static OrbitSamplingProvider defaultKepler() {
        return new KeplerOrbitSamplingProvider();
    }
}
