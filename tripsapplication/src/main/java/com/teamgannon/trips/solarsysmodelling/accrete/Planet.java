package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class Planet extends SystemObject implements Comparable<Planet> {
    private double axialTilt = 0.0;
    private double radius = 0.0; // in km
    private double dustMass = 0.0;
    private double gasMass = 0.0;
    private double density = 0.0; // in g/cc
    private double orbitalPeriod = 0.0;
    private double coreRadius = 0.0;
    private double dayLength = 0.0;
    private double exosphericTemperature = 0.0;
    private double estimatedTemperature = 0.0;
    private double estimatedTerrestrialTemperature = 0.0;
    private double rmsVelocity = 0.0;
    private double escapeVelocity = 0.0;
    private double volatileGasInventory = 0.0;
    private double surfaceAcceleration = 0.0;
    private double surfaceGravity = 0.0;
    private double surfacePressure = 0.0;
    private double surfaceTemperature = 0.0;
    private double highTemperature = 0.0;
    private double lowTemperature = 0.0;
    private double maxTemperature = 0.0;
    private double minTemperature = 0.0;
    private double boilingPoint = 0.0;
    private double greenhouseRise = 0.0;
    private double minimumMolecularWeight = 0.0;
    private double hydrosphere = 0.0;
    private double cloudCover = 0.0;
    private double albedo = 0.0;
    private double iceCover = 0.0;
    private int orbitalZone = -1;

    private boolean gasGiant = false;
    private boolean habitableJovian = false;
    private boolean isMoon = false;
    private boolean resonantPeriod = false;
    private boolean greenhouseEffect = false;
    private boolean habitable = false;
    private boolean earthlike = false;
    private boolean habitableMoon = false;

    private PlanetTypeEnum type;
    private SimStar primary;
    private List<Planet> moons = new ArrayList<>();
    private List<AtmosphericChemical> atmosphere = new ArrayList<>();

    public Planet(SimStar primary) {
        this.primary = primary;
    }

    /**
     * @param separation  The average distance between the two bodies.
     * @param smallerBody The smaller body in this equation.
     * @param largerBody  The larger body in this equation, usually the star.
     * @return The orbital period of this binary pair in seconds.
     */
    public static double binaryOrbitalPeriod(double separation, SystemObject smallerBody, SystemObject largerBody) {
        return sqrt(pow(smaInMeters(separation), 3.0) / (G * (smallerBody.massInKg() + largerBody.massInKg()))) * PI * 2.0;
    }

    @Override
    public int compareTo(Planet p) {
        if (this.isMoon) {
            if (p.isMoon) {
                if (p.asMoonSMA > this.asMoonSMA) {
                    return -1;
                }
                if (p.asMoonSMA < this.asMoonSMA) {
                    return 1;
                }
            } else {
                return 0; // not comparable
            }
        } else {
            if (p.isMoon) {
                return 0; // not comparable
            } else {
                if (p.sma > this.sma) {
                    return -1;
                }
                if (p.sma < this.sma) {
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * Ensures this planets and its moons have a correct mass and type
     */
    public void validate(boolean moon) {
        this.isMoon = moon;
/*
        if(this.dustMass + this.gasMass != this.mass) {
            java.lang.System.out.println("Unequal masses: Dust Mass: " + String.format("%.3E", massInKg(this.dustMass)) + " kg, Gas Mass: " + String.format("%.3E", massInKg(this.gasMass)) + " kg, Mass: " + String.format("%.3E", massInKg(this.mass)) + " kg, Difference: " + String.format("%.3E", massInKg(this.mass - (this.dustMass + this.gasMass))) + " kg");
            if(this.gasMass == 0.0) {
                java.lang.System.out.println("No gas mass, ignoring difference.");
            } else {

            }
        }
*/
        // validate all the moons
        if (!moon) {
            for (Planet planet : this.moons) {
                planet.validate(true);
            }
        }
    }

    /**
     * Performs finalizing calculation on the planet based on its given parameters.
     * Make all necessary changes to sma, eccentricity, various masses, etc... before running this.
     */
    public void finalize(boolean doMoons) {
        this.surfaceTemperature = 0.0;
        this.highTemperature = 0.0;
        this.lowTemperature = 0.0;
        this.maxTemperature = 0.0;
        this.minTemperature = 0.0;
        this.greenhouseRise = 0.0;
        this.resonantPeriod = false;
        this.orbitalZone = orbitalZone(this.primary.luminosity);
        this.orbitalPeriod = orbitalPeriod(this.primary);
        this.axialTilt = axialTilt();
        this.exosphericTemperature = EARTH_EXOSPHERIC_TEMPERATURE / pow(this.sma / this.primary.radiusEcosphere, 2.0);
        this.rmsVelocity = rmsVelocity(MOLECULAR_NITROGEN);
        this.coreRadius = kothariRadius();

        // Calculate the radius as a gas giant, to verify it will retain gas. Then if mass > Earth,
        // it's at least 5% gas, and retains He, it's some flavor of gas giant.

        this.density = empiricalDensity(this.primary.radiusEcosphere);
        this.radius = volumeRadius();
        this.surfaceAcceleration = gravitationalAcceleration();
        this.surfaceGravity = gravity();
        this.minimumMolecularWeight = minimumMolecularWeight();

        if (massInEarthMasses() > 1.0 && this.gasMass / this.mass > 0.05 && this.minimumMolecularWeight <= 4.0) {
            // Gas Giant Planet
            if (this.gasMass / this.mass < 0.20) {
                this.type = PlanetTypeEnum.tSubSubGasGiant;
            } else if (massInEarthMasses() < 20.0) {
                this.type = PlanetTypeEnum.tSubGasGiant;
            } else {
                this.type = PlanetTypeEnum.tGasGiant;
            }
        } else {
            // Rocky Planet
            this.radius = kothariRadius();
            this.density = volumeDensity();
            this.surfaceAcceleration = gravitationalAcceleration();
            this.surfaceGravity = gravity();
            this.type = PlanetTypeEnum.tUnknown; // Basic initialization.

            if (this.gasMass / this.mass > 0.000001) {
                double h2Mass = this.gasMass * 0.85;
                double heMass = (this.gasMass - h2Mass) * 0.999;
                double h2Life = gasLife(MOLECULAR_HYDROGEN);
                double heLife = gasLife(HELIUM);
                double h2Loss, heLoss;

                if (h2Life < this.primary.age) {
                    h2Loss = (1.0 - (1.0 / exp(this.primary.age / h2Life))) * h2Mass;
                    this.gasMass -= h2Loss;
                    this.mass -= h2Loss;
                    this.surfaceAcceleration = gravitationalAcceleration();
                    this.surfaceGravity = gravity();
                }

                if (heLife < this.primary.age) {
                    heLoss = (1.0 - (1.0 / exp(this.primary.age / heLife))) * heMass;
                    this.gasMass -= heLoss;
                    this.mass -= heLoss;
                    this.surfaceAcceleration = gravitationalAcceleration();
                    this.surfaceGravity = gravity();
                }
            }
        }

        this.dayLength = dayLength();
        this.escapeVelocity = escapeVelocity();

        if (this.type == PlanetTypeEnum.tGasGiant
                || this.type == PlanetTypeEnum.tSubGasGiant
                || this.type == PlanetTypeEnum.tSubSubGasGiant) {

            this.greenhouseEffect = false;
            this.volatileGasInventory = Double.MAX_VALUE;
            this.surfacePressure = Double.MAX_VALUE;
            this.boilingPoint = Double.MAX_VALUE;
            this.surfaceTemperature = Double.MAX_VALUE;
            this.greenhouseRise = 0.0;
            this.albedo = Utils.instance().about(GAS_GIANT_ALBEDO, 0.1);
            this.hydrosphere = 1.0;
            this.cloudCover = 1.0;
            this.iceCover = 0.0;
            this.surfaceGravity = gravity();
            this.minimumMolecularWeight = minimumMolecularWeight();
            this.surfaceGravity = Double.MAX_VALUE;
            this.estimatedTemperature = estimatedTemperature();
            this.estimatedTerrestrialTemperature = estimatedTerrestrialTemperature();

            if (this.estimatedTerrestrialTemperature >= FREEZING_POINT_OF_WATER
                    && this.estimatedTerrestrialTemperature <= EARTH_AVERAGE_KELVIN + 10.0
                    && this.primary.age > 2.0E9) {
                this.habitableJovian = true;
            }
        } else {
            this.estimatedTemperature = estimatedTemperature();
            this.estimatedTerrestrialTemperature = estimatedTerrestrialTemperature();
            this.surfaceGravity = gravity();
            this.minimumMolecularWeight = minimumMolecularWeight();
            this.greenhouseEffect = greenhouse();
            this.volatileGasInventory = volatileGasInventory();
            this.surfacePressure = pressure();

            if (this.surfacePressure <= 0.0) { // TODO: May have to come back to this, was originally 0.0
                this.boilingPoint = 0.0;
            } else {
                this.boilingPoint = boilingPoint();
            }

            iterateSurfaceTemperature();

            if (this.maxTemperature >= FREEZING_POINT_OF_WATER && this.minTemperature <= this.boilingPoint) {
                calculateGases();
            }

            if (this.surfacePressure < 1.0) {
                if (!this.isMoon && massInEarthMasses() < ASTEROID_MASS_LIMIT) {
                    this.type = PlanetTypeEnum.tAsteroids;
                } else {
                    this.type = PlanetTypeEnum.tRock;
                }
            } else if (this.surfacePressure > 6000.0 && this.minimumMolecularWeight <= 2.0) { // Retains Hydrogen
                // This might qualify as a Super-Earth since they tend to have rock densities.
                if (this.density < 2.0) {
                    this.type = PlanetTypeEnum.tSubSubGasGiant;
                    this.atmosphere.clear();
                } else {
                    if (this.atmosphere.size() > 0) {
                        this.type = PlanetTypeEnum.tSuperEarth;
                    } else {
                        this.type = PlanetTypeEnum.tRock;
                    }
                }
            } else { // Atmospheres:
                if (secondsToHoursRounded(this.dayLength) == secondsToHoursRounded(this.orbitalPeriod)
                        || this.resonantPeriod) {
                    this.type = PlanetTypeEnum.tTidallyLocked;
                } else if (this.hydrosphere >= 0.95) {
                    this.type = PlanetTypeEnum.tWater; // >95% water
                } else if (this.iceCover >= 0.95) {
                    this.type = PlanetTypeEnum.tIce; // >95% ice
                } else if (this.hydrosphere > 0.05) {
                    this.type = PlanetTypeEnum.tTerrestrial; // Terrestrial
                    // else <5% water
                } else if (this.maxTemperature > this.boilingPoint) {
                    this.type = PlanetTypeEnum.tVenusian; // Hot = Venusian
                } else if (this.gasMass / this.mass > 0.0001) { // Accreted gas
                    this.type = PlanetTypeEnum.tIce; // But no Greenhouse
                    this.iceCover = 1.0; // or liquid water
                } else if (surfacePressure <= 250.0) { // Thin air = Martian
                    this.type = PlanetTypeEnum.tMartian;
                } else if (this.surfaceTemperature < FREEZING_POINT_OF_WATER) {
                    this.type = PlanetTypeEnum.tIce;
                } else {
                    this.type = PlanetTypeEnum.tUnknown;
                }
            }
        }

        if (doMoons && !this.isMoon) {
            for (Planet p : this.moons) {
                if (p.massInEarthMasses() > 0.000001) {
                    p.sma = this.sma;
                    p.eccentricity = this.eccentricity;
                    p.finalize(false);

                    double rocheLimit = 2.44 * this.radius * pow(this.density / p.density, 1.0 / 3.0);
                    double hillSphere = this.sma * KM_PER_AU * pow(this.mass / (3.0 * this.primary.mass), 1.0 / 3.0);

                    if (rocheLimit * 3.0 < hillSphere) {
                        p.asMoonSMA = Utils.instance().randomNumber(rocheLimit * 1.5, hillSphere / 2.0) / KM_PER_AU;
                        p.asMoonEccentricty = Utils.instance().randomEccentricity();
                    }
                    if (p.habitable) {
                        this.habitableMoon = true;
                    }
                } else {
                    // TODO: here we have a moon that isn't a moon?
                    throw new IllegalArgumentException("Planet exists as part of a moon list but isn't a moon!");
                }
            }
        }

        // ensure we don't have a mislabeled planet.
        if (this.type != PlanetTypeEnum.tSubSubGasGiant
                && this.type != PlanetTypeEnum.tSubGasGiant
                && this.type != PlanetTypeEnum.tGasGiant) {
            this.gasGiant = false;
        } else {
            this.gasGiant = true;
            return; // no need to continue
        }

        // check for those golden planets
        if (breathableAtmosphere() == AtmosphereTypeEnum.BREATHABLE) {
            if (!(this.resonantPeriod
                    || secondsToHoursRounded(this.dayLength) == secondsToHoursRounded(this.orbitalPeriod))) {
                // TODO: Investigate habitable tidally locked planets.
                this.habitable = true;

                double relTemp = this.surfaceTemperature - FREEZING_POINT_OF_WATER - EARTH_AVERAGE_CELSIUS;
                double pressure = this.surfacePressure / EARTH_SURF_PRES_IN_MILLIBARS;

                if (this.surfaceGravity >= 0.8
                        && this.surfaceGravity <= 1.2
                        && relTemp >= -2.0
                        && relTemp <= 3.0
                        && this.iceCover <= 0.1
                        && pressure >= 0.5
                        && pressure <= 2.0
                        && this.cloudCover >= 0.4
                        && this.cloudCover <= 0.8
                        && this.hydrosphere >= 0.5
                        && this.hydrosphere <= 0.8
                        && this.type != PlanetTypeEnum.tWater) {
                    this.earthlike = true;
                }
            }
        }
    }

    public void calculateGases() {
        if (this.surfacePressure > 0.0) {
            Chemical[] chems = Utils.instance().getChemicals();
            double[] amount = new double[chems.length];
            double totalAmount = 0.0;
            double pressure = this.surfacePressure / 1000.0; // convert from millibars to bars

            for (int i = 0; i < chems.length; i++) {
                double yp = chems[i].getBoilingPoint() / (373.0 * ((log(pressure + 0.001) / -5050.5) + (1.0 / 373.0)));

                if ((yp >= 0.0
                        && yp < this.lowTemperature)
                        && chems[i].getWeight() >= this.minimumMolecularWeight) {

                    double vrms = rmsVelocity(chems[i].getWeight());
                    // This should still have the correct ratio when changed to m/s.
                    double pvrms = pow(1.0 / (1.0 + vrms / this.escapeVelocity), this.primary.age / 1e9);
                    double abund = chems[i].getAbundanceS();
                    double react;
                    double fract;
                    double pres2;

                    if (chems[i].getAtomicNumber() == 18) {
                        react = .15 * this.primary.age / 4e9;
                    } else if (chems[i].getAtomicNumber() == 2) {
                        abund = abund * (0.001 + (this.gasMass / this.mass));
                        pres2 = (0.75 + pressure);
                        react = pow(1.0 / (1.0 + chems[i].getReactivity()), this.primary.age / 2e9 * pres2);
                    } else if ((chems[i].getAtomicNumber() == 8 || chems[i].getAtomicNumber() == 912)
                            && this.primary.age > 2e9
                            && this.surfaceTemperature > 270.0
                            && this.surfaceTemperature < 400.0) {
                        /*	pres2 = (0.65 + pressure/2); Breathable - M: .55-1.4 */
                        pres2 = (0.89 + pressure / 4); /*	Breathable - M: .6 -1.8 */
                        react = pow(1.0 / (1.0 + chems[i].getReactivity()), pow(this.primary.age / 2e9, 0.25) * pres2);
                    } else if (chems[i].getAtomicNumber() == 902
                            && this.primary.age > 2e9
                            && this.surfaceTemperature > 270
                            && this.surfaceTemperature < 400) {
                        pres2 = (0.75 + pressure);
                        react = pow(1.0 / (1.0 + chems[i].getReactivity()), pow(this.primary.age / 2e9, 0.5) * pres2);
                        react *= 1.5;
                    } else {
                        pres2 = (0.75 + pressure);
                        react = pow(1.0 / (1.0 + chems[i].getReactivity()), this.primary.age / 2e9 * pres2);
                    }

                    fract = 1.0 - (this.minimumMolecularWeight / chems[i].getWeight());
                    amount[i] = abund * pvrms * react * fract;
//                    java.lang.System.out.println("Found chemical " + chems[i].symbol + " with a yp = " + yp + " a low temp of " + this.lowTemperature + "  and a min molar mass of " + this.minimumMolecularWeight + " with amount " + amount[i]);

                    totalAmount += amount[i];
                } else {
                    amount[i] = 0.0;
                }
            }

            if (totalAmount > 0.0) {
                for (int i = 0; i < chems.length; i++) {
                    if (amount[i] > 0.0) {
                        this.atmosphere.add(new AtmosphericChemical(chems[i], this.surfacePressure * amount[i] / totalAmount));
                        // Logger.instance().log(Logger.level.systemOut, String.format("%1$,.2f", massInEarthMasses())
                        // + "em, added: " + chems[i].symbol + " - " + String.format("%1$,.4f", amount[i]) + " "
                        // + String.format("%1$,.4f", amount[i] / totalAmount) + "%");
                    }
                }

                // TODO: Do some sorting on the resulting vector ?
                // qsort(this.atmosphere, this.gases, sizeof(gas), diminishing_pressure);
            }
        }
    }

    public double radiusInMeters() {
        return this.radius * 1000.0;
    }

    public double ratioRadiusToEarth() {
        return EARTH_RADIUS / radiusInMeters();
    }

    public int numberOfMoons() {
        return this.moons.size();
    }

    /**
     * @param luminosity The stellar luminosity of the primary.
     * @return The "orbital zone" of this planet.
     */
    public int orbitalZone(double luminosity) {
        // TODO: Does this have anything to do with the frost line?
        if (this.sma < 4.0 * sqrt(luminosity)) {
            return 1;
        }
        if (this.sma < 15.0 * sqrt(luminosity)) {
            return 2;
        }
        return 3;
    }

    /**
     * @return The radius of this planet in km
     */
    public double volumeRadius() {
        return pow(((massInGrams() / this.density) / PI) * (3.0 / 4.0), 1.0 / 3.0) / CM_PER_KM;
        // return Math.pow(3.0 * (massInGrams() / this.density) / (4.0 * Math.PI), 1.0 / 3.0) / CM_PER_KM;
    }

    /**
     * This formula is listed as eq.9 in Fogg's article, although some typos crop up in that eq.
     * See "The Internal Constitution of Planets", by Dr. D. S. Kothari, Mon. Not. of the Royal
     * Astronomical Society, vol 96 pp.833-843, 1936 for the derivation.  Specifically, this is
     * Kothari's eq.23, which appears on page 840.
     *
     * @return The radius of the planet in kilometers.
     */
    public double kothariRadius() {
        double temp1, temp, temp2, atomicWeight, atomicNumber;
        double A1_20 = 6.485E12,
                A2_20 = 4.0032E-8,
                BETA_20 = 5.71E12;

        if (this.orbitalZone == 1) {
            if (this.gasGiant) {
                atomicWeight = 9.5;
                atomicNumber = 4.5;
            } else {
                atomicWeight = 15.0;
                atomicNumber = 8.0;
            }
        } else if (this.orbitalZone == 2) {
            if (this.gasGiant) {
                atomicWeight = 2.47;
                atomicNumber = 2.0;
            } else {
                atomicWeight = 10.0;
                atomicNumber = 5.0;
            }
        } else {
            if (this.gasGiant) {
                atomicWeight = 7.0;
                atomicNumber = 4.0;
            } else {
                atomicWeight = 10.0;
                atomicNumber = 5.0;
            }
        }

        temp1 = atomicWeight * atomicNumber;

        temp = (2.0 * BETA_20 * pow(SUN_MASS_IN_GRAMS, 1.0 / 3.0)) / (A1_20 * pow(temp1, 1.0 / 3.0));
        temp2 = A2_20 * pow(atomicWeight, 4.0 / 3.0) * pow(SUN_MASS_IN_GRAMS, 2.0 / 3.0);
        temp2 = temp2 * pow(this.mass, 2.0 / 3.0);
        temp2 = temp2 / (A1_20 * pow(atomicNumber, 2.0));
        temp2 = 1.0 + temp2;
        temp = temp / temp2;
        temp = (temp * pow(this.mass, (1.0 / 3.0))) / CM_PER_KM;

        temp /= 1.004; // Make Earth = actual earth, called "JIMS_FUDGE" in the original code.

        return temp;
    }

    /**
     * @param radiusEcosphere SMA of the ecosphere in AU.
     * @return the empirical density of the planet in g/cc
     */
    public double empiricalDensity(double radiusEcosphere) {
        double temp;

        temp = pow(massInEarthMasses(), 1.0 / 8.0);
        temp = temp * sqrt(sqrt(radiusEcosphere / this.sma));
        if (this.gasGiant) {
            return temp * 1.2;
        } else {
            return temp * 5.5;
        }
    }

    /**
     * @return The volume density based on assumption the planet is a perfect sphere, in g/cc
     */
    public double volumeDensity() {
        return massInGrams() / ((4.0 * PI * pow(this.radius * CM_PER_KM, 3.0)) / 3.0);
    }

    /**
     * @param largerBody The larger body this planet orbits around
     * @return The orbital period of this planet in seconds.
     */
    public double orbitalPeriod(SystemObject largerBody) {
        return sqrt(pow(smaInMeters(), 3.0) / largerBody.mu()) * PI * 2.0;
    }

    /**
     * Fogg's information for this routine came from Dole "Habitable Planets for Man", Blaisdell
     * Publishing Company, NY, 1964. From this, he came up with his eq.12, which is the equation
     * for the 'base_angular_velocity' below. He then used an equation for the change in angular
     * velocity per time (dw/dt) from P. Goldreich and S. Soter's paper "Q in the Solar System" in
     * Icarus, vol 5, pp.375-389 (1966). Using as a comparison the change in angular velocity
     * for the Earth, Fogg has come up with an approximation for our new planet (his eq.13) and
     * take that into account. This is used to find 'changeInAngularVelocity' below.
     *
     * @return The length of this planet's rotation in seconds.
     */
    public double dayLength() {
        final double
                J = 1.46E-19, // Used in day-length calcs (cm2/sec2 g)
                CHANGE_IN_EARTH_ANGULAR_VELOCITY = -1.3E-15; // Units of radians/sec/year

        double k2,
                baseAngularVelocity,
                changeInAngularVelocity,
                angularVelocity,
                spinResonanceFactor,
                dayInSeconds;
        boolean stopped = false;

        if (this.gasGiant) {
            k2 = 0.24;
        } else {
            k2 = 0.33;
        }

        baseAngularVelocity = sqrt(2.0 * J * massInGrams() / (k2 * pow(radius * CM_PER_KM, 2.0)));

        // This next calculation determines how much the planet's rotation is slowed by the presence of the star.
        changeInAngularVelocity = CHANGE_IN_EARTH_ANGULAR_VELOCITY * (this.density / EARTH_DENSITY) * (radiusInMeters()
                / EARTH_RADIUS) * (1.0 / massInEarthMasses()) * pow(this.primary.mass, 2.0) * (1.0 / pow(this.sma, 6.0));
        angularVelocity = baseAngularVelocity + (changeInAngularVelocity * this.primary.age);
        if (angularVelocity <= 0.0) {
            stopped = true;
            dayInSeconds = Double.MAX_VALUE;
        } else {
            dayInSeconds = 2.0 * PI * angularVelocity;
        }

        // tidal or resonant locking?
        if (dayInSeconds >= this.orbitalPeriod || stopped) {
            if (this.eccentricity > 0.1) {
                spinResonanceFactor = (1.0 - this.eccentricity) / (1.0 + this.eccentricity);
                this.resonantPeriod = true;
                return spinResonanceFactor * this.orbitalPeriod;
            } else {
                return this.orbitalPeriod;
            }
        }

        return dayInSeconds;
    }

    /**
     * Randomly generates an axial tilt for this planet
     *
     * @return The axial tilt in degrees.
     */
    public double axialTilt() {
        final double EARTH_AXIAL_TILT = 23.5;
        double temp;

        temp = pow(this.sma, 0.2) * Utils.instance().about(EARTH_AXIAL_TILT, 0.4);
        return temp % 360.0;
    }

    /**
     * This function implements the escape velocity calculation. Note that it appears that Fogg's
     * eq.15 is incorrect.
     *
     * @return The escape velocity in m/s.
     */
    public double escapeVelocity() {
        return sqrt((2.0 * mu()) / radiusInMeters());
    }

    /**
     * This is Fogg's eq.16.  The molecular weight (usually assumed to be N2) is used as the basis
     * of the Root Mean Square (RMS) velocity of the molecule or atom.
     *
     * @param molecularWeight x
     * @return The RMS Velocity of the molecule in m/s
     */
    public double rmsVelocity(double molecularWeight) {
        return sqrt((3.0 * MOLAR_GAS_CONST * this.exosphericTemperature) / (molecularWeight / 1000.0)); // convert to kg/mol
    }

    /**
     * This function returns the smallest molecular weight retained by the body, which is useful
     * for determining the atmosphere composition.
     *
     * @return x
     */
    public double molecularLimit() {
        // The molar gas constant in the original was in grams rather than kilograms, hence the multiplication by 3000 instead of 3.
        double muResult = pow(escapeVelocity() / GAS_RETENTION_THRESHOLD, 2.0);
        return (3000.0 * MOLAR_GAS_CONST * this.exosphericTemperature) / muResult;
    }

    /**
     * This function calculates the surface acceleration of a planet.
     *
     * @return The surface acceleration in m/s.
     */
    public double gravitationalAcceleration() {
        return mu() / pow(radiusInMeters(), 2.0);
    }

    /**
     * @return The gravity of this planet in Earth gravities.
     */
    public double gravity() {
        return gravitationalAcceleration() / EARTH_ACCELERATION;
    }

    /**
     * This implements Fogg's eq.17.  The 'inventory' returned is unitless.
     *
     * @return The unitless "inventory".
     */
    public double volatileGasInventory() {
        double proportionalConst, temp;

        double velocityRatio = this.escapeVelocity / this.rmsVelocity;
        if (velocityRatio >= GAS_RETENTION_THRESHOLD) {
            // printf("Error: orbital zone not initialized correctly!\n");
            proportionalConst = switch (this.orbitalZone) {
                // 100 -> 140 JLB
                case 1 -> 140000.0;
                case 2 -> 75000.0;
                case 3 -> 250.0;
                default -> 0.0;
            };
            temp = (proportionalConst * massInEarthMasses()) / this.primary.mass;
            /* TODO: I'm sure the original author had something in mind here...
            temp2 = Utils.instance().about(temp1, 0.2);
            temp2 = temp; // what? */
            if (this.greenhouseEffect || this.gasMass / this.mass > 0.000001) {
                return temp;
            } else {
                return temp / 140.0; // 100 -> 140 JLB
            }
        } else {
            return 0.0;
        }
    }

    /**
     * This implements Fogg's eq.18. JLB: Aparently this assumed that earth pressure = 1000mb. I've
     * added a fudge factor (EARTH_SURF_PRES_IN_MILLIBARS / 1000.) to correct for that.
     *
     * @return Pressure in millibars.
     */
    public double pressure() {
        return this.volatileGasInventory * this.surfaceGravity
                * (EARTH_SURF_PRES_IN_MILLIBARS / 1000.0) / pow(ratioRadiusToEarth(), 2.0);
    }

    /**
     * This function returns the boiling point of water in this planet's atmosphere. This is Fogg's
     * eq.21.
     *
     * @return The boiling point in units of Kelvin.
     */
    public double boilingPoint() {
        return 1.0 / ((log(this.surfacePressure / 1000.0) / -5050.5) + (1.0 / 373.0));
    }

    /**
     * This function is Fogg's eq.22. Given the volatile gas inventory and planetary radius of a
     * planet (in Km), this function returns the fraction of the planet covered with water. I have
     * changed the function very slightly:	 the fraction of Earth's surface covered by water is
     * 71%, not 75% as Fogg used.
     *
     * @return The fraction of the planet covered by water.
     */
    public double waterCoverage() {
        double temp;

        temp = (0.708 * this.volatileGasInventory / 1000.0) * pow(ratioRadiusToEarth(), 2.0);
        return min(temp, 1.0);
    }

    /**
     * This function returns the fraction of cloud cover available. This is Fogg's eq.23. See Hart
     * in "Icarus" (vol 33, pp23 - 39, 1978) for an explanation. This equation is Hart's eq.3. I
     * have modified it slightly using constants and relationships from Glass's book "Introduction
     * to Planetary Geology", p.46. The 'CLOUD_COVERAGE_FACTOR' is the amount of surface area on
     * Earth covered by one Kg. of cloud.
     *
     * @return The cloud coverage fraction available.
     */
    public double cloudFraction() {
        double waterVaporKg, fraction, surfaceArea, hydroMass;
        final double
                Q2_36 = 0.0698, // 1/Kelvin
                CLOUD_COVERAGE_FACTOR = 1.839E-8; // Km2/kg

        if (this.minimumMolecularWeight > WATER_VAPOR) {
            return 0.0;
        } else {
            surfaceArea = 4.0 * PI * pow(this.radius, 2.0);
            hydroMass = this.hydrosphere * surfaceArea * EARTH_WATER_MASS_PER_AREA;
            waterVaporKg = (0.00000001 * hydroMass) * exp(Q2_36 * (this.surfaceTemperature - EARTH_AVERAGE_KELVIN));
            fraction = CLOUD_COVERAGE_FACTOR * waterVaporKg / surfaceArea;
            return min(fraction, 1.0);
        }
    }

    /**
     * This is Fogg's eq.24. See Hart[24] in Icarus vol.33, p.28 for an explanation. I have changed
     * a constant from 70 to 90 in order to bring it more in line with the fraction of the Earth's
     * surface covered with ice, which is approximatly .016 (=1.6%).
     *
     * @return The fraction of the planet's surface covered by ice.
     */
    public double iceFraction() {
        double temp, surfTemp;
        surfTemp = this.surfaceTemperature;

        if (surfTemp > 328.0) {
            surfTemp = 328.0;
        }
        temp = pow((328.0 - surfTemp) / 90.0, 5.0);
        if (temp > 1.5 * this.hydrosphere) {
            temp = 1.5 * this.hydrosphere;
        }
        return min(temp, 1.0);
    }

    /**
     * This is Fogg's eq.19.
     *
     * @return The temperature in Kelvin.
     */
    public double effectiveTemperature(double givenAlbedo) {
        final double EARTH_EFFECTIVE_TEMP = 250.0; // Units of degrees Kelvin (was 255)
        return sqrt(this.primary.radiusEcosphere / this.sma) * sqrt(sqrt((1.0 - givenAlbedo) / (1.0 - EARTH_ALBEDO))) * EARTH_EFFECTIVE_TEMP;
    }

    /**
     * @return The temperature in Kelvin.
     */
    public double estimatedTemperature() {
        return sqrt(this.primary.radiusEcosphere / this.sma) * sqrt(sqrt((1.0 - this.albedo) / (1.0 - EARTH_ALBEDO))) * EARTH_AVERAGE_KELVIN;
    }

    /**
     * @return The temperature in Kelvin.
     */
    public double estimatedTerrestrialTemperature() {
        return sqrt(this.primary.radiusEcosphere / this.sma) * EARTH_AVERAGE_KELVIN;
    }

    /**
     * Old greenhouse: Note that if the orbital radius of the planet is greater than or equal to
     * R_inner, 99% of it's volatiles are assumed to have been deposited in surface reservoirs
     * (otherwise, it suffers from the greenhouse effect).
     * <p>
     * if((sma < r_greenhouse) && (orbitalZone == 1))
     * <p>
     * The new definition is based on the inital surface temperature and what state water is in. If
     * it's too hot, the water will never condense out of the atmosphere, rain down and form an
     * ocean. The albedo used here was chosen so that the boundary is about the same as the old
     * method Neither zone, nor r_greenhouse are used in this version - JLB
     *
     * @return Whether a greenhouse effect exists
     */
    boolean greenhouse() {
        return effectiveTemperature(GREENHOUSE_TRIGGER_ALBEDO) > FREEZING_POINT_OF_WATER;
    }

    /**
     * This is Fogg's eq.20, and is also Hart's eq.20 in his "Evolution of Earth's Atmosphere"
     * article. The effective temperature given is in units of Kelvin, as is the rise in
     * temperature produced by the greenhouse effect, which is returned. I tuned this by changing a
     * pow(x,.25) to pow(x,.4) to match Venus - JLB
     *
     * @return the rise in temperature produced by the greenhouse effect in Kelvin
     */
    public double greenhouseRise(double effectiveTemperature) {
        final double EARTH_CONVECTION_FACTOR = 0.43; // from Hart, eq.20

        double convection_factor = EARTH_CONVECTION_FACTOR * pow(this.surfacePressure / EARTH_SURF_PRES_IN_MILLIBARS, 0.4);
        double rise = (sqrt(sqrt(1.0 + 0.75 * opticalDepth())) - 1.0) * effectiveTemperature * convection_factor;

        if (rise < 0.0) rise = 0.0;

        return rise;
    }

    /**
     * The surface temperature passed in is in units of Kelvin. The cloud adjustment is the
     * fraction of cloud cover obscuring each of the three major components of albedo that lie
     * below the clouds.
     *
     * @return x
     */
    public double planetAlbedo() {
        double rockFraction, cloudAdjustment, components = 0.0, cloudPart, rockPart, waterPart, icePart;

        rockFraction = 1.0 - this.hydrosphere - this.iceCover;
        if (this.hydrosphere > 0.0) {
            components += 1.0;
        }
        if (this.iceCover > 0.0) {
            components += 1.0;
        }
        if (rockFraction > 0.0) {
            components += 1.0;
        }

        cloudAdjustment = this.cloudCover / components;

        if (rockFraction >= cloudAdjustment) {
            rockFraction -= cloudAdjustment;
        } else {
            rockFraction = 0.0;
        }

        if (this.hydrosphere > cloudAdjustment) {
            this.hydrosphere -= cloudAdjustment;
        } else {
            this.hydrosphere = 0.0;
        }

        if (this.iceCover > cloudAdjustment) {
            this.iceCover -= cloudAdjustment;
        } else {
            this.iceCover = 0.0;
        }

        cloudPart = this.cloudCover * CLOUD_ALBEDO;

        if (this.surfacePressure == 0.0) {
            rockPart = rockFraction * ROCKY_AIRLESS_ALBEDO;
            icePart = this.iceCover * AIRLESS_ICE_ALBEDO;
            waterPart = 0.0;
        } else {
            rockPart = rockFraction * ROCKY_ALBEDO;
            waterPart = this.hydrosphere * WATER_ALBEDO;
            icePart = this.iceCover * ICE_ALBEDO;
        }

        return cloudPart + rockPart + waterPart + icePart;
    }

    /**
     * This function returns the dimensionless quantity of optical depth, which is useful in determining
     * the amount of greenhouse effect on a planet.
     *
     * @return x
     */
    public double opticalDepth() {
        double opticalDepth;

        opticalDepth = 0.0;
        if (this.minimumMolecularWeight >= 0.0 && this.minimumMolecularWeight < 10.0) {
            opticalDepth += 3.0;
        }
        if (this.minimumMolecularWeight >= 10.0 && this.minimumMolecularWeight < 20.0) {
            opticalDepth += 2.34;
        }
        if (this.minimumMolecularWeight >= 20.0 && this.minimumMolecularWeight < 30.0) {
            opticalDepth += 1.0;
        }
        if (this.minimumMolecularWeight >= 30.0 && this.minimumMolecularWeight < 45.0) {
            opticalDepth += 0.15;
        }
        if (this.minimumMolecularWeight >= 45.0 && this.minimumMolecularWeight < 100.0) {
            opticalDepth += 0.05;
        }

        if (this.surfacePressure >= 70.0 * EARTH_SURF_PRES_IN_MILLIBARS) {
            opticalDepth = opticalDepth * 8.333;
        } else if (this.surfacePressure >= 50.0 * EARTH_SURF_PRES_IN_MILLIBARS) {
            opticalDepth = opticalDepth * 6.666;
        } else if (this.surfacePressure >= 30.0 * EARTH_SURF_PRES_IN_MILLIBARS) {
            opticalDepth = opticalDepth * 3.333;
        } else if (this.surfacePressure >= 10.0 * EARTH_SURF_PRES_IN_MILLIBARS) {
            opticalDepth = opticalDepth * 2.0;
        } else if (this.surfacePressure >= 5.0 * EARTH_SURF_PRES_IN_MILLIBARS) {
            opticalDepth = opticalDepth * 1.5;
        }

        return opticalDepth;
    }

    /**
     * calculates the number of years it takes for 1/e of a gas to escape from a planet's
     * atmosphere. Taken from Dole p. 34. He cites Jeans (1916) & Jones (1923)
     *
     * @param molecularWeight The molecular weight of the gas in question
     * @return x
     */
    public double gasLife(double molecularWeight) {
        double v = rmsVelocity(molecularWeight);
        double r = radiusInMeters();
        double t = (pow(v, 3.0) /
                (2.0 * pow(this.surfaceAcceleration, 2.0) * r))
                * exp((3.0 * this.surfaceAcceleration * r) / pow(v, 2.0));
        return t / (3600.0 * 24.0 * 365.256); // convert seconds to years
    }

    public double minimumMolecularWeight() {
        double
                target = this.primary.age,
                guess1 = molecularLimit(),
                guess2 = guess1,
                life = gasLife(guess1);

        int loops = 0;
        if (life > target) {
            while (life > target && loops++ < 25) {
                guess1 = guess1 / 2.0;
                life = gasLife(guess1);
            }
        } else {
            while (life < target && loops++ < 25) {
                guess2 = guess2 * 2.0;
                life = gasLife(guess2);
            }
        }

        loops = 0;
        while (guess2 - guess1 > 0.1 && loops++ < 25) {
            double guess3 = (guess1 + guess2) / 2.0;
            life = gasLife(guess3);

            if (life < target) {
                guess1 = guess3;
            } else {
                guess2 = guess3;
            }
        }

        return guess2;
    }


    /*--------------------------------------------------------------------------*/
    /*	 The temperature calculated is in degrees Kelvin.						*/
    /*	 Quantities already known which are used in these calculations:			*/
    /*		 planet->molec_weight												*/
    /*		 planet->surf_pressure												*/
    /*		 R_ecosphere														*/
    /*		 planet->a															*/
    /*		 planet->volatile_gas_inventory										*/
    /*		 planet->radius														*/
    /*		 planet->boil_point													*/
    /*--------------------------------------------------------------------------*/

    public void calculateSurfaceTemperature(boolean first,
                                            double lastWater,
                                            double lastClouds,
                                            double lastIce,
                                            double lastTemperature,
                                            double lastAlbedo) {
        double effectiveTemperature;
        double greenhouseTemperature;
        boolean boilOff = false;

        if (first) {
            this.albedo = EARTH_ALBEDO;
            effectiveTemperature = effectiveTemperature(this.albedo);
            greenhouseTemperature = greenhouseRise(effectiveTemperature);
            this.surfaceTemperature = effectiveTemperature + greenhouseTemperature;
            setTemperatureRange();
        }

        if (this.greenhouseEffect
                && this.maxTemperature < this.boilingPoint) {
            this.greenhouseEffect = false;
            this.volatileGasInventory = volatileGasInventory();
            this.surfacePressure = pressure();
            this.boilingPoint = boilingPoint();
        }

        this.hydrosphere = waterCoverage();
        this.cloudCover = cloudFraction();
        this.iceCover = iceFraction();

        if (this.greenhouseEffect
                && this.surfacePressure > 0.0) {
            this.cloudCover = 1.0;
        }

        if (this.highTemperature >= this.boilingPoint
                && !first
                && !(secondsToHoursRounded(this.dayLength) == secondsToHoursRounded(this.orbitalPeriod)
                || this.resonantPeriod)) {
            this.hydrosphere = 0.0;
            boilOff = true;

            if (this.minimumMolecularWeight > WATER_VAPOR) {
                this.cloudCover = 0.0;
            } else {
                this.cloudCover = 1.0;
            }
        }

        if (this.surfaceTemperature < FREEZING_POINT_OF_WATER - 3.0) {
            this.hydrosphere = 0.0;
        }

        this.albedo = planetAlbedo();
        effectiveTemperature = effectiveTemperature(this.albedo);
        greenhouseTemperature = greenhouseRise(effectiveTemperature);
        this.surfaceTemperature = effectiveTemperature + greenhouseTemperature;

        if (!first) {
            if (!boilOff) {
                this.hydrosphere = (this.hydrosphere + (lastWater * 2)) / 3;
            }
            this.cloudCover = (this.cloudCover + (lastClouds * 2)) / 3;
            this.iceCover = (this.iceCover + (lastIce * 2)) / 3;
            this.albedo = (this.albedo + (lastAlbedo * 2)) / 3;
            this.surfaceTemperature = (this.surfaceTemperature + (lastTemperature * 2)) / 3;
        }

        setTemperatureRange();
    }

    public void iterateSurfaceTemperature() {
        double initialTemperature = estimatedTemperature();

        calculateSurfaceTemperature(
                true, 0.0,
                0.0, 0.0, 0.0, 0.0
        );

        for (int count = 0; count <= 25; count++) {
            double lastWater = this.hydrosphere;
            double lastClouds = this.cloudCover;
            double lastIce = this.iceCover;
            double lastTemperature = this.surfaceTemperature;
            double lastAlbedo = this.albedo;

            calculateSurfaceTemperature(false, lastWater, lastClouds, lastIce, lastTemperature, lastAlbedo);

            if (abs(this.surfaceTemperature - lastTemperature) < 0.25) {
                break;
            }
        }

        this.greenhouseRise = this.surfaceTemperature - initialTemperature;
    }

    /**
     * Inspired partial pressure, taking into account humidification of the air in the nasal
     * passage and throat This formula is on Dole's p. 14
     *
     * @param gasPressure The ?surface pressure? of the gas in question
     * @return x
     */
    public double inspiredPartialPressure(double gasPressure) {
        double pH2O = 47.0 * MMHG_TO_MILLIBARS; // Dole p. 15
        double fraction = gasPressure / this.surfacePressure;

        return (this.surfacePressure - pH2O) * fraction;
    }

    /**
     * This function uses figures on the maximum inspired partial pressures of Oxygen, other
     * atmospheric and traces gases as laid out on pages 15, 16 and 18 of Dole's Habitable Planets
     * for Man to derive breathability of the planet's atmosphere. - JLB
     *
     * @return x
     */
    private AtmosphereTypeEnum breathableAtmosphere() {
        boolean oxygenOK = false;
        double
                MIN_O2_IPP = 72.0 * MMHG_TO_MILLIBARS, // Dole, p. 15
                MAX_O2_IPP = 400.0 * MMHG_TO_MILLIBARS; // Dole, p. 15

        if (this.atmosphere.size() == 0) {
            return AtmosphereTypeEnum.NONE;
        }

        for (AtmosphericChemical ac : this.atmosphere) {
            double ipp = inspiredPartialPressure(ac.getSurfacePressure());
            if (ipp > ac.getChem().getMaxInspiredPartialPressure()) {
                return AtmosphereTypeEnum.POISONOUS;
            }
            if (ac.getChem().getAtomicNumber() == 8) { // Check for oxygen
                oxygenOK = (ipp >= MIN_O2_IPP && ipp <= MAX_O2_IPP);
            }
        }

        if (oxygenOK) {
            return AtmosphereTypeEnum.BREATHABLE;
        } else {
            return AtmosphereTypeEnum.UNBREATHABLE;
        }
    }

    /**
     * Functions for 'soft limiting' temperatures
     *
     * @param x x
     * @return x
     */
    public double lim(double x) {
        return x / sqrt(sqrt(1.0 + x * x * x * x));
    }

    /**
     * Functions for 'soft limiting' temperatures
     *
     * @param v   x
     * @param max x
     * @param min x
     * @return x
     */
    public double soft(double v, double max, double min) {
        double dv = v - min;
        double dm = max - min;
        return (lim(2.0 * dv / dm - 1.0) + 1.0) / 2.0 * dm + min;
    }

    public void setTemperatureRange() {
        double pressmod = 1.0 / sqrt(1.0 + 20.0 * this.surfacePressure / 1000.0);
        double ppmod = 1.0 / sqrt(10.0 + 5.0 * this.surfacePressure / 1000.0);
        double tiltmod = abs(Math.cos(this.axialTilt * PI / 180.0) * pow(1.0 + this.eccentricity, 2.0));
        double daymod = 1.0 / (200.0 / this.dayLength + 1);
        double mh = pow(1.0 + daymod, pressmod);
        double ml = pow(1.0 - daymod, pressmod);
        double hi = mh * this.surfaceTemperature;
        double lo = ml * this.surfaceTemperature;
        double sh = hi + pow((100.0 + hi) * tiltmod, sqrt(ppmod));
        double wl = lo - pow((150.0 + lo) * tiltmod, sqrt(ppmod));
        double max = this.surfaceTemperature + sqrt(this.surfaceTemperature) * 10.0;
        double min = this.surfaceTemperature / sqrt(this.dayLength + 24.0);

        if (lo < min) {
            lo = min;
        }
        if (wl < 0.0) {
            wl = 0.0;
        }

        this.highTemperature = soft(hi, max, min);
        this.lowTemperature = soft(lo, max, min);
        this.maxTemperature = soft(sh, max, min);
        this.minTemperature = soft(wl, max, min);
    }


    public String planetType() {
        return switch (this.type) {
            case tSubSubGasGiant -> "semi Gas Giant";
            case tSubGasGiant -> "sub Gas Giant";
            case tGasGiant -> "Gas Giant";
            case tSuperEarth -> "Super Earth";
            case tRock -> "Rock";
            case tVenusian -> "Venusian";
            case tTerrestrial -> "Terrestrial";
            case tMartian -> "Martian";
            case tWater -> "Water";
            case tIce -> "Ice";
            case tAsteroids -> "Asteroids";
            case tTidallyLocked -> "Tidally Locked";
            default -> "Unknown";
        };
    }

    public String atmosphereType() {
        return switch (breathableAtmosphere()) {
            case BREATHABLE -> "Breathable";
            case UNBREATHABLE -> "Unbreathable";
            case POISONOUS -> "Poisonous";
            default -> "None";
        };
    }

    public String datasheet() {
        return datasheet("");
    }

    public String datasheet(String prepend) {
        String cr = System.lineSeparator();
        StringBuilder retval;
        if (this.isMoon) {
            retval = new StringBuilder(cr + prepend + "Moon: " + planetType());
        } else {
            retval = new StringBuilder(prepend + planetType());
        }
        if (!this.gasGiant) {
            if (this.habitable) {
                if (this.earthlike) {
                    retval.append(" (earthlike)");
                } else {
                    retval.append(" (habitable)");
                }
            } else {
                retval.append(" (")
                        .append(atmosphereType())
                        .append(")");
            }
        }
        if (this.resonantPeriod) {
            retval.append(" - tidally locked or resonant period.");
        }
        retval.append(cr).append(prepend)
                .append("  Mass: ")
                .append(this.gasGiant ? String.format("%1$,.2f", massInJupiterMasses()) + " jm, " : "")
                .append(String.format("%1$,.2f", massInEarthMasses()))
                .append(" em, ")
                .append(String.format("%.3E", massInKg()))
                .append(" kg, density - ")
                .append(String.format("%1$,.2f", this.density))
                .append(" g/cc, radius - ")
                .append(String.format("%1$,.2f", radiusInMeters() / 1000.0))
                .append("km");
        if (this.isMoon) {
            retval.append(cr)
                    .append(prepend)
                    .append("  Orbit: SMA - ")
                    .append(String.format("%.3E", AUtoKm(this.asMoonSMA)))
                    .append(" km, eccentricity - ")
                    .append(String.format("%1$,.3f", this.asMoonEccentricty))
                    .append(", apoapsis: ")
                    .append(String.format("%.3E", AUtoKm(asMoonApoapsis())))
                    .append(" km, periapsis: ")
                    .append(String.format("%.3E", AUtoKm(asMoonPeriapsis())))
                    .append(" km");
        } else {
            retval.append(cr)
                    .append(prepend)
                    .append("  Orbit: SMA - ")
                    .append(String.format("%1$,.2f", this.sma))
                    .append(" AU, eccentricity - ")
                    .append(String.format("%1$,.3f", this.eccentricity))
                    .append(", apoapsis: ")
                    .append(String.format("%1$,.4f", apoapsis()))
                    .append(" AU, periapsis: ")
                    .append(String.format("%1$,.2f", periapsis()))
                    .append(" AU");
        }
        // TODO: Fix the following values for moons so that they correctly use their moon SMA and eccentricity
        retval.append(cr)
                .append(prepend)
                .append("  Axial Tilt: ")
                .append(String.format("%1$,.2f", this.axialTilt))
                .append(", Day: ")
                .append(String.format("%1$,.2f", secondsToHours(this.dayLength)))
                .append(" hours, Year: ")
                .append(String.format("%1$,.2f", secondsToYears(this.orbitalPeriod)))
                .append(" Earth years");
        if (!this.gasGiant) {
            retval.append(cr).append(prepend)
                    .append("  Surface: Gravity - ")
                    .append(String.format("%1$,.2f", this.surfaceGravity))
                    .append(", Temperature - ")
                    .append(String.format("%1$,.2f", this.surfaceTemperature - 273.15))
                    .append("C (")
                    .append(String.format("%1$,.2f", this.surfaceTemperature * (9.0 / 5.0) - 459.67))
                    .append("F)");
            retval.append(cr)
                    .append(prepend).append("  Water: ")
                    .append(String.format("%1$,.2f", this.hydrosphere * 100.0))
                    .append("%, Cloud Cover: ")
                    .append(String.format("%1$,.2f", this.cloudCover * 100.0))
                    .append("%, Ice Cover: ")
                    .append(String.format("%1$,.2f", this.iceCover * 100.0))
                    .append("%");
            retval.append(cr)
                    .append(prepend)
                    .append("  Min Temperature - ")
                    .append(String.format("%1$,.2f", this.minTemperature - 273.15))
                    .append("C (")
                    .append(String.format("%1$,.2f", this.minTemperature * (9.0 / 5.0) - 459.67))
                    .append("F), Low Temperature - ")
                    .append(String.format("%1$,.2f", this.lowTemperature - 273.15))
                    .append("C (")
                    .append(String.format("%1$,.2f", this.lowTemperature * (9.0 / 5.0) - 459.67))
                    .append("F), High Temperature - ")
                    .append(String.format("%1$,.2f", this.highTemperature - 273.15))
                    .append("C (")
                    .append(String.format("%1$,.2f", this.highTemperature * (9.0 / 5.0) - 459.67))
                    .append("F), Max Temperature - ")
                    .append(String.format("%1$,.2f", this.maxTemperature - 273.15))
                    .append("C (")
                    .append(String.format("%1$,.2f", this.maxTemperature * (9.0 / 5.0) - 459.67))
                    .append("F)");
            double yp = (373.0 * ((log(this.surfacePressure / 1000.0 + 0.001) / -5050.5) + (1.0 / 373.0)));
            retval.append(cr)
                    .append(prepend)
                    .append("  Atmosphere: Pressure - ")
                    .append(String.format("%1$,.2f", this.surfacePressure / 1000.0))
                    .append(" bar, Minimum Molecular Weight - ")
                    .append(String.format("%1$,.3f", this.minimumMolecularWeight))
                    .append(", yp - ")
                    .append(String.format("%1$,.3f", yp));
        }
        if (this.atmosphere != null) {
            if (this.atmosphere.size() > 0) {
                retval.append(cr)
                        .append(prepend)
                        .append("  Atmospheric Constituents:");
                AtmosphericChemical ac;
                for (AtmosphericChemical atmosphericChemical : atmosphere) {
                    ac = atmosphericChemical;
                    retval.append(cr).append(prepend)
                            .append("    ")
                            .append(ac.getChem().getSymbol())
                            .append(" (")
                            .append(ac.getChem().getName())
                            .append(") ")
                            .append(String.format("%1$,.4f", ac.getSurfacePressure() / 1000.0))
                            .append(" bar");
                }
            }
        }
        // cap it off for the next planet
        retval.append(cr);
        return retval.toString();
    }

    public String toString() {
        StringBuilder retval = Optional.ofNullable(datasheet()).map(StringBuilder::new).orElse(null);
        for (Planet moon : this.moons) {
            retval = (retval == null ? new StringBuilder("null") : retval).append(moon.datasheet("    "));
        }
        return retval == null ? null : retval.toString();
    }
}
