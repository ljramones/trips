package com.teamgannon.trips.search;

import com.teamgannon.trips.search.components.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * The search query user interface
 * <p>
 * Created by larrymitchell on 2017-04-07.
 */
public class PlotQueryFilterWindow extends Dialog<AstroSearchQuery> {

    private AstroSearchQuery astroSearchQuery;

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

    /**
     * create the dialog
     *
     * @param astroSearchQuery the query to search for
     */
    public PlotQueryFilterWindow(AstroSearchQuery astroSearchQuery) {

        this.astroSearchQuery = astroSearchQuery;

        final DialogPane dialogPane = getDialogPane();

        setTitle("Select Parameters for Search");

        // TODO add style
        dialogPane.setId("search-star-dialog");

        // the main pane
        dialogPane.setContent(createContent());

        // configure buttons: you may cancel or finish the dialog
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // configure a standard button ...
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("Search");

        // IMPORTANT : without converter there is no data to return
        setResultConverter(dialogButton -> {
            ButtonBar.ButtonData data =
                    dialogButton == null ? null : dialogButton.getButtonData();

            AstroSearchQuery astroSearchQuery1 = PlotQueryFilterWindow.this.createSearchQuery();

            return data == ButtonBar.ButtonData.OK_DONE
                    ? astroSearchQuery1
                    : null;
        });
    }

    private Node createContent() {

        VBox vBox = new VBox();
        vBox.setPrefWidth(675.0);
        vBox.setSpacing(10);

        vBox.getChildren().add(d2EarthSlider.getPane());
        vBox.getChildren().add(stellarClassSelectionPanel.getPane());
        vBox.getChildren().add(categorySelectionPanel.getPane());
        vBox.getChildren().add(fuelSelectionPanel.getPane());
        vBox.getChildren().add(worldSelectionPanel.getPane());
        vBox.getChildren().add(portSelectionPanel.getPane());
        vBox.getChildren().add(populationSelectionPanel.getPane());
        vBox.getChildren().add(politySelectionPanel.getPane());
        vBox.getChildren().add(techSelectionPanel.getPane());
        vBox.getChildren().add(productsSelectionPanel.getPane());
        vBox.getChildren().add(milSpaceSelectionPanel.getPane());
        vBox.getChildren().add(milPlanetSelectionPanel.getPane());
        vBox.getChildren().add(miscellaneousSelectionPanel.getPane());

        return vBox;
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
