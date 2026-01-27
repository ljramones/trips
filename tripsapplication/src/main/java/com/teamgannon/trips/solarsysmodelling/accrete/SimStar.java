package com.teamgannon.trips.solarsysmodelling.accrete;

import com.teamgannon.trips.solarsysmodelling.habitable.HabitableZone;
import com.teamgannon.trips.solarsysmodelling.habitable.HabitableZoneCalculator;
import com.teamgannon.trips.solarsysmodelling.habitable.HabitableZoneTypesEnum;

import java.util.Map;

import static java.lang.Math.pow;

public class SimStar extends SystemObject {

    /**
     * Amount by which individual stars might vary from their base type
     */
    public final static double STELLAR_DEVIATION = 0.05;

    protected String stellarType = "";
    protected String name = "";
    protected String desig = "";
    protected int red = 0;
    protected int green = 0;
    protected int blue = 0;
    protected double luminosity = 0.0;
    protected double radius = 0.0;
    protected double temperature = 0.0;
    protected double absoluteMagnitude = 0.0;
    protected double lifeTime = 0.0;
    protected double age = 0.0;
    protected double radiusEcosphere = 0.0;

    /**
     * Inner edge of the maximum (optimistic) habitable zone in AU.
     * Based on Recent Venus limit from Kopparapu et al.
     */
    protected double hzInnerMax = 0.0;

    /**
     * Outer edge of the maximum (optimistic) habitable zone in AU.
     * Based on Early Mars limit from Kopparapu et al.
     */
    protected double hzOuterMax = 0.0;

    /**
     * Inner edge of the optimal (conservative) habitable zone in AU.
     * Based on Runaway Greenhouse limit from Kopparapu et al.
     */
    protected double hzInnerOptimal = 0.0;

    /**
     * Outer edge of the optimal (conservative) habitable zone in AU.
     * Based on Maximum Greenhouse limit from Kopparapu et al.
     */
    protected double hzOuterOptimal = 0.0;

    protected double M2 = 0.0;
    protected double A = 0.0;
    protected double E = 0.0;

    public SimStar(double stellarMass, double stellarLuminosity, double stellarRadius, double temp, double mag) {
        // Validate critical stellar parameters to prevent division by zero and invalid calculations
        if (stellarMass <= 0) {
            throw new IllegalArgumentException(
                "Invalid stellar mass: " + stellarMass + " (must be positive)");
        }
        if (stellarLuminosity <= 0) {
            throw new IllegalArgumentException(
                "Invalid stellar luminosity: " + stellarLuminosity + " (must be positive)");
        }
        if (stellarRadius <= 0) {
            throw new IllegalArgumentException(
                "Invalid stellar radius: " + stellarRadius + " (must be positive)");
        }
        if (temp <= 0) {
            throw new IllegalArgumentException(
                "Invalid stellar temperature: " + temp + " (must be positive)");
        }

        this.mass = stellarMass;
        this.luminosity = stellarLuminosity;
        this.radius = stellarRadius;
        this.temperature = temp;
        this.absoluteMagnitude = mag;
        this.lifeTime = 1.0E10 * (mass / luminosity);

        recalc();
    }

    public void recalc() {
        // http://hyperphysics.phy-astr.gsu.edu/hbase/Astro/startime.html
        this.lifeTime = 10E10 * pow(this.mass, 2.5);

        // Calculate habitable zones using Kopparapu et al. model
        calculateHabitableZones();
    }

    /**
     * Calculate habitable zone boundaries using the Kopparapu et al. (2013/2014) model.
     * This provides temperature-dependent HZ boundaries that are more accurate than
     * the simple sqrt(luminosity) estimate.
     */
    private void calculateHabitableZones() {
        // Use effective temperature if available, otherwise estimate from luminosity
        double teff = this.temperature;
        if (teff <= 0) {
            // Estimate temperature from luminosity using Stefan-Boltzmann approximation
            // For main sequence stars: L ∝ T^4 * R^2, and R ∝ M^0.8, L ∝ M^3.5
            // Rough estimate: T ≈ 5780 * (L^0.25) for solar-like stars
            teff = 5780.0 * Math.pow(this.luminosity, 0.25);
        }

        // Use the HabitableZoneCalculator for accurate Kopparapu boundaries
        HabitableZoneCalculator calculator = new HabitableZoneCalculator();
        Map<HabitableZoneTypesEnum, HabitableZone> zones = calculator.getHabitableZones(teff, this.luminosity);

        HabitableZone maxZone = zones.get(HabitableZoneTypesEnum.MAX);
        if (maxZone != null) {
            this.hzInnerMax = maxZone.getInnerRadius();
            this.hzOuterMax = maxZone.getOuterRadius();
        }

        HabitableZone optimalZone = zones.get(HabitableZoneTypesEnum.OPTIMAL);
        if (optimalZone != null) {
            this.hzInnerOptimal = optimalZone.getInnerRadius();
            this.hzOuterOptimal = optimalZone.getOuterRadius();
        }

        // Keep radiusEcosphere as the center of the optimal HZ for backward compatibility
        // This is used in temperature calculations throughout Planet.java
        if (this.hzInnerOptimal > 0 && this.hzOuterOptimal > 0) {
            this.radiusEcosphere = (this.hzInnerOptimal + this.hzOuterOptimal) / 2.0;
        } else {
            // Fallback to simple estimate
            this.radiusEcosphere = Math.sqrt(this.luminosity);
        }
    }

    public void setAge() {
        if (lifeTime < 6.0E9) {
            this.age = Utils.instance().randomNumber(1.0E9, lifeTime);
        } else {
            this.age = Utils.instance().randomNumber(1.0E9, 6.0E9);
        }
    }

    /**
     * Get the age of the star in years.
     *
     * @return the stellar age in years
     */
    public double getAge() {
        return this.age;
    }

    /**
     * set the mass of the sim star
     *
     * @param mass the relative mass to the sun (must be positive)
     */
    public void setMass(double mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Stellar mass must be positive: " + mass);
        }
        this.mass = mass;
    }

    /**
     * set the radius of the star
     *
     * @param radius the relative radius to the sun (must be positive)
     */
    public void setRadius(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Stellar radius must be positive: " + radius);
        }
        this.radius = radius;
    }

    /**
     * set the luminosity
     *
     * @param luminosity the relative luminosity to the sun (must be positive)
     */
    public void setLuminosity(double luminosity) {
        if (luminosity <= 0) {
            throw new IllegalArgumentException("Stellar luminosity must be positive: " + luminosity);
        }
        this.luminosity = luminosity;
    }

    /**
     * set the temperature
     *
     * @param temperature the temperature in Kelvin (must be positive)
     */
    public void setTemperature(double temperature) {
        if (temperature <= 0) {
            throw new IllegalArgumentException("Stellar temperature must be positive: " + temperature);
        }
        this.temperature = temperature;
    }

    /**
     * set the absolute magnitude
     * @param absoluteMagnitude the relative ( :) ) absolute magnitude to the sun
     */
    public void setAbsoluteMagnitude(double absoluteMagnitude) {
        this.absoluteMagnitude = absoluteMagnitude;
    }


    public double stellarDustLimit() {
        return 200.0 * pow(this.mass, 1.0 / 3.0);
    }

    public double innermostPlanet() {
        return 0.3 * pow(mass, 1.0 / 3.0); // TODO: Check these numbers to ensure accuracy
    }

    public double outermostPlanet() {
        return 50.0 * pow(mass, 1.0 / 3.0); // TODO: Check these numbers to ensure accuracy
    }

    /**
     * @return A copy of this star
     */
    public SimStar copy() {
        SimStar s = new SimStar(this.mass, this.luminosity, this.radius, this.temperature, this.absoluteMagnitude);
        s.stellarType = this.stellarType;
        s.red = this.red;
        s.green = this.green;
        s.blue = this.blue;
        // HZ fields are calculated in constructor via recalc()

        return s;
    }

    // ==================== Habitable Zone Getters ====================

    /**
     * @return Inner edge of the maximum (optimistic) habitable zone in AU
     */
    public double getHzInnerMax() {
        return hzInnerMax;
    }

    /**
     * @return Outer edge of the maximum (optimistic) habitable zone in AU
     */
    public double getHzOuterMax() {
        return hzOuterMax;
    }

    /**
     * @return Inner edge of the optimal (conservative) habitable zone in AU
     */
    public double getHzInnerOptimal() {
        return hzInnerOptimal;
    }

    /**
     * @return Outer edge of the optimal (conservative) habitable zone in AU
     */
    public double getHzOuterOptimal() {
        return hzOuterOptimal;
    }

    /**
     * Check if a given orbital distance is within the optimal (conservative) habitable zone.
     *
     * @param semiMajorAxis orbital distance in AU
     * @return true if within optimal HZ
     */
    public boolean isInOptimalHZ(double semiMajorAxis) {
        return semiMajorAxis >= hzInnerOptimal && semiMajorAxis <= hzOuterOptimal;
    }

    /**
     * Check if a given orbital distance is within the maximum (optimistic) habitable zone.
     *
     * @param semiMajorAxis orbital distance in AU
     * @return true if within max HZ
     */
    public boolean isInMaxHZ(double semiMajorAxis) {
        return semiMajorAxis >= hzInnerMax && semiMajorAxis <= hzOuterMax;
    }

    /**
     * @return A copy of this star deviated by a random amount
     */
    public SimStar deviate() {
        SimStar s = this.copy();
        double v = Utils.instance().about(STELLAR_DEVIATION, 1);
        s.mass = s.mass + s.mass * v;
        s.luminosity = s.luminosity + s.luminosity * v;
        s.radius = s.radius + s.radius * v;
        s.temperature = s.temperature + s.temperature * v;
        s.recalc();

        return s;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(stellarType)
                .append(" (")
                .append("%1$,.2f".formatted(mass))
                .append("sm, ")
                .append("%1$,.2f".formatted(luminosity))
                .append("L☉, ")
                .append("%1$,.0f".formatted(temperature))
                .append("K)");

        // Include habitable zone information
        sb.append(" HZ: ")
                .append("%1$,.2f".formatted(hzInnerOptimal))
                .append("-")
                .append("%1$,.2f".formatted(hzOuterOptimal))
                .append(" AU (optimal), ")
                .append("%1$,.2f".formatted(hzInnerMax))
                .append("-")
                .append("%1$,.2f".formatted(hzOuterMax))
                .append(" AU (max)");

        return sb.toString();
    }
}
