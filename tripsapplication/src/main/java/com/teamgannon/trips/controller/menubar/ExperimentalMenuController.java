package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.experimental.AsteroidFieldWindow;
import com.teamgannon.trips.particlefields.RingFieldFactory;
import com.teamgannon.trips.particlefields.RingFieldWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the Experimental menu.
 * Handles experimental features that are still in development.
 */
@Slf4j
@Component
public class ExperimentalMenuController {

    @FXML
    public MenuItem asteroidFieldMenuItem;

    private AsteroidFieldWindow asteroidFieldWindow;
    private RingFieldWindow ringFieldWindow;

    /**
     * Shows the legacy Asteroid Field visualization window.
     */
    public void showAsteroidField(ActionEvent actionEvent) {
        log.info("Opening Asteroid Field window (legacy)");
        try {
            if (asteroidFieldWindow == null || !asteroidFieldWindow.getStage().isShowing()) {
                asteroidFieldWindow = new AsteroidFieldWindow();
                asteroidFieldWindow.getStage().setOnCloseRequest(event -> {
                    asteroidFieldWindow.dispose();
                    asteroidFieldWindow = null;
                });
            }
            asteroidFieldWindow.show();
            asteroidFieldWindow.getStage().toFront();
        } catch (Exception e) {
            log.error("Error opening Asteroid Field window", e);
            showErrorAlert("Asteroid Field", "Failed to open window: " + e.getMessage());
        }
    }

    // ========== Ring Field Preset Handlers ==========

    public void showSaturnRing(ActionEvent actionEvent) {
        showRingFieldPreset("Saturn Ring");
    }

    public void showUranusRing(ActionEvent actionEvent) {
        showRingFieldPreset("Uranus Ring");
    }

    public void showAsteroidBelt(ActionEvent actionEvent) {
        showRingFieldPreset("Main Asteroid Belt");
    }

    public void showKuiperBelt(ActionEvent actionEvent) {
        showRingFieldPreset("Kuiper Belt");
    }

    public void showProtoplanetaryDisk(ActionEvent actionEvent) {
        showRingFieldPreset("Protoplanetary Disk");
    }

    public void showCollisionDebris(ActionEvent actionEvent) {
        showRingFieldPreset("Collision Debris");
    }

    public void showEmissionNebula(ActionEvent actionEvent) {
        showRingFieldPreset("Emission Nebula");
    }

    public void showDarkNebula(ActionEvent actionEvent) {
        showRingFieldPreset("Dark Nebula");
    }

    public void showBlackHoleAccretion(ActionEvent actionEvent) {
        showRingFieldPreset("Black Hole Accretion");
    }

    public void showNeutronStarAccretion(ActionEvent actionEvent) {
        showRingFieldPreset("Neutron Star Accretion");
    }

    /**
     * Shows the Ring Field window with the specified preset.
     */
    private void showRingFieldPreset(String presetName) {
        log.info("Opening Ring Field window with preset: {}", presetName);
        try {
            if (ringFieldWindow == null || !ringFieldWindow.getStage().isShowing()) {
                ringFieldWindow = RingFieldWindow.fromPreset(presetName);
                ringFieldWindow.getStage().setOnCloseRequest(event -> {
                    ringFieldWindow.dispose();
                    ringFieldWindow = null;
                });
                ringFieldWindow.show();
            } else {
                // Window already open, switch preset
                ringFieldWindow.switchPreset(presetName);
                ringFieldWindow.getStage().toFront();
            }
        } catch (Exception e) {
            log.error("Error opening Ring Field window", e);
            showErrorAlert("Ring Field", "Failed to open window: " + e.getMessage());
        }
    }
}
