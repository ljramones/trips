package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

/**
 * Displays overview information about the current solar system.
 * Shows star properties, habitable zone, and planet/moon counts.
 */
@Slf4j
@Component
public class SystemOverviewPane extends VBox {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat INT_FORMAT = new DecimalFormat("#,##0");

    // Star info labels
    private final Label starNameValue = new Label("-");
    private final Label spectralClassValue = new Label("-");
    private final Label luminosityValue = new Label("-");
    private final Label massValue = new Label("-");
    private final Label temperatureValue = new Label("-");
    private final Label distanceValue = new Label("-");

    // System info labels
    private final Label habitableZoneValue = new Label("-");
    private final Label planetCountValue = new Label("-");
    private final Label moonCountValue = new Label("-");
    private final Label companionStarsValue = new Label("-");

    public SystemOverviewPane() {
        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPadding(new Insets(10));
        setSpacing(10);

        // Star section
        Label starHeader = new Label("Host Star");
        starHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        GridPane starGrid = createStarGrid();

        // System section
        Label systemHeader = new Label("System");
        systemHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        GridPane systemGrid = createSystemGrid();

        getChildren().addAll(
                starHeader,
                starGrid,
                new Separator(),
                systemHeader,
                systemGrid
        );
    }

    private GridPane createStarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 0, 5, 10));

        int row = 0;
        addRow(grid, row++, "Name:", starNameValue);
        addRow(grid, row++, "Spectral Class:", spectralClassValue);
        addRow(grid, row++, "Luminosity (L☉):", luminosityValue);
        addRow(grid, row++, "Mass (M☉):", massValue);
        addRow(grid, row++, "Temperature (K):", temperatureValue);
        addRow(grid, row++, "Distance (ly):", distanceValue);

        return grid;
    }

    private GridPane createSystemGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 0, 5, 10));

        int row = 0;
        addRow(grid, row++, "Habitable Zone:", habitableZoneValue);
        addRow(grid, row++, "Planets:", planetCountValue);
        addRow(grid, row++, "Moons:", moonCountValue);
        addRow(grid, row++, "Companion Stars:", companionStarsValue);

        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #666666;");
        valueLabel.setStyle("-fx-font-weight: bold;");

        grid.add(label, 0, row);
        grid.add(valueLabel, 1, row);
    }

    /**
     * Update the pane with solar system information.
     */
    public void setSystem(SolarSystemDescription system) {
        if (system == null) {
            clear();
            return;
        }

        StarDisplayRecord star = system.getStarDisplayRecord();
        if (star != null) {
            starNameValue.setText(star.getStarName() != null ? star.getStarName() : "-");
            spectralClassValue.setText(star.getSpectralClass() != null ? star.getSpectralClass() : "-");
            luminosityValue.setText(star.getLuminosity() > 0 ? DECIMAL_FORMAT.format(star.getLuminosity()) : "-");
            massValue.setText(star.getMass() > 0 ? DECIMAL_FORMAT.format(star.getMass()) : "-");
            temperatureValue.setText(star.getTemperature() > 0 ? INT_FORMAT.format(star.getTemperature()) : "-");
            distanceValue.setText(star.getDistance() > 0 ? DECIMAL_FORMAT.format(star.getDistance()) : "-");
        }

        // Habitable zone
        double hzInner = system.getHabitableZoneInnerAU();
        double hzOuter = system.getHabitableZoneOuterAU();
        if (hzInner > 0 && hzOuter > 0) {
            habitableZoneValue.setText(DECIMAL_FORMAT.format(hzInner) + " - " + DECIMAL_FORMAT.format(hzOuter) + " AU");
        } else {
            habitableZoneValue.setText("-");
        }

        // Planet and moon counts
        int planetCount = 0;
        int moonCount = 0;
        if (system.getPlanetDescriptionList() != null) {
            for (var planet : system.getPlanetDescriptionList()) {
                if (planet.isMoon()) {
                    moonCount++;
                } else {
                    planetCount++;
                }
            }
        }
        planetCountValue.setText(String.valueOf(planetCount));
        moonCountValue.setText(String.valueOf(moonCount));

        // Companion stars
        int companionCount = system.getCompanionStars() != null ? system.getCompanionStars().size() : 0;
        companionStarsValue.setText(String.valueOf(companionCount));

        log.debug("SystemOverviewPane updated for: {}", star != null ? star.getStarName() : "unknown");
    }

    /**
     * Clear all displayed information.
     */
    public void clear() {
        starNameValue.setText("-");
        spectralClassValue.setText("-");
        luminosityValue.setText("-");
        massValue.setText("-");
        temperatureValue.setText("-");
        distanceValue.setText("-");
        habitableZoneValue.setText("-");
        planetCountValue.setText("-");
        moonCountValue.setText("-");
        companionStarsValue.setText("-");
    }
}
