package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.controller.TransitFilterPane;
import javafx.fxml.FXML;
import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.dataset.model.DataSetDescriptorCellFactory;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.PlanetarySystemsPane;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.screenobjects.planetary.PlanetarySidePane;
import com.teamgannon.trips.screenobjects.solarsystem.SolarSystemSidePane;
import com.teamgannon.trips.service.DatasetService;
import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RightPanelController {

    @Getter
    @FXML
    private BorderPane rightBorderPane;

    // Stack pane for contextual side pane switching
    private StackPane contextStackPane;

    @Getter
    @FXML
    private VBox settingsPane;

    @Getter
    @FXML
    private Accordion propertiesAccordion;

    @Getter
    @FXML
    private TitledPane datasetsPane;
    @Getter
    @FXML
    private TitledPane objectsViewPane;
    @Getter
    @FXML
    private TitledPane planetarySystemsPane;
    @Getter
    @FXML
    private TitledPane stellarObjectPane;
    @Getter
    @FXML
    private TitledPane transitPane;
    @Getter
    @FXML
    private TitledPane routingPane;
    @FXML
    private ScrollPane stellarObjectScrollPane;
    @FXML
    private ScrollPane transitScrollPane;
    @Getter
    private TransitFilterPane transitFilterPane;
    private final ListView<DataSetDescriptor> dataSetsListView = new ListView<>();

    private final StarPropertiesPane starPropertiesPane;
    @Getter
    private final RoutingPanel routingPanel;
    @Getter
    private final ObjectViewPane objectViewPane;
    @Getter
    private final PlanetarySystemsPane planetarySystemsPaneContent;
    @Getter
    private final SolarSystemSidePane solarSystemSidePane;
    @Getter
    private final PlanetarySidePane planetarySidePane;

    public RightPanelController(StarPropertiesPane starPropertiesPane,
                                RoutingPanel routingPanel,
                                ObjectViewPane objectViewPane,
                                PlanetarySystemsPane planetarySystemsPaneContent,
                                SolarSystemSidePane solarSystemSidePane,
                                PlanetarySidePane planetarySidePane) {
        this.starPropertiesPane = starPropertiesPane;
        this.routingPanel = routingPanel;
        this.objectViewPane = objectViewPane;
        this.planetarySystemsPaneContent = planetarySystemsPaneContent;
        this.solarSystemSidePane = solarSystemSidePane;
        this.planetarySidePane = planetarySidePane;
    }

    @FXML
    public void initialize() {
        settingsPane.setPrefHeight(588.0);
        settingsPane.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        rightBorderPane.setMinWidth(0);

        datasetsPane.setMinWidth(MainPane.SIDE_PANEL_SIZE);
        datasetsPane.setMinHeight(200);
        datasetsPane.setMaxHeight(500);

        objectsViewPane.setMinWidth(MainPane.SIDE_PANEL_SIZE);
        objectsViewPane.setMinHeight(200);
        objectsViewPane.setMaxHeight(460);

        planetarySystemsPane.setMinWidth(MainPane.SIDE_PANEL_SIZE);
        planetarySystemsPane.setMinHeight(200);
        planetarySystemsPane.setPrefHeight(300);
        planetarySystemsPane.setMaxHeight(500);
        ScrollPane planetaryScrollPane = new ScrollPane();
        planetaryScrollPane.setFitToWidth(true);
        planetaryScrollPane.setContent(planetarySystemsPaneContent);
        planetarySystemsPane.setContent(planetaryScrollPane);

        stellarObjectPane.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        stellarObjectPane.setPrefHeight(500);
        stellarObjectPane.setMaxHeight(520);
        stellarObjectScrollPane.setContent(starPropertiesPane);

        transitPane.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        transitPane.setPrefHeight(500);
        transitPane.setMaxHeight(520);
        transitFilterPane = new TransitFilterPane();
        transitScrollPane.setContent(transitFilterPane);

        routingPane.setMinWidth(MainPane.SIDE_PANEL_SIZE);
        routingPane.setMinHeight(400);
        routingPane.setMaxHeight(400);
        routingPane.setContent(routingPanel);

        // Create context stack pane to enable switching between interstellar and solar system side panes
        setupContextStackPane();
    }

    /**
     * Set up the stack pane for contextual side pane switching.
     * Wraps the interstellar accordion, solar system side pane, and planetary side pane.
     * Only ONE pane is visible at a time - they do NOT share content.
     */
    private void setupContextStackPane() {
        // Create the stack pane
        contextStackPane = new StackPane();

        // Remove accordion from settings pane and add to stack
        settingsPane.getChildren().remove(propertiesAccordion);

        // Add all panes to the stack
        contextStackPane.getChildren().addAll(
                propertiesAccordion,      // Interstellar side pane
                solarSystemSidePane,      // Solar system side pane
                planetarySidePane         // Planetary side pane
        );

        // Add stack pane to settings pane
        settingsPane.getChildren().add(0, contextStackPane);

        // IMPORTANT: Hide solar system and planetary panes completely at startup
        // Only interstellar pane should be visible initially
        solarSystemSidePane.setVisible(false);
        solarSystemSidePane.setManaged(false);

        planetarySidePane.setVisible(false);
        planetarySidePane.setManaged(false);

        propertiesAccordion.setVisible(true);
        propertiesAccordion.setManaged(true);
        propertiesAccordion.toFront();

        log.info("Context stack pane initialized - interstellar side pane visible, solar system and planetary side panes hidden");
    }

    /**
     * Switch to the interstellar side pane (default view).
     */
    public void showInterstellarSidePane() {
        // Hide solar system pane completely
        solarSystemSidePane.setVisible(false);
        solarSystemSidePane.setManaged(false);
        solarSystemSidePane.clear();

        // Hide planetary pane completely
        planetarySidePane.setVisible(false);
        planetarySidePane.setManaged(false);
        planetarySidePane.clear();

        // Show interstellar pane
        propertiesAccordion.setVisible(true);
        propertiesAccordion.setManaged(true);
        propertiesAccordion.toFront();

        log.info("Switched to interstellar side pane");
    }

    /**
     * Switch to the solar system side pane.
     */
    public void showSolarSystemSidePane(SolarSystemDescription system) {
        // Hide interstellar pane completely
        propertiesAccordion.setVisible(false);
        propertiesAccordion.setManaged(false);

        // Hide planetary pane completely
        planetarySidePane.setVisible(false);
        planetarySidePane.setManaged(false);
        planetarySidePane.clear();

        // Show solar system pane
        solarSystemSidePane.setSystem(system);
        solarSystemSidePane.setVisible(true);
        solarSystemSidePane.setManaged(true);
        solarSystemSidePane.toFront();

        log.info("Switched to solar system side pane for: {}",
                system != null && system.getStarDisplayRecord() != null
                        ? system.getStarDisplayRecord().getStarName()
                        : "null");
    }

    /**
     * Switch to the planetary side pane.
     */
    public void showPlanetarySidePane(PlanetaryContext context) {
        // Hide interstellar pane completely
        propertiesAccordion.setVisible(false);
        propertiesAccordion.setManaged(false);

        // Hide solar system pane completely
        solarSystemSidePane.setVisible(false);
        solarSystemSidePane.setManaged(false);
        solarSystemSidePane.clear();

        // Show planetary pane
        planetarySidePane.setContext(context);
        planetarySidePane.setVisible(true);
        planetarySidePane.setManaged(true);
        planetarySidePane.toFront();

        log.info("Switched to planetary side pane for: {}",
                context != null ? context.getPlanetName() : "null");
    }

    public void setupObjectViewPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(objectViewPane);
        objectsViewPane.setContent(scrollPane);
    }

    public List<DataSetDescriptor> setupDataSetView(DatasetService datasetService,
                                                    ApplicationEventPublisher eventPublisher,
                                                    SearchContextCoordinator searchContextCoordinator) {
        datasetsPane.setContent(dataSetsListView);
        dataSetsListView.setPrefHeight(10);
        dataSetsListView.setCellFactory(new DataSetDescriptorCellFactory(eventPublisher));
        dataSetsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                DataSetDescriptor current = searchContextCoordinator.getCurrentDescriptor();
                if (current != null && current.getDataSetName().equals(newValue.getDataSetName())) {
                    return;
                }
                searchContextCoordinator.setDescriptor(newValue);
                eventPublisher.publishEvent(new com.teamgannon.trips.events.SetContextDataSetEvent(this, newValue));
                eventPublisher.publishEvent(new com.teamgannon.trips.events.StatusUpdateEvent(this,
                        "Selected dataset " + newValue.getDataSetName()));
            }
        });

        List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
        addDataSetToList(dataSetDescriptorList, true);
        return dataSetDescriptorList;
    }

    public void refreshDataSets(DatasetService datasetService) {
        List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
        addDataSetToList(dataSetDescriptorList, true);
    }


    public void addDataSetToList(List<DataSetDescriptor> list, boolean clear) {
        if (clear) {
            dataSetsListView.getItems().clear();
        }
        list.forEach(descriptor -> dataSetsListView.getItems().add(descriptor));
    }

    public void selectDataSet(DataSetDescriptor descriptor) {
        dataSetsListView.getSelectionModel().select(descriptor);
    }

    /**
     * Refresh the planetary systems list.
     */
    public void refreshPlanetarySystems() {
        planetarySystemsPaneContent.refresh();
    }

}
