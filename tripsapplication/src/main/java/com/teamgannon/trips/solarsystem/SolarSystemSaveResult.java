package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.StarSystem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result object from PlanetDialog indicating whether the user wants to save
 * the generated solar system to the database.
 */
@Data
@Builder
public class SolarSystemSaveResult {

    /**
     * Whether the user clicked "Save to Database"
     */
    private boolean saveRequested;

    /**
     * Whether the user clicked "Dismiss" (no action)
     */
    private boolean dismissed;

    /**
     * The generated star system containing the planets
     */
    private StarSystem starSystem;

    /**
     * The source star object for which the system was generated
     */
    private StarObject sourceStar;

    /**
     * The list of generated planets
     */
    private List<Planet> planets;

    /**
     * Factory method for dismissed result
     */
    public static SolarSystemSaveResult dismissed() {
        return SolarSystemSaveResult.builder()
                .dismissed(true)
                .saveRequested(false)
                .build();
    }

    /**
     * Factory method for save request
     */
    public static SolarSystemSaveResult saveRequest(StarSystem starSystem, StarObject sourceStar) {
        return SolarSystemSaveResult.builder()
                .dismissed(false)
                .saveRequested(true)
                .starSystem(starSystem)
                .sourceStar(sourceStar)
                .planets(starSystem.getPlanets())
                .build();
    }
}
