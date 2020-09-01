package com.teamgannon.trips.search;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.search.components.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class SearchPane extends Pane {

    private final SearchContext searchContext;
    private final DataSetContext dataSetContext;
    private final StellarDataUpdater updater;
    private final DistanceSelectionPanel d2EarthSlider = new DistanceSelectionPanel();
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


    public SearchPane(SearchContext query, DataSetContext dataSetContext, StellarDataUpdater updater) {
        this.searchContext = query;
        this.dataSetContext = dataSetContext;
        this.updater = updater;
        this.getChildren().add(createContent());
    }

    private Node createContent() {

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        VBox queryBox = new VBox();
        queryBox.setPrefWidth(675.0);
        queryBox.setSpacing(10);

        DataSetPanel dataSetChoicePanel = new DataSetPanel(searchContext, dataSetContext);
        queryBox.getChildren().add(dataSetChoicePanel.getPane());
        queryBox.getChildren().add(d2EarthSlider.getPane());
        queryBox.getChildren().add(stellarClassSelectionPanel.getPane());
        queryBox.getChildren().add(categorySelectionPanel.getPane());
        queryBox.getChildren().add(fuelSelectionPanel.getPane());
        queryBox.getChildren().add(worldSelectionPanel.getPane());
        queryBox.getChildren().add(portSelectionPanel.getPane());
        queryBox.getChildren().add(populationSelectionPanel.getPane());
        queryBox.getChildren().add(politySelectionPanel.getPane());
        queryBox.getChildren().add(techSelectionPanel.getPane());
        queryBox.getChildren().add(productsSelectionPanel.getPane());
        queryBox.getChildren().add(milSpaceSelectionPanel.getPane());
        queryBox.getChildren().add(milPlanetSelectionPanel.getPane());
        queryBox.getChildren().add(miscellaneousSelectionPanel.getPane());

        vBox.getChildren().add(queryBox);

        return vBox;
    }

    public void runQuery(boolean showPlot, boolean showTable) {

        // pull derived query
        AstroSearchQuery newQuery = createSearchQuery();
        log.info("New search request:{}", newQuery);

        if (newQuery.getDescriptor() != null) {
            // update main screen
            updater.showNewStellarData(newQuery, showPlot, showTable);
        } else {
            showErrorAlert("Query Dialog", "You must specify a dataset!");
        }
    }

    ///////////////   Query Construction   //////////////////

    /**
     * construct the query
     *
     * @return the search query to feed to elasticsearch
     */
    private AstroSearchQuery createSearchQuery() {
        AstroSearchQuery astroSearchQuery = searchContext.getAstroSearchQuery();

        astroSearchQuery.setDistanceFromCenterStar(d2EarthSlider.getDistance());
        astroSearchQuery.setRealStars(categorySelectionPanel.isRealStars());
        astroSearchQuery.setAnomalySearch(miscellaneousSelectionPanel.isAnomalyPresent());
        astroSearchQuery.setOtherSearch(miscellaneousSelectionPanel.isOtherPresent());

        getPolityValues(astroSearchQuery);
        getStellarTypes(astroSearchQuery);
        getPortTypes(astroSearchQuery);
        getTechTypes(astroSearchQuery);
        getStellarTypes(astroSearchQuery);
        getFuelTypes(astroSearchQuery);
        getPopulationTypes(astroSearchQuery);
        getWorldSearch(astroSearchQuery);
        getMilSpaceSearch(astroSearchQuery);
        getMilPlanSearch(astroSearchQuery);
        getProductSearch(astroSearchQuery);

        return astroSearchQuery;
    }

    private void getProductSearch(AstroSearchQuery astroSearchQuery) {
        if (productsSelectionPanel.isSelected()) {
            astroSearchQuery.setProductSearch(true);
            astroSearchQuery.addProducts(productsSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setProductSearch(false);
        }
    }

    private void getPortTypes(AstroSearchQuery astroSearchQuery) {
        if (portSelectionPanel.isSelected()) {
            astroSearchQuery.setPortSearch(false);
            astroSearchQuery.addPorts(portSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setPortSearch(false);
        }
    }

    private void getMilPlanSearch(AstroSearchQuery astroSearchQuery) {
        if (milPlanetSelectionPanel.isSelected()) {
            astroSearchQuery.setMilPlanetSearch(true);
            astroSearchQuery.addMilPlans(milPlanetSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setMilPlanetSearch(false);
        }
    }

    private void getMilSpaceSearch(AstroSearchQuery astroSearchQuery) {
        if (milSpaceSelectionPanel.isSelected()) {
            astroSearchQuery.setMilSpaceSearch(true);
            astroSearchQuery.addMilSpaces(milSpaceSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setMilSpaceSearch(false);
        }
    }

    private void getWorldSearch(AstroSearchQuery astroSearchQuery) {
        if (worldSelectionPanel.isSelected()) {
            astroSearchQuery.setWorldSearch(true);
            astroSearchQuery.addWorldTypes(worldSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setWorldSearch(false);
        }
    }

    private void getPopulationTypes(AstroSearchQuery astroSearchQuery) {
        if (populationSelectionPanel.isSelected()) {
            astroSearchQuery.setPopSearch(true);
            astroSearchQuery.addPopulationTypes(populationSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setPopSearch(false);
        }
    }

    private void getFuelTypes(AstroSearchQuery astroSearchQuery) {
        if (fuelSelectionPanel.isSelected()) {
            astroSearchQuery.setFuelSearch(true);
            astroSearchQuery.addFuelTypes(fuelSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setFuelSearch(false);
        }
    }

    private void getTechTypes(AstroSearchQuery astroSearchQuery) {
        if (techSelectionPanel.isSelected()) {
            astroSearchQuery.addTechs(techSelectionPanel.getSelections());
        } else {
            astroSearchQuery.setTechSearch(false);
        }
    }

    private void getStellarTypes(AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.addStellarTypes(stellarClassSelectionPanel.getSelection());
    }

    private void getPolityValues(AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.addPolities(politySelectionPanel.getPolitySelections());
    }

}
