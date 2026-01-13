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
        @Index(columnList = "planetStatus")
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

}
