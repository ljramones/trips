package com.teamgannon.trips.search;

import com.teamgannon.trips.search.components.*;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchPane extends Pane {

    private AstroSearchQuery astroSearchQuery;
    private StellarDataUpdater updater;

    private DistanceSelectionPanel d2EarthSlider = new DistanceSelectionPanel();
    private StellarClassSelectionPanel stellarClassSelectionPanel = new StellarClassSelectionPanel();
    private CategorySelectionPanel categorySelectionPanel = new CategorySelectionPanel();
    private PolitySelectionPanel politySelectionPanel = new PolitySelectionPanel();
    private TechSelectionPanel techSelectionPanel = new TechSelectionPanel();
    private FuelSelectionPanel fuelSelectionPanel = new FuelSelectionPanel();
    private WorldSelectionPanel worldSelectionPanel = new WorldSelectionPanel();
    private PortSelectionPanel portSelectionPanel = new PortSelectionPanel();
    private PopulationSelectionPanel populationSelectionPanel = new PopulationSelectionPanel();
    private MilSpaceSelectionPanel milSpaceSelectionPanel = new MilSpaceSelectionPanel();
    private MilPlanetSelectionPanel milPlanetSelectionPanel = new MilPlanetSelectionPanel();
    private ProductsSelectionPanel productsSelectionPanel = new ProductsSelectionPanel();
    private MiscellaneousSelectionPanel miscellaneousSelectionPanel = new MiscellaneousSelectionPanel();

    private Button searchButton = new Button("New Search");


    public SearchPane(AstroSearchQuery query, StellarDataUpdater updater) {
        this.astroSearchQuery = query;
        this.updater = updater;
        this.getChildren().add(createContent());
    }

    private Node createContent() {

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        VBox queryBox = new VBox();
        queryBox.setPrefWidth(675.0);
        queryBox.setSpacing(10);

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

        VBox buttonBox = new VBox();

        searchButton.setOnAction(this::handleButtonAction);
        searchButton.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(searchButton);
        buttonBox.setAlignment(Pos.CENTER);

        vBox.getChildren().add(queryBox);
        vBox.getChildren().add(buttonBox);

        return vBox;
    }

    private void handleButtonAction(ActionEvent event) {

        // pull derived query
        AstroSearchQuery newQuery = createSearchQuery();
        log.info("New search request:{}", newQuery);

        // update main screen
        updater.showNewStellarData(newQuery);
    }

    ///////////////   Query Construction   //////////////////

    /**
     * construct the query
     *
     * @return the search query to feed to elasticsearch
     */
    private AstroSearchQuery createSearchQuery() {

        astroSearchQuery.setDistanceFromCenterStar(d2EarthSlider.getDistance());
        astroSearchQuery.setRealStars(categorySelectionPanel.isRealStars());
        astroSearchQuery.setFictionalStars(categorySelectionPanel.isFictionalStars());
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
