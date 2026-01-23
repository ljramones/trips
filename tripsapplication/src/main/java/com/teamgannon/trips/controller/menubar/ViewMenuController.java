package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.MainSplitPaneManager;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the View menu.
 * Handles plot display, routes panel, and screenshot functionality.
 */
@Slf4j
@Component
public class ViewMenuController {

    @FXML
    public MenuItem showRoutesMenuitem;

    private final TripsContext tripsContext;
    private final SharedUIFunctions sharedUIFunctions;
    private final SharedUIState sharedUIState;
    private final InterstellarSpacePane interstellarSpacePane;
    private final MainSplitPaneManager mainSplitPaneManager;

    public ViewMenuController(TripsContext tripsContext,
                              SharedUIFunctions sharedUIFunctions,
                              SharedUIState sharedUIState,
                              InterstellarSpacePane interstellarSpacePane,
                              MainSplitPaneManager mainSplitPaneManager) {
        this.tripsContext = tripsContext;
        this.sharedUIFunctions = sharedUIFunctions;
        this.sharedUIState = sharedUIState;
        this.interstellarSpacePane = interstellarSpacePane;
        this.mainSplitPaneManager = mainSplitPaneManager;
    }

    /**
     * Plots stars from the current search context.
     */
    public void plotStars(ActionEvent actionEvent) {
        try {
            sharedUIFunctions.plotStars();
        } catch (Exception e) {
            log.error("Error plotting stars", e);
            showErrorAlert("Plot Stars", "Failed to plot stars: " + e.getMessage());
        }
    }

    /**
     * Shows the routes panel in the side pane.
     */
    public void showRoutes(ActionEvent actionEvent) {
        try {
            sharedUIState.setSidePaneOn(true);
            sharedUIFunctions.applySidePaneState(true);
            mainSplitPaneManager.getPropertiesAccordion().setExpandedPane(mainSplitPaneManager.getRoutingPane());
        } catch (Exception e) {
            log.error("Error showing routes panel", e);
            showErrorAlert("Show Routes", "Failed to show routes: " + e.getMessage());
        }
    }

    /**
     * Takes a snapshot of the current plot and saves it to a file.
     */
    public void onSnapShot(ActionEvent actionEvent) {
        try {
            WritableImage image = interstellarSpacePane.snapshot(new SnapshotParameters(), null);

            FileChooser saveFileChooser = new FileChooser();
            saveFileChooser.setTitle("Save Plot Snapshot");
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            saveFileChooser.getExtensionFilters().add(extFilter);
            saveFileChooser.setInitialDirectory(new File("."));

            Stage stage = getStageFromEvent(actionEvent);
            File file = saveFileChooser.showSaveDialog(stage);
            if (file != null) {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                log.info("Snapshot saved to: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Unable to save the snapshot file", e);
            showErrorAlert("Snapshot Error", "Failed to save snapshot: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error taking snapshot", e);
            showErrorAlert("Snapshot Error", "Failed to take snapshot: " + e.getMessage());
        }
    }

    /**
     * Attempts to get the Stage from the action event source.
     */
    private Stage getStageFromEvent(ActionEvent actionEvent) {
        try {
            if (actionEvent != null && actionEvent.getSource() instanceof javafx.scene.Node) {
                javafx.scene.Node source = (javafx.scene.Node) actionEvent.getSource();
                return (Stage) source.getScene().getWindow();
            }
        } catch (Exception e) {
            log.debug("Could not get stage from event, using null", e);
        }
        return null;
    }
}
