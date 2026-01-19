package com.teamgannon.trips.jpa.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Yes, this appears to be a header or schema for a dataset related to exoplanets. The fields describe various properties and characteristics of exoplanets and their host stars.
 * <p>
 * Let's break it down:
 * <p>
 * 1. **Exoplanet Properties**:
 * - `name`: Name of the exoplanet.
 * - `planet_status`: Current status of the planet (e.g., confirmed, candidate).
 * - `mass` and its errors: Mass of the exoplanet.
 * - `mass_sini` and its errors: Projected mass (i.e., minimum mass) of the exoplanet.
 * - `radius` and its errors: Radius of the exoplanet.
 * - `orbital_period` and its errors: Orbital period of the exoplanet.
 * - `semi_major_axis` and its errors: Semi-major axis of the exoplanet's orbit.
 * - `eccentricity` and its errors: Eccentricity of the orbit.
 * - `inclination` and its errors: Inclination of the orbit.
 * - `angular_distance`: Angular distance from the star.
 * - `discovered`: Year of discovery.
 * - `updated`: Last update.
 * - `omega` and its errors: Argument of periastron.
 * - `tperi`, `tconj`, `tzero_tr`, `tzero_tr_sec` and their errors: Various time-related parameters, often related to transit or radial velocity measurements.
 * - `lambda_angle`, `impact_parameter` and their errors: Parameters related to the planet's transit.
 * - `k` and its errors: Radial velocity semi-amplitude.
 * - `temp_calculated`, `temp_measured` and their errors: Temperature of the exoplanet.
 * - `hot_poInteger_lon`: Longitude of the hottest poInteger.
 * - `geometric_albedo` and its errors: Reflectivity of the exoplanet.
 * - `log_g`: Surface gravity.
 * - `publication`: Publication related to the planet.
 * - `detection_type`: Method used for detection.
 * - `mass_detection_type`, `radius_detection_type`: Methods used for mass and radius detection.
 * - `alternate_names`: Other names for the exoplanet.
 * - `molecules`: Molecules detected in the exoplanet's atmosphere.
 * <p>
 * 2. **Host Star Properties**:
 * - `star_name`: Name of the host star.
 * - `ra`, `dec`: Right ascension and declination, respectively.
 * - `mag_v`, `mag_i`, `mag_j`, `mag_h`, `mag_k`: Various magnitudes/brightnesses in different bands.
 * - `star_distance` and its errors: Distance to the star.
 * - `star_metallicity` and its errors: Metallicity of the star.
 * - `star_mass`, `star_radius` and their errors: Mass and radius of the host star.
 * - `star_sp_type`: Spectral type of the star.
 * - `star_age` and its errors: Age of the star.
 * - `star_teff` and its errors: Effective temperature of the star.
 * - `star_detected_disc`: Presence of a detected disc around the star.
 * - `star_magnetic_field`: Magnetic field of the star.
 * - `star_alternate_names`: Other names for the star.
 * <p>
 * This kind of dataset provides comprehensive information about exoplanets and their host stars. It's very likely used in astrophysical research, especially in the field of exoplanet studies.
 */
@Slf4j
@Getter
@Setter
@ToString
@DynamicUpdate
@Entity(name = "EXOPLANET")
@Table(indexes = {
        @Index(columnList = "name ASC"),
        @Index(columnList = "starName ASC"),
        @Index(columnList = "solarSystemId"),
        @Index(columnList = "hostStarId"),
        @Index(columnList = "parentPlanetId"),
        @Index(columnList = "planetStatus"),
        @Index(columnList = "isMoon")
})
public class ExoPlanet {

    @Id
    private String id;

    /**
     * name`: Name of the exoplanet.
     */
    private String name;

    /**
     * Foreign key reference to the SolarSystem entity this planet belongs to.
     * Links the planet to its parent stellar system.
     */
    private String solarSystemId;

    /**
     * Foreign key reference to the specific StarObject this planet orbits.
     * In multi-star systems, this identifies which star the planet orbits.
     * For circumbinary planets, this would be the primary star.
     */
    private String hostStarId;

    /**
     * Foreign key reference to the parent planet if this is a moon.
     * Null for planets, set for moons.
     */
    private String parentPlanetId;

    /**
     * Whether this object is a moon (orbits a planet) rather than a planet (orbits a star).
     */
    private Boolean isMoon;

    /**
     * * - `planet_status`: Current status of the planet (e.g., confirmed, candidate).
     */
    private String planetStatus;

    /**
     * `mass` and its errors: Mass of the exoplanet.
     */
    @Column(nullable = true)
    private Double mass;

    /**
     * * - `mass_sini` and its errors: Projected mass (i.e., minimum mass) of the exoplanet.
     */
    @Column(nullable = true)
    private Double massSini;

    /**
     * `radius` and its errors: Radius of the exoplanet.
     */
    @Column(nullable = true)
    private Double radius;

    /**
     * `orbital_period` and its errors: Orbital period of the exoplanet.
     */
    @Column(nullable = true)
    private Double orbitalPeriod;

    /**
     * `semi_major_axis` and its errors: Semi-major axis of the exoplanet's orbit.
     */
    @Column(nullable = true)
    private Double semiMajorAxis;

    /**
     * `eccentricity` and its errors: Eccentricity of the orbit.
     */
    @Column(nullable = true)
    private Double eccentricity;

    /**
     * * - `inclination` and its errors: Inclination of the orbit.
     */
    @Column(nullable = true)
    private Double inclination;

    /**
     * `angular_distance`: Angular distance from the star.
     */
    @Column(nullable = true)
    private Double angularDistance;

    /**
     * `discovered`: Year of discovery.
     */
    @Column(nullable = true)
    private Integer discovered;

    /**
     * * - `updated`: Last update.
     */
    @Column(nullable = true)
    private String updated;

    /**
     * * - `omega` and its errors: Argument of periastron.
     */
    @Column(nullable = true)
    private Double omega;

    /**
     * Longitude of the ascending node in degrees.
     * Part of the Keplerian orbital elements for fully specifying an orbit in 3D.
     */
    @Column(nullable = true)
    private Double longitudeOfAscendingNode;

    /**
     * * - `tperi`, `tconj`, `tzero_tr`, `tzero_tr_sec` and their errors: Various time-related parameters, often related to transit or radial velocity measurements.
     */
    @Column(nullable = true)
    private Double tperi;

    /**
     * * - `tperi`, `tconj`, `tzero_tr`, `tzero_tr_sec` and their errors: Various time-related parameters, often related to transit or radial velocity measurements.
     */
    @Column(nullable = true)
    private Double tconj;

    /**
     * * - `tperi`, `tconj`, `tzero_tr`, `tzero_tr_sec` and their errors: Various time-related parameters, often related to transit or radial velocity measurements.
     */
    @Column(nullable = true)
    private Double tzeroTr;

    /**
     * * - `tperi`, `tconj`, `tzero_tr`, `tzero_tr_sec` and their errors: Various time-related parameters, often related to transit or radial velocity measurements.
     */
    @Column(nullable = true)
    private Double tzeroTrSec;

    /**
     * * - `lambda_angle`, `impact_parameter` and their errors: Parameters related to the planet's transit.
     */
    @Column(nullable = true)
    private Double lambdaAngle;

    /**
     * * - `lambda_angle`, `impact_parameter` and their errors: Parameters related to the planet's transit.
     */
    @Column(nullable = true)
    private Double impactParameter;

    /**
     * * - `tperi`, `tconj`, `tzero_tr`, `tzero_tr_sec` and their errors: Various time-related parameters, often related to transit or radial velocity measurements.
     */
    @Column(nullable = true)
    private Double tzeroVr;

    /**
     * * - `k` and its errors: Radial velocity semi-amplitude.
     */
    @Column(nullable = true)
    private Double k;

    /**
     * * - `temp_calculated`, `temp_measured` and their errors: Temperature of the exoplanet.
     */
    @Column(nullable = true)
    private Double tempCalculated;

    /**
     * * - `temp_calculated`, `temp_measured` and their errors: Temperature of the exoplanet.
     */
    @Column(nullable = true)
    private Double tempMeasured;

    /**
     * * - `hot_poInteger_lon`: Longitude of the hottest poInteger.
     */
    @Column(nullable = true)
    private Double hotPoIntegerLon;

    /**
     * * - `geometric_albedo` and its errors: Reflectivity of the exoplanet.
     */
    @Column(nullable = true)
    private Double geometricAlbedo;

    /**
     * * - `log_g`: Surface gravity.
     */
    @Column(nullable = true)
    private Double logG;

    /**
     * * - `publication`: Publication related to the planet.
     */
    private String publication;

    /**
     * * - `detection_type`: Method used for detection.
     */
    private String detectionType;

    /**
     * * - `mass_detection_type`, `radius_detection_type`: Methods used for mass and radius detection.
     */
    private String massDetectionType;

    /**
     * * - `mass_detection_type`, `radius_detection_type`: Methods used for mass and radius detection.
     */
    private String radiusDetectionType;

    /**
     * * - `alternate_names`: Other names for the exoplanet.
     */
    private String alternateNames;

    /**
     * * - `molecules`: Molecules detected in the exoplanet's atmosphere.
     * e.g. C, CH4, CO, CO2, CrH, Fe, FeH, H2O, Mg2SiO4, NH3, O, SiO
     */
    private String molecules;

    // Host Star properties

    /**
     * ` star_name`: Name of the host star.
     */
    private String starName;

    /**
     * `ra`, `dec`: Right ascension and declination, respectively.
     */
    @Column(nullable = true)
    private Double ra;

    /**
     * `ra`, `dec`: Right ascension and declination, respectively.
     */
    @Column(nullable = true)
    private Double dec;

    /**
     * `mag_v`, `mag_i`, `mag_j`, `mag_h`, `mag_k`: Various magnitudes/brightnesses in different bands.
     */
    @Column(nullable = true)
    private Double magV;

    /**
     * `mag_v`, `mag_i`, `mag_j`, `mag_h`, `mag_k`: Various magnitudes/brightnesses in different bands.
     */
    @Column(nullable = true)
    private Double magI;

    /**
     * `mag_v`, `mag_i`, `mag_j`, `mag_h`, `mag_k`: Various magnitudes/brightnesses in different bands.
     */
    @Column(nullable = true)
    private Double magJ;

    /**
     * `mag_v`, `mag_i`, `mag_j`, `mag_h`, `mag_k`: Various magnitudes/brightnesses in different bands.
     */
    @Column(nullable = true)
    private Double magH;

    /**
     * `mag_v`, `mag_i`, `mag_j`, `mag_h`, `mag_k`: Various magnitudes/brightnesses in different bands.
     */
    @Column(nullable = true)
    private Double magK;

    /**
     * `star_distance` and its errors: Distance to the star.
     */
    @Column(nullable = true)
    private Double starDistance;

    /**
     * * - `star_metallicity` and its errors: Metallicity of the star.
     */
    @Column(nullable = true)
    private Double starMetallicity;

    /**
     * * - `star_mass`, `star_radius` and their errors: Mass and radius of the host star.
     */
    @Column(nullable = true)
    private Double starMass;

    /**
     * * - `star_mass`, `star_radius` and their errors: Mass and radius of the host star.
     */
    @Column(nullable = true)
    private Double starRadius;

    /**
     * * - `star_sp_type`: Spectral type of the star.
     */
    private String starSpType;

    /**
     * * - `star_age` and its errors: Age of the star.
     */
    @Column(nullable = true)
    private Double starAge;

    /**
     * * - `star_teff` and its errors: Effective temperature of the star.
     */
    @Column(nullable = true)
    private Double starTeff;

    /**
     * * - `star_detected_disc`: Presence of a detected disc around the star.
     */
    private Boolean starDetectedDisc;

    /**
     * * - `star_magnetic_field`: Magnetic field of the star.
     */
    private String starMagneticField;

    /**
     * * - `star_alternate_names`: Other names for the star.
     */
    private String starAlternateNames;

    // ==================== Extended Planet Properties (ACCRETE/Sci-Fi) ====================

    // --- Planet Type and Classification ---

    /**
     * Planet type classification (e.g., Terrestrial, Venusian, Martian, Water, Ice, Gas Giant, etc.)
     * Matches PlanetTypeEnum values from ACCRETE simulation.
     */
    private String planetType;

    /**
     * Orbital zone (1=inner, 2=middle, 3=outer) based on star luminosity
     */
    @Column(nullable = true)
    private Integer orbitalZone;

    // --- Habitability Flags ---

    /**
     * Whether the planet has a breathable atmosphere and suitable conditions
     */
    private Boolean habitable;

    /**
     * Whether the planet is Earth-like (habitable with Earth-similar conditions)
     */
    private Boolean earthlike;

    /**
     * Whether this is a gas giant
     */
    private Boolean gasGiant;

    /**
     * Whether this is a habitable Jovian (gas giant with potentially habitable moons)
     */
    private Boolean habitableJovian;

    /**
     * Whether this planet has a habitable moon
     */
    private Boolean habitableMoon;

    /**
     * Whether this planet has a runaway greenhouse effect
     */
    private Boolean greenhouseEffect;

    /**
     * Whether this planet is tidally locked or in resonant rotation
     */
    private Boolean tidallyLocked;

    // --- Physical Properties ---

    /**
     * Planet density in g/cc
     */
    @Column(nullable = true)
    private Double density;

    /**
     * Core radius in km (for differentiated planets)
     */
    @Column(nullable = true)
    private Double coreRadius;

    /**
     * Axial tilt in degrees
     */
    @Column(nullable = true)
    private Double axialTilt;

    /**
     * Day length (rotation period) in hours
     */
    @Column(nullable = true)
    private Double dayLength;

    /**
     * Surface gravity in Earth gravities (g)
     */
    @Column(nullable = true)
    private Double surfaceGravity;

    /**
     * Surface acceleration in m/s^2
     */
    @Column(nullable = true)
    private Double surfaceAcceleration;

    /**
     * Escape velocity in m/s
     */
    @Column(nullable = true)
    private Double escapeVelocity;

    // --- Climate Properties ---

    /**
     * Fraction of surface covered by water (0.0 to 1.0)
     */
    @Column(nullable = true)
    private Double hydrosphere;

    /**
     * Fraction of atmosphere covered by clouds (0.0 to 1.0)
     */
    @Column(nullable = true)
    private Double cloudCover;

    /**
     * Fraction of surface covered by ice (0.0 to 1.0)
     */
    @Column(nullable = true)
    private Double iceCover;

    /**
     * Planetary albedo (reflectivity, 0.0 to 1.0)
     */
    @Column(nullable = true)
    private Double albedo;

    /**
     * Surface pressure in millibars
     */
    @Column(nullable = true)
    private Double surfacePressure;

    /**
     * Volatile gas inventory (unitless measure of atmospheric gases)
     */
    @Column(nullable = true)
    private Double volatileGasInventory;

    // --- Temperature Properties ---

    /**
     * Surface temperature in Kelvin
     */
    @Column(nullable = true)
    private Double surfaceTemperature;

    /**
     * Daytime high temperature in Kelvin
     */
    @Column(nullable = true)
    private Double highTemperature;

    /**
     * Nighttime low temperature in Kelvin
     */
    @Column(nullable = true)
    private Double lowTemperature;

    /**
     * Maximum temperature (summer day) in Kelvin
     */
    @Column(nullable = true)
    private Double maxTemperature;

    /**
     * Minimum temperature (winter night) in Kelvin
     */
    @Column(nullable = true)
    private Double minTemperature;

    /**
     * Boiling point of water at surface pressure in Kelvin
     */
    @Column(nullable = true)
    private Double boilingPoint;

    /**
     * Exospheric temperature in Kelvin
     */
    @Column(nullable = true)
    private Double exosphericTemperature;

    /**
     * Temperature rise due to greenhouse effect in Kelvin
     */
    @Column(nullable = true)
    private Double greenhouseRise;

    // --- Atmospheric Properties ---

    /**
     * Minimum molecular weight retained by the atmosphere
     */
    @Column(nullable = true)
    private Double minimumMolecularWeight;

    /**
     * Atmosphere type (Breathable, Unbreathable, Poisonous, None)
     */
    private String atmosphereType;

    /**
     * Detailed atmospheric composition as JSON or semicolon-separated list
     * Format: "N2:780;O2:210;Ar:9;CO2:0.4" (symbol:partial_pressure_mb)
     */
    @Column(length = 2000)
    private String atmosphereComposition;

    // --- Science Fiction Properties ---

    /**
     * Population of the planet (for colonized worlds)
     */
    @Column(nullable = true)
    private Long population;

    /**
     * Technology level (1-10 scale for sci-fi settings)
     */
    @Column(nullable = true)
    private Integer techLevel;

    /**
     * Whether this planet has been colonized
     */
    private Boolean colonized;

    /**
     * Year of colonization (for sci-fi settings)
     */
    @Column(nullable = true)
    private Integer colonizationYear;

    /**
     * Polity or faction controlling this planet
     */
    private String polity;

    /**
     * Strategic importance rating (1-10)
     */
    @Column(nullable = true)
    private Integer strategicImportance;

    /**
     * Primary economic activity or resource
     */
    private String primaryResource;

    /**
     * General notes about the planet
     */
    @Column(length = 4000)
    private String notes;

    // ==================== Procedural Generation Metadata ====================

    /**
     * Seed used for procedural planet generation.
     */
    @Column(nullable = true)
    private Long proceduralSeed;

    /**
     * Version identifier for the procedural generator/bias logic.
     */
    private String proceduralGeneratorVersion;

    /**
     * Origin of the procedural config (ACCRETE, USER_OVERRIDES, MANUAL).
     */
    private String proceduralSource;

    /**
     * JSON snapshot of Accrete-derived inputs used for generation.
     */
    @Lob
    private String proceduralAccreteSnapshot;

    /**
     * JSON of non-default procedural config overrides.
     */
    @Lob
    private String proceduralOverrides;

    /**
     * Timestamp of the last procedural generation (ISO-8601 string).
     */
    private String proceduralGeneratedAt;

    /**
     * Cached preview image (PNG or JPEG) for the generated planet.
     */
    @Lob
    private byte[] proceduralPreview;

}
