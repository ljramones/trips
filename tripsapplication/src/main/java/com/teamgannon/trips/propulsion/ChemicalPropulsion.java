package com.teamgannon.trips.propulsion;

import com.teamgannon.trips.planetarymodelling.chemical.MolecularWeightCalculator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.teamgannon.trips.propulsion.Constants.*;
import static java.lang.Math.*;

@Component
public class ChemicalPropulsion {

    /**
     * used to calculate moleculor weight of compounds
     */
    private final MolecularWeightCalculator molecularWeightCalculator;

    public ChemicalPropulsion(MolecularWeightCalculator molecularWeightCalculator) {
        this.molecularWeightCalculator = molecularWeightCalculator;
    }

    /**
     * calculate the thrust of a spacecraft
     * <p>
     * A spacecraft's engine ejects mass at a rate of 30 kg/s with an exhaust velocity
     * of 3,100 m/s.  The pressure at the nozzle exit is 5 kPa and the exit area is
     * 0.7 m2.  What is the thrust of the engine in a vacuum?
     * <p>
     * Given:  q = 30 kg/s
     * Ve = 3,100 m/s
     * Ae = 0.7 m2
     * Pe = 5 kPa = 5,000 N/m2
     * Pa = 0
     *
     * @param q  the rate of the ejected mass flow
     * @param ve exhaust gas ejection speed
     * @param ae area of the nozzle exit
     * @param pe the pressure of the exhaust gases at the nozzle exit
     * @param pa the pressure of the ambient atmosphere (external to craft)
     * @return the thrust
     */
    public static double calcThrust(double q, double ve, double ae, double pe, double pa) {
        return q * ve + (pe - pa) * ae;
    }

    /**
     * calculate the thrust of a spacecraft
     *
     * @param q the rate of the ejected mass flow
     * @param C the effective gas velocity
     * @return the thrust of a spacecraft
     */
    public static double calcThrust(double q, double C) {
        return q * C;
    }

    /**
     * calculate the effective gas velocity C
     *
     * @param ve exhaust gas ejection speed
     * @param pe the pressure of the exhaust gases at the nozzle exit
     * @param pa the pressure of the ambient atmosphere (external to craft)
     * @param ae area of the nozzle exit
     * @param q  the rate of the ejected mass flow
     * @return the effective gas velocity C
     */
    public static double effectiveGasVelocity(double ve, double pe, double pa, double ae, double q) {
        return ve + (pe - pa) * ae / q;
    }

    /**
     * calculate the delta V
     * <p>
     * also known as Tsiolkovsky's rocket equation, named after Russian rocket pioneer
     * Konstantin E. Tsiolkovsky (1857-1935) who first derived it.
     * <p>
     * The spacecraft in problem 1.1 has an initial mass of 30,000 kg.  What is the
     * change in velocity if the spacecraft burns its engine for one minute?
     * <p>
     * Given:  M = 30,000 kg
     * q = 30 kg/s
     * Ve = 3,100 m/s
     * t = 60 s
     * <p>
     * LN is napien log or natural log (log to base e)
     * V = Ve × LN[ M / (M - qt) ]
     * V = 3,100 × LN[ 30,000 / (30,000 - (30 × 60)) ]
     * V = 192 m/s
     *
     * @param mass0 the mass of the space craft
     * @param q     rate of mass ejection
     * @param ve    the exhaust velocity
     * @param t     the time of exhaust firing
     * @return the delta V
     */
    public static double calcDeltaV(double mass0, double q, double ve, double t) {
        return ve * log(mass0 / (mass0 - q * t));
    }

    /**
     * calculate the delta v based on exhaust velocity and mass ratio
     *
     * @param massRatio the mass ratio (initial mass and final mass after burning)
     * @param ve        the exhaust velocity
     * @return the delta V
     */
    public static double calcDeltaV(double massRatio, double ve) {
        return ve * log(massRatio);
    }

    /**
     * calculate the mass ratio
     *
     * @param mass0 the initial mass
     * @param q     rate of mass ejection
     * @param t     span of time
     * @return the mass ratio
     */
    public static double calculateMassRatio(double mass0, double q, double t) {
        return mass0 / (mass0 - q * t);
    }

    /**
     * calculate the propellant mass Mf needed to effect request
     * <p>
     * A spacecraft's dry mass is 75,000 kg and the effective exhaust gas velocity
     * of its main engine is 3,100 m/s.  How much propellant must be carried if the
     * propulsion system is to produce a total v of 700 m/s?
     * <p>
     * Given:  Mf = 75,000 kg
     * C = 3,100 m/s
     * V = 700 m/s
     * <p>
     * Equation (1.20),
     * <p>
     * Mo = Mf × e(DV / C)
     * Mo = 75,000 × e(700 / 3,100)
     * Mo = 94,000 kg
     * <p>
     * Propellant mass,
     * <p>
     * Mp = Mo - Mf
     * Mp = 94,000 - 75,000
     * Mp = 19,000 kg
     *
     * @param mass   the spacecraft dry mass
     * @param C      effective exhaust gas velocity
     * @param deltav the delta V
     * @return the mass of the prellant neede
     */
    public static double calcPropellantMass(double mass, double C, double deltav) {
        return mass * (pow(e, deltav / C) - 1);
    }

    /**
     * calculate the burn duration
     * <p>
     * A 5,000 kg spacecraft is in Earth orbit traveling at a velocity of 7,790 m/s.
     * Its engine is burned to accelerate it to a velocity of 12,000 m/s placing it
     * on an escape trajectory.  The engine expels mass at a rate of 10 kg/s and an
     * effective velocity of 3,000 m/s.  Calculate the duration of the burn.
     * <p>
     * Given:  M = 5,000 kg
     * q = 10 kg/s
     * C = 3,000 m/s
     * V = 12,000 - 7,790 = 4,210 m/s
     * <p>
     * Equation (1.21),
     * <p>
     * t = M / q × [ 1 - 1 / e(DV / C) ]
     * t = 5,000 / 10 × [ 1 - 1 / e(4,210 / 3,000) ]
     * t = 377 s
     *
     * @param initialMass the spacecraft mass
     * @param q           rate of mass ejection
     * @param C           effective exhaust gas velocity
     * @param deltaV      the delta V
     * @return the burn duration
     */
    public double calculateBurnDuration(double initialMass, double q, double C, double deltaV) {
        return initialMass / q * (1 - 1 / pow(e, deltaV / C));
    }

    /**
     * return the oxidizer to fuel mixture ratio
     *
     * @param fuel      the molecular formula of the fuel
     * @param fuelMoles the amount of fuel in moles
     * @param oxidizer  the molecular formula of the oxidizer
     * @param oxMoles   the amount of oxidizer in
     * @return the mixture ratio
     */
    public double calculateMixture(String fuel, double fuelMoles, String oxidizer, double oxMoles) {
        double fuelWeight = molecularWeightCalculator.getMolecularWeight(fuel).getWeight() * fuelMoles;
        double oxidizerWeight = molecularWeightCalculator.getMolecularWeight(oxidizer).getWeight() * oxMoles;
        return oxidizerWeight / fuelWeight;
    }

    /**
     * calculate the exhaust gas velocity relative
     * to the rocket
     * <p>
     * we see that high chamber temperature and pressure, and low exhaust gas molecular weight results in
     * high ejection velocity, thus high thrust. Based on this criterion, we can see why liquid hydrogen is
     * very desirable as a rocket fuel.
     * <p>
     * A rocket engine burning liquid oxygen and kerosene operates at a mixture ratio
     * of 2.26 and a combustion chamber pressure of 50 atmospheres.  If the nozzle is
     * expanded to operate at sea level, calculate the exhaust gas velocity relative
     * to the rocket.
     * <p>
     * Given:  O/F = 2.26
     * Pc = 50 atm
     * Pe = Pa = 1 atm
     * <p>
     * From LOX/Kerosene Charts we estimate,
     * <p>
     * Tc = 3,470 K
     * M = 21.40
     * k = 1.221
     * <p>
     * Equation (1.22),
     * <p>
     * Ve = SQRT[ (2 × k / (k - 1)) × (R × Tc / M) × (1 - (Pe / Pc)(k-1)/k) ]
     * Ve = SQRT[ (2 × 1.221 / (1.221 - 1)) × (8,314.46 × 3,470 / 21.40) × (1 - (1 / 50)(1.221-1)/1.221) ]
     * Ve = 2,749 m/s
     *
     * @param mixture mixture ratio
     * @param pc      combustion chamber pressure
     * @param pe      pressure at nozzle exit (usually the ambient pressure external tot he craft
     * @param tc      combustion chamber temperature
     * @param M       is the average molecular weight of the exhaust gases
     * @param k       the specific heat ratio for this gas
     * @return the exhaust gas velocity relative
     */
    public double calculateRelativeExhaustGasVelocity(double mixture, double pc, double pe, double tc, double M, double k) {
        return sqrt((2 * k / (k - 1) * (R * tc / M) * (1 - pow(pe / pc, (k - 1) / k))));
    }

    /**
     * Calculate the specific impulse.
     * <p>
     * A rocket engine produces a thrust of 1,000 kN at sea level with a propellant
     * flow rate of 400 kg/s.  Calculate the specific impulse.
     * <p>
     * Given:  F = 1,000,000 N
     * q = 400 kg/s
     * <p>
     * Equation (1.23),
     * <p>
     * Isp = F / (q × g)
     * Isp = 1,000,000 / (400 × 9.80665)
     * Isp = 255 s (sea level)
     *
     * @param f thrust force in newtons
     * @param q propellant flow rate
     * @return the specific impulse (measured in seconds)
     */
    public double calculateSpecificImpulse(double f, double q) {
        return f / (q * g);
    }

    /**
     * calculate the specific impulse based on effective gas velocity
     *
     * @param C effective gas velocity
     * @return the specific impulse
     */
    public double calculateSpecificImpulse(double C) {
        return C * g;
    }

    /**
     * find the characteristic exhaust velocity, C* (pronounced "C star"), which is a measure of the energy
     * available from the combustion process
     *
     * @param pc combustion chamber pressure
     * @param at the area of the nozzle throat
     * @param q  rate of propellant flow
     * @return the characteristic exhaust velocity
     */
    public double findCharacteristicGasVelocity(double pc, double at, double q) {
        return pc * at / q;
    }

    /**
     * find the nozzle throat area, At
     *
     * @param q  the propellant flow rate
     * @param pt the pressure at the throat
     * @param tt the temperature at the throat
     * @param M  the average molecular weight
     * @param k  the specific heat ratio of the gase
     * @return nozzle throat area
     */
    public double calculateNozzleThroatArea(double q, double pt, double tt, double M, double k) {
        return q / pt * sqrt(R * tt / (M * k));
    }

    /**
     * find pressure at throat nozzle
     *
     * @param pc the pressure of the combustion chamber
     * @param k  the specific heat ratio
     * @return pressure at throat nozzle
     */
    public double findPressureAtNozzleThroat(double pc, double k) {
        return pc * pow(1 + (k - 1) / 2, -k / (k - 1));
    }

    /**
     * find the temperature at the nozzle throat
     *
     * @param tc the combustion chamber flame temperature
     * @param k  the specific heat ration of the gas mixture
     * @return the temperature at the nozzle throat
     */
    public double findTempAtNozzleThroat(double tc, double k) {
        return tc / (1 - (k - 1) / 2);
    }

    /**
     * calculate the area of the exhaust nozzle throat.
     * <p>
     * A rocket engine uses the same propellant, mixture ratio, and combustion chamber
     * pressure as that in problem 1.5.  If the propellant flow rate is 500 kg/s,
     * calculate the area of the exhaust nozzle throat.
     * <p>
     * Given:  Pc = 50 × 0.101325 = 5.066 MPa
     * Tc = 3,470 K
     * M = 21.40
     * k = 1.221
     * q = 500 kg/s
     * <p>
     * Equation (1.27),
     * <p>
     * Pt = Pc × [1 + (k - 1) / 2]-k/(k-1)
     * Pt = 5.066 × [1 + (1.221 - 1) / 2]-1.221/(1.221-1)
     * Pt = 2.839 MPa = 2.839×106 N/m2
     * <p>
     * Equation (1.28),
     * <p>
     * Tt = Tc / (1 + (k - 1) / 2)
     * Tt = 3,470 / (1 + (1.221 - 1) / 2)
     * Tt = 3,125 K
     * <p>
     * Equation (1.26),
     * <p>
     * At = (q / Pt) × SQRT[ (R* × Tt) / (M × k) ]
     * At = (500 / 2.839×106) × SQRT[ (8,314.46 × 3,125) / (21.40 × 1.221) ]
     * At = 0.1756 m2
     *
     * @param pc combustion chamber pressure
     * @param tc combustion chamber temperature
     * @param m  propellant mass
     * @param k  specific heat ratio for this gas
     * @param q  propellant flow rate
     * @return area of the exhaust nozzle throat
     */
    public double calculateExhaustThroatArea(double pc, double tc, double m, double k, double q, double rstar) {
        double pt = pc * pow(1 + (k - 1) / 2, -k / (k - 1));
        double tt = tc / (1 + (k - 1) / 2);
        // area of nozzle throat
        return (q / pt) * sqrt((rstar * tt) / (m * k));
    }

    /**
     * Calculate the area of the nozzle exit
     * <p>
     * The rocket engine in problem 1.7 is optimized to operate at an elevation of 2000
     * meters.  Calculate the area of the nozzle exit
     * <p>
     * Given:  Pc = 5.066 MPa
     * At = 0.1756 m2
     * k = 1.221
     * <p>
     * From Atmosphere Properties,
     * <p>
     * Pa = 0.0795 MPa
     * <p>
     * Equation (1.29),
     * <p>
     * Nm2 = (2 / (k - 1)) × [(Pc / Pa)(k-1)/k - 1]
     * Nm2 = (2 / (1.221 - 1)) × [(5.066 / 0.0795)(1.221-1)/1.221 - 1]
     * Nm2 = 10.15
     * Nm = (10.15)1/2 = 3.185
     * <p>
     * Equation (1.30),
     * <p>
     * Ae = (At / Nm) × [(1 + (k - 1) / 2 × Nm2)/((k + 1) / 2)](k+1)/(2(k-1))
     * Ae = (0.1756 / 3.185) × [(1 + (1.221 - 1) / 2 × 10.15)/((1.221 + 1) / 2)](1.221+1)/(2(1.221-1))
     * Ae = 1.426 m2
     *
     * @param pc combustion chamber pressure
     * @param at area of nozzle throat
     * @param k  specific heat ratio for this gas
     * @param pa ambient pressure (external to craft)
     * @return Calculate the area of the nozzle exit
     */
    public double calcAreaOfNozzleExit(double pc, double at, double k, double pa) {
        double nm = sqrt((2 / (k - 1)) * (pow(pc / pa, (k - 1) / k) - 1));
        return (at / nm) * pow((1 + (k - 1) / 2 * nm * nm) / ((k + 1) / 2), (k + 1) / (2 * (k - 1)));
    }

    /**
     * Calculate the area of the nozzle exit
     *
     * @param nm the Mach number of the reaction
     * @param at the area of the throat nozzle
     * @param k  the specific heat ratio
     * @return the area of the nozzle exit
     */
    public double calcAreaOfNozzleExit(double nm, double at, double k) {
        return (at / nm) * pow((1 + (k - 1) / 2 * nm * nm) / ((k + 1) / 2), (k + 1) / (2 * (k - 1)));
    }

    /**
     * find the chamber volume
     * <p>
     * The combustion chamber serves as an envelope to retain the propellants for a sufficient period to ensure
     * complete mixing and combustion. The required stay time, or combustion residence time, is a function of
     * many parameters. The theoretically required combustion chamber volume is a function of the mass flow rate
     * of the propellants, the average density of the combustion products, and the stay time needed for
     * efficient combustion.
     *
     * @param q  the propellant flow rate
     * @param v  the average specific volume
     * @param ts the propellant stay-time (how long your propellant is expected to stay in the chamber during combustion)
     *           Do the dimensional analysis, and it should show that it is the time it takes to get from the top of
     *           the chamber, through to the nozzle throat.
     *           Most Chambers run fuel rich, so you can get "more burn" when the atmospheric o2 hits the stream, but its
     *           outside the nozzle, so it doesn't do you any good. But, the propellant is still reacting up through
     *           your nozzle exit, so there are some expansive gains, there.
     *           But, for the equation above, you are talking about chamber dimensions and mass flow, so that is
     *           what the time deals with.
     * @return the chamber volume
     */
    public double findCombustionChamberVolume(double q, double v, double ts) {
        return q * v * ts;
    }

    /**
     * find the L* (Length star)
     * residence time is the characteristic length, L* (pronounced "L star"), the chamber volume divided by the
     * nozzle sonic throat area.
     * <p>
     * The L* concept is much easier to visualize than the more elusive "combustion residence time", expressed in
     * small fractions of a second. Since the value of At is in nearly direct proportion to the product of q
     * and V, L* is essentially a function of ts.
     * <p>
     * The customary method of establishing the L* of a new thrust chamber design largely relies on past experience
     * with similar propellants and engine size. Under a given set of operating conditions, such as type of propellant,
     * mixture ratio, chamber pressure, injector design, and chamber geometry, the value of the minimum required
     * L* can only be evaluated by actual firings of experimental thrust chambers. Typical L* values for various
     * propellants are shown in the table below. With throat area and minimum required L* established,
     * the chamber volume can be calculated by equation.
     *
     * @param vc the chamber volume
     * @param at the area of the nozzle throat
     * @return the Length star
     */
    public double findLstar(double vc, double at) {
        return vc / at;
    }

    /**
     * calculate the Mach number of the reaction
     *
     * @param k  the specific heat ratio
     * @param pc the pressure of the combustion chamber
     * @param pa the pressure of the ambient atmosphere.
     * @return the Mach number of the reaction
     */
    public double calculateMachNumber(double k, double pc, double pa) {
        return sqrt((2 / (k - 1)) * (pow(pc / pa, (k - 1) / k) - 1));
    }

    /**
     * calculate section ratio
     * <p>
     * Section Ratio,
     * <p>
     * Ae / At = 1.426 / 0.1756 = 8.12
     *
     * @param ae the area of the nozzle exit
     * @param at area of nozzle throat
     * @return calculate section ratio
     */
    public double calculateSectionRatio(double ae, double at) {
        return ae / at;
    }

    ////////////  cylindrical combustion chamber  ////////////

    /**
     * find the volume of the combustion chamber
     * <p>
     * Given:  At = 0.1756 m2 = 1,756 cm2
     * Dt = 2 × (1,756/)1/2 = 47.3 cm
     * = 20o
     * <p>
     * From Table 1,
     * <p>
     * L* = 102-127 cm for LOX/RP-1, let's use 110 cm
     * <p>
     * Equation (1.33),
     * <p>
     * Vc = At × L*
     * Vc = 1,756 × 110 = 193,160 cm3
     * <p>
     * From Figure 1.7,
     * <p>
     * Lc = 66 cm (second-order approximation)
     * <p>
     * Equation (1.35),
     * <p>
     * Dc = SQRT[(Dt3 + 24/ × tan  × Vc) / (Dc + 6 × tan  × Lc)]
     * Dc = SQRT[(47.33 + 24/ × tan(20) × 193,160) / (Dc + 6 × tan(20) × 66)]
     * Dc = 56.6 cm (four interations)
     * <p>
     * For the rocket engine in problem 1.7, calculate the volume and dimensions of a
     * possible combustion chamber.  The convergent cone half-angle is 20 degrees.
     *
     * @param lc    the length of the combustion chamber
     * @param dc    the diameter of the combustion chamber
     * @param dt    the diameter of the throat
     * @param angle convergent cone half-angle
     * @return the volume of the combustion chamber
     */
    public double calcCylindricalCombustionChamberVolume(double lc, double dc, double dt, double angle) {
        return pi / 24 * (6 * lc * dc * dc + (pow(dc, 3) - pow(dt, 3)) / tan(angle));
    }

    /**
     * find the chamber diameter via iteration
     * <p>
     * see above for example
     *
     * @param dc    the combustion diameter
     * @param dt    the throat diameter
     * @param vc    the combustion volume
     * @param lc    the length of the combustion chamber
     * @param angle convergent cone half-angle
     * @return the chamber diameter
     */
    public double calcCylindricalCombustionChamberDiameter(double dc, double dt, double vc, double lc, double angle) {
        return sqrt((pow(dt, 3) + 24 / pi * tan(angle) * vc) / (dc + 6 * tan(angle) * lc));
    }

    /**
     * calculate the burn rate of a solid rocket booster
     * <p>
     * he values of a and n are determined empirically for a particular propellant formulation and cannot be
     * theoretically predicted. It is important to realize that a single set of a, n values are typically valid
     * over a distinct pressure range. More than one set may be necessary to accurately represent the full
     * pressure regime of interest.
     * <p>
     * Example a, n values are 5.6059* (pressure in MPa, burn rate in mm/s) and 0.35 respectively for the S
     * pace Shuttle SRBs, which gives a burn rate of 9.34 mm/s at the average chamber pressure of 4.3 MPa.
     * <p>
     * * NASA publications gives a burn rate coefficient of 0.0386625 (pressure in PSI, burn rate in inch/s).
     *
     * @param a  the burn rate coefficient
     * @param pc the combustion chamber pressure
     * @param n  the pressure exponent
     * @return burn rate of a solid rocket booster
     */
    public double calcSRBburnRate(double a, double pc, double n) {
        return a * pow(pc, n);
    }

    /**
     * rate at which combustion products
     * <p>
     * A solid rocket motor burns along the face of a central cylindrical channel 10
     * meters long and 1 meter in diameter.  The propellant has a burn rate coefficient
     * of 5.5, a pressure exponent of 0.4, and a density of 1.70 g/ml.  Calculate the
     * burn rate and the product generation rate when the chamber pressure is 5.0 MPa.
     * <p>
     * Given:  a = 5.5
     * n = 0.4
     * Pc = 5.0 MPa
     * p = 1.70 g/ml
     * Ab =  × 1 × 10 = 31.416 m2
     * <p>
     * Equation (1.36),
     * <p>
     * r = a × Pcn
     * r = 5.5 × 5.00.4 = 10.47 mm/s
     * <p>
     * Equation (1.37),
     * <p>
     * q = p × Ab × r
     * q = 1.70 × 31.416 × 10.47 = 559 kg/s
     *
     * @param phip he solid propellant density
     * @param ab   the area of the burning surface
     * @param r    the propellant burn rate
     * @return ate at which combustion products
     */
    public double calcCombustionProductGenerationRate(double phip, double ab, double r) {
        return phip * ab * r;
    }

    /**
     * Calculate the ideal density of a solid rocket propellant consisting of 68%
     * ammonium perchlorate, 18% aluminum, and 14% HTPB by mass.
     * <p>
     * Given:  wAP = 0.68
     * wAl = 0.18
     * wHTPB = 0.14
     * <p>
     * From Properties of Rocket Propellants we have,
     * <p>
     * AP = 1.95 g/ml
     * Al = 2.70 g/ml
     * HTPB = ≈0.93 g/ml
     * <p>
     * Equation (1.38),
     * <p>
     * p = 1 / i (w / )i
     * p = 1 / [(0.68 / 1.95) + (0.18 / 2.70) + (0.14 / 0.93)]
     * p = 1.767
     * <p>
     * w is the mass fraction and the subscript i denotes the individual constituents. This is the ideal density;
     * the actual density is typically 94%-97% of the ideal density, owing to tiny voids in the grain, and is
     * dependant upon the manufacturing technique.
     *
     * @param w(i)       the mass fraction at of component i
     * @param density(i) of component i
     * @return the density
     */
    public double calculatePropellantDensity(double[] w, double[] density) {
        double sum = IntStream.range(0, w.length).mapToDouble(i -> w[i] / density[i]).sum();
        if (sum != 0) {
            return 1.0 / sum;
        } else {
            return 0;
        }
    }

    /**
     * calculate the delta v for a stage on a multi stage rocket
     * <p>
     * It is important to realize that the payload mass for any stage consists of the mass of all subsequent stages
     * plus the ultimate payload itself. The velocity increment for the vehicle is then the sum of those for the
     * individual stages where n is the total number of stages.
     *
     * @param ci  the effective exhaust velocity for this stage
     * @param moi total vehicle mass when stage i is ignited
     * @param mfi mfi is the total vehicle mass when stage i is burned out but not yet discarded.
     * @return the delta v for a segment
     */
    public double calcDeltaVForSegment(double ci, double moi, double mfi) {
        return ci * log(moi / mfi);
    }

    /**
     * the total delta v for a multi stage rocket
     * <p>
     * A two-stage rocket has the following masses:  1st-stage propellant mass 120,000
     * kg, 1st-stage dry mass 9,000 kg, 2nd-stage propellant mass 30,000 kg, 2nd-stage
     * dry mass 3,000 kg, and payload mass 3,000 kg.  The specific impulses of the
     * 1st and 2nd stages are 260 s and 320 s respectively.  Calculate the rocket's
     * total V.
     * <p>
     * Given:  Mo1 = 120,000 + 9,000 + 30,000 + 3,000 + 3,000 = 165,000 kg
     * Mf1 = 9,000 + 30,000 + 3,000 + 3,000 = 45,000 kg
     * Isp1 = 260 s
     * Mo2 = 30,000 + 3,000 + 3,000 = 36,000 kg
     * Mf2 = 3,000 + 3,000 = 6,000 kg
     * Isp2 = 320 s
     * <p>
     * Equation (1.24),
     * <p>
     * C1 = Isp1g
     * C1 = 260 × 9.80665 = 2,550 m/s
     * <p>
     * C2 = Isp2g
     * C2 = 320 × 9.80665 = 3,138 m/s
     * <p>
     * Equation (1.39),
     * <p>
     * V1 = C1 × LN[ Mo1 / Mf1 ]
     * V1 = 2,550 × LN[ 165,000 / 45,000 ]
     * V1 = 3,313 m/s
     * <p>
     * V2 = C2 × LN[ Mo2 / Mf2 ]
     * V2 = 3,138 × LN[ 36,000 / 6,000 ]
     * V2 = 5,623 m/s
     * <p>
     * Equation (1.40),
     * <p>
     * VTotal = V1 + V2
     * VTotal = 3,313 + 5,623
     * VTotal = 8,936 m/s
     *
     * @param v the set of delta v values for each stage
     * @return the total delta v for a multi stage
     */
    public double calcTotalDeltaV(double[] v) {
        return Arrays.stream(v).sum();
    }

}
