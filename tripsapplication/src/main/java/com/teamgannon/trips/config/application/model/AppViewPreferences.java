package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import lombok.Data;

@Data
public class AppViewPreferences {

    private ColorPalette colorPallete = new ColorPalette();

    private GraphEnablesPersist graphEnablesPersist = new GraphEnablesPersist();

    private StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();

}
