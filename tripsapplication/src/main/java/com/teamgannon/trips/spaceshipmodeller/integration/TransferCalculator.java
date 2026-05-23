package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;

/**
 * First-order orbital-transfer maths for circular, coplanar orbits.
 * <p>
 * Pure and dependency-free (no UI, no Spring) so it is easy to unit-test. Uses SI internally; inputs are in
 * AU and solar masses. The model is a two-impulse Hohmann transfer (via vis-viva), the Tsiolkovsky rocket
 * equation for propellant, and a constant-thrust approximation for burn time.
 */
public final class TransferCalculator {

    /** Gravitational constant (m^3 kg^-1 s^-2). */
    public static final double G = 6.674e-11;
    /** Solar mass (kg). */
    public static final double SOLAR_MASS_KG = 1.989e30;
    /** Astronomical unit (m). */
    public static final double AU_METERS = 1.495978707e11;
    /** Seconds in a day. */
    public static final double DAY_SECONDS = 86_400.0;

    private TransferCalculator() {
    }

    /**
     * @param centralMassSolar central body mass in solar masses
     * @return standard gravitational parameter μ = G·M, in m^3/s^2
     */
    public static double mu(double centralMassSolar) {
        return G * SOLAR_MASS_KG * centralMassSolar;
    }

    /**
     * Total Hohmann transfer delta-V between two circular orbits.
     *
     * @param r1Au             starting orbital radius (AU)
     * @param r2Au             target orbital radius (AU)
     * @param centralMassSolar central body mass (solar masses)
     * @return total delta-V (sum of both burns), in km/s
     */
    public static double hohmannDeltaVKmps(double r1Au, double r2Au, double centralMassSolar) {
        double r1 = r1Au * AU_METERS;
        double r2 = r2Au * AU_METERS;
        if (r1 <= 0 || r2 <= 0 || centralMassSolar <= 0) {
            return 0;
        }
        double mu = mu(centralMassSolar);
        double a = (r1 + r2) / 2.0;
        double v1 = Math.sqrt(mu / r1);
        double vPeri = Math.sqrt(mu * (2.0 / r1 - 1.0 / a));
        double v2 = Math.sqrt(mu / r2);
        double vApo = Math.sqrt(mu * (2.0 / r2 - 1.0 / a));
        double dv = Math.abs(vPeri - v1) + Math.abs(v2 - vApo);
        return dv / 1000.0;
    }

    /**
     * Hohmann transfer (coast) time: half the period of the transfer ellipse.
     *
     * @param r1Au             starting orbital radius (AU)
     * @param r2Au             target orbital radius (AU)
     * @param centralMassSolar central body mass (solar masses)
     * @return transfer time, in days
     */
    public static double hohmannTransferTimeDays(double r1Au, double r2Au, double centralMassSolar) {
        double r1 = r1Au * AU_METERS;
        double r2 = r2Au * AU_METERS;
        if (r1 <= 0 || r2 <= 0 || centralMassSolar <= 0) {
            return 0;
        }
        double mu = mu(centralMassSolar);
        double a = (r1 + r2) / 2.0;
        double seconds = Math.PI * Math.sqrt(a * a * a / mu);
        return seconds / DAY_SECONDS;
    }

    /**
     * Builds a {@link TransferEstimate} between two named bodies.
     *
     * @param origin           origin body
     * @param destination      destination body
     * @param centralMassSolar central body mass (solar masses)
     * @param ship             the ship attempting the transfer
     * @return the estimate
     */
    public static TransferEstimate estimate(TransferBody origin, TransferBody destination,
                                            double centralMassSolar, SpaceshipDesign ship) {
        return estimate(origin.semiMajorAxisAu(), destination.semiMajorAxisAu(), centralMassSolar, ship);
    }

    /**
     * Builds a full {@link TransferEstimate} for a ship between two orbits.
     *
     * @param originAu         origin orbital radius (AU)
     * @param destAu           destination orbital radius (AU)
     * @param centralMassSolar central body mass (solar masses)
     * @param ship             the ship attempting the transfer
     * @return the estimate
     */
    public static TransferEstimate estimate(double originAu, double destAu,
                                            double centralMassSolar, SpaceshipDesign ship) {
        double requiredDv = hohmannDeltaVKmps(originAu, destAu, centralMassSolar);
        double transferDays = hohmannTransferTimeDays(originAu, destAu, centralMassSolar);
        double shipDv = ship.estimateDeltaVKmps(); // NaN for reactionless drives
        boolean feasible = !Double.isNaN(shipDv) && shipDv >= requiredDv;

        // Propellant for this manoeuvre via the rocket equation, taking dry mass as the final mass.
        double propellantRequired = Double.NaN;
        double veKmps = ship.driveSpecs().exhaustVelocityAverageKmps(); // infinite for sails
        if (Double.isFinite(veKmps) && veKmps > 0) {
            double dry = ship.massBudget().dryMassTons();
            propellantRequired = dry * (Math.exp(requiredDv / veKmps) - 1.0);
        }
        double propellantAvailable = ship.massBudget().propellantMassTons();

        // Rough constant-thrust burn time: t ≈ Δv · m / F (using wet mass and average thrust).
        double burnSeconds = Double.NaN;
        double thrustMN = ship.driveSpecs().typicalThrustAverageMN();
        if (thrustMN > 0) {
            double thrustN = thrustMN * 1.0e6;
            double wetKg = ship.massBudget().wetMassTons() * 1000.0;
            burnSeconds = (requiredDv * 1000.0) * wetKg / thrustN;
        }

        return new TransferEstimate(originAu, destAu, centralMassSolar, requiredDv, shipDv,
                transferDays, propellantRequired, propellantAvailable, burnSeconds, feasible);
    }
}
