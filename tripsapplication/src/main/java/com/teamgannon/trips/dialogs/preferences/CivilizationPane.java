package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.CivilizationDisplayPreferences;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class CivilizationPane extends Pane {

    private final CivilizationDisplayPreferences civilizationDisplayPreferences;

    public CivilizationPane(CivilizationDisplayPreferences civilizationDisplayPreferences, String style) {
        this.civilizationDisplayPreferences = civilizationDisplayPreferences;
        this.setStyle(style);

        this.getChildren().add(new Label("Civilization"));

    }

}
