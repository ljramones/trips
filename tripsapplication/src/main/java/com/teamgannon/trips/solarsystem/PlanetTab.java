package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

public class PlanetTab extends Tab {

    private final Label planetTypeLabel = new Label();
    private final CheckBox isMoon = new CheckBox("Moon");
    private final CheckBox isHabitableMoon = new CheckBox("Habitable Moon");
    private final CheckBox isHabitable = new CheckBox("Habitable");
    private final CheckBox isEarthLike = new CheckBox("Earthlike");
    private final CheckBox isTidallyLocked = new CheckBox("Tidally locked");
    private final CheckBox isGasGiant = new CheckBox("Gas Giant");
    private final CheckBox isHabitableJovian = new CheckBox("Habitable Jovian");
    private final CheckBox isGreenHouseEffect = new CheckBox("Greenhouse Effect");

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

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);

        this.setContent(gridPane);

        TitledPane generalPane = createGeneralSection(planet);
        gridPane.add(generalPane, 0, 0);

        TitledPane dynamicsPane = createDynamicsSection(planet);
        gridPane.add(dynamicsPane, 0, 1);

        TitledPane climatePane = createClimateSection(planet);
        gridPane.add(climatePane, 1, 0);

        TitledPane atmospherePane = createAtmosphereSection(planet);
        gridPane.add(atmospherePane, 1, 1);
    }


    private TitledPane createGeneralSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("General");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);

        pane.setContent(gridPane);

        planetTypeLabel.setText(planet.planetType());
        gridPane.add(new Label("Category"), 0, 0);
        gridPane.add(planetTypeLabel, 1, 0);

        isMoon.setSelected(planet.isMoon());
        isMoon.setDisable(true);
        gridPane.add(isMoon, 0, 1);

        isHabitableMoon.setSelected(planet.isHabitableMoon());
        isHabitableMoon.setDisable(true);
        gridPane.add(isHabitableMoon, 1, 1);

        isHabitable.setSelected(planet.isHabitable());
        isHabitable.setDisable(true);
        gridPane.add(isHabitable, 0, 2);

        isEarthLike.setSelected(planet.isEarthlike());
        isEarthLike.setDisable(true);
        gridPane.add(isEarthLike, 1, 2);

        isGasGiant.setSelected(planet.isGasGiant());
        isGasGiant.setDisable(true);
        gridPane.add(isGasGiant, 0, 3);

        isHabitableJovian.setSelected(planet.isHabitableJovian());
        isHabitableJovian.setDisable(true);
        gridPane.add(isHabitableJovian, 1, 3);

        isTidallyLocked.setSelected(planet.isResonantPeriod());
        isTidallyLocked.setDisable(true);
        gridPane.add(isTidallyLocked, 0, 4);

        isGreenHouseEffect.setSelected(planet.isGreenhouseEffect());
        isGreenHouseEffect.setDisable(true);
        gridPane.add(isGreenHouseEffect, 1, 4);

        return pane;
    }


    private TitledPane createDynamicsSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Dynamics");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);

        pane.setContent(gridPane);

        gridPane.add(new Label("Axial tilt"), 0, 0);
        axialTiltLabel.setText(String.format("%.2g", planet.axialTilt()));
        gridPane.add(axialTiltLabel, 1, 0);

        gridPane.add(new Label("Radius"), 0, 1);
        radiusLabel.setText(String.format("%.2g", planet.getRadius()));
        gridPane.add(radiusLabel, 1, 1);

        gridPane.add(new Label("Core radius"), 0, 2);
        coreRadiusLabel.setText(String.format("%.2g", planet.getCoreRadius()));
        gridPane.add(coreRadiusLabel, 1, 2);

        gridPane.add(new Label("Orbital period"), 0, 3);
        orbitalPeriodLabel.setText(String.format("%.2g", planet.getOrbitalPeriod()));
        gridPane.add(orbitalPeriodLabel, 1, 3);

        gridPane.add(new Label("RMS velocity"), 0, 4);
        rmsVelocityLabel.setText(String.format("%.2g", planet.axialTilt()));
        gridPane.add(rmsVelocityLabel, 1, 4);

        gridPane.add(new Label("Escape velocity"), 0, 5);
        escapeVelocityLabel.setText(String.format("%.2g", planet.getEscapeVelocity()));
        gridPane.add(escapeVelocityLabel, 1, 5);

        gridPane.add(new Label("Day length"), 0, 6);
        dayLengthLabel.setText(String.format("%.2g", planet.getDayLength()));
        gridPane.add(dayLengthLabel, 1, 6);

        gridPane.add(new Label("Surface acceleration"), 0, 7);
        surfaceAccelLabel.setText(String.format("%.2g", planet.getSurfaceAcceleration()));
        gridPane.add(surfaceAccelLabel, 1, 7);

        gridPane.add(new Label("Surface gravity"), 0, 8);
        surfaceGravityLabel.setText(String.format("%.2g", planet.getSurfaceGravity()));
        gridPane.add(surfaceGravityLabel, 1, 8);

        gridPane.add(new Label("Orbital zone"), 0, 9);
        orbitalZoneLabel.setText(Integer.toString(planet.getOrbitalZone()));
        gridPane.add(orbitalZoneLabel, 1, 9);

        return pane;
    }


    private TitledPane createAtmosphereSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Atmosphere");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);

        pane.setContent(gridPane);

        gridPane.add(new Label("Exospheric temperature"), 0, 0);
        exosphericTempLabel.setText(String.format("%.2g", planet.getExosphericTemperature()));
        gridPane.add(exosphericTempLabel, 1, 0);

        gridPane.add(new Label("Estimated temperature"), 0, 1);
        estimatedTempLabel.setText(String.format("%.2g", planet.getEstimatedTemperature()));
        gridPane.add(estimatedTempLabel, 1, 1);

        gridPane.add(new Label("Est. Terrestial temperature"), 0, 2);
        estTerrestialTempLabel.setText(String.format("%.2g", planet.getExosphericTemperature()));
        gridPane.add(estTerrestialTempLabel, 1, 2);

        gridPane.add(new Label("Surface temperature"), 0, 3);
        surfaceTempLabel.setText(String.format("%.2g", planet.getSurfaceTemperature()));
        gridPane.add(surfaceTempLabel, 1, 3);

        gridPane.add(new Label("Low temperature"), 0, 4);
        lowTempLabel.setText(String.format("%.2g", planet.getLowTemperature()));
        gridPane.add(lowTempLabel, 1, 4);

        gridPane.add(new Label("High temperature"), 0, 5);
        highTempLabel.setText(String.format("%.2g", planet.getHighTemperature()));
        gridPane.add(highTempLabel, 1, 5);

        gridPane.add(new Label("Min temperature"), 0, 6);
        minTempLabel.setText(String.format("%.2g", planet.getMinTemperature()));
        gridPane.add(minTempLabel, 1, 6);

        gridPane.add(new Label("Max temperature"), 0, 7);
        maxTempLabel.setText(String.format("%.2g", planet.getMaxTemperature()));
        gridPane.add(maxTempLabel, 1, 7);

        gridPane.add(new Label("Boiling point"), 0, 8);
        boilingPointLabel.setText(String.format("%.2g", planet.getBoilingPoint()));
        gridPane.add(boilingPointLabel, 1, 8);

        gridPane.add(new Label("Greenhouse rise"), 0, 9);
        greenHouseRiseLabel.setText(String.format("%.2g", planet.getGreenhouseRise()));
        gridPane.add(greenHouseRiseLabel, 1, 9);

        return pane;
    }

    private TitledPane createClimateSection(Planet planet) {
        TitledPane pane = new TitledPane();
        pane.setText("Climate");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);

        pane.setContent(gridPane);

        gridPane.add(new Label("Volatile gas inventory"), 0, 0);
        volatileGasInventoryLabel.setText(String.format("%.2g", planet.getVolatileGasInventory()));
        gridPane.add(volatileGasInventoryLabel, 1, 0);

        gridPane.add(new Label("Surface pressure"), 0, 1);
        surfacePressureLabel.setText(String.format("%.2g", planet.getSurfacePressure()));
        gridPane.add(surfacePressureLabel, 1, 1);

        gridPane.add(new Label("Min molecular weight"), 0, 2);
        minMolecularWeightLabel.setText(String.format("%.2g", planet.getMinimumMolecularWeight()));
        gridPane.add(minMolecularWeightLabel, 1, 2);

        gridPane.add(new Label("Hydrosphere"), 0, 3);
        hydrosphereLabel.setText(String.format("%.2g", planet.getHydrosphere()));
        gridPane.add(hydrosphereLabel, 1, 3);

        gridPane.add(new Label("Cloud cover"), 0, 4);
        cloudCoverLabel.setText(String.format("%.2g", planet.getCloudCover()));
        gridPane.add(cloudCoverLabel, 1, 4);

        gridPane.add(new Label("Ice cover"), 0, 5);
        iceCoverLabel.setText(String.format("%.2g", planet.getIceCover()));
        gridPane.add(iceCoverLabel, 1, 5);

        gridPane.add(new Label("Albedo"), 0, 6);
        albedoLabel.setText(String.format("%.2g", planet.getAlbedo()));
        gridPane.add(albedoLabel, 1, 6);

        return pane;
    }

}
