package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import lombok.Data;

@Data
public class AppViewPreferences {

    /**
     * the graph color palette
     */
    private ColorPalette colorPallete = new ColorPalette();

    /**
     * what parameters are turned on and off
     */
    private GraphEnablesPersist graphEnablesPersist = new GraphEnablesPersist();

    /**
     * the values of stars, size, type, color
     */
    private StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();

    /**
     * the civilizations
     */
    private CivilizationDisplayPreferences civilizationDisplayPreferences = new CivilizationDisplayPreferences();

}
