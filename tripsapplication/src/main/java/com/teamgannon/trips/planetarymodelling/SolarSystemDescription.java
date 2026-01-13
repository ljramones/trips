package com.teamgannon.trips.planetarymodelling;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.SolarSystem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of a solar system for rendering purposes.
 * This is a display model that combines the persistent SolarSystem entity
 * with runtime display data.
 */
@Data
public class SolarSystemDescription {

    /**
     * The persistent solar system entity (may be null if not yet created)
     */
    private SolarSystem solarSystem;

    /**
     * The central/primary star for display
     */
    private StarDisplayRecord starDisplayRecord;

    /**
     * Additional stars in multi-star systems
     */
    private List<StarDisplayRecord> companionStars = new ArrayList<>();

    /**
     * The list of planets
     */
    private List<PlanetDescription> planetDescriptionList = new ArrayList<>();

    /**
     * The list of comets
     */
    private List<CometDescription> cometDescriptions = new ArrayList<>();

    /**
     * Inner edge of habitable zone in AU
     */
    private double habitableZoneInnerAU;

    /**
     * Outer edge of habitable zone in AU
     */
    private double habitableZoneOuterAU;

    /**
     * Whether this system has any known planets
     */
    public boolean hasPlanets() {
        return !planetDescriptionList.isEmpty();
    }

    /**
     * Whether this is a multi-star system
     */
    public boolean isMultiStarSystem() {
        return !companionStars.isEmpty();
    }

    /**
     * Get the total number of stars in the system
     */
    public int getStarCount() {
        return 1 + companionStars.size();
    }

    /**
     * Convenience method to get the solar system ID
     */
    public String getSolarSystemId() {
        return solarSystem != null ? solarSystem.getId() : null;
    }

    /**
     * Convenience method to set the solar system ID by creating a minimal SolarSystem if needed
     */
    public void setSolarSystemId(String solarSystemId) {
        if (solarSystem == null) {
            solarSystem = new SolarSystem();
        }
        solarSystem.setId(solarSystemId);
    }

}
