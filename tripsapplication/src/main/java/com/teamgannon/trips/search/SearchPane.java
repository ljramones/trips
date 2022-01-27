package com.teamgannon.trips.search;

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
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class SearchPane extends Pane {

    private final SearchContext searchContext;
    private final DataSetChangeListener dataSetChangeListener;
    private final StellarDataUpdaterListener updater;
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
    private DataSetPanel dataSetChoicePanel;


    /**
     * constructor
     *
     * @param searchContext the search context
     * @param updater       the data updater
     */
    public SearchPane(@NotNull SearchContext searchContext,
                      DataSetChangeListener dataSetChangeListener,
                      StellarDataUpdaterListener updater) {
        this.searchContext = searchContext;
        this.dataSetChangeListener = dataSetChangeListener;
        this.updater = updater;

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

        this.getChildren().add(createContent());
    }

    private @NotNull Node createContent() {

        VBox vBox = new VBox();
        vBox.setSpacing(10);

        GridPane queryBox = new GridPane();
        queryBox.setPadding(new Insets(10, 10, 10, 10));
        queryBox.setVgap(5);
        queryBox.setHgap(5);

        dataSetChoicePanel = new DataSetPanel(searchContext, dataSetChangeListener);
        queryBox.add(dataSetChoicePanel.getPane(), 0, 1, 2, 1);
        queryBox.add(d2EarthSlider.getPane(), 0, 2, 2, 1);
        queryBox.add(stellarClassSelectionPanel.getPane(), 0, 3);
        queryBox.add(categorySelectionPanel.getPane(), 0, 4);
        queryBox.add(fuelSelectionPanel.getPane(), 0, 5);
        queryBox.add(worldSelectionPanel.getPane(), 0, 6);
        queryBox.add(portSelectionPanel.getPane(), 0, 7);
        queryBox.add(populationSelectionPanel.getPane(), 0, 8);
        queryBox.add(politySelectionPanel.getPane(), 0, 9);

        queryBox.add(techSelectionPanel.getPane(), 1, 3);
        queryBox.add(productsSelectionPanel.getPane(), 1, 4);
        queryBox.add(milSpaceSelectionPanel.getPane(), 1, 5);
        queryBox.add(milPlanetSelectionPanel.getPane(), 1, 6);
        queryBox.add(miscellaneousSelectionPanel.getPane(), 1, 7);

        vBox.getChildren().add(queryBox);

        return vBox;
    }

    public void setDataSetContext(@NotNull DataSetDescriptor descriptor) {
        dataSetChoicePanel.setDataSetContext(descriptor);
        d2EarthSlider.setDataSetDescriptor(descriptor);
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
                updater.doExport(newQuery);
            }

            if (newQuery.getDataSetContext().getDescriptor() != null) {
                // update main screen
                updater.showNewStellarData(newQuery, showPlot, showTable);
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
            astroSearchQuery.setPortSearch(false);
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

    private void getPolityValues(@NotNull AstroSearchQuery astroSearchQuery) {
        astroSearchQuery.clearPolities();
        if (politySelectionPanel.isSelected()) {
            astroSearchQuery.addPolities(politySelectionPanel.getPolitySelections());
        }
    }

}
