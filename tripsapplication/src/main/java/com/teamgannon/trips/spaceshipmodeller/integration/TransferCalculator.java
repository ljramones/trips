package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.ThrustLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /**
     * Builds a full {@link TransferPlan} with separate departure and arrival burns, sequential
     * propellant accounting (rocket equation, arriving at dry mass), and per-burn timing.
     *
     * @param origin           origin body
     * @param destination      destination body
     * @param centralMassSolar central body mass (solar masses)
     * @param ship             the ship
     * @param type             the transfer type (only {@link TransferType#HOHMANN} is computed)
     * @return the plan
     */
    public static TransferPlan plan(TransferBody origin, TransferBody destination,
                                    double centralMassSolar, SpaceshipDesign ship, TransferType type) {
        // TEMP diagnostic: confirms the actual ship/drive/Isp/mass reaching the calculator.
        System.out.println("Calculating transfer for ship: " + ship.name()
                + " | Drive: " + ship.driveType()
                + " | IspAvg(s): " + ship.driveSpecs().ispAverageSeconds()
                + " | Total Mass (t): " + ship.grossMassTons());
        TransferType t = type == null ? TransferType.HOHMANN : type;
        return switch (t) {
            case HOHMANN -> planHohmann(origin, destination, centralMassSolar, ship);
            case BI_ELLIPTIC -> planBiElliptic(origin, destination, centralMassSolar, ship);
            case LOW_THRUST_APPROX -> planLowThrust(origin, destination, centralMassSolar, ship);
        };
    }

    /**
     * @param ship the ship
     * @return a sensible default transfer type: low-thrust drives default to a spiral, others to Hohmann
     */
    public static TransferType defaultTypeFor(SpaceshipDesign ship) {
        ThrustLevel level = ship.driveSpecs().thrustLevel();
        return level.ordinal() <= ThrustLevel.LOW.ordinal()
                ? TransferType.LOW_THRUST_APPROX : TransferType.HOHMANN;
    }

    private static TransferPlan planHohmann(TransferBody origin, TransferBody destination,
                                            double centralMassSolar, SpaceshipDesign ship) {
        double r1 = origin.semiMajorAxisAu() * AU_METERS;
        double r2 = destination.semiMajorAxisAu() * AU_METERS;
        double dv1 = 0;
        double dv2 = 0;
        double transferDays = 0;
        if (valid(r1, r2, centralMassSolar)) {
            double mu = mu(centralMassSolar);
            double a = (r1 + r2) / 2.0;
            dv1 = Math.abs(Math.sqrt(mu * (2.0 / r1 - 1.0 / a)) - Math.sqrt(mu / r1)) / 1000.0;
            dv2 = Math.abs(Math.sqrt(mu / r2) - Math.sqrt(mu * (2.0 / r2 - 1.0 / a))) / 1000.0;
            transferDays = halfPeriodDays(a, mu);
        }
        return assemble(origin, destination, TransferType.HOHMANN, ship,
                new double[]{dv1, dv2},
                new String[]{"Departure burn", "Arrival burn"},
                new double[]{0, transferDays}, transferDays);
    }

    private static TransferPlan planBiElliptic(TransferBody origin, TransferBody destination,
                                               double centralMassSolar, SpaceshipDesign ship) {
        double r1 = origin.semiMajorAxisAu() * AU_METERS;
        double r2 = destination.semiMajorAxisAu() * AU_METERS;
        double dv1 = 0;
        double dv2 = 0;
        double dv3 = 0;
        double t1Days = 0;
        double transferDays = 0;
        if (valid(r1, r2, centralMassSolar)) {
            double mu = mu(centralMassSolar);
            double rb = 2.0 * Math.max(r1, r2); // intermediate apoapsis
            double a1 = (r1 + rb) / 2.0;
            double a2 = (r2 + rb) / 2.0;
            double vc1 = Math.sqrt(mu / r1);
            double vp1 = Math.sqrt(mu * (2.0 / r1 - 1.0 / a1));
            double va1 = Math.sqrt(mu * (2.0 / rb - 1.0 / a1));
            double va2 = Math.sqrt(mu * (2.0 / rb - 1.0 / a2));
            double vp2 = Math.sqrt(mu * (2.0 / r2 - 1.0 / a2));
            double vc2 = Math.sqrt(mu / r2);
            dv1 = Math.abs(vp1 - vc1) / 1000.0;
            dv2 = Math.abs(va2 - va1) / 1000.0;
            dv3 = Math.abs(vp2 - vc2) / 1000.0;
            t1Days = halfPeriodDays(a1, mu);
            transferDays = t1Days + halfPeriodDays(a2, mu);
        }
        return assemble(origin, destination, TransferType.BI_ELLIPTIC, ship,
                new double[]{dv1, dv2, dv3},
                new String[]{"Departure burn", "Apoapsis raise", "Arrival burn"},
                new double[]{0, t1Days, transferDays}, transferDays);
    }

    private static TransferPlan planLowThrust(TransferBody origin, TransferBody destination,
                                              double centralMassSolar, SpaceshipDesign ship) {
        double r1 = origin.semiMajorAxisAu() * AU_METERS;
        double r2 = destination.semiMajorAxisAu() * AU_METERS;
        double dv = 0;
        if (valid(r1, r2, centralMassSolar)) {
            double mu = mu(centralMassSolar);
            // continuous-thrust circle-to-circle spiral approximation: dv = |v_c1 - v_c2|
            dv = Math.abs(Math.sqrt(mu / r1) - Math.sqrt(mu / r2)) / 1000.0;
        }
        // continuous burn drives the (long) transfer time; estimate from wet mass and thrust
        double thrustMN = ship.driveSpecs().typicalThrustAverageMN();
        double transferDays = burnSeconds(dv, ship.massBudget().wetMassTons(), thrustMN) / DAY_SECONDS;
        return assemble(origin, destination, TransferType.LOW_THRUST_APPROX, ship,
                new double[]{dv},
                new String[]{"Continuous low-thrust spiral"},
                new double[]{0}, transferDays);
    }

    /**
     * Assembles a plan from a sequence of burns, accounting propellant and mass backwards from the dry
     * mass arrived with, so each node carries the propellant it uses and the mass remaining afterwards.
     */
    private static TransferPlan assemble(TransferBody origin, TransferBody destination, TransferType type,
                                         SpaceshipDesign ship, double[] burnsKmps, String[] names,
                                         double[] timesDays, double transferDays) {
        double totalReqDv = Arrays.stream(burnsKmps).sum();
        double shipDv = ship.estimateDeltaVKmps();
        boolean feasible = !Double.isNaN(shipDv) && shipDv >= totalReqDv;

        double veKmps = ship.driveSpecs().exhaustVelocityAverageKmps();
        double thrustMN = ship.driveSpecs().typicalThrustAverageMN();
        double dry = ship.massBudget().dryMassTons();
        int n = burnsKmps.length;

        double[] massAfter = new double[n];
        double[] prop = new double[n];
        boolean derivable = Double.isFinite(veKmps) && veKmps > 0;
        if (derivable) {
            double m = dry; // mass after the final burn is the dry mass
            for (int i = n - 1; i >= 0; i--) {
                massAfter[i] = m;
                double before = m * Math.exp(burnsKmps[i] / veKmps);
                prop[i] = before - m;
                m = before;
            }
        } else {
            Arrays.fill(massAfter, Double.NaN);
            Arrays.fill(prop, Double.NaN);
        }
        double totalProp = derivable ? Arrays.stream(prop).sum() : Double.NaN;

        List<ManeuverNode> nodes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double massBefore = derivable ? massAfter[i] + prop[i] : dry;
            nodes.add(new ManeuverNode(names[i], burnsKmps[i], timesDays[i], prop[i],
                    burnSeconds(burnsKmps[i], massBefore, thrustMN), massAfter[i]));
        }
        boolean propellantSufficient = !Double.isNaN(totalProp)
                && ship.massBudget().propellantMassTons() >= totalProp;

        return new TransferPlan(ship.name(), type, origin, destination, nodes,
                totalReqDv, totalProp, transferDays, shipDv, feasible, propellantSufficient);
    }

    private static boolean valid(double r1, double r2, double centralMassSolar) {
        return r1 > 0 && r2 > 0 && centralMassSolar > 0;
    }

    private static double halfPeriodDays(double semiMajorAxisMeters, double mu) {
        return Math.PI * Math.sqrt(semiMajorAxisMeters * semiMajorAxisMeters * semiMajorAxisMeters / mu)
                / DAY_SECONDS;
    }

    private static double burnSeconds(double dvKmps, double massTons, double thrustMN) {
        if (thrustMN <= 0) {
            return Double.NaN;
        }
        return (dvKmps * 1000.0) * (massTons * 1000.0) / (thrustMN * 1.0e6);
    }
}
