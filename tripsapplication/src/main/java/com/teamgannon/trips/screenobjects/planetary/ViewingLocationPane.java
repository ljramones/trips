package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetary.PlanetaryContext;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shows the viewing location information in the planetary side pane.
 * Displays planet name, type, parent star, and viewing position.
 */
@Slf4j
@Component
public class ViewingLocationPane extends VBox {

    private final Label planetNameLabel = new Label("-");
    private final Label planetTypeLabel = new Label("-");
    private final Label hostStarLabel = new Label("-");
    private final Label distanceFromStarLabel = new Label("-");
    private final Label viewDirectionLabel = new Label("-");
    private final Label localTimeLabel = new Label("-");

    public ViewingLocationPane() {
        setPadding(new Insets(10));
        setSpacing(5);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        int row = 0;

        // Planet name
        grid.add(new Label("Planet:"), 0, row);
        planetNameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(planetNameLabel, 1, row++);

        // Planet type
        grid.add(new Label("Type:"), 0, row);
        grid.add(planetTypeLabel, 1, row++);

        // Host star
        grid.add(new Label("Host Star:"), 0, row);
        grid.add(hostStarLabel, 1, row++);

        // Distance from star
        grid.add(new Label("Orbit:"), 0, row);
        grid.add(distanceFromStarLabel, 1, row++);

        // Separator
        grid.add(new Separator(), 0, row++, 2, 1);

        // View direction
        grid.add(new Label("Looking:"), 0, row);
        grid.add(viewDirectionLabel, 1, row++);

        // Local time
        grid.add(new Label("Local Time:"), 0, row);
        grid.add(localTimeLabel, 1, row++);

        getChildren().add(grid);
    }

    /**
     * Set the planetary context to display.
     */
    public void setContext(PlanetaryContext context) {
        if (context == null || context.getPlanet() == null) {
            clear();
            return;
        }

        ExoPlanet planet = context.getPlanet();

        planetNameLabel.setText(planet.getName());
        planetTypeLabel.setText(getPlanetTypeDescription(planet));
        hostStarLabel.setText(context.getHostStarName());

        // Distance from star in AU
        Double semiMajorAxis = planet.getSemiMajorAxis();
        if (semiMajorAxis != null) {
            distanceFromStarLabel.setText(String.format("%.2f AU", semiMajorAxis));
        } else {
            distanceFromStarLabel.setText("-");
        }

        // View direction
        double azimuth = context.getViewingAzimuth();
        String direction = getCompassDirection(azimuth);
        double altitude = context.getViewingAltitude();
        viewDirectionLabel.setText(String.format("%s (%.0f° alt)", direction, altitude));

        // Local time
        double localTime = context.getLocalTime();
        int hours = (int) localTime;
        int minutes = (int) ((localTime - hours) * 60);
        localTimeLabel.setText(String.format("%02d:%02d", hours, minutes));

        log.debug("Updated viewing location for planet: {}", planet.getName());
    }

    /**
     * Clear all displayed information.
     */
    public void clear() {
        planetNameLabel.setText("-");
        planetTypeLabel.setText("-");
        hostStarLabel.setText("-");
        distanceFromStarLabel.setText("-");
        viewDirectionLabel.setText("-");
        localTimeLabel.setText("-");
    }

    /**
     * Get a description of the planet type.
     */
    private String getPlanetTypeDescription(ExoPlanet planet) {
        String type = planet.getPlanetType();
        if (type != null && !type.isEmpty()) {
            return type;
        }

        // Infer from other properties
        Boolean isGasGiant = planet.getGasGiant();
        if (isGasGiant != null && isGasGiant) {
            return "Gas Giant";
        }

        Double radius = planet.getRadius();
        if (radius != null) {
            if (radius > 6.0) return "Gas Giant";
            if (radius > 2.0) return "Super-Earth";
            if (radius > 0.5) return "Terrestrial";
            return "Sub-Earth";
        }

        return "Unknown";
    }

    /**
     * Convert azimuth to compass direction.
     */
    private String getCompassDirection(double azimuth) {
        // Normalize to 0-360
        azimuth = ((azimuth % 360) + 360) % 360;

        if (azimuth < 22.5 || azimuth >= 337.5) return "North";
        if (azimuth < 67.5) return "NE";
        if (azimuth < 112.5) return "East";
        if (azimuth < 157.5) return "SE";
        if (azimuth < 202.5) return "South";
        if (azimuth < 247.5) return "SW";
        if (azimuth < 292.5) return "West";
        return "NW";
    }
}
