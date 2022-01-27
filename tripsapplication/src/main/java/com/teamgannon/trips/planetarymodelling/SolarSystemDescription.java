package com.teamgannon.trips.planetarymodelling;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.CometDescription;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * decription of a solar system
 */
@Data
public class SolarSystemDescription {

    /**
     * the central star
     */
    private StarDisplayRecord starDisplayRecord;

    /**
     * the list of planets
     */
    private List<PlanetDescription> planetDescriptionList = new ArrayList<>();

    /**
     * the list of comets
     */
    private List<CometDescription> cometDescriptions = new ArrayList<>();

}
