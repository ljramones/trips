package com.teamgannon.trips.search;

import com.teamgannon.trips.events.ExportQueryEvent;
import com.teamgannon.trips.events.ShowStellarDataEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.components.*;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class SearchPane extends VBox {

    private final SearchContext searchContext;
    private final ApplicationEventPublisher eventPublisher;
    private final DistanceSelectionPanel d2EarthSlider;
    private final StellarClassSelectionPanel stellarClassSelectionPanel = new StellarClassSelectionPanel();
    private final CategorySelectionPanel categorySelectionPanel = new CategorySelectionPanel();
    private final PolitySelectionPanel politySelectionPanel = new PolitySelectionPanel();
    private final TechSelectionPanel techSelectionPanel = new TechSelectionPanel();
    private final FuelSelectionPanel fuelSelectionPanel = new FuelSelectionPanel();
    private final WorldSelectionPanel worldSelectionPanel = new WorldSelectionPanel();
    private final PortSelectionPanel portSelectionPanel = new PortSelectionPanel();
    private final PopulationSelectionPanel populationSelectionPanel = new PopulationSelectionPanel();
    private final MilSpaceSelectionPanel milSpaceSelectionPanel = new MilSpaceSelectionPanel();
    private final MilPlanetSelectionPanel milPlanetSelectionPanel = new MilPlanetSelectionPanel();
    private final ProductsSelectionPanel productsSelectionPanel = new ProductsSelectionPanel();
    private final MiscellaneousSelectionPanel miscellaneousSelectionPanel = new MiscellaneousSelectionPanel();
    private final SpectralComponentSelectionPanel spectralComponentSelectionPanel = new SpectralComponentSelectionPanel();
    private DataSetPanel dataSetChoicePanel;

    @FXML
    private GridPane queryBox;
    @FXML
    private Pane datasetBox;
    @FXML
    private Pane distanceBox;
    @FXML
    private Pane stellarClassBox;
    @FXML
    private Pane categoryBox;
    @FXML
    private Pane fuelBox;
    @FXML
    private Pane worldBox;
    @FXML
    private Pane portBox;
    @FXML
    private Pane populationBox;
    @FXML
    private Pane polityBox;
    @FXML
    private Pane techBox;
    @FXML
    private Pane productsBox;
    @FXML
    private Pane milSpaceBox;
    @FXML
    private Pane milPlanetBox;
    @FXML
    private Pane miscBox;
    @FXML
    private Pane spectralComponentBox;

    /**
     * constructor
     *
     * @param searchContext  the search context
     * @param eventPublisher the event publisher
     */
    public SearchPane(@NotNull SearchContext searchContext,
                      ApplicationEventPublisher eventPublisher) {
        this.searchContext = searchContext;
        this.eventPublisher = eventPublisher;

        DistanceRange distanceRange = DistanceRange
                .builder()
                .min(0)
                .lowValue(0)
                .highValue(20)
                .max(20)
                .build();

        DataSetDescriptor descriptor = searchContext.getDataSetDescriptor();
        if (searchContext.getDataSetDescriptor() != null) {
            log.info("Dataset distance range:{}", descriptor.getDistanceRange());
            distanceRange.setMax(descriptor.getDistanceRange());
        }

        d2EarthSlider = new DistanceSelectionPanel(searchContext.getAstroSearchQuery().getUpperDistanceLimit(), distanceRange);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load SearchPane.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        dataSetChoicePanel = new DataSetPanel(searchContext, eventPublisher);
        // Dataset and Distance are essential - keep expanded
        datasetBox.getChildren().add(createTitledPane("Dataset", dataSetChoicePanel.getPane(), true));
        distanceBox.getChildren().add(createTitledPane("Distance Range", d2EarthSlider.getPane(), true));

        // Stellar filters - collapsed by default
        stellarClassBox.getChildren().add(createTitledPane("Stellar Class", stellarClassSelectionPanel.getPane(), false));
        categoryBox.getChildren().add(createTitledPane("Star Category", categorySelectionPanel.getPane(), false));

        // World building filters - collapsed by default
        fuelBox.getChildren().add(createTitledPane("Fuel Types", fuelSelectionPanel.getPane(), false));
        worldBox.getChildren().add(createTitledPane("World Types", worldSelectionPanel.getPane(), false));
        portBox.getChildren().add(createTitledPane("Port Types", portSelectionPanel.getPane(), false));
        populationBox.getChildren().add(createTitledPane("Population", populationSelectionPanel.getPane(), false));
        polityBox.getChildren().add(createTitledPane("Polity", politySelectionPanel.getPane(), false));

        techBox.getChildren().add(createTitledPane("Technology", techSelectionPanel.getPane(), false));
        productsBox.getChildren().add(createTitledPane("Products", productsSelectionPanel.getPane(), false));
        milSpaceBox.getChildren().add(createTitledPane("Military Space", milSpaceSelectionPanel.getPane(), false));
        milPlanetBox.getChildren().add(createTitledPane("Military Planet", milPlanetSelectionPanel.getPane(), false));
        miscBox.getChildren().add(createTitledPane("Miscellaneous", miscellaneousSelectionPanel.getPane(), false));

        // Advanced spectral component filter
        spectralComponentBox.getChildren().add(createTitledPane("Advanced Spectral Filter", spectralComponentSelectionPanel, false));
    }

    /**
     * Create a collapsible TitledPane wrapper for a panel.
     *
     * @param title    the title for the section
     * @param content  the content node to wrap
     * @param expanded whether the section starts expanded
     * @return the TitledPane wrapper
     */
    private TitledPane createTitledPane(String title, Node content, boolean expanded) {
        TitledPane titledPane = new TitledPane(title, content);
        titledPane.setExpanded(expanded);
        titledPane.setCollapsible(true);
        titledPane.setAnimated(true);
        return titledPane;
    }

    public void setDataSetContext(@NotNull DataSetDescriptor descriptor) {
        dataSetChoicePanel.setDataSetContext(descriptor);
        d2EarthSlider.setDataSetDescriptor(descriptor);
    }

    public void refreshDataSets() {
        dataSetChoicePanel.refreshDatasetChoices();
        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();
        if (descriptor != null) {
            d2EarthSlider.setDataSetDescriptor(descriptor);
        }
    }


    public void updateDataContext(@NotNull DataSetDescriptor dataSetDescriptor) {
        dataSetChoicePanel.updateDataContext(dataSetDescriptor);
    }


    public void removeDataset(DataSetDescriptor dataSetDescriptor) {
        dataSetChoicePanel.removeDataset(dataSetDescriptor);
    }

    public AstroSearchQuery runQuery(boolean showPlot, boolean showTable, boolean doExport) {

        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();

        if (descriptor == null) {
            showErrorAlert("Run Query", "Please select a dataset first");
        } else {
            // pull derived query
            AstroSearchQuery newQuery = createSearchQuery();
            log.info("New search request:{}", newQuery);

            // process file location for export if selected
            if (doExport) {
                eventPublisher.publishEvent(new ExportQueryEvent(this, newQuery));
            }

            if (newQuery.getDataSetContext().getDescriptor() != null) {
                // update main screen
                eventPublisher.publishEvent(new ShowStellarDataEvent(this, newQuery, showPlot, showTable));
            } else {
                showErrorAlert("Query Dialog", "You must specify a dataset!");
            }
            return newQuery;
        }
        return searchContext.getAstroSearchQuery();
    }

    ///////////////   Query Construction   //////////////////

    /**
     * construct the query
     *
     * @return the search query to feed to elasticsearch
     */
    private AstroSearchQuery createSearchQuery() {
        AstroSearchQuery astroSearchQuery = searchContext.getAstroSearchQuery();

        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();
        astroSearchQuery.setDescriptor(descriptor);

        astroSearchQuery.setLowerDistanceLimit(d2EarthSlider.getDistance().getLowValue());
        astroSearchQuery.setUpperDistanceLimit(d2EarthSlider.getDistance().getHighValue());
        astroSearchQuery.setRealStars(categorySelectionPanel.isRealStars());
        astroSearchQuery.setAnomalySearch(miscellaneousSelectionPanel.isAnomalyPresent());
        astroSearchQuery.setOtherSearch(miscellaneousSelectionPanel.isOtherPresent());

        getPolityValues(astroSearchQuery);
        getStellarTypes(astroSearchQuery);
        getSpectralComponentFilter(astroSearchQuery);
        getPortTypes(astroSearchQuery);
        getTechTypes(astroSearchQuery);
        getFuelTypes(astroSearchQuery);
        getPopulationTypes(astroSearchQuery);
        getWorldSearch(astroSearchQuery);
        getMilSpaceSearch(astroSearchQuery);
        getMilPlanSearch(astroSearchQuery);
        getProductSearch(astroSearchQuery);

        return astroSearchQuery;
    }

    private void getProductSearch(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearProductTypes();
        if (productsSelectionPanel.isSelected()) {
            astroSearchQuery.setProductSearch(true);
            astroSearchQuery.addProducts(productsSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setProductSearch(false);
        }
    }

    private void getPortTypes(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearPortTypes();
        if (portSelectionPanel.isSelected()) {
            astroSearchQuery.setPortSearch(true);
            astroSearchQuery.addPorts(portSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setPortSearch(false);
        }
    }

    private void getMilPlanSearch(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearMilPlanTypes();
        if (milPlanetSelectionPanel.isSelected()) {
            astroSearchQuery.setMilPlanetSearch(true);
            astroSearchQuery.addMilPlans(milPlanetSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setMilPlanetSearch(false);
        }
    }

    private void getMilSpaceSearch(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearMilSpaceTypes();
        if (milSpaceSelectionPanel.isSelected()) {
            astroSearchQuery.setMilSpaceSearch(true);
            astroSearchQuery.addMilSpaces(milSpaceSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setMilSpaceSearch(false);
        }
    }

    private void getWorldSearch(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearWorldTypes();
        if (worldSelectionPanel.isSelected()) {
            astroSearchQuery.setWorldSearch(true);
            astroSearchQuery.addWorldTypes(worldSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setWorldSearch(false);
        }
    }

    private void getPopulationTypes(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearPopulationTypes();
        if (populationSelectionPanel.isSelected()) {
            astroSearchQuery.setPopSearch(true);
            astroSearchQuery.addPopulationTypes(populationSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setPopSearch(false);
        }
    }

    private void getFuelTypes(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearFuelTypes();
        if (fuelSelectionPanel.isSelected()) {
            astroSearchQuery.setFuelSearch(true);
            astroSearchQuery.addFuelTypes(fuelSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setFuelSearch(false);
        }
    }

    private void getTechTypes(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearTechTypes();
        if (techSelectionPanel.isSelected()) {
            astroSearchQuery.setTechSearch(true);
            astroSearchQuery.addTechs(techSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setTechSearch(false);
        }
    }

    private void getStellarTypes(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearStellarTypes();
        List<String> stellarTypes = stellarClassSelectionPanel.getSelection();
        if (stellarClassSelectionPanel.isSelected()) {
            astroSearchQuery.addStellarTypes(stellarTypes);
        }
    }

    private void getSpectralComponentFilter(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearSpectralComponentFilter();
        if (spectralComponentSelectionPanel.hasSelections()) {
            astroSearchQuery.setSpectralComponentFilter(
                    spectralComponentSelectionPanel.getSelectedClasses(),
                    spectralComponentSelectionPanel.getSelectedSubtypes(),
                    spectralComponentSelectionPanel.getSelectedLuminosityClasses()
            );
        }
    }

    private void getPolityValues(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearPolities();
        if (politySelectionPanel.isSelected()) {
            astroSearchQuery.addPolities(politySelectionPanel.getPolitySelections());
        }
    }

}
