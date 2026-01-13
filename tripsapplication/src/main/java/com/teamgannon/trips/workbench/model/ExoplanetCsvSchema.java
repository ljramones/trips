package com.teamgannon.trips.workbench.model;

/**
 * Schema constants for parsing exoplanet.eu CSV catalog files.
 * Column indices based on the standard exoplanet.eu export format (98 columns).
 */
public final class ExoplanetCsvSchema {

    private ExoplanetCsvSchema() {
        // Utility class
    }

    // Planet identification and status
    public static final int COL_NAME = 0;
    public static final int COL_PLANET_STATUS = 1;

    // Planet mass (Jupiter masses)
    public static final int COL_MASS = 2;
    public static final int COL_MASS_ERROR_MIN = 3;
    public static final int COL_MASS_ERROR_MAX = 4;

    // Minimum mass from RV (Jupiter masses)
    public static final int COL_MASS_SINI = 5;
    public static final int COL_MASS_SINI_ERROR_MIN = 6;
    public static final int COL_MASS_SINI_ERROR_MAX = 7;

    // Planet radius (Jupiter radii)
    public static final int COL_RADIUS = 8;
    public static final int COL_RADIUS_ERROR_MIN = 9;
    public static final int COL_RADIUS_ERROR_MAX = 10;

    // Orbital period (days)
    public static final int COL_ORBITAL_PERIOD = 11;
    public static final int COL_ORBITAL_PERIOD_ERROR_MIN = 12;
    public static final int COL_ORBITAL_PERIOD_ERROR_MAX = 13;

    // Semi-major axis (AU)
    public static final int COL_SEMI_MAJOR_AXIS = 14;
    public static final int COL_SEMI_MAJOR_AXIS_ERROR_MIN = 15;
    public static final int COL_SEMI_MAJOR_AXIS_ERROR_MAX = 16;

    // Eccentricity
    public static final int COL_ECCENTRICITY = 17;
    public static final int COL_ECCENTRICITY_ERROR_MIN = 18;
    public static final int COL_ECCENTRICITY_ERROR_MAX = 19;

    // Inclination (degrees)
    public static final int COL_INCLINATION = 20;
    public static final int COL_INCLINATION_ERROR_MIN = 21;
    public static final int COL_INCLINATION_ERROR_MAX = 22;

    // Angular distance (arcseconds)
    public static final int COL_ANGULAR_DISTANCE = 23;

    // Discovery info
    public static final int COL_DISCOVERED = 24;
    public static final int COL_UPDATED = 25;

    // Argument of periapsis / omega (degrees)
    public static final int COL_OMEGA = 26;
    public static final int COL_OMEGA_ERROR_MIN = 27;
    public static final int COL_OMEGA_ERROR_MAX = 28;

    // Time parameters
    public static final int COL_TPERI = 29;
    public static final int COL_TPERI_ERROR_MIN = 30;
    public static final int COL_TPERI_ERROR_MAX = 31;

    public static final int COL_TCONJ = 32;
    public static final int COL_TCONJ_ERROR_MIN = 33;
    public static final int COL_TCONJ_ERROR_MAX = 34;

    public static final int COL_TZERO_TR = 35;
    public static final int COL_TZERO_TR_ERROR_MIN = 36;
    public static final int COL_TZERO_TR_ERROR_MAX = 37;

    public static final int COL_TZERO_TR_SEC = 38;
    public static final int COL_TZERO_TR_SEC_ERROR_MIN = 39;
    public static final int COL_TZERO_TR_SEC_ERROR_MAX = 40;

    public static final int COL_LAMBDA_ANGLE = 41;
    public static final int COL_LAMBDA_ANGLE_ERROR_MIN = 42;
    public static final int COL_LAMBDA_ANGLE_ERROR_MAX = 43;

    public static final int COL_IMPACT_PARAMETER = 44;
    public static final int COL_IMPACT_PARAMETER_ERROR_MIN = 45;
    public static final int COL_IMPACT_PARAMETER_ERROR_MAX = 46;

    public static final int COL_TZERO_VR = 47;
    public static final int COL_TZERO_VR_ERROR_MIN = 48;
    public static final int COL_TZERO_VR_ERROR_MAX = 49;

    // RV semi-amplitude (m/s)
    public static final int COL_K = 50;
    public static final int COL_K_ERROR_MIN = 51;
    public static final int COL_K_ERROR_MAX = 52;

    // Temperature (Kelvin)
    public static final int COL_TEMP_CALCULATED = 53;
    public static final int COL_TEMP_CALCULATED_ERROR_MIN = 54;
    public static final int COL_TEMP_CALCULATED_ERROR_MAX = 55;
    public static final int COL_TEMP_MEASURED = 56;

    // Other planet properties
    public static final int COL_HOT_POINT_LON = 57;
    public static final int COL_GEOMETRIC_ALBEDO = 58;
    public static final int COL_GEOMETRIC_ALBEDO_ERROR_MIN = 59;
    public static final int COL_GEOMETRIC_ALBEDO_ERROR_MAX = 60;
    public static final int COL_LOG_G = 61;

    // Publication and detection info
    public static final int COL_PUBLICATION = 62;
    public static final int COL_DETECTION_TYPE = 63;
    public static final int COL_MASS_DETECTION_TYPE = 64;
    public static final int COL_RADIUS_DETECTION_TYPE = 65;
    public static final int COL_ALTERNATE_NAMES = 66;
    public static final int COL_MOLECULES = 67;

    // Host star properties (starting at column 68)
    public static final int COL_STAR_NAME = 68;
    public static final int COL_RA = 69;
    public static final int COL_DEC = 70;
    public static final int COL_MAG_V = 71;
    public static final int COL_MAG_I = 72;
    public static final int COL_MAG_J = 73;
    public static final int COL_MAG_H = 74;
    public static final int COL_MAG_K = 75;

    public static final int COL_STAR_DISTANCE = 76;
    public static final int COL_STAR_DISTANCE_ERROR_MIN = 77;
    public static final int COL_STAR_DISTANCE_ERROR_MAX = 78;

    public static final int COL_STAR_METALLICITY = 79;
    public static final int COL_STAR_METALLICITY_ERROR_MIN = 80;
    public static final int COL_STAR_METALLICITY_ERROR_MAX = 81;

    public static final int COL_STAR_MASS = 82;
    public static final int COL_STAR_MASS_ERROR_MIN = 83;
    public static final int COL_STAR_MASS_ERROR_MAX = 84;

    public static final int COL_STAR_RADIUS = 85;
    public static final int COL_STAR_RADIUS_ERROR_MIN = 86;
    public static final int COL_STAR_RADIUS_ERROR_MAX = 87;

    public static final int COL_STAR_SP_TYPE = 88;

    public static final int COL_STAR_AGE = 89;
    public static final int COL_STAR_AGE_ERROR_MIN = 90;
    public static final int COL_STAR_AGE_ERROR_MAX = 91;

    public static final int COL_STAR_TEFF = 92;
    public static final int COL_STAR_TEFF_ERROR_MIN = 93;
    public static final int COL_STAR_TEFF_ERROR_MAX = 94;

    public static final int COL_STAR_DETECTED_DISC = 95;
    public static final int COL_STAR_MAGNETIC_FIELD = 96;
    public static final int COL_STAR_ALTERNATE_NAMES = 97;

    // Total expected columns
    public static final int EXPECTED_COLUMN_COUNT = 98;

    // Conversion constants
    public static final double JUPITER_MASS_TO_EARTH_MASS = 317.8;
    public static final double JUPITER_RADIUS_TO_EARTH_RADIUS = 11.209;
}
