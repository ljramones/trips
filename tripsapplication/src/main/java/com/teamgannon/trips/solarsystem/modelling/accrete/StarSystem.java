package com.teamgannon.trips.solarsystem.modelling.accrete;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.*;


@Slf4j
@Data
public class StarSystem {

    // ==================== ACCRETE physics constants ====================
    //
    // These are the canonical Dole-1970 accretion model parameters, retained
    // as published. The TRIPS implementation is a Java port of the well-known
    // ACCRETE algorithm (originally Dole 1970, refined by Carl Burke 1985 and
    // others), and these values are part of that algorithm's contract — not
    // free parameters to tune.
    //
    // Reference: Dole, S.H. (1970), "Computer Simulation of the Formation of
    //            Planetary Systems", Icarus 13, 494-508.
    //            Burke, C. (1985), "starform.c" reference implementation
    //            (http://www.eldacur.com/~brons/NerdCorner/StarGen/StarGen.html).
    //
    // The values produce systems that match the broad statistical properties
    // of observed exoplanets; deviations from the source paper are deliberate
    // tunings inherited from the ACCRETE community, not bugs to "fix".

    /** Used in critical-mass calculations (Dole 1970 eqn 19, coefficient B). */
    static final double B = 1.2E-5;

    /**
     * Initial protoplanet mass seed in stellar masses (1e-15 M☉ ≈ 2e15 kg —
     * smaller than Vesta's 2.6e20 kg). The accretion simulation grows
     * planets up from this seed; the small starting value is a numerical
     * trick to avoid biasing the orbit-injection process and is part of the
     * ACCRETE algorithm's contract, not a physical claim.
     */
    static final double PROTOPLANET_MASS = 1.0E-15;

    /**
     * Dust density coefficient ("A" in Dole 1970, eqn 8): controls the total
     * mass of dust in the protoplanetary disk. The 2e-3 value is the ACCRETE
     * reference value that calibrates the simulation to roughly Sol-sized
     * end states for solar-mass stars.
     */
    static final double DUST_DENSITY_COEFF = 2.0E-3;

    /**
     * Disk-density radial-falloff parameter (Dole 1970, eqn 8). The dust
     * density at radius r is A · e^(-ALPHA · r^(1/N)). ALPHA + N together
     * shape how steeply density drops with distance from the star.
     */
    static final double ALPHA = 5.0;

    /**
     * Disk-density radial-exponent (paired with ALPHA above). Together with
     * ALPHA, defines the protoplanetary disk's radial density profile.
     */
    static final double N = 3.0;

    /**
     * Gas-to-dust ratio in the protoplanetary disk (K in Dole 1970, eqn 9).
     * 50:1 is the canonical ACCRETE value, calibrated for solar-metallicity
     * disks — accepted as a "known approximation" by the ACCRETE community
     * and retained here for parity with reference implementations.
     */
    static final double K = 50.0;


    private StarObject starObject;
    private final boolean doMoons;
    private final boolean verbose;
    private final boolean extraVerbose;


    private final double outerPlanetLimit = 0.0;
    private boolean habitable = false;

    /**
     * our idealization of the star under question,
     * based on its spectral classification
     * real data is added as available
     */
    private SimStar centralBody;
    private List<Planet> planets = new ArrayList<>();
    private List<Planet> failedPlanets = new ArrayList<>();
    private List<Planet> escapedMoons = new ArrayList<>();

    // Post-accretion structures
    private PostAccretionGenerator postAccretionGenerator;
    private PostAccretionGenerator.AsteroidBeltData asteroidBelt;
    private PostAccretionGenerator.KuiperBeltData kuiperBelt;

    // planetary formation variables, these are mostly temporary
    private boolean dustLeft = false;
    private double radiusInner = 0.0;
    private double radiusOuter = 0.0;
    private double reducedMass = 0.0;
    private double dustDensity = 0.0;
    private double cloudEccentricity = 0.0;
    private DustRecord dustHead = null;

    /**
     * for testing the simulator
     *
     * @param doMoons      create moon
     * @param verbose      general verbosity
     * @param extraVerbose extra verbosity
     */
    public StarSystem(boolean doMoons, boolean verbose, boolean extraVerbose) {
        this.doMoons = doMoons;
        this.verbose = verbose;
        this.extraVerbose = extraVerbose;
        centralBody = Utils.instance().randomStar();
//        centralBody = Utils.instance().randomKStar();
        centralBody.setAge();
        distributePlanetaryMasses();
        checkPlanets();
        migratePlanets();
        setEnvironments();
        generatePostAccretionStructures();
    }

    /**
     * primary constructor for generating this
     *
     * @param starObject   the star under consideration
     * @param doMoons      create moons
     * @param verbose      general verbosity
     * @param extraVerbose extra verbosity
     */
    public StarSystem(StarObject starObject, boolean doMoons, boolean verbose, boolean extraVerbose) {
        this.starObject = starObject;
        this.doMoons = doMoons;
        this.verbose = verbose;
        this.extraVerbose = extraVerbose;

        centralBody = starObject.toSimStar();

        // Validate stellar parameters - fail fast with clear error message
        validateAndFixStarParams(starObject.getDisplayName());

        centralBody.setAge();

        // run solar system dust aggregation
        distributePlanetaryMasses();
        checkPlanets();
        migratePlanets();
        setEnvironments();
        generatePostAccretionStructures();
    }

    /**
     * Validates stellar parameters and applies fallback defaults if invalid.
     * This prevents division by zero and NaN propagation in system generation.
     *
     * @param starName the star name for logging/error messages
     */
    private void validateAndFixStarParams(String starName) {
        boolean invalid = false;
        StringBuilder issues = new StringBuilder();

        // Access protected fields directly (same package)
        if (centralBody.mass <= 0) {
            centralBody.mass = 1.0;  // Sun-like fallback
            issues.append("Mass was ≤0 → set to 1.0 M☉; ");
            invalid = true;
        }
        if (centralBody.luminosity <= 0) {
            centralBody.luminosity = 1.0;  // Sun-like fallback
            issues.append("Luminosity was ≤0 → set to 1.0 L☉; ");
            invalid = true;
        }
        if (centralBody.temperature <= 0) {
            centralBody.temperature = 5772;  // Sun's effective temperature
            issues.append("Temperature was ≤0 → set to 5772 K; ");
            invalid = true;
        }
        if (centralBody.radius <= 0) {
            centralBody.radius = 1.0;  // Sun-like fallback
            issues.append("Radius was ≤0 → set to 1.0 R☉; ");
            invalid = true;
        }

        if (invalid) {
            log.warn("""
                     Fixed invalid stellar parameters for '{}': {} \
                     Please update the star record with correct values.\
                     """, starName, issues);
            // Recalculate derived values after fixing
            centralBody.recalc();
        }
    }

    ////////////////  APIs  //////////////////////////////////////////


    public String toString() {
        String cr = System.lineSeparator();
        String str = "New System (seed: " + Utils.instance().getSeed() + ")" + cr;
        str = str.concat("Primary: " + centralBody.toString() + cr);
        str = str.concat("Captured Moons: " + numberOfMoons() + cr);
        str = str.concat(cr);
        ListIterator<Planet> i = this.planets.listIterator();
        int x = 1;
        while (i.hasNext()) {
            str = str.concat("\tPlanet " + "%02d".formatted(x++) + ": " + i.next() + cr);
        }
        return str;
    }


    public static double sumMassOfPlanets(List<Planet> p) {
        if (p.size() == 0) {
            return 0.0;
        }
        ListIterator<Planet> i = p.listIterator();
        double x = 0.0;
        while (i.hasNext()) {
            // {@code mass} is the planet's total mass post-accretion (dust + gas).
            // For terrestrial planets, gasMass ≈ 0 so mass ≈ dustMass; for gas
            // giants the two contribute together. Per the ACCRETE reference
            // implementation, summing {@code mass} is the correct disk-mass total.
            x += i.next().mass;
        }
        return x;
    }


    public int numberOfMoons() {
        int moons = 0;
        for (Planet planet : planets) {
            moons += planet.numberOfMoons();
        }
        return moons;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    private List<Planet> getFailedPlanets() {
        return failedPlanets;
    }

    private List<Planet> getEscapedMoons() {
        return escapedMoons;
    }

    //////////////////////////////  Create First first Planets  //////////////////////////////////

    /**
     * distribute solar system dust based on model for star
     */
    private void distributePlanetaryMasses() {
        double sma,
                ecc,
                mass,
                criticalMass,
                innerBound,
                outerBound;

        WDouble dustMass = new WDouble(0.0), gasMass = new WDouble(0.0);

        setInitialConditions();
        innerBound = centralBody.innermostPlanet();

        // initial setting
        outerBound = centralBody.outermostPlanet();

        while (dustLeft) {
            sma = Utils.instance().randomNumber(innerBound, outerBound);
            ecc = Utils.instance().randomEccentricity();

            mass = PROTOPLANET_MASS;

            if (verbose && extraVerbose) {
                // this really isn't significant
                log.debug("Checking at {} AU...", "%1$,.2f".formatted(sma));
            }

            if (dustAvailable(innerEffectLimit(sma, ecc, mass), outerEffectLimit(sma, ecc, mass))) {
                if (verbose) {
                    log.debug("Injecting protoplanet at {} AU...", "%1$,.2f".formatted(sma));
                }
                dustDensity = DUST_DENSITY_COEFF * sqrt(centralBody.mass) * exp(-ALPHA * pow(sma, (1.0 / N)));
                criticalMass = criticalMass(sma, ecc);
                mass = accreteDust(mass, dustMass, gasMass, sma, ecc, criticalMass, innerBound, outerBound);

                dustMass.value += PROTOPLANET_MASS;

                if (mass > PROTOPLANET_MASS) {
                    coalescePlanetesimals(sma, ecc, mass, criticalMass, dustMass.value, gasMass.value, innerBound, outerBound);
                } else {
                    // save the failed planetesimal
                    Planet p = new Planet(this.centralBody);
                    p.sma = sma;
                    p.eccentricity = ecc;
                    p.mass = dustMass.value + gasMass.value;
                    p.setDustMass(dustMass.value);
                    p.setGasMass(gasMass.value);
                    failedPlanets.add(p);

                    if (verbose) {
                        log.debug("Failed due to larger neighbor.");
                    }
                }
            } else if (verbose && extraVerbose) {
                // this really isn't significant.
                log.debug("Failed due to lack of dust.");
            }
        }
    }

    /**
     * set the initial condistions for this solar system
     */
    private void setInitialConditions() {
        this.dustHead = new DustRecord();
        this.dustHead.dustPresent = true;
        this.dustHead.gasPresent = true;
        this.dustHead.outerEdge = centralBody.stellarDustLimit();
        this.dustLeft = true;
        // Dust-cloud eccentricity W (Dole 1970, eqn 11). The protoplanetary
        // dust cloud isn't perfectly circular — particles move on slightly
        // eccentric orbits. W = 0.2 is the canonical ACCRETE value.
        this.cloudEccentricity = 0.2;
    }


    /**
     * Inner edge of the gravitational reach of a protoplanet at the given
     * orbital parameters. Implements the "reach" formula r_i = a(1-e)(1-μ)/(1+W)
     * from Dole 1970 eqn 4, where a=sma, e=ecc, μ=mass ratio, W=cloud
     * eccentricity. Protoplanets sweep up dust in [innerEffectLimit, outerEffectLimit].
     */
    private double innerEffectLimit(double sma, double ecc, double mass) {
        return (sma * (1.0 - ecc) * (1.0 - mass) / (1.0 + cloudEccentricity));
    }

    /**
     * Outer edge of the gravitational reach of a protoplanet — the dual of
     * {@link #innerEffectLimit}. From Dole 1970 eqn 5:
     * r_o = a(1+e)(1+μ)/(1-W).
     */
    private double outerEffectLimit(double sma, double ecc, double mass) {
        return (sma * (1.0 + ecc) * (1.0 + mass) / (1.0 - cloudEccentricity));
    }

    /**
     * Critical mass (in stellar masses) above which a protoplanet starts
     * accreting gas and becomes a gas giant. Per Dole 1970 eqn 19:
     * M_crit = B · (r_p · √L)^(-3/4) where B is the calibration constant
     * (1.2e-5) and L is stellar luminosity. The coefficient B is a published
     * empirical fit; see header doc on the {@link #B} constant for source.
     */
    private double criticalMass(double sma, double ecc) {
        double periapsis = SystemObject.periapsis(sma, ecc);
        double temp = periapsis * sqrt(centralBody.luminosity);
        return B * pow(temp, -0.75);
    }

    private boolean dustAvailable(double innerRange, double outerRange) {
        DustRecord currentBand;
        boolean dustHere = false;
        currentBand = this.dustHead;

        while (currentBand != null && currentBand.getOuterEdge() < innerRange) {
            currentBand = currentBand.next;
        }
        if (currentBand != null) {
            dustHere = currentBand.dustPresent;
        }
        while (currentBand != null && currentBand.getInnerEdge() < outerRange) {
            dustHere = dustHere || currentBand.dustPresent;
            currentBand = currentBand.next;
        }

        return dustHere;
    }

    private double accreteDust(double seedMass,
                               WDouble newDust,
                               WDouble newGas,
                               double sma,
                               double ecc,
                               double criticalMass,
                               double innerBound,
                               double outerBound) {
        double newMass = seedMass, tempMass;

        do {
            tempMass = newMass;
            newMass = collectDust(newMass, newDust, newGas, sma, ecc, criticalMass, dustHead);
        } while (!(newMass - tempMass < 0.0001 * tempMass));

        seedMass += newMass;
        updateDustLanes(radiusInner, radiusOuter, seedMass, criticalMass, innerBound, outerBound);

        return seedMass;
    }

    private double collectDust(double lastMass,
                               WDouble newDust,
                               WDouble newGas,
                               double sma,
                               double ecc,
                               double criticalMass,
                               DustRecord band) {
        double massDensity,
                temp,
                temp1,
                temp2,
                tempDensity,
                bandWidth,
                width,
                volume,
                gasDensity = 0.0,
                newMass,
                nextMass;

        WDouble nextDust = new WDouble(0.0),
                nextGas = new WDouble(0.0);

        temp = lastMass / (1.0 + lastMass);
        reducedMass = pow(temp, (1.0 / 4.0));
        radiusInner = innerEffectLimit(sma, ecc, reducedMass);
        radiusOuter = outerEffectLimit(sma, ecc, reducedMass);

        if (radiusInner < 0.0) {
            radiusInner = 0.0;
        }

        if (band == null) {
            return 0.0;
        } else {
            if (!band.dustPresent) {
                tempDensity = 0.0;
            } else {
                tempDensity = dustDensity;
            }

            if (lastMass < criticalMass || !band.gasPresent) {
                massDensity = tempDensity;
            } else {
                massDensity = K * tempDensity / (1.0 + sqrt(criticalMass / lastMass) * (K - 1.0));
                gasDensity = massDensity - tempDensity;
            }

            if (band.outerEdge <= radiusInner || band.innerEdge >= radiusOuter) {
                return collectDust(lastMass, new WDouble(newDust.value),
                        new WDouble(newGas.value), sma, ecc, criticalMass, band.next
                );
            } else {
                bandWidth = (radiusOuter - radiusInner);

                temp1 = radiusOuter - band.outerEdge;
                if (temp1 < 0.0) {
                    temp1 = 0.0;
                }
                width = bandWidth - temp1;

                temp2 = band.innerEdge - radiusInner;
                if (temp2 < 0.0)
                    temp2 = 0.0;
                width = width - temp2;

                temp = 4.0 * PI * pow(sma, 2.0) * reducedMass * (1.0 - ecc * (temp1 - temp2) / bandWidth);
                volume = temp * width;

                newMass = volume * massDensity;
                newGas.value = volume * gasDensity;
                newDust.value = newMass - newGas.value;

                nextMass = collectDust(lastMass, nextDust, nextGas, sma, ecc, criticalMass, band.next);

                newGas.value += nextGas.value;
                newDust.value += nextDust.value;

                return newMass + nextMass;
            }
        }
    }

    private void updateDustLanes(double min,
                                 double max,
                                 double mass,
                                 double criticalMass,
                                 double innerBound,
                                 double outerBound) {

        // I see no need to convert this to an ArrayList, the single linked list works fine here,
        // especially since we have no need to sort it.
        boolean gas;
        DustRecord node1, node2, node3;

        dustLeft = false;
        gas = !(mass > criticalMass);
        node1 = dustHead;
        while (node1 != null) {
            if (node1.innerEdge < min && node1.outerEdge > max) {
                node2 = new DustRecord();
                node2.innerEdge = min;
                node2.outerEdge = max;
                if (node1.gasPresent) {
                    node2.gasPresent = gas;
                } else {
                    node2.gasPresent = false;
                }
                node2.dustPresent = false;
                node3 = new DustRecord();
                node3.innerEdge = max;
                node3.outerEdge = node1.outerEdge;
                node3.gasPresent = node1.gasPresent;
                node3.dustPresent = node1.dustPresent;
                node3.next = node1.next;
                node1.next = node2;
                node2.next = node3;
                node1.outerEdge = min;
                node1 = node3.next;
            } else {
                if (node1.innerEdge < max && node1.outerEdge > max) {
                    node2 = new DustRecord();
                    node2.next = node1.next;
                    node2.dustPresent = node1.dustPresent;
                    node2.gasPresent = node1.gasPresent;
                    node2.outerEdge = node1.outerEdge;
                    node2.innerEdge = max;
                    node1.next = node2;
                    node1.outerEdge = max;
                    if (node1.gasPresent) {
                        node1.gasPresent = gas;
                    } else {
                        node1.gasPresent = false;
                    }
                    node1.dustPresent = false;
                    node1 = node2.next;
                } else {
                    if (node1.innerEdge < min && node1.outerEdge > min) {
                        node2 = new DustRecord();
                        node2.next = node1.next;
                        node2.dustPresent = false;
                        if (node1.gasPresent) {
                            node2.gasPresent = gas;
                        } else {
                            node2.gasPresent = false;
                        }
                        node2.outerEdge = node1.outerEdge;
                        node2.innerEdge = min;
                        node1.next = node2;
                        node1.outerEdge = min;
                        node1 = node2.next;
                    } else {
                        if (node1.innerEdge >= min && node1.outerEdge <= max) {
                            if (node1.gasPresent) {
                                node1.gasPresent = gas;
                            }
                            node1.dustPresent = false;
                            node1 = node1.next;
                        } else {
                            if (node1.outerEdge < min || node1.innerEdge > max) {
                                node1 = node1.next;
                            }
                        }
                    }
                }
            }
        }
        node1 = dustHead;
        while (node1 != null) {
            if (node1.dustPresent && node1.outerEdge >= innerBound && node1.innerEdge <= outerBound) {
                dustLeft = true;
            }
            node2 = node1.next;
            if (node2 != null) {
                if (node1.dustPresent == node2.dustPresent && node1.gasPresent == node2.gasPresent) {
                    node1.outerEdge = node2.outerEdge;
                    node1.next = node2.next;
                }
            }
            node1 = node1.next;
        }
    }

    public void coalescePlanetesimals(double sma,
                                      double ecc,
                                      double mass,
                                      double criticalMass,
                                      double dustMass,
                                      double gasMass,
                                      double innerBound,
                                      double outerBound) {

        Planet thePlanet;
        boolean finished = false;
        double temp, diff, dist1, dist2;

        // First we try to find an existing planet with an over-lapping orbit.
        for (Planet planet : planets) { // thePlanet = planetHead; thePlanet != null; thePlanet = thePlanet.next) {
            thePlanet = planet;
            diff = thePlanet.sma - sma;

            if (diff > 0.0) {
                dist1 = (sma * (1.0 + ecc) * (1.0 + reducedMass)) - sma;
                // apoapsis
                reducedMass = pow(thePlanet.mass / (1.0 + thePlanet.mass), 1.0 / 4.0);
                dist2 = thePlanet.sma - (thePlanet.sma * (1.0 - thePlanet.eccentricity) * (1.0 - reducedMass));
            } else {
                dist1 = sma - (sma * (1.0 - ecc) * (1.0 - reducedMass));
                // periapsis
                reducedMass = pow(thePlanet.mass / (1.0 + thePlanet.mass), 1.0 / 4.0);
                dist2 = (thePlanet.sma * (1.0 + thePlanet.eccentricity) * (1.0 + reducedMass)) - thePlanet.sma;
            }

            if (abs(diff) <= abs(dist1) || abs(diff) <= abs(dist2)) {
                WDouble newDust = new WDouble(0.0), newGas = new WDouble(0.0);
                double newSMA = (thePlanet.mass + mass) / ((thePlanet.mass / thePlanet.sma) + (mass / sma));

                temp = thePlanet.mass * sqrt(thePlanet.sma) * sqrt(1.0 - pow(thePlanet.eccentricity, 2.0));
                temp = temp + (mass * sqrt(sma) * sqrt(sqrt(1.0 - pow(ecc, 2.0))));
                temp = temp / ((thePlanet.mass + mass) * sqrt(newSMA));
                temp = 1.0 - pow(temp, 2.0);
                if (temp < 0.0 || temp >= 1.0) {
                    temp = 0.0;
                }
                ecc = sqrt(temp);

                if (doMoons) {
                    double existingMass = 0.0;

                    if (thePlanet.getMoons().size() > 0) {
                        for (Planet value : thePlanet.getMoons()) {
                            existingMass += value.mass;
                        }
                    }

                    if (mass < criticalMass) {
                        if (SystemObject.massInEarthMasses(mass) < 2.5
                                && SystemObject.massInEarthMasses(mass) > .0001
                                && existingMass < thePlanet.mass * .05) {

                            Planet theMoon = new Planet(this.centralBody);

                            theMoon.sma = sma;
                            theMoon.eccentricity = ecc;
                            theMoon.mass = mass;
                            theMoon.setDustMass(dustMass);
                            theMoon.setGasMass(gasMass);
                            theMoon.setMoon(true);

                            if (theMoon.getDustMass() + theMoon.getGasMass() > thePlanet.getDustMass() + thePlanet.getGasMass()) {
                                double tempDust = thePlanet.getDustMass();
                                double tempGas = thePlanet.getGasMass();
                                double tempMass = thePlanet.mass;

                                thePlanet.setDustMass(theMoon.getDustMass());
                                thePlanet.setGasMass(theMoon.getGasMass());
                                thePlanet.mass = theMoon.mass;

                                theMoon.setDustMass(tempDust);
                                theMoon.setGasMass(tempGas);
                                theMoon.mass = tempMass;
                            }

                            thePlanet.getMoons().add(theMoon);
                            finished = true;

                            if (verbose) {
                                log.debug("Moon Captured... {}AU ({}EM) <- {}EM",
                                        "%1$,.2f".formatted(thePlanet.sma),
                                        "%1$,.2f".formatted(thePlanet.massInEarthMasses()),
                                        "%1$,.2f".formatted(Planet.massInEarthMasses(mass)));
                            }
                        } else {
                            // save the moon
                            Planet p = new Planet(this.centralBody);
                            p.sma = sma;
                            p.eccentricity = ecc;
                            p.mass = dustMass + gasMass;
                            p.setDustMass(dustMass);
                            p.setGasMass(gasMass);
                            escapedMoons.add(p);

                            if (verbose) {
                                String msg = "Moon Escapes... " + "%1$,.2f".formatted(thePlanet.sma) + " AU (" + "%1$,.2f".formatted(thePlanet.massInEarthMasses()) + "EM)";
                                if (existingMass >= thePlanet.mass * 0.05) {
                                    msg += " (big moons)";
                                }
                                if (Planet.massInEarthMasses(mass) >= 2.5) {
                                    msg += ", too big";
                                } else if (Planet.massInEarthMasses(mass) <= 0.0001) {
                                    msg += ", too small";
                                }
                                msg += " moon at " + "%1$,.2f".formatted(Planet.massInEarthMasses(mass)) + "EM";
                                log.debug(msg);
                            }
                        }
                    }
                }

                if (!finished) {
                    if (verbose) {
                        log.debug("Collision between two planetesimals: {} AU ({}EM), {} AU ({}EM = {}EM dust + {}EM gas [{}EM])-> {} AU ({})",
                                "%1$,.2f".formatted(thePlanet.sma),
                                "%1$,.2f".formatted(thePlanet.massInEarthMasses()),
                                "%1$,.2f".formatted(sma),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(mass)),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(dustMass)),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(gasMass)),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(criticalMass)),
                                "%1$,.2f".formatted(newSMA),
                                "%1$,.2f".formatted(ecc));
                    }
                    temp = thePlanet.mass + mass;
                    temp = accreteDust(temp, newDust, newGas, newSMA, ecc, centralBody.luminosity, innerBound, outerBound);
                    thePlanet.sma = newSMA;
                    thePlanet.eccentricity = ecc;
                    thePlanet.mass = temp;
                    double inter1 = thePlanet.getDustMass() + dustMass + newDust.value;
                    thePlanet.setDustMass(inter1);
                    double inter2 = thePlanet.getGasMass() + gasMass + newGas.value;
                    thePlanet.setGasMass(inter2);
                    if (thePlanet.mass >= criticalMass) {
                        thePlanet.setGasGiant(true);
                    }
                    // planets.add(thePlanet); // the planet already exists, don't duplicate it
                }

                finished = true;
                break;
            }
        }

        if (!finished) { // Planetesimals didn't collide. Make it a planet.
            thePlanet = new Planet(this.centralBody);

            thePlanet.sma = sma;
            thePlanet.eccentricity = ecc;
            thePlanet.mass = mass;
            thePlanet.setDustMass(dustMass);
            thePlanet.setGasMass(gasMass);
            if (mass >= criticalMass) {
                thePlanet.setGasGiant(true);
            }
            planets.add(thePlanet);
        }

        // adding a new planet requires sorting the list.
        sortPlanetsBySMA(planets);
    }

    /**
     * This is basically a late-generation coalescePlanetesimals designed to reintegrate masses that initially failed.
     *
     * @param sma          x
     * @param ecc          x
     * @param mass         x
     * @param criticalMass x
     * @param dustMass     x
     * @param gasMass      x
     */
    public void injectMass(double sma, double ecc, double mass, double criticalMass, double dustMass, double gasMass) {
        Planet thePlanet;
        boolean finished = false;
        double temp, diff, dist1, dist2;

        // First we try to find an existing planet with an over-lapping orbit.
        for (Planet planet : planets) {
            thePlanet = planet;
            diff = thePlanet.sma - sma;

            if (diff > 0.0) {
                dist1 = (sma * (1.0 + ecc) * (1.0 + reducedMass)) - sma;
                // apoapsis
                reducedMass = pow(thePlanet.mass / (1.0 + thePlanet.mass), 1.0 / 4.0);
                dist2 = thePlanet.sma - (thePlanet.sma * (1.0 - thePlanet.eccentricity) * (1.0 - reducedMass));
            } else {
                dist1 = sma - (sma * (1.0 - ecc) * (1.0 - reducedMass));
                // periapsis
                reducedMass = pow(thePlanet.mass / (1.0 + thePlanet.mass), 1.0 / 4.0);
                dist2 = (thePlanet.sma * (1.0 + thePlanet.eccentricity) * (1.0 + reducedMass)) - thePlanet.sma;
            }

            if (abs(diff) <= abs(dist1) || abs(diff) <= abs(dist2)) {
                // This is where the magic happens
                double newSMA = (thePlanet.mass + mass) / ((thePlanet.mass / thePlanet.sma) + (mass / sma));

                temp = thePlanet.mass * sqrt(thePlanet.sma) * sqrt(1.0 - pow(thePlanet.eccentricity, 2.0));
                temp = temp + (mass * sqrt(sma) * sqrt(sqrt(1.0 - pow(ecc, 2.0))));
                temp = temp / ((thePlanet.mass + mass) * sqrt(newSMA));
                temp = 1.0 - pow(temp, 2.0);
                if (temp < 0.0 || temp >= 1.0) {
                    temp = 0.0;
                }
                ecc = sqrt(temp);

                if (doMoons) {
                    double existingMass = 0.0;

                    for (Planet value : thePlanet.getMoons()) {
                        existingMass += value.mass;
                    }

                    if (mass < criticalMass) {
                        if (SystemObject.massInEarthMasses(mass) < 2.5
                                && SystemObject.massInEarthMasses(mass) > .0001
                                && existingMass < thePlanet.mass * .05) {

                            Planet theMoon = new Planet(this.centralBody);

                            theMoon.sma = sma;
                            theMoon.eccentricity = ecc;
                            theMoon.mass = mass;
                            theMoon.setDustMass(dustMass);
                            theMoon.setGasMass(gasMass);
                            theMoon.setMoon(true);

                            if (theMoon.getDustMass() + theMoon.getGasMass() > thePlanet.getDustMass() + thePlanet.getGasMass()) {
                                double tempDust = thePlanet.getDustMass();
                                double tempGas = thePlanet.getGasMass();
                                double tempMass = thePlanet.mass;

                                thePlanet.setDustMass(theMoon.getDustMass());
                                thePlanet.setGasMass(theMoon.getGasMass());
                                thePlanet.mass = theMoon.mass;

                                theMoon.setDustMass(tempDust);
                                theMoon.setGasMass(tempGas);
                                theMoon.mass = tempMass;
                            }

                            thePlanet.getMoons().add(theMoon);
                            finished = true;

                            if (verbose) {
                                log.debug("Moon Captured... {}AU ({}EM) <- {}EM",
                                        "%1$,.2f".formatted(thePlanet.sma),
                                        "%1$,.2f".formatted(thePlanet.massInEarthMasses()),
                                        "%1$,.2f".formatted(Planet.massInEarthMasses(mass)));
                            }
                        }
                    }
                }

                if (!finished) {
                    if (verbose) {
                        log.debug("Collision between two planetesimals: {} AU ({}EM), {} AU ({}EM = {}EM dust + {}EM gas [{}EM])-> {} AU ({})",
                                "%1$,.2f".formatted(thePlanet.sma),
                                "%1$,.2f".formatted(thePlanet.massInEarthMasses()),
                                "%1$,.2f".formatted(sma),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(mass)),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(dustMass)),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(gasMass)),
                                "%1$,.2f".formatted(SystemObject.massInEarthMasses(criticalMass)),
                                "%1$,.2f".formatted(newSMA),
                                "%1$,.2f".formatted(ecc));
                    }
                    thePlanet.sma = newSMA;
                    thePlanet.eccentricity = ecc;
                    thePlanet.mass += mass;
                    double inter1 = thePlanet.getDustMass() + dustMass;
                    thePlanet.setDustMass(inter1);
                    double inter2 = thePlanet.getGasMass() + gasMass;
                    thePlanet.setGasMass(inter2);
                    if (thePlanet.mass >= criticalMass) {
                        thePlanet.setGasGiant(true);
                    }
                    // planets.add(thePlanet); // The planet already exists, don't duplicate it
                }

                finished = true;
                break;
            }
        }

        if (!finished) { // Planetesimals didn't collide. Make it a planet.
            thePlanet = new Planet(this.centralBody);

            thePlanet.sma = sma;
            thePlanet.eccentricity = ecc;
            thePlanet.mass = mass;
            thePlanet.setDustMass(dustMass);
            thePlanet.setGasMass(gasMass);
            if (mass >= criticalMass) {
                thePlanet.setGasGiant(true);
            }
            planets.add(thePlanet);
        }

        // adding a new planet requires sorting the list.
        sortPlanetsBySMA(planets);
    }

    private void sortPlanetsBySMA(List<Planet> list) {
        Collections.sort(list);
    }

    //////////////////////////////////  Check (validate) Planets   ///////////////////////////////////////////////////

    private void checkPlanets() {
        ListIterator<Planet> i;
        Planet p;

        // For any escaped moons, alter their eccentricity.
        i = this.escapedMoons.listIterator();
        while (i.hasNext()) {
            p = i.next();
            p.eccentricity += pow(Utils.instance().randomNumber(0.1, 0.9) * p.sma, Utils.ECCENTRICITY_COEFF); // Should be pretty wide.
        }

        // Now decide how to handle each failure or escapee.
        i = this.failedPlanets.listIterator();
        while (i.hasNext()) {
            p = i.next();
            injectMass(p.sma, p.eccentricity, p.mass, criticalMass(p.sma, p.eccentricity), p.getDustMass(), p.getGasMass());
        }
        this.failedPlanets.clear();

        i = this.escapedMoons.listIterator();
        while (i.hasNext()) {
            p = i.next();
            injectMass(p.sma, p.eccentricity, p.mass, criticalMass(p.sma, p.eccentricity), p.getDustMass(), p.getGasMass());
        }
        this.escapedMoons.clear();

        // ensure nothing has a moon bigger than itself.
        i = this.planets.listIterator();
        ArrayList<Planet> newPlanets = new ArrayList<>();
        while (i.hasNext()) {
            p = i.next();
            if (p.numberOfMoons() > 0) {
                // Remember that it's unsafe to remove an item during iteration, make a whole new arraylist.
                ListIterator<Planet> m = p.getMoons().listIterator();
                Planet largerMoon = p, checkMoon;
                while (m.hasNext()) {
                    checkMoon = m.next();
                    if (checkMoon.mass > largerMoon.mass) {
                        largerMoon = checkMoon;
                    }
                }
                if (largerMoon == p) {
                    // nothing changes
                    newPlanets.add(p);
                } else {
                    // swap planets
                    p.getMoons().remove(largerMoon);
                    largerMoon.getMoons().addAll(p.getMoons());
                    p.getMoons().clear();
                    largerMoon.getMoons().add(p);
                    newPlanets.add(largerMoon);
                }
            } else {
                newPlanets.add(p);
            }
        }
        this.planets.clear();
        this.planets.addAll(newPlanets);

        // sort the planets by SMA
        sortPlanetsBySMA(this.planets);

        // ensure each planet has a correct mass and a type
        i = this.planets.listIterator();
        while (i.hasNext()) {
            i.next().validate(false);
        }
    }

    ///////////////////////////////  Migrate Planets  //////////////////////////

    private void migratePlanets() {

    }


    ///////////////////////////  Generate Environments   //////////////////////////

    private void setEnvironments() {
        for (Planet p : planets) {
            p.finalize(doMoons);
            if (p.isHabitable() || p.isHabitableMoon()) {
                this.habitable = true;
            }
        }
    }

    ///////////////////////////  Post-Accretion Structures   //////////////////////////

    /**
     * Generate post-accretion structures: planetary rings, asteroid belts, and Kuiper belt.
     * This should be called after setEnvironments() when all planet properties are finalized.
     */
    private void generatePostAccretionStructures() {
        postAccretionGenerator = new PostAccretionGenerator(centralBody, Utils.instance().getSeed());
        postAccretionGenerator.generate(planets);

        // Store the belt data for later access
        asteroidBelt = postAccretionGenerator.getAsteroidBelt();
        kuiperBelt = postAccretionGenerator.getKuiperBelt();

        if (asteroidBelt != null) {
            log.info("System has asteroid belt: {:.2f} - {:.2f} AU",
                    asteroidBelt.getInnerRadiusAU(), asteroidBelt.getOuterRadiusAU());
        }
        if (kuiperBelt != null) {
            log.info("System has Kuiper belt: {:.2f} - {:.2f} AU",
                    kuiperBelt.getInnerRadiusAU(), kuiperBelt.getOuterRadiusAU());
        }

        // Log rings for planets
        for (Planet p : planets) {
            if (p.getRingType() != null) {
                log.info("Planet at {:.2f} AU has {} ring system",
                        p.getSma(), p.getRingType());
            }
        }
    }

    /**
     * Check if this system has an asteroid belt.
     */
    public boolean hasAsteroidBelt() {
        return asteroidBelt != null;
    }

    /**
     * Check if this system has a Kuiper belt.
     */
    public boolean hasKuiperBelt() {
        return kuiperBelt != null;
    }

}
