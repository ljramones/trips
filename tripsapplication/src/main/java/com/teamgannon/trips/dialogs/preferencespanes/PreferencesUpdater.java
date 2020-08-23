package com.teamgannon.trips.dialogs.preferencespanes;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.preferencespanes.model.GraphEnables;

public interface PreferencesUpdater {

    void updateGraphColors(ColorPalette colorPalette);

    void changesGraphEnables(GraphEnables graphEnables);

}
