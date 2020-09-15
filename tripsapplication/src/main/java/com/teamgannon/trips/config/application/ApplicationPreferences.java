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

import java.io.Serializable;
import java.util.UUID;

@Slf4j
@Data
public class ApplicationPreferences implements Serializable {

    // center settings
    /**
     * current center coordinates
     */
    private double[] currentCenter = new double[3];

    /**
     * the name of the center star shosen
     */
    private String centerStarName = "Sol";

    /**
     * the database id of the center star
     * if id is null then it is sol
     */
    private UUID centerStarId = null;

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
    private Color routeColor = Color.WHITE;

    //////////////
    private DisplayPreferences displayPreferences = new DisplayPreferences();

    private LinkDisplayPreferences linkDisplayPreferences = new LinkDisplayPreferences();

    private StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();

    private PositionDisplayPreferences positionDisplayPreferences = new PositionDisplayPreferences();

    private RouteDisplayPreferences routeDisplayPreferences = new RouteDisplayPreferences();

    private CivilizationDisplayPreferences civilizationDisplayPreferences = new CivilizationDisplayPreferences();

    /**
     * ctor
     */
    public ApplicationPreferences() {
        currentCenter[0] = 0;
        currentCenter[1] = 0;
        currentCenter[2] = 0;
    }

    public String currentCenterToString() {
        return "x(" + currentCenter[0] + "), y(" + currentCenter[1] + "), z(" + currentCenter[2] + ")";
    }

    public String centerStarIdAsString() {
        if (centerStarId != null) {
            return centerStarId.toString();
        } else {
            return "no id";
        }
    }

    public Label getRouteColorAsLabel() {
        Label color = new Label("Color");
        if (routeColor.equals(Color.WHITE)) {
            color.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        color.setTextFill(routeColor);
        return color;
    }


}
