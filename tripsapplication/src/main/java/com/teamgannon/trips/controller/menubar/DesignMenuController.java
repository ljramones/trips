package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.io.SpaceshipJsonService;
import com.teamgannon.trips.spaceshipmodeller.service.SpaceshipService;
import com.teamgannon.trips.spaceshipmodeller.templates.SpaceshipTemplateLibrary;
import com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipDesignerPanel;
import com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels;
import com.teamgannon.trips.spaceshipmodeller.ui.TransferPlannerLauncher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the "Design" menu, which hosts asset-design tools.
 * <p>
 * Currently it opens the Spaceship Modeller in its own non-modal window, following the same pattern as the
 * Data Workbench ({@code ToolsMenuController.openDataWorkbench}). The window is created lazily and reused:
 * re-selecting the menu item brings the existing window to the front instead of opening a duplicate.
 */
@Slf4j
@Component
public class DesignMenuController {

    private final SpaceshipService spaceshipService;
    private final SpaceshipJsonService jsonService;
    private final SpaceshipTemplateLibrary templateLibrary;
    private final TransferPlannerBridge transferPlannerBridge;
    private final TransferPlannerLauncher transferPlannerLauncher;

    /** Reused window instance; recreated after it is closed. */
    private Stage spaceshipStage;

    public DesignMenuController(SpaceshipService spaceshipService,
                               SpaceshipJsonService jsonService,
                               SpaceshipTemplateLibrary templateLibrary,
                               TransferPlannerBridge transferPlannerBridge,
                               TransferPlannerLauncher transferPlannerLauncher) {
        this.spaceshipService = spaceshipService;
        this.jsonService = jsonService;
        this.templateLibrary = templateLibrary;
        this.transferPlannerBridge = transferPlannerBridge;
        this.transferPlannerLauncher = transferPlannerLauncher;
    }

    /**
     * Confirms the Design menu's controller was instantiated and wired when the menu bar loaded.
     */
    @FXML
    public void initialize() {
        log.debug("DesignMenuController initialized");
    }

    /**
     * Opens (or re-focuses) the Spaceship Modeller window.
     *
     * @param actionEvent the menu action
     */
    public void openSpaceshipModeller(ActionEvent actionEvent) {
        try {
            if (spaceshipStage == null || !spaceshipStage.isShowing()) {
                SpaceshipDesignerPanel panel = new SpaceshipDesignerPanel(
                        spaceshipService, jsonService, templateLibrary, transferPlannerBridge,
                        transferPlannerLauncher);
                spaceshipStage = new Stage();
                spaceshipStage.setTitle(SpaceshipModellerLabels.get("window.title"));
                spaceshipStage.initModality(Modality.NONE);
                spaceshipStage.setScene(new Scene(panel, 1000, 700));
                spaceshipStage.setOnCloseRequest(e -> spaceshipStage = null);
                spaceshipStage.show();
            } else {
                spaceshipStage.toFront();
            }
        } catch (Exception e) {
            log.error("Error opening Spaceship Modeller", e);
            showErrorAlert("Spaceship Modeller", "Failed to open modeller: " + e.getMessage());
        }
    }

    /**
     * Opens (or re-focuses) the Transfer Planner window.
     *
     * @param actionEvent the menu action
     */
    public void openTransferPlanner(ActionEvent actionEvent) {
        try {
            transferPlannerLauncher.open();
        } catch (Exception e) {
            log.error("Error opening Transfer Planner", e);
            showErrorAlert("Transfer Planner", "Failed to open planner: " + e.getMessage());
        }
    }
}
