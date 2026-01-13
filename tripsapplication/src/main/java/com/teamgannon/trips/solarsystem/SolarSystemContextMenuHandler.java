package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import javafx.scene.Node;

/**
 * Callback interface for handling context menu events in the solar system visualization.
 */
public interface SolarSystemContextMenuHandler {

    /**
     * Called when user right-clicks a planet sphere.
     *
     * @param source  the 3D node that was clicked
     * @param planet  the planet description
     * @param screenX screen X coordinate for menu placement
     * @param screenY screen Y coordinate for menu placement
     */
    void onPlanetContextMenu(Node source, PlanetDescription planet, double screenX, double screenY);

    /**
     * Called when user right-clicks the central star.
     *
     * @param source  the 3D node that was clicked
     * @param star    the star display record
     * @param screenX screen X coordinate for menu placement
     * @param screenY screen Y coordinate for menu placement
     */
    void onStarContextMenu(Node source, StarDisplayRecord star, double screenX, double screenY);

    /**
     * Called when user right-clicks an orbit path.
     *
     * @param source  the 3D node that was clicked
     * @param planet  the planet whose orbit was clicked
     * @param screenX screen X coordinate for menu placement
     * @param screenY screen Y coordinate for menu placement
     */
    void onOrbitContextMenu(Node source, PlanetDescription planet, double screenX, double screenY);
}
