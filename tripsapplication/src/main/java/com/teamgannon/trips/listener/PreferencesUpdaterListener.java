package com.teamgannon.trips.listener;

import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.UserControls;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;

public interface PreferencesUpdaterListener {

    void updateGraphColors(ColorPalette colorPalette);

    void changesGraphEnables(GraphEnablesPersist graphEnablesPersist);

    void changeStarPreferences(StarDisplayPreferences starDisplayPreferences);

    void changePolitiesPreferences(CivilizationDisplayPreferences civilizationDisplayPreferences);

    void changeUserControlsPreferences(UserControls userControls);
}
