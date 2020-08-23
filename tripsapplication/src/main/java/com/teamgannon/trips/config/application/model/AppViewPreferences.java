package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.dialogs.preferencespanes.model.GraphEnables;
import lombok.Data;

@Data
public class AppViewPreferences {

    private ColorPalette colorPallete = new ColorPalette();

    private GraphEnables graphEnables = new GraphEnables();

}
