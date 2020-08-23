package com.teamgannon.trips.dialogs.preferencespanes;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;

public interface PreferencesUpdater {

    void updateGraphColors(ColorPalette colorPalette);

    void changesGraphEnables(GraphEnablesPersist graphEnablesPersist);

}
