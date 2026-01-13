package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.SolarSystemObjectSelectedEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

/**
 * Displays detailed properties of the currently selected object
 * (planet or star) in the solar system view.
 */
@Slf4j
@Component
public class SolarSystemObjectPropertiesPane extends VBox {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.000");
    private static final DecimalFormat INT_FORMAT = new DecimalFormat("#,##0");

    private final Label titleLabel = new Label("No Selection");
    private final VBox contentBox = new VBox(5);

    // Planet properties labels
    private final Label planetNameValue = new Label("-");
    private final Label planetTypeValue = new Label("-");
    private final Label massValue = new Label("-");
    private final Label radiusValue = new Label("-");
    private final Label temperatureValue = new Label("-");
    private final Label gravityValue = new Label("-");

    // Orbital properties labels
    private final Label semiMajorAxisValue = new Label("-");
    private final Label eccentricityValue = new Label("-");
    private final Label inclinationValue = new Label("-");
    private final Label periodValue = new Label("-");
    private final Label argPeriapsisValue = new Label("-");
    private final Label longAscNodeValue = new Label("-");

    public SolarSystemObjectPropertiesPane() {
        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPadding(new Insets(10));
        setSpacing(10);

        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        getChildren().addAll(titleLabel, scrollPane);

        // Build the content layout
        buildContentLayout();
    }

    private void buildContentLayout() {
        contentBox.setPadding(new Insets(5));

        // Physical properties section
        Label physicalHeader = new Label("Physical Properties");
        physicalHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        GridPane physicalGrid = new GridPane();
        physicalGrid.setHgap(10);
        physicalGrid.setVgap(4);
        physicalGrid.setPadding(new Insets(5, 0, 5, 10));

        int row = 0;
        addRow(physicalGrid, row++, "Name:", planetNameValue);
        addRow(physicalGrid, row++, "Type:", planetTypeValue);
        addRow(physicalGrid, row++, "Mass (M\u2295):", massValue);
        addRow(physicalGrid, row++, "Radius (R\u2295):", radiusValue);
        addRow(physicalGrid, row++, "Temperature (K):", temperatureValue);
        addRow(physicalGrid, row++, "Gravity (log g):", gravityValue);

        // Orbital properties section
        Label orbitalHeader = new Label("Orbital Properties");
        orbitalHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        GridPane orbitalGrid = new GridPane();
        orbitalGrid.setHgap(10);
        orbitalGrid.setVgap(4);
        orbitalGrid.setPadding(new Insets(5, 0, 5, 10));

        row = 0;
        addRow(orbitalGrid, row++, "Semi-major axis (AU):", semiMajorAxisValue);
        addRow(orbitalGrid, row++, "Eccentricity:", eccentricityValue);
        addRow(orbitalGrid, row++, "Inclination (\u00B0):", inclinationValue);
        addRow(orbitalGrid, row++, "Orbital Period (d):", periodValue);
        addRow(orbitalGrid, row++, "Arg. Periapsis (\u00B0):", argPeriapsisValue);
        addRow(orbitalGrid, row++, "Long. Asc. Node (\u00B0):", longAscNodeValue);

        contentBox.getChildren().addAll(
                physicalHeader,
                physicalGrid,
                new Separator(),
                orbitalHeader,
                orbitalGrid
        );
    }

    private void addRow(GridPane grid, int row, String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        valueLabel.setStyle("-fx-font-size: 10px;");

        grid.add(label, 0, row);
        grid.add(valueLabel, 1, row);
    }

    /**
     * Display properties for a planet.
     */
    public void showPlanetProperties(PlanetDescription planet) {
        if (planet == null) {
            clear();
            return;
        }

        String type = planet.isMoon() ? "Moon" : "Planet";
        titleLabel.setText(type + ": " + (planet.getName() != null ? planet.getName() : "Unknown"));

        // Physical properties
        planetNameValue.setText(planet.getName() != null ? planet.getName() : "-");
        planetTypeValue.setText(planet.getPlanetTypeEnum() != null ? planet.getPlanetTypeEnum().toString() : "-");
        massValue.setText(planet.getMass() > 0 ? DECIMAL_FORMAT.format(planet.getMass()) : "-");
        radiusValue.setText(planet.getRadius() > 0 ? DECIMAL_FORMAT.format(planet.getRadius()) : "-");
        temperatureValue.setText(planet.getEquilibriumTemperature() > 0 ? INT_FORMAT.format(planet.getEquilibriumTemperature()) : "-");
        gravityValue.setText(planet.getSurfaceGravity() != 0 ? DECIMAL_FORMAT.format(planet.getSurfaceGravity()) : "-");

        // Orbital properties
        semiMajorAxisValue.setText(planet.getSemiMajorAxis() > 0 ? DECIMAL_FORMAT.format(planet.getSemiMajorAxis()) : "-");
        eccentricityValue.setText(DECIMAL_FORMAT.format(planet.getEccentricity()));
        inclinationValue.setText(DECIMAL_FORMAT.format(planet.getInclination()));
        periodValue.setText(planet.getOrbitalPeriod() > 0 ? DECIMAL_FORMAT.format(planet.getOrbitalPeriod()) : "-");
        argPeriapsisValue.setText(DECIMAL_FORMAT.format(planet.getArgumentOfPeriapsis()));
        longAscNodeValue.setText(DECIMAL_FORMAT.format(planet.getLongitudeOfAscendingNode()));

        log.debug("Showing properties for planet: {}", planet.getName());
    }

    /**
     * Display properties for a star.
     */
    public void showStarProperties(StarDisplayRecord star) {
        if (star == null) {
            clear();
            return;
        }

        titleLabel.setText("Star: " + (star.getStarName() != null ? star.getStarName() : "Unknown"));

        // Repurpose labels for star properties
        planetNameValue.setText(star.getStarName() != null ? star.getStarName() : "-");
        planetTypeValue.setText(star.getSpectralClass() != null ? star.getSpectralClass() : "-");
        massValue.setText(star.getMass() > 0 ? DECIMAL_FORMAT.format(star.getMass()) + " M\u2609" : "-");
        radiusValue.setText(star.getRadius() > 0 ? DECIMAL_FORMAT.format(star.getRadius()) + " R\u2609" : "-");
        temperatureValue.setText(star.getTemperature() > 0 ? INT_FORMAT.format(star.getTemperature()) : "-");
        gravityValue.setText("-");  // Not typically available for stars

        // Clear orbital properties for star
        semiMajorAxisValue.setText("-");
        eccentricityValue.setText("-");
        inclinationValue.setText("-");
        periodValue.setText("-");
        argPeriapsisValue.setText("-");
        longAscNodeValue.setText("-");

        log.debug("Showing properties for star: {}", star.getStarName());
    }

    /**
     * Clear the properties display.
     */
    public void clear() {
        titleLabel.setText("No Selection");

        planetNameValue.setText("-");
        planetTypeValue.setText("-");
        massValue.setText("-");
        radiusValue.setText("-");
        temperatureValue.setText("-");
        gravityValue.setText("-");

        semiMajorAxisValue.setText("-");
        eccentricityValue.setText("-");
        inclinationValue.setText("-");
        periodValue.setText("-");
        argPeriapsisValue.setText("-");
        longAscNodeValue.setText("-");
    }

    /**
     * Handle selection events from the solar system view or planet list.
     */
    @EventListener
    public void onObjectSelected(SolarSystemObjectSelectedEvent event) {
        Platform.runLater(() -> {
            switch (event.getSelectionType()) {
                case PLANET -> {
                    if (event.getSelectedObject() instanceof PlanetDescription planet) {
                        showPlanetProperties(planet);
                    }
                }
                case STAR -> {
                    if (event.getSelectedObject() instanceof StarDisplayRecord star) {
                        showStarProperties(star);
                    }
                }
                case NONE -> clear();
            }
        });
    }
}
