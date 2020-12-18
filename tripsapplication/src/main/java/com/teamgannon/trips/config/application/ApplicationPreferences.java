package com.teamgannon.trips.config.application;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.UUID;

@Slf4j
@Data
public class ApplicationPreferences implements Serializable {

    // center settings
    /**
     * current center coordinates
     */
    private double @NotNull [] currentCenter = new double[3];

    /**
     * the name of the center star shosen
     */
    private @NotNull String centerStarName = "Sol";

    /**
     * the database id of the center star
     * if id is null then it is sol
     */
    private @Nullable UUID centerStarId = null;

    /**
     * the distance for the center star
     */
    private int distanceFromCenter = 20;

    /**
     * the length of the routing line
     */
    private int routeLength = 8;

    /**
     * the gridsize in lightyears
     */
    private int gridsize = 5;

    /**
     * color of the routing line
     */
    private @NotNull Color routeColor = Color.WHITE;

    //////////////
    private @NotNull DisplayPreferences displayPreferences = new DisplayPreferences();

    private @NotNull LinkDisplayPreferences linkDisplayPreferences = new LinkDisplayPreferences();

    private @NotNull StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();

    private @NotNull PositionDisplayPreferences positionDisplayPreferences = new PositionDisplayPreferences();

    private @NotNull RouteDisplayPreferences routeDisplayPreferences = new RouteDisplayPreferences();

    private @NotNull CivilizationDisplayPreferences civilizationDisplayPreferences = new CivilizationDisplayPreferences();

    /**
     * ctor
     */
    public ApplicationPreferences() {
        currentCenter[0] = 0;
        currentCenter[1] = 0;
        currentCenter[2] = 0;
    }

    public @NotNull String currentCenterToString() {
        return "x(" + currentCenter[0] + "), y(" + currentCenter[1] + "), z(" + currentCenter[2] + ")";
    }

    public String centerStarIdAsString() {
        if (centerStarId != null) {
            return centerStarId.toString();
        } else {
            return "no id";
        }
    }

    public @NotNull Label getRouteColorAsLabel() {
        Label color = new Label("Color");
        if (routeColor.equals(Color.WHITE)) {
            color.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        color.setTextFill(routeColor);
        return color;
    }


}
