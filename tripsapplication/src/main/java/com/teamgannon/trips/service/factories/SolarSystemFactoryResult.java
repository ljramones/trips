package com.teamgannon.trips.service.factories;

import com.teamgannon.trips.jpa.model.StarObject;

/**
 * Summary of a {@link SolarSystemFactory#generate} call. Carries the
 * factory's identity and the rows it added so callers and tests can
 * verify the right path ran.
 */
public record SolarSystemFactoryResult(
        String factoryName,
        StarObject sourceStar,
        int planetsCreated,
        int moonsCreated,
        int featuresCreated,
        boolean alreadyExisted) {

    /**
     * Factory was selected but the system already existed (idempotent
     * short-circuit). All counts zero.
     */
    public static SolarSystemFactoryResult alreadyExisted(String factoryName, StarObject star) {
        return new SolarSystemFactoryResult(factoryName, star, 0, 0, 0, true);
    }

    /** Sum of planets, moons, and features created in this call. */
    public int totalBodiesCreated() {
        return planetsCreated + moonsCreated + featuresCreated;
    }
}
