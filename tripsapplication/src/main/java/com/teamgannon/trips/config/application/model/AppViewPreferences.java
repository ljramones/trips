package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AppViewPreferences {

    /**
     * the graph color palette
     */
    private @NotNull ColorPalette colorPalette = new ColorPalette();

    /**
     * what parameters are turned on and off
     */
    private @NotNull GraphEnablesPersist graphEnablesPersist = new GraphEnablesPersist();

    /**
     * the values of stars, size, type, color
     */
    private @NotNull StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();

    /**
     * the civilizations
     */
    private @NotNull CivilizationDisplayPreferences civilizationDisplayPreferences = new CivilizationDisplayPreferences();

    /**
     * the user controls
     */
    private @NotNull UserControls userControls = new UserControls();

}
