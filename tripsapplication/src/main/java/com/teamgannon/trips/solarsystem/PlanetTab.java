package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class PlanetTab extends Tab {

    // Status indicator styles
    private static final String STYLE_TRUE = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 3;";
    private static final String STYLE_FALSE = "-fx-background-color: #e0e0e0; -fx-text-fill: #888888; -fx-padding: 2 6; -fx-background-radius: 3;";
    private static final String STYLE_HZ_OPTIMAL = "-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 3; -fx-font-weight: bold;";
    private static final String STYLE_HZ_EXTENDED = "-fx-background-color: #66BB6A; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 3;";
    private static final String STYLE_HZ_OUTSIDE = "-fx-background-color: #f5f5f5; -fx-text-fill: #666666; -fx-padding: 2 6; -fx-background-radius: 3;";

    private final Label planetTypeLabel = new Label();
    private final Label hzStatusLabel = new Label();

    private final Label axialTiltLabel = new Label();
    private final Label radiusLabel = new Label();
    private final Label coreRadiusLabel = new Label();
    private final Label orbitalPeriodLabel = new Label();
    private final Label rmsVelocityLabel = new Label();
    private final Label escapeVelocityLabel = new Label();
    private final Label dayLengthLabel = new Label();
    private final Label surfaceAccelLabel = new Label();
    private final Label surfaceGravityLabel = new Label();
    private final Label orbitalZoneLabel = new Label();

    private final Label exosphericTempLabel = new Label();
    private final Label estimatedTempLabel = new Label();
    private final Label estTerrestialTempLabel = new Label();
    private final Label surfaceTempLabel = new Label();
    private final Label highTempLabel = new Label();
    private final Label lowTempLabel = new Label();
    private final Label minTempLabel = new Label();
    private final Label maxTempLabel = new Label();
    private final Label boilingPointLabel = new Label();
    private final Label greenHouseRiseLabel = new Label();

    private final Label volatileGasInventoryLabel = new Label();
    private final Label surfacePressureLabel = new Label();
    private final Label minMolecularWeightLabel = new Label();
    private final Label hydrosphereLabel = new Label();
    private final Label cloudCoverLabel = new Label();
    private final Label iceCoverLabel = new Label();
    private final Label albedoLabel = new Label();


    /**
     * the constructor for the tab
     *
     * @param planet    the planet
     * @param tabNumber the number
     */
    public PlanetTab(Planet planet, int tabNumber) {
        if (!planet.isMoon()) {
            this.setText("Planet " + tabNumber);
        } else {
            this.setText("Moon " + tabNumber);
        }

        VBox mainBox = new VBox(8);
        mainBox.setPadding(new Insets(8));

        // Status tags at the top
        FlowPane statusPane = createStatusPane(planet);
        mainBox.getChildren().add(statusPane);

        // Two-column grid for the sections
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TitledPane generalPane = createGeneralSection(planet);
        gridPane.add(generalPane, 0, 0);

        TitledPane dynamicsPane = createDynamicsSection(planet);
        gridPane.add(dynamicsPane, 0, 1);

        TitledPane climatePane = createClimateSection(planet);
        gridPane.add(climatePane, 1, 0);

        TitledPane atmospherePane = createAtmosphereSection(planet);
        gridPane.add(atmospherePane, 1, 1);

        mainBox.getChildren().add(gridPane);

        // Wrap in scroll pane for content overflow
        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.setContent(scrollPane);
    }

    /**
     * Creates a flow pane with status indicator labels.
     * Shows: [Category] [HZ Status] [Additional Status Tags...]
     */
    private FlowPane createStatusPane(Planet planet) {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(6);
        flowPane.setVgap(4);
        flowPane.setPadding(new Insets(4, 0, 8, 0));

        // Planet category (type) as primary indicator - blue badge
        Label typeLabel = new Label(planet.planetType());
        typeLabel.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-weight: bold;");
        flowPane.getChildren().add(typeLabel);

        // HZ Status indicator - green shades
        Label hzLabel = new Label(planet.getHabitableZoneStatus());
        if (planet.isInOptimalHZ()) {
            hzLabel.setStyle(STYLE_HZ_OPTIMAL);
        } else if (planet.isInMaxHZ()) {
            hzLabel.setStyle(STYLE_HZ_EXTENDED);
        } else {
            hzLabel.setStyle(STYLE_HZ_OUTSIDE);
        }
        flowPane.getChildren().add(hzLabel);

        // Additional status indicators (only show if NOT redundant with category)
        // Note: Don't add "Gas Giant" badge since planetType() already shows it
        if (planet.isHabitable()) {
            flowPane.getChildren().add(createStatusLabel("Habitable", true));
        }
        if (planet.isEarthlike()) {
            flowPane.getChildren().add(createStatusLabel("Earthlike", true));
        }
        // Gas giant status is now indicated by the category badge, not a separate tag
        if (planet.isHabitableJovian()) {
            flowPane.getChildren().add(createStatusLabel("Hab. Jovian", true));
        }
        if (planet.isMoon()) {
            flowPane.getChildren().add(createStatusLabel("Moon", true));
        }
        if (planet.isHabitableMoon()) {
            flowPane.getChildren().add(createStatusLabel("Hab. Moon", true));
        }
        if (planet.isResonantPeriod()) {
            Label tidalLabel = new Label("Tidally Locked");
            tidalLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 3;");
            flowPane.getChildren().add(tidalLabel);
        }
        // Greenhouse effect requires actual atmosphere (pressure > 0)
        if (planet.isGreenhouseEffect() && planet.getSurfacePressure() > 0.001) {
            Label ghLabel = new Label("Greenhouse");
            ghLabel.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 3;");
            flowPane.getChildren().add(ghLabel);
        }

        return flowPane;
    }

    /**
     * Creates a styled status label
     */
    private Label createStatusLabel(String text, boolean active) {
        Label label = new Label(text);
        label.setStyle(active ? STYLE_TRUE : STYLE_FALSE);
        return label;
    }


    private TitledPane createGeneralSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Orbital");
        pane.setCollapsible(false);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(3);
        gridPane.setPadding(new Insets(5));

        pane.setContent(gridPane);

        int row = 0;

        gridPane.add(new Label("Semi-major axis:"), 0, row);
        gridPane.add(new Label(checkValue(planet.getSma()) + " AU"), 1, row++);

        gridPane.add(new Label("Eccentricity:"), 0, row);
        gridPane.add(new Label(checkValue(planet.getEccentricity())), 1, row++);

        gridPane.add(new Label("Mass:"), 0, row);
        double massEarth = planet.massInEarthMasses();
        double massJupiter = planet.massInJupiterMasses();
        // Show in both Earth and Jupiter masses for large planets
        if (massJupiter >= 0.1) {
            gridPane.add(new Label(checkValue(massJupiter) + " M\u2643 (" + checkValue(massEarth) + " M\u2295)"), 1, row++);
        } else {
            gridPane.add(new Label(checkValue(massEarth) + " M\u2295"), 1, row++);
        }

        gridPane.add(new Label("Density:"), 0, row);
        gridPane.add(new Label(checkValue(planet.getDensity()) + " g/cm\u00B3"), 1, row++);

        return pane;
    }


    private TitledPane createDynamicsSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Physical");
        pane.setCollapsible(false);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(3);
        gridPane.setPadding(new Insets(5));

        pane.setContent(gridPane);

        int row = 0;

        gridPane.add(new Label("Radius:"), 0, row);
        // Convert from km to Earth radii (Earth radius = 6371 km)
        double radiusInEarthRadii = planet.ratioRadiusToEarth();
        double radiusKm = planet.getRadius();
        // Jupiter radius = 69,911 km, Earth radius = 6,371 km
        double radiusInJupiterRadii = radiusKm / 69911.0;
        // Show in both Earth and Jupiter radii for large planets
        if (radiusInJupiterRadii >= 0.5) {
            radiusLabel.setText(checkValue(radiusInJupiterRadii) + " R\u2643 (" + checkValue(radiusInEarthRadii) + " R\u2295)");
        } else {
            radiusLabel.setText(checkValue(radiusInEarthRadii) + " R\u2295 (" + checkValue(radiusKm) + " km)");
        }
        gridPane.add(radiusLabel, 1, row++);

        gridPane.add(new Label("Core radius:"), 0, row);
        coreRadiusLabel.setText(checkValue(planet.getCoreRadius()) + " km");
        gridPane.add(coreRadiusLabel, 1, row++);

        // For gas giants, gravity is at 1-bar (cloud-top) level
        if (planet.isGasGiant()) {
            gridPane.add(new Label("Cloud-top gravity:"), 0, row);
        } else {
            gridPane.add(new Label("Surface gravity:"), 0, row);
        }
        surfaceGravityLabel.setText(checkValue(planet.getSurfaceGravity()) + " g");
        gridPane.add(surfaceGravityLabel, 1, row++);

        gridPane.add(new Label("Escape velocity:"), 0, row);
        // Convert from m/s to km/s
        double escapeVelKmS = planet.getEscapeVelocity() / 1000.0;
        escapeVelocityLabel.setText(checkValue(escapeVelKmS) + " km/s");
        gridPane.add(escapeVelocityLabel, 1, row++);

        gridPane.add(new Label("Axial tilt:"), 0, row);
        axialTiltLabel.setText(checkValue(planet.axialTilt()) + "\u00B0");
        gridPane.add(axialTiltLabel, 1, row++);

        gridPane.add(new Label("Day length:"), 0, row);
        // Convert from seconds to hours
        double dayLengthHrs = planet.getDayLength() / 3600.0;
        double dayLengthDays = planet.getDayLength() / 86400.0;
        // For very long day lengths (> 1000 hours), also show in days
        if (dayLengthHrs > 1000 && !Double.isInfinite(dayLengthHrs)) {
            dayLengthLabel.setText(checkValue(dayLengthDays) + " days (" + checkValue(dayLengthHrs) + " hrs)");
        } else {
            dayLengthLabel.setText(checkValue(dayLengthHrs) + " hrs");
        }
        // Indicate if tidally locked (day = orbital period)
        if (planet.isResonantPeriod()) {
            dayLengthLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-style: italic;");
        } else {
            dayLengthLabel.setStyle("");
        }
        gridPane.add(dayLengthLabel, 1, row++);

        gridPane.add(new Label("Orbital period:"), 0, row);
        // Convert from seconds to Earth days
        double orbitalPeriodDays = planet.getOrbitalPeriod() / 86400.0;
        // Also show in years for long orbital periods (> 365 days)
        double orbitalPeriodYears = orbitalPeriodDays / 365.25;
        if (orbitalPeriodYears >= 1.0) {
            orbitalPeriodLabel.setText(checkValue(orbitalPeriodYears) + " yrs (" + checkValue(orbitalPeriodDays) + " days)");
        } else {
            orbitalPeriodLabel.setText(checkValue(orbitalPeriodDays) + " days");
        }
        gridPane.add(orbitalPeriodLabel, 1, row++);

        return pane;
    }


    private TitledPane createAtmosphereSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Temperature");
        pane.setCollapsible(false);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(8, 8, 12, 8));  // Extra bottom padding

        pane.setContent(gridPane);

        int row = 0;
        boolean hasAtmosphere = planet.getSurfacePressure() > 0.001;
        boolean isGasGiant = planet.isGasGiant();

        // For gas giants, exospheric temp is meaningful; surface temps are not
        gridPane.add(new Label("Equilibrium temp:"), 0, row);
        estimatedTempLabel.setText(formatTemp(planet.getEstimatedTemperature()));
        gridPane.add(estimatedTempLabel, 1, row++);

        if (!isGasGiant) {
            gridPane.add(new Label("Surface temp:"), 0, row);
            surfaceTempLabel.setText(formatTemp(planet.getSurfaceTemperature()));
            gridPane.add(surfaceTempLabel, 1, row++);

            gridPane.add(new Label("Day/Night range:"), 0, row);
            String range = formatTemp(planet.getLowTemperature()) + " to " + formatTemp(planet.getHighTemperature());
            highTempLabel.setText(range);
            gridPane.add(highTempLabel, 1, row++);

            if (hasAtmosphere) {
                // Show greenhouse warming or albedo cooling depending on which dominates
                if (planet.hasNetCooling()) {
                    gridPane.add(new Label("Albedo cooling:"), 0, row);
                    greenHouseRiseLabel.setText(checkValue(planet.getAlbedoCooling()) + " K");
                    greenHouseRiseLabel.setStyle("-fx-text-fill: #2196F3;");  // Blue for cooling
                } else {
                    gridPane.add(new Label("Greenhouse rise:"), 0, row);
                    greenHouseRiseLabel.setText("+" + checkValue(planet.getGreenhouseRise()) + " K");
                    greenHouseRiseLabel.setStyle("-fx-text-fill: #FF5722;");  // Orange for warming
                }
                gridPane.add(greenHouseRiseLabel, 1, row++);

                gridPane.add(new Label("Boiling point:"), 0, row);
                boilingPointLabel.setText(formatTemp(planet.getBoilingPoint()));
                gridPane.add(boilingPointLabel, 1, row++);
            }
        } else {
            gridPane.add(new Label("Exospheric temp:"), 0, row);
            exosphericTempLabel.setText(formatTemp(planet.getExosphericTemperature()));
            gridPane.add(exosphericTempLabel, 1, row++);

            // Add note for gas giants
            Label noteLabel = new Label("(No solid surface)");
            noteLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic; -fx-font-size: 10px;");
            gridPane.add(noteLabel, 0, row++, 2, 1);
        }

        return pane;
    }

    /**
     * Formats temperature in both Kelvin and Celsius
     */
    private String formatTemp(double kelvin) {
        if (kelvin > 10e10 || kelvin < 0) {
            return "--";
        }
        double celsius = kelvin - 273.15;
        return "%.0f K (%.0f\u00B0C)".formatted(kelvin, celsius);
    }

    private TitledPane createClimateSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Atmosphere");
        pane.setCollapsible(false);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(3);
        gridPane.setPadding(new Insets(5));

        pane.setContent(gridPane);

        int row = 0;
        boolean hasAtmosphere = planet.getSurfacePressure() > 0.001;
        boolean isGasGiant = planet.isGasGiant();

        gridPane.add(new Label("Surface pressure:"), 0, row);
        if (isGasGiant) {
            surfacePressureLabel.setText("N/A (gas giant)");
            surfacePressureLabel.setStyle("-fx-text-fill: #888888;");
        } else {
            surfacePressureLabel.setText(formatPressure(planet.getSurfacePressure()));
            surfacePressureLabel.setStyle("");
        }
        gridPane.add(surfacePressureLabel, 1, row++);

        gridPane.add(new Label("Albedo:"), 0, row);
        albedoLabel.setText(checkValue(planet.getAlbedo()));
        gridPane.add(albedoLabel, 1, row++);

        if (!isGasGiant && hasAtmosphere) {
            gridPane.add(new Label("Volatile inventory:"), 0, row);
            volatileGasInventoryLabel.setText(checkValue(planet.getVolatileGasInventory()));
            gridPane.add(volatileGasInventoryLabel, 1, row++);

            gridPane.add(new Label("Min molecular wt:"), 0, row);
            minMolecularWeightLabel.setText(checkValue(planet.getMinimumMolecularWeight()));
            gridPane.add(minMolecularWeightLabel, 1, row++);
        }

        if (!isGasGiant) {
            gridPane.add(new Label("Hydrosphere:"), 0, row);
            hydrosphereLabel.setText(formatPercent(planet.getHydrosphere()));
            gridPane.add(hydrosphereLabel, 1, row++);

            gridPane.add(new Label("Cloud cover:"), 0, row);
            cloudCoverLabel.setText(formatPercent(planet.getCloudCover()));
            gridPane.add(cloudCoverLabel, 1, row++);

            gridPane.add(new Label("Ice cover:"), 0, row);
            iceCoverLabel.setText(formatPercent(planet.getIceCover()));
            gridPane.add(iceCoverLabel, 1, row++);
        }

        return pane;
    }

    /**
     * Formats pressure in millibars/Earth atmospheres
     */
    private String formatPressure(double pressure) {
        if (pressure > 10e10) {
            return "--";
        }
        if (pressure < 0.001) {
            return "None";
        }
        double atm = pressure / 1013.25;
        if (atm > 0.1) {
            return "%.0f mb (%.2f atm)".formatted(pressure, atm);
        } else {
            return "%.2f mb".formatted(pressure);
        }
    }

    /**
     * Formats a value as percentage
     */
    private String formatPercent(double value) {
        if (value > 10e10 || value < 0) {
            return "--";
        }
        return "%.1f%%".formatted(value * 100);
    }

    /**
     * Formats a numeric value for display.
     * - Values >= 1,000,000: use scientific notation
     * - Values >= 1,000: use comma formatting (e.g., 12,345)
     * - Values >= 1: use 2 decimal places
     * - Values < 1: use up to 4 decimal places
     * - Invalid/extreme values: show "--"
     */
    private String checkValue(double value) {
        try {
            if (value > 10e10 || Double.isNaN(value) || Double.isInfinite(value)) {
                return "--";
            }
            if (value >= 1_000_000) {
                // Use scientific notation for very large values
                return "%.2e".formatted(value);
            } else if (value >= 1000) {
                // Use comma formatting for thousands
                return "%,.0f".formatted(value);
            } else if (value >= 1) {
                // Standard 2 decimal places
                return "%.2f".formatted(value);
            } else if (value >= 0.0001) {
                // Small values with more precision
                return "%.4f".formatted(value);
            } else if (value > 0) {
                // Very small values use scientific notation
                return "%.2e".formatted(value);
            } else {
                return "0";
            }
        } catch (NumberFormatException nfe) {
            return "--";
        }
    }

}
