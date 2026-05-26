package com.teamgannon.trips.spaceshipmodeller.integration;

import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.ThrustLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Orbital- and interstellar-transfer maths, from established orbital mechanics through hard-SF to
 * speculative concepts.
 * <p>
 * Pure and dependency-free (no UI, no Spring). Realistic types ({@link TransferType#HOHMANN},
 * {@link TransferType#BI_ELLIPTIC}, etc.) use vis-viva and the rocket equation; advanced types use
 * constant-thrust / Δv-budget models; exotic types use deliberately illustrative approximations (a fixed
 * fraction of light speed, instantaneous traversal, etc.). Every type is reduced to a list of equivalent
 * impulsive burns plus a transfer time, then {@link #assemble} does the propellant/mass accounting.
 */
public final class TransferCalculator {

    /** Gravitational constant (m^3 kg^-1 s^-2). */
    public static final double G = 6.674e-11;
    /** Solar mass (kg). */
    public static final double SOLAR_MASS_KG = 1.989e30;
    /** Astronomical unit (m). */
    public static final double AU_METERS = 1.495978707e11;
    /** Astronomical unit (km). */
    public static final double AU_KM = AU_METERS / 1000.0;
    /** Seconds in a day. */
    public static final double DAY_SECONDS = 86_400.0;
    /** Speed of light (km/s). */
    public static final double SPEED_OF_LIGHT_KMPS = 299_792.458;

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
     * @return total delta-V, in km/s
     */
    public static double hohmannDeltaVKmps(double r1Au, double r2Au, double centralMassSolar) {
        double r1 = r1Au * AU_METERS;
        double r2 = r2Au * AU_METERS;
        if (!valid(r1, r2, centralMassSolar)) {
            return 0;
        }
        double mu = mu(centralMassSolar);
        double a = (r1 + r2) / 2.0;
        double v1 = Math.sqrt(mu / r1);
        double vPeri = Math.sqrt(mu * (2.0 / r1 - 1.0 / a));
        double v2 = Math.sqrt(mu / r2);
        double vApo = Math.sqrt(mu * (2.0 / r2 - 1.0 / a));
        return (Math.abs(vPeri - v1) + Math.abs(v2 - vApo)) / 1000.0;
    }

    /**
     * Hohmann transfer (coast) time.
     *
     * @param r1Au             starting orbital radius (AU)
     * @param r2Au             target orbital radius (AU)
     * @param centralMassSolar central body mass (solar masses)
     * @return transfer time, in days
     */
    public static double hohmannTransferTimeDays(double r1Au, double r2Au, double centralMassSolar) {
        double r1 = r1Au * AU_METERS;
        double r2 = r2Au * AU_METERS;
        if (!valid(r1, r2, centralMassSolar)) {
            return 0;
        }
        return halfPeriodDays((r1 + r2) / 2.0, mu(centralMassSolar));
    }

    /**
     * Builds a {@link TransferEstimate} (Hohmann basis) between two named bodies.
     */
    public static TransferEstimate estimate(TransferBody origin, TransferBody destination,
                                            double centralMassSolar, SpaceshipDesign ship) {
        return estimate(origin.semiMajorAxisAu(), destination.semiMajorAxisAu(), centralMassSolar, ship);
    }

    /**
     * Builds a Hohmann-basis {@link TransferEstimate} for a ship between two orbits.
     */
    public static TransferEstimate estimate(double originAu, double destAu,
                                            double centralMassSolar, SpaceshipDesign ship) {
        double requiredDv = hohmannDeltaVKmps(originAu, destAu, centralMassSolar);
        double transferDays = hohmannTransferTimeDays(originAu, destAu, centralMassSolar);
        double shipDv = ship.estimateDeltaVKmps();
        boolean feasible = !Double.isNaN(shipDv) && shipDv >= requiredDv;

        double propellantRequired = Double.NaN;
        double veKmps = ship.driveSpecs().exhaustVelocityAverageKmps();
        if (Double.isFinite(veKmps) && veKmps > 0) {
            double dry = ship.massBudget().dryMassTons();
            propellantRequired = dry * (Math.exp(requiredDv / veKmps) - 1.0);
        }
        double propellantAvailable = ship.massBudget().propellantMassTons();

        double burnSecondsValue = Double.NaN;
        double thrustMN = ship.driveSpecs().typicalThrustAverageMN();
        if (thrustMN > 0) {
            double wetKg = ship.massBudget().wetMassTons() * 1000.0;
            burnSecondsValue = (requiredDv * 1000.0) * wetKg / (thrustMN * 1.0e6);
        }

        return new TransferEstimate(originAu, destAu, centralMassSolar, requiredDv, shipDv,
                transferDays, propellantRequired, propellantAvailable, burnSecondsValue, feasible);
    }

    /**
     * @param ship the ship
     * @return a sensible default transfer type: low-thrust drives default to a spiral, others to Hohmann
     */
    public static TransferType defaultTypeFor(SpaceshipDesign ship) {
        if (ship.driveSpecs().reactionless()) {
            return TransferType.LASER_SAIL_BEAM; // sails ride a beam; no impulsive burns
        }
        ThrustLevel level = ship.driveSpecs().thrustLevel();
        return level.ordinal() <= ThrustLevel.LOW.ordinal()
                ? TransferType.LOW_THRUST_APPROX : TransferType.HOHMANN;
    }

    /**
     * Builds a full {@link TransferPlan} for a ship between two bodies using the given transfer type.
     *
     * @param origin           origin body
     * @param destination      destination body
     * @param centralMassSolar central body mass (solar masses)
     * @param ship             the ship
     * @param type             the transfer type
     * @return the plan
     */
    public static TransferPlan plan(TransferBody origin, TransferBody destination,
                                    double centralMassSolar, SpaceshipDesign ship, TransferType type) {
        TransferType t = type == null ? TransferType.HOHMANN : type;
        BurnSpec spec = buildSpec(origin, destination, centralMassSolar, ship, t);
        return assemble(origin, destination, t, ship,
                spec.burnsKmps(), spec.names(), spec.timesDays(), spec.transferDays());
    }

    /** Equivalent impulsive burns + timing for a transfer, before propellant accounting. */
    private record BurnSpec(double[] burnsKmps, String[] names, double[] timesDays, double transferDays) {
    }

    private static BurnSpec buildSpec(TransferBody origin, TransferBody dest,
                                      double mass, SpaceshipDesign ship, TransferType type) {
        double r1 = origin.semiMajorAxisAu() * AU_METERS;
        double r2 = dest.semiMajorAxisAu() * AU_METERS;
        boolean ok = valid(r1, r2, mass);
        double mu = ok ? mu(mass) : 0;

        double hd1 = 0;
        double hd2 = 0;
        double ht = 0;
        if (ok) {
            double a = (r1 + r2) / 2.0;
            hd1 = Math.abs(Math.sqrt(mu * (2.0 / r1 - 1.0 / a)) - Math.sqrt(mu / r1)) / 1000.0;
            hd2 = Math.abs(Math.sqrt(mu / r2) - Math.sqrt(mu * (2.0 / r2 - 1.0 / a))) / 1000.0;
            ht = halfPeriodDays(a, mu);
        }
        double hd = hd1 + hd2;

        double dKm = Math.abs(r2 - r1) / 1000.0;
        if (dKm < 1.0) {
            dKm = Math.max(r1, r2) / 1000.0; // degenerate same-orbit fallback
        }
        double dM = dKm * 1000.0;

        double s = ship.estimateDeltaVKmps();
        double shipDv = Double.isNaN(s) ? 0 : s;
        double thrustN = ship.driveSpecs().typicalThrustAverageMN() * 1.0e6;
        double wetKg = ship.massBudget().wetMassTons() * 1000.0;
        double accel = (thrustN > 0 && wetKg > 0) ? thrustN / wetKg : 0;
        final double c = SPEED_OF_LIGHT_KMPS;

        return switch (type) {
            case HOHMANN -> new BurnSpec(new double[]{hd1, hd2},
                    new String[]{"Departure burn", "Arrival burn"}, new double[]{0, ht}, ht);
            case BI_ELLIPTIC -> biElliptic(r1, r2, mu, ok);
            case HIGH_ENERGY -> new BurnSpec(new double[]{hd1 * 1.4, hd2 * 1.4},
                    new String[]{"High-energy departure", "Arrival burn"}, new double[]{0, ht * 0.6}, ht * 0.6);
            case OBERTH -> new BurnSpec(new double[]{hd1 * 0.8, hd2 * 0.85},
                    new String[]{"Oberth periapsis burn", "Arrival burn"}, new double[]{0, ht}, ht);
            case AEROBRAKING -> new BurnSpec(new double[]{hd1, 0.0},
                    new String[]{"Departure burn", "Aerobraking capture"}, new double[]{0, ht}, ht);
            case GRAVITY_ASSIST -> new BurnSpec(new double[]{hd * 0.35, hd * 0.05},
                    new String[]{"Departure burn", "Flyby correction"}, new double[]{0, ht * 3}, ht * 3);
            case RESONANT_PHASING -> new BurnSpec(new double[]{hd1, hd2, hd * 0.1},
                    new String[]{"Departure burn", "Arrival burn", "Phasing trim"},
                    new double[]{0, ht, ht * 1.8}, ht * 1.8);
            case LOW_THRUST_APPROX -> lowThrust(r1, r2, mu, ok, ship);
            case HYBRID_CHEM_ELECTRIC -> hybrid(r1, r2, mu, ok, ship, ht);
            case LOW_ENERGY_WSB -> new BurnSpec(new double[]{hd1 * 0.7, hd2 * 0.6},
                    new String[]{"WSB departure", "WSB capture"}, new double[]{0, ht * 4}, ht * 4);
            case BRACHISTOCHRONE -> brachistochrone(dM, accel, "Accelerate (flip point)", "Decelerate");
            case FAST_TRANSIT -> coastTransfer(dKm, shipDv * 0.4, "Boost burn", "Braking burn");
            case MINIMUM_TIME -> coastTransfer(dKm, shipDv, "Maximum boost", "Maximum braking");
            case RELATIVISTIC -> relativistic(dKm, c, 0.3);
            case LASER_SAIL_BEAM -> cruise(dKm, 0.2 * c, "Beam-riding cruise");
            case BUSSARD_RAMJET_TRANSIT -> cruise(dKm, 0.12 * c, "Ramjet cruise");
            case ANTIMATTER_TORCH -> brachistochrone(dM, accel, "Antimatter boost", "Antimatter brake");
            case WORMHOLE -> instant(3600.0, "Wormhole traversal");
            case ALCUBIERRE_WARP -> cruise(dKm, 10.0 * c, "Warp bubble transit");
            case JUMP_DRIVE -> instant(DAY_SECONDS, "Hyperspace jump (charge + transit)");
            case QUANTUM_TELEPORT -> instant(1.0, "Quantum teleport");
        };
    }

    private static BurnSpec biElliptic(double r1, double r2, double mu, boolean ok) {
        double dv1 = 0;
        double dv2 = 0;
        double dv3 = 0;
        double t1 = 0;
        double total = 0;
        if (ok) {
            double rb = 2.0 * Math.max(r1, r2);
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
            t1 = halfPeriodDays(a1, mu);
            total = t1 + halfPeriodDays(a2, mu);
        }
        return new BurnSpec(new double[]{dv1, dv2, dv3},
                new String[]{"Departure burn", "Apoapsis raise", "Arrival burn"},
                new double[]{0, t1, total}, total);
    }

    private static BurnSpec lowThrust(double r1, double r2, double mu, boolean ok, SpaceshipDesign ship) {
        double dv = ok ? Math.abs(Math.sqrt(mu / r1) - Math.sqrt(mu / r2)) / 1000.0 : 0;
        double days = burnSeconds(dv, ship.massBudget().wetMassTons(),
                ship.driveSpecs().typicalThrustAverageMN()) / DAY_SECONDS;
        return new BurnSpec(new double[]{dv}, new String[]{"Continuous low-thrust spiral"},
                new double[]{0}, days);
    }

    private static BurnSpec hybrid(double r1, double r2, double mu, boolean ok,
                                   SpaceshipDesign ship, double ht) {
        double chem = 0;
        double spiral = 0;
        if (ok) {
            double a = (r1 + r2) / 2.0;
            chem = Math.abs(Math.sqrt(mu * (2.0 / r1 - 1.0 / a)) - Math.sqrt(mu / r1)) / 1000.0;
            spiral = Math.abs(Math.sqrt(mu / r1) - Math.sqrt(mu / r2)) / 1000.0 * 0.6;
        }
        double spiralDays = burnSeconds(spiral, ship.massBudget().wetMassTons(),
                ship.driveSpecs().typicalThrustAverageMN()) / DAY_SECONDS;
        double total = ht * 0.5 + spiralDays;
        return new BurnSpec(new double[]{chem, spiral},
                new String[]{"Chemical departure", "Electric spiral cruise"},
                new double[]{0, ht * 0.5}, total);
    }

    private static BurnSpec brachistochrone(double dM, double accel, String n1, String n2) {
        double dv;
        double days;
        if (accel > 0) {
            dv = 2.0 * Math.sqrt(accel * dM) / 1000.0;
            days = (2.0 * Math.sqrt(dM / accel)) / DAY_SECONDS;
        } else {
            dv = Double.NaN;
            days = Double.NaN;
        }
        return new BurnSpec(new double[]{dv / 2.0, dv / 2.0}, new String[]{n1, n2},
                new double[]{0, days}, days);
    }

    private static BurnSpec coastTransfer(double dKm, double dvUsedKmps, String n1, String n2) {
        double vCruise = dvUsedKmps / 2.0;
        double days = vCruise > 0 ? (dKm / vCruise) / DAY_SECONDS : Double.NaN;
        return new BurnSpec(new double[]{dvUsedKmps / 2.0, dvUsedKmps / 2.0},
                new String[]{n1, n2}, new double[]{0, days}, days);
    }

    private static BurnSpec relativistic(double dKm, double c, double fraction) {
        double vCruise = fraction * c;
        double days = (dKm / vCruise) / DAY_SECONDS;
        return new BurnSpec(new double[]{vCruise, vCruise},
                new String[]{"Relativistic boost", "Relativistic decel"}, new double[]{0, days}, days);
    }

    private static BurnSpec cruise(double dKm, double vKmps, String name) {
        double days = vKmps > 0 ? (dKm / vKmps) / DAY_SECONDS : Double.NaN;
        return new BurnSpec(new double[]{0.0}, new String[]{name}, new double[]{0}, days);
    }

    private static BurnSpec instant(double seconds, String name) {
        return new BurnSpec(new double[]{0.0}, new String[]{name}, new double[]{0}, seconds / DAY_SECONDS);
    }

    /**
     * Assembles a plan from a sequence of burns, accounting propellant and mass backwards from the dry mass
     * arrived with. Transfers needing no delta-V (sails, wormholes) are feasible regardless of budget.
     */
    private static TransferPlan assemble(TransferBody origin, TransferBody destination, TransferType type,
                                         SpaceshipDesign ship, double[] burnsKmps, String[] names,
                                         double[] timesDays, double transferDays) {
        double totalReqDv = Arrays.stream(burnsKmps).sum();
        double shipDv = ship.estimateDeltaVKmps();

        double veKmps = ship.driveSpecs().exhaustVelocityAverageKmps();
        double thrustMN = ship.driveSpecs().typicalThrustAverageMN();
        double dry = ship.massBudget().dryMassTons();
        int n = burnsKmps.length;

        double[] massAfter = new double[n];
        double[] prop = new double[n];
        boolean derivable = Double.isFinite(veKmps) && veKmps > 0;
        if (derivable) {
            double m = dry;
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
        double availableProp = ship.massBudget().propellantMassTons();
        boolean feasible = TransferFeasibility.evaluate(totalReqDv, shipDv, totalProp, availableProp)
                != Feasibility.INSUFFICIENT;
        boolean propellantSufficient =
                TransferFeasibility.propellantStatus(totalProp, availableProp) != Feasibility.INSUFFICIENT;

        return new TransferPlan(ship.name(), type, origin, destination, nodes,
                totalReqDv, totalProp, availableProp, transferDays, shipDv, feasible, propellantSufficient);
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
