package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.controller.TransitFilterPane;
import javafx.fxml.FXML;
import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.dataset.model.DataSetDescriptorCellFactory;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.PlanetarySystemsPane;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.service.DatasetService;
import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RightPanelController {

    @Getter
    @FXML
    private BorderPane rightBorderPane;

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

    public RightPanelController(StarPropertiesPane starPropertiesPane,
                                RoutingPanel routingPanel,
                                ObjectViewPane objectViewPane,
                                PlanetarySystemsPane planetarySystemsPaneContent) {
        this.starPropertiesPane = starPropertiesPane;
        this.routingPanel = routingPanel;
        this.objectViewPane = objectViewPane;
        this.planetarySystemsPaneContent = planetarySystemsPaneContent;
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
        planetarySystemsPane.setMinHeight(150);
        planetarySystemsPane.setMaxHeight(400);
        ScrollPane planetaryScrollPane = new ScrollPane();
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
