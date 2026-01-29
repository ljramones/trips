package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.experimental.AsteroidFieldWindow;
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

    /**
     * Shows the Asteroid Field visualization window.
     */
    public void showAsteroidField(ActionEvent actionEvent) {
        log.info("Opening Asteroid Field window");
        try {
            // Create a new window each time, or reuse if already open
            if (asteroidFieldWindow == null || !asteroidFieldWindow.getStage().isShowing()) {
                asteroidFieldWindow = new AsteroidFieldWindow();
                // Set up close handler to clean up resources
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
}
