package com.teamgannon.trips.solarsysmodelling.accrete;

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
    protected double M2 = 0.0;
    protected double A = 0.0;
    protected double E = 0.0;

    public SimStar(double stellarMass, double stellarLuminosity, double stellarRadius, double temp, double mag) {
        this.mass = stellarMass;
        this.luminosity = stellarLuminosity;
        this.radius = stellarRadius;
        this.temperature = temp;
        this.absoluteMagnitude = mag;
        this.radiusEcosphere = Math.sqrt(luminosity); // is this accurate? Presumably this scales with the inverse square law, so it sounds right.
        this.lifeTime = 1.0E10 * (mass / luminosity);

        recalc();
    }

    public void recalc() {
        // http://hyperphysics.phy-astr.gsu.edu/hbase/Astro/startime.html
        this.lifeTime = 10E10 * Math.pow(this.mass, 2.5);
    }

    public void setAge() {
        if (lifeTime < 6.0E9) {
            this.age = Utils.instance().randomNumber(1.0E9, lifeTime);
        } else {
            this.age = Utils.instance().randomNumber(1.0E9, 6.0E9);
        }
    }

    /**
     * set the mass of the sim star
     *
     * @param mass the relative mass to the sun
     */
    public void setMass(double mass) {
        this.mass = mass;
    }

    /**
     * set the radius of the star
     *
     * @param radius the relative radius to the sun
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * set the luminosity
     *
     * @param luminosity the relative luminosity to the sun
     */
    public void setLuminosity(double luminosity) {
        this.luminosity = luminosity;
    }

    /**
     * set the temperature
     *
     * @param temperature the relative temperature to the sun
     */
    public void setTemperature(double temperature) {
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
        return 200.0 * Math.pow(this.mass, 1.0 / 3.0);
    }

    public double innermostPlanet() {
        return 0.3 * Math.pow(mass, 1.0 / 3.0); // TODO: Check these numbers to ensure accuracy
    }

    public double outermostPlanet() {
        return 50.0 * Math.pow(mass, 1.0 / 3.0); // TODO: Check these numbers to ensure accuracy
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

        return s;
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
        return stellarType + " (" + String.format("%1$,.2f", mass) + "sm)";
    }
}
