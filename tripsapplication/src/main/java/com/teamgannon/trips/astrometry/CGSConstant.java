package com.teamgannon.trips.astrometry;

public class CGSConstant {

    /** Light speed in cm/s. */
    public static final double SPEED_OF_LIGHT = Constant.SPEED_OF_LIGHT * 100.0;

    /** Mass of the hydrogen molecule in g. */
    public static final double H2_MASS = Constant.H2_MASS * 1000.0;

    /** AU to cm transform coefficient. */
    public static final double AU = Constant.AU * 1.0E5;

    /** pc to cm transform coefficient. */
    public static final double PARSEC = Constant.PARSEC * 100.0;

    /** Mass of the sun in g. */
    public static final double SUN_MASS = Constant.SUN_MASS * 1000.0;

    /** Boltzmann constant in erg/K.	  */
    public static final double BOLTZMANN_CONSTANT = Constant.BOLTZMANN_CONSTANT / Constant.ERGIO_TO_JULE;

    /** Planck constant in erg*s. */
    public static final double PLANCK_CONSTANT = Constant.PLANCK_CONSTANT / Constant.ERGIO_TO_JULE;

    /** Atomic unit mass, or 1/12 of the mass of the C-12 isotope, in g. */
    public static final double AMU = Constant.AMU * 1000.0;

}
