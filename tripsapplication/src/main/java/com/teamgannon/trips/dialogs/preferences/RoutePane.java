package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.model.RouteDisplayPreferences;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class RoutePane extends Pane {

    private final RouteDisplayPreferences routeDisplayPreferences;

    public RoutePane(RouteDisplayPreferences routeDisplayPreferences, String style) {
        this.routeDisplayPreferences = routeDisplayPreferences;
        this.setStyle(style);

        this.getChildren().add(new Label("Route"));
    }

    public void reset() {
        // nothing to do yet but we leave this for the future
    }
}
