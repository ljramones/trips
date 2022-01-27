package com.teamgannon.trips.propulsion;

/**
 * Similar to Solar Moth, but uses a stationary ground or space-station based laser instead of the sun. Basically
 * the propulsion system leaves the power plant at home and relies upon a laser beam instead of an incredibly
 * long extension cord.
 * <p>
 * As a general rule, the collector mirror of a laser thermal rocket can be much smaller than a comparable solar
 * moth, since the laser beam probably has a higher energy density than natural sunlight.
 * <p>
 * With the mass of the power plant not actually on the spacecraft, more mass is available for payload. Or the
 * reduced mass makes for a higher mass ratio to increase the spacecraft's delta V. The reduced mass also increases
 * the acceleration. In some science fiction novels, combat "motherships" will have batteries of lasers, used to
 * power hordes of ultra-high acceleration missiles and/or fighter spacecraft.
 * <p>
 * The drawback include the fact that there is a maximum effective range you can send a worthwhile laser beam from
 * station to spacecraft, and the fact that the spacecraft is at the mercy of whoever is controlling the laser
 * station.
 * <p>
 * Propellant is hydrogen seeded with alkali metal. As always the reason for seeding is that hydrogen is more or
 * less transparent so the laser beam will mostly pass right through without heating the hydrogen. The seeding make
 * the hydrogen more opaque so the blasted stuff will heat up. Having said that, the Mirror Steamer has an alternate
 * solution.
 */
public class LaserThermalPropulsion {

    public double calculateDeltaV(double bp, double be, double R) {
        return 0;
    }

}
