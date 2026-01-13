package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a solar system containing one or more stars and their associated
 * celestial bodies (planets, comets, asteroid belts, etc.).
 * <p>
 * This entity serves as the aggregate root for all objects within a stellar system,
 * supporting single star systems as well as binary/trinary configurations.
 * <p>
 * Designed to model real exoplanet systems for science fiction scenarios.
 */
@Slf4j
@Getter
@Setter
@ToString
@DynamicUpdate
@Entity(name = "SOLAR_SYSTEM")
@Table(indexes = {
        @Index(columnList = "systemName ASC"),
        @Index(columnList = "primaryStarId"),
        @Index(columnList = "dataSetName ASC")
})
public class SolarSystem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this solar system
     */
    @Id
    private String id;

    /**
     * Human-readable name for the system (e.g., "Alpha Centauri system", "Sol system")
     */
    @Column(nullable = false)
    private String systemName;

    /**
     * The dataset this solar system belongs to
     */
    private String dataSetName;

    /**
     * ID of the primary (most massive or central) star in the system.
     * For single star systems, this is the only star.
     * For binary/trinary systems, this is typically the A component.
     */
    private String primaryStarId;

    /**
     * Number of stars in this system (1 = single, 2 = binary, 3 = trinary, etc.)
     */
    private int starCount = 1;

    /**
     * Total number of confirmed planets in the system
     */
    private int planetCount = 0;

    /**
     * Total number of confirmed comets in the system
     */
    private int cometCount = 0;

    /**
     * Whether this system has a known asteroid belt
     */
    private boolean hasAsteroidBelt = false;

    /**
     * Whether this system has a known debris disk
     */
    private boolean hasDebrisDisk = false;

    /**
     * Whether this system has planets in the habitable zone
     */
    private boolean hasHabitableZonePlanets = false;

    /**
     * Inner edge of the habitable zone in AU (calculated from star luminosity)
     */
    @Column(nullable = true)
    private Double habitableZoneInnerAU;

    /**
     * Outer edge of the habitable zone in AU (calculated from star luminosity)
     */
    @Column(nullable = true)
    private Double habitableZoneOuterAU;

    /**
     * Distance from Sol in light years (copied from primary star for convenience)
     */
    @Column(nullable = true)
    private Double distanceFromSol;

    /**
     * General notes about the system
     */
    @Column(length = 2000)
    private String notes;

    /**
     * Science fiction specific: the polity or faction that controls this system
     */
    private String polity;

    /**
     * Science fiction specific: strategic importance rating (1-10)
     */
    @Column(nullable = true)
    private Integer strategicImportance;

    /**
     * Science fiction specific: total population across all bodies in system
     */
    @Column(nullable = true)
    private Long totalPopulation;

    /**
     * Science fiction specific: whether the system has been colonized
     */
    private boolean colonized = false;

    /**
     * Science fiction specific: year of first colonization
     */
    @Column(nullable = true)
    private Integer colonizationYear;

    /**
     * Custom data field 1 for extensibility
     */
    private String customData1;

    /**
     * Custom data field 2 for extensibility
     */
    private String customData2;

    /**
     * Custom data field 3 for extensibility
     */
    private String customData3;

    /**
     * Custom data field 4 for extensibility
     */
    private String customData4;

    /**
     * Custom data field 5 for extensibility
     */
    private String customData5;

    /**
     * Default constructor
     */
    public SolarSystem() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Constructor with system name
     *
     * @param systemName the name of the solar system
     */
    public SolarSystem(String systemName) {
        this.id = UUID.randomUUID().toString();
        this.systemName = systemName;
    }

    /**
     * Constructor with system name and primary star
     *
     * @param systemName    the name of the solar system
     * @param primaryStarId the ID of the primary star
     */
    public SolarSystem(String systemName, String primaryStarId) {
        this.id = UUID.randomUUID().toString();
        this.systemName = systemName;
        this.primaryStarId = primaryStarId;
    }

    /**
     * Factory method to create a solar system from a StarObject
     *
     * @param star the primary star
     * @return a new SolarSystem instance
     */
    public static SolarSystem fromStar(StarObject star) {
        SolarSystem system = new SolarSystem();
        system.setSystemName(star.getDisplayName() + " system");
        system.setPrimaryStarId(star.getId());
        system.setDataSetName(star.getDataSetName());
        system.setDistanceFromSol(star.getDistance());
        system.setPolity(star.getPolity());
        system.setStarCount(1);
        return system;
    }

    /**
     * Check if this is a multi-star system
     *
     * @return true if binary, trinary, or higher
     */
    public boolean isMultiStarSystem() {
        return starCount > 1;
    }

    /**
     * Check if this system has any known planets
     *
     * @return true if planetCount > 0
     */
    public boolean hasPlanets() {
        return planetCount > 0;
    }

}
