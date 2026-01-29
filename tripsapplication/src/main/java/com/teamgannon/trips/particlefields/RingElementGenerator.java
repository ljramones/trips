package com.teamgannon.trips.particlefields;

import java.util.List;
import java.util.Random;

/**
 * Interface for generating ring elements based on ring type-specific physics.
 * Each implementation encapsulates the generation logic for a specific ring type.
 */
public interface RingElementGenerator {

    /**
     * Generates all ring elements based on the configuration.
     *
     * @param config the ring configuration
     * @param random random number generator for reproducible results
     * @return list of generated ring elements
     */
    List<RingElement> generate(RingConfiguration config, Random random);

    /**
     * Returns the ring type this generator creates.
     */
    RingType getRingType();

    /**
     * Returns a description of this generator's behavior.
     */
    default String getDescription() {
        return getRingType().getDisplayName() + " generator";
    }
}
