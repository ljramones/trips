package com.teamgannon.trips.solarsysmodelling.accrete;

public abstract class SystemObject {
    public final static double G = 6.67408E-11;
    public final static double SUN_MASS = 1.989E30; // Mass of the Sun in kg
    public final static double SUN_MASS_IN_GRAMS = 1.989E33; // Mass of the Sun on g
    public final static double SUN_RADIUS = 6.96392E8; // radius of the Sun in m
    public final static double SUN_MASS_IN_EARTH_MASSES = 333054.25;
    public final static double JUPITER_MASS = 1.8982E27; // Mass of Jupiter in kg
    public final static double JUPITER_RADIUS = 6.9911E7; // radius of Jupiter in m
    public final static double EARTH_MASS = 5.97237E24; // mass of Earth in kg
    public final static double EARTH_RADIUS = 6.371E6; // radius of Earth in m
    public final static double EARTH_SMA = 149597870700.0; // for calculation of an astronomical unit
    public final static double KM_PER_AU = 149597870.7; // Both of these are set to the metric SI unit for AU
    public final static double CM_PER_KM = 100000.0;
    public final static double CM_PER_METER = 100.0;
    // MOLAR_GAS_CONST = 8314.41, // units: g*m2/(sec2*K*mol)
    public final static double MOLAR_GAS_CONST = 8.3144621; // units of J/molK
    public final static double GAS_RETENTION_THRESHOLD = 6.0;
    public final static double EARTH_ACCELERATION = 9.80655; // m/s
    public final static double EARTH_DENSITY = 5.514; // g/cc
    public final static double FREEZING_POINT_OF_WATER = 273.15; // Units of degrees Kelvin
    public final static double EARTH_AVERAGE_CELSIUS = 14.0; // Average Earth Temperature
    public final static double EARTH_AVERAGE_KELVIN = EARTH_AVERAGE_CELSIUS + FREEZING_POINT_OF_WATER;
    public final static double EARTH_WATER_MASS_PER_AREA = 3.83E15; // grams per square km
    public final static double EARTH_SURF_PRES_IN_MILLIBARS = 1013.25;
    public final static double EARTH_EXOSPHERIC_TEMPERATURE = 1273.0; // in Kelvin
    public final static double MMHG_TO_MILLIBARS = 1.33322;
    public final static double ASTEROID_MASS_LIMIT = 0.001;

    // Molecular weights seperated for because.
    public final static double ATOMIC_HYDROGEN = 1.0;
    public final static double MOLECULAR_HYDROGEN = 2.0;
    public final static double HELIUM = 4.0;
    public final static double ATOMIC_NITROGEN = 14.0;
    public final static double ATOMIC_OXYGEN = 16.0;
    public final static double METHANE = 16.0;
    public final static double AMMONIA = 17.0;
    public final static double WATER_VAPOR = 18.0;
    public final static double NEON = 20.2;
    public final static double MOLECULAR_NITROGEN = 28.0;
    public final static double CARBON_MONOXIDE = 28.0;
    public final static double NITRIC_OXIDE = 30.0;
    public final static double MOLECULAR_OXYGEN = 32.0;
    public final static double HYDROGEN_SULFIDE = 34.1;
    public final static double ARGON = 39.9;
    public final static double CARBON_DIOXIDE = 44.0;
    public final static double NITROUS_OXIDE = 44.0;
    public final static double NITROGEN_DIOXIDE = 46.0;
    public final static double OZONE = 48.0;
    public final static double SULFUR_DIOXIDE = 64.1;
    public final static double SULFUR_TRIOXIDE = 80.1;
    public final static double KRYPTON = 83.8;
    public final static double XENON = 131.3;

    // Albedos
    public final double ICE_ALBEDO = 0.7;
    public final double CLOUD_ALBEDO = 0.52;
    public final double GAS_GIANT_ALBEDO = 0.5;
    public final double AIRLESS_ICE_ALBEDO = 0.5;
    public final double EARTH_ALBEDO = 0.3;
    public final double GREENHOUSE_TRIGGER_ALBEDO = 0.20;
    public final double ROCKY_ALBEDO = 0.15;
    public final double ROCKY_AIRLESS_ALBEDO = 0.07;
    public final double WATER_ALBEDO = 0.04;

    protected double mass; // This is in solar masses, use a conversion method if needed.
    protected double sma;
    protected double asMoonSMA = 0.0;
    protected double asMoonEccentricty = 0.0;
    protected double eccentricity;
    protected double inclination;

    public static double massInKg(double mass) {
        return mass * SUN_MASS;
    }

    public static double massInGrams(double mass) {
        return mass * SUN_MASS_IN_GRAMS;
    }

    public static double massInJupiterMasses(double mass) {
        return massInKg(mass) / JUPITER_MASS;
    }

    public static double massInEarthMasses(double mass) {
        return massInKg(mass) / EARTH_MASS;
    }

    /**
     * @return The orbital apoapsis
     */
    public static double apoapsis(double sma, double ecc) {
        return sma * (1.0 + ecc);
    }

    /**
     * @return The orbital periapsis
     */
    public static double periapsis(double sma, double ecc) {
        return sma * (1.0 - ecc);
    }

    public static double smaInMeters(double sma) {
        return sma * EARTH_SMA;
    }

    public static double AUtoKm(double au) {
        return au * KM_PER_AU;
    }

    /**
     * @return The Standard Gravitational Parameter of this object.
     */
    public double mu() {
        return massInKg() * G;
    }

    public double massInKg() {
        return massInKg(this.mass);
    }

    public double massInGrams() {
        return massInGrams(this.mass);
    }

    public double massInJupiterMasses() {
        return massInJupiterMasses(this.mass);
    }

    public double massInEarthMasses() {
        return massInEarthMasses(this.mass);
    }

    /**
     * @return The orbital apoapsis
     */
    public double apoapsis() {
        return apoapsis(this.sma, this.eccentricity);
    }

    /**
     * @return The orbital periapsis
     */
    public double periapsis() {
        return periapsis(this.sma, this.eccentricity);
    }

    /**
     * @return The orbital apoapsis
     */
    public double asMoonApoapsis() {
        return apoapsis(this.asMoonSMA, this.asMoonEccentricty);
    }

    /**
     * @return The orbital periapsis
     */
    public double asMoonPeriapsis() {
        return periapsis(this.asMoonSMA, this.asMoonEccentricty);
    }

    public double smaInMeters() {
        return smaInMeters(this.sma);
    }

    /**
     * Returns the period of this orbit given the gravitational parameter of the central body.
     *
     * @param mu The Standard Gravitational Parameter of the central body. See https://en.wikipedia.org/wiki/Standard_gravitational_parameter
     * @return The orbital period in seconds.
     */
    public double orbitalPeriod(double mu) {
        return 2.0 * Math.PI * Math.sqrt(Math.pow(sma, 3.0) / mu);
    }

    public int secondsToHoursRounded(double sec) {
        return (int) (sec / 3600.0);
    }

    public double secondsToHours(double sec) {
        return sec / 3600.0;
    }

    public double secondsToYears(double sec) {
        return sec / 31557600;
    }

    /**
     * @return The orbital SMA expressed as a unit of astronomical distance (AU, one Earth SMA).
     */
    public double AU() {
        return sma / EARTH_SMA;
    }
}
