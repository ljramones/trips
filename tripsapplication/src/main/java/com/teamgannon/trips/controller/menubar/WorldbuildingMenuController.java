package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.construct.ConstructRegistry;
import com.teamgannon.trips.construct.ui.ConstructLabels;
import com.teamgannon.trips.construct.ui.InstallationDesignerPanel;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.io.SpaceshipJsonService;
import com.teamgannon.trips.spaceshipmodeller.service.SpaceshipService;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.TransportNodeService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
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
public class WorldbuildingMenuController {

    private final SpaceshipService spaceshipService;
    private final SpaceshipJsonService jsonService;
    private final SpaceshipTemplateLibrary templateLibrary;
    private final TransferPlannerBridge transferPlannerBridge;
    private final TransferPlannerLauncher transferPlannerLauncher;
    private final ConstructRegistry constructRegistry;
    private final StationDesignerService stationDesignerService;
    private final WeaponInstallationDesignerService weaponInstallationDesignerService;
    private final TransportNodeService transportNodeService;
    private final com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService megastructureDesignerService;
    /** v2 Phase F.1 §5.3 — passed into both designer panels so they filter by active-universe set. */
    private final com.teamgannon.trips.worldbuilding.UniverseFilteringService universeFilteringService;
    /** v2 Phase F.1 Step 7 — backs the Universes dialog's activate/deactivate calls. */
    private final com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService universeDesignerService;
    /** v2 Phase F.2 Step 5 — backs the Aliases dialog's CRUD calls. */
    private final com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService aliasDesignerService;
    /** v2 Phase F.2 §6.3 — used by AliasesDialog + AliasEditorDialog for target name resolution. */
    private final com.teamgannon.trips.jpa.repository.StarObjectRepository starObjectRepository;
    /** v2 Phase F.2 §6.3 — used by AliasesDialog + AliasEditorDialog for exoplanet target resolution. */
    private final com.teamgannon.trips.jpa.repository.ExoPlanetRepository exoPlanetRepository;

    /** Reused window instance; recreated after it is closed. */
    private Stage spaceshipStage;

    /** Reused Installations Designer window instance; recreated after it is closed. */
    private Stage installationStage;

    /** Reused Universes dialog window instance; recreated after it is closed (F.1 Step 7). */
    private Stage universesStage;

    /** Reused Aliases dialog window instance; recreated after it is closed (F.2 Step 5). */
    private Stage aliasesStage;

    public WorldbuildingMenuController(SpaceshipService spaceshipService,
                               SpaceshipJsonService jsonService,
                               SpaceshipTemplateLibrary templateLibrary,
                               TransferPlannerBridge transferPlannerBridge,
                               TransferPlannerLauncher transferPlannerLauncher,
                               ConstructRegistry constructRegistry,
                               StationDesignerService stationDesignerService,
                               WeaponInstallationDesignerService weaponInstallationDesignerService,
                               TransportNodeService transportNodeService,
                               com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService megastructureDesignerService,
                               com.teamgannon.trips.worldbuilding.UniverseFilteringService universeFilteringService,
                               com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService universeDesignerService,
                               com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService aliasDesignerService,
                               com.teamgannon.trips.jpa.repository.StarObjectRepository starObjectRepository,
                               com.teamgannon.trips.jpa.repository.ExoPlanetRepository exoPlanetRepository) {
        this.spaceshipService = spaceshipService;
        this.jsonService = jsonService;
        this.templateLibrary = templateLibrary;
        this.transferPlannerBridge = transferPlannerBridge;
        this.transferPlannerLauncher = transferPlannerLauncher;
        this.constructRegistry = constructRegistry;
        this.stationDesignerService = stationDesignerService;
        this.weaponInstallationDesignerService = weaponInstallationDesignerService;
        this.transportNodeService = transportNodeService;
        this.megastructureDesignerService = megastructureDesignerService;
        this.universeFilteringService = universeFilteringService;
        this.universeDesignerService = universeDesignerService;
        this.aliasDesignerService = aliasDesignerService;
        this.starObjectRepository = starObjectRepository;
        this.exoPlanetRepository = exoPlanetRepository;
    }

    /**
     * Opens (or re-focuses) the Universes dialog — v2 Phase F.1 Step 7. Modeless so the user
     * can have it open alongside the Spaceship Modeller / Installations Designer to see live
     * filter updates on toggle.
     */
    public void openUniversesDialog(ActionEvent actionEvent) {
        try {
            if (universesStage == null || !universesStage.isShowing()) {
                com.teamgannon.trips.worldbuilding.UniversesDialog dialog =
                        new com.teamgannon.trips.worldbuilding.UniversesDialog(
                                universeDesignerService, universeFilteringService);
                universesStage = new Stage();
                universesStage.setTitle("Worldbuilding — Universes");
                universesStage.initModality(Modality.NONE);
                universesStage.setScene(new Scene(dialog, 640, 540));
                // v2 Phase F.1 §5.3 — dispose unsubscribes from the broker; same lifecycle
                // pattern as the designer panels.
                universesStage.setOnCloseRequest(e -> {
                    dialog.dispose();
                    universesStage = null;
                });
                universesStage.show();
            } else {
                universesStage.toFront();
            }
        } catch (Exception e) {
            log.error("Error opening Universes dialog", e);
            showErrorAlert("Universes", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Opens (or re-focuses) the Aliases dialog — v2 Phase F.2 Step 5. Modeless so the user
     * can have it open alongside the Universes dialog and see live filter updates when they
     * toggle universe activation.
     */
    public void openAliasesDialog(ActionEvent actionEvent) {
        try {
            if (aliasesStage == null || !aliasesStage.isShowing()) {
                com.teamgannon.trips.worldbuilding.AliasesDialog dialog =
                        new com.teamgannon.trips.worldbuilding.AliasesDialog(
                                aliasDesignerService, universeDesignerService, universeFilteringService,
                                starObjectRepository, exoPlanetRepository);
                aliasesStage = new Stage();
                aliasesStage.setTitle("Worldbuilding — Aliases");
                aliasesStage.initModality(Modality.NONE);
                aliasesStage.setScene(new Scene(dialog, 920, 540));
                aliasesStage.setOnCloseRequest(e -> {
                    dialog.dispose();
                    aliasesStage = null;
                });
                aliasesStage.show();
            } else {
                aliasesStage.toFront();
            }
        } catch (Exception e) {
            log.error("Error opening Aliases dialog", e);
            showErrorAlert("Aliases", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Confirms the Worldbuilding menu's controller was instantiated and wired when the menu bar loaded.
     */
    @FXML
    public void initialize() {
        log.debug("WorldbuildingMenuController initialized");
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
                        transferPlannerLauncher, universeFilteringService);
                spaceshipStage = new Stage();
                spaceshipStage.setTitle(SpaceshipModellerLabels.get("window.title"));
                spaceshipStage.initModality(Modality.NONE);
                spaceshipStage.setScene(new Scene(panel, 1000, 700));
                // v2 Phase F.1 §5.3 — panel.dispose() unsubscribes from the UniverseFilteringService
                // broker so a closed panel doesn't keep receiving activation events (leak).
                spaceshipStage.setOnCloseRequest(e -> {
                    panel.dispose();
                    spaceshipStage = null;
                });
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
     * Opens (or re-focuses) the Installations Designer window — Phase C read-only browser over
     * the non-spaceship Constructs (stations, weapon installations, transport nodes).
     *
     * @param actionEvent the menu action
     */
    public void openInstallationDesigner(ActionEvent actionEvent) {
        try {
            if (installationStage == null || !installationStage.isShowing()) {
                InstallationDesignerPanel panel = new InstallationDesignerPanel(
                        constructRegistry,
                        stationDesignerService,
                        weaponInstallationDesignerService,
                        transportNodeService,
                        megastructureDesignerService,
                        universeFilteringService);
                installationStage = new Stage();
                installationStage.setTitle(ConstructLabels.get("window.title"));
                installationStage.initModality(Modality.NONE);
                installationStage.setScene(new Scene(panel, 1100, 700));
                // v2 Phase F.1 §5.3 — dispose unsubscribes from the broker; see openSpaceshipModeller.
                installationStage.setOnCloseRequest(e -> {
                    panel.dispose();
                    installationStage = null;
                });
                installationStage.show();
                // Fire the background load after the stage is showing so a slow registry read
                // can't delay the window appearing on the user's screen.
                panel.loadAsync();
            } else {
                installationStage.toFront();
            }
        } catch (Exception e) {
            log.error("Error opening Installations Designer", e);
            showErrorAlert("Installations Designer", "Failed to open designer: " + e.getMessage());
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
