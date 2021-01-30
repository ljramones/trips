package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.PositionDisplayPreferences;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class PositionPane extends Pane {

    private final PositionDisplayPreferences positionDisplayPreferences;

    public PositionPane(PositionDisplayPreferences positionDisplayPreferences, String style) {
        this.positionDisplayPreferences = positionDisplayPreferences;
        this.setStyle(style);

        this.getChildren().add(new Label("Position"));
    }
}
