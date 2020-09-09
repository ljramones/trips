package com.teamgannon.trips.search;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StellarDataUpdaterListener;
import com.teamgannon.trips.search.components.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class SearchPane extends Pane {

    private final SearchContext searchContext;
    private final DataSetContext dataSetContext;
    private final DataSetChangeListener dataSetChangeListener;
    private final StellarDataUpdaterListener updater;
    private DataSetPanel dataSetChoicePanel;
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


    /**
     * constructor
     *
     * @param query          the search context
     * @param dataSetContext the data set context
     * @param updater        the data updater
     */
    public SearchPane(SearchContext query,
                      DataSetContext dataSetContext,
                      DataSetChangeListener dataSetChangeListener,
                      StellarDataUpdaterListener updater) {
        this.searchContext = query;
        this.dataSetContext = dataSetContext;
        this.dataSetChangeListener = dataSetChangeListener;
        this.updater = updater;

        double distanceRange = 20.0;
        if (dataSetContext.getDescriptor() != null) {
            distanceRange = dataSetContext.getDescriptor().getDistanceRange();
        }
        d2EarthSlider = new DistanceSelectionPanel(query.getAstroSearchQuery().getDistanceFromCenterStar(), distanceRange);

        this.getChildren().add(createContent());
    }

    private Node createContent() {

        VBox vBox = new VBox();
        vBox.setSpacing(10);

        GridPane queryBox = new GridPane();
        queryBox.setPadding(new Insets(10, 10, 10, 10));
        queryBox.setVgap(5);
        queryBox.setHgap(5);

        dataSetChoicePanel = new DataSetPanel(searchContext, dataSetContext, dataSetChangeListener);
        queryBox.add(dataSetChoicePanel.getPane(),0,1,2,1);
        queryBox.add(d2EarthSlider.getPane(),0,2, 2,1);
        queryBox.add(stellarClassSelectionPanel.getPane(),0,3);
        queryBox.add(categorySelectionPanel.getPane(),0,4);
        queryBox.add(fuelSelectionPanel.getPane(),0,5);
        queryBox.add(worldSelectionPanel.getPane(),0,6);
        queryBox.add(portSelectionPanel.getPane(),0,7);
        queryBox.add(populationSelectionPanel.getPane(),0,8);
        queryBox.add(politySelectionPanel.getPane(),0,9);

        queryBox.add(techSelectionPanel.getPane(),1,3);
        queryBox.add(productsSelectionPanel.getPane(),1,4);
        queryBox.add(milSpaceSelectionPanel.getPane(),1,5);
        queryBox.add(milPlanetSelectionPanel.getPane(),1,6);
        queryBox.add(miscellaneousSelectionPanel.getPane(),1,7);

        vBox.getChildren().add(queryBox);

        return vBox;
    }

    public void setDataSetContext(DataSetDescriptor descriptor) {
        dataSetChoicePanel.setDataSetContext(descriptor);
        d2EarthSlider.setMaxRange(descriptor.getDistanceRange());
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

        DataSetDescriptor descriptor = dataSetChoicePanel.getSelected();
        astroSearchQuery.setDescriptor(descriptor);

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
