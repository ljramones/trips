package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dialogs.search.model.CatalogIdEnum;
import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class FindByCatalogIdDialog extends Dialog<StarSearchResults> {


    private final TextField starName = new TextField();
    private final ChoiceBox<String> datasets = new ChoiceBox<>();
    private final ChoiceBox<String> catalogIdSelection = new ChoiceBox<>();
    private final DatabaseManagementService service;

    public FindByCatalogIdDialog(DatabaseManagementService service, List<String> datasetNames, DataSetDescriptor dataSetDescriptor) {

        this.service = service;
        this.setTitle("Find a star by Catalog Id in database");
        this.setHeight(500);
        this.setWidth(500);

        datasets.getItems().addAll(datasetNames);
        datasets.getSelectionModel().select(dataSetDescriptor.getDataSetName());

        List<String> catalogIds = Arrays.stream(CatalogIdEnum.values()).map(CatalogIdEnum::getCatalogId).toList();
        catalogIdSelection.getItems().addAll(catalogIds);
        catalogIdSelection.getSelectionModel().select(CatalogIdEnum.HIPPARCOS.getCatalogId());

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);


        Label datasetLabel = new Label("Please select dataset: ");
        datasetLabel.setFont(font);
        gridPane.add(datasetLabel, 0, 0);
        gridPane.add(datasets, 1, 0);

        Label catIdLabel = new Label("Please select Catalog Id type: ");
        catIdLabel.setFont(font);
        gridPane.add(catIdLabel, 0, 1);
        gridPane.add(catalogIdSelection, 1, 1);


        Label starSearchLabel = new Label("Please enter the Catalog Id: ");
        starSearchLabel.setFont(font);
        gridPane.add(starSearchLabel, 0, 2);
        gridPane.add(starName, 1, 2);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button gotToStarButton = new Button("find star");
        gotToStarButton.setOnAction(this::searchStarClicked);
        hBox2.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(ActionEvent actionEvent) {
        StarSearchResults findResults = StarSearchResults.builder().starsFound(false).build();
        setResult(findResults);
    }

    private void close(WindowEvent windowEvent) {
        StarSearchResults findResults = StarSearchResults.builder().starsFound(false).build();
        setResult(findResults);
    }

    private void searchStarClicked(ActionEvent actionEvent) {

        String nameToSearch = starName.getText();

        String dataSetName = datasets.getValue();

        String selectedCatalogId = catalogIdSelection.getValue();

        StarObject starObject = null;

        // Use the fromString method from your enum to get the corresponding enum value
        CatalogIdEnum selectedEnum = CatalogIdEnum.fromString(selectedCatalogId);

        switch (selectedEnum) {
            case HIPPARCOS -> {

                // Handle HIPPARCOS case
                log.info("Selected: HIPPARCOS");
                starObject = service.findStarWithHipId(dataSetName, nameToSearch.trim());
            }
            case HD -> {
                // Handle HD case
                log.info("Selected: Henry Draper");
                starObject = service.findStarWithHDId(dataSetName, nameToSearch.trim());
                log.info("star object: {}", starObject);
            }
            case BAYER -> {
                // Handle BAYER case
                log.info("Selected: Bayer");
                starObject = service.findStarWithBayerId(dataSetName, nameToSearch.trim());
            }
            case FLAMSTEED -> {
                log.info("Selected: Flamsteed");
                starObject = service.findStarWithFlamsteedId(dataSetName, nameToSearch.trim());
            }
            case GLIESE -> {
                log.info("Selected: GLIESE");
                starObject = service.findStarWithGJId(dataSetName, nameToSearch.trim());
            }
            case GAIADR2 -> {
                log.info("Selected: GAIADR2");
                starObject = service.findWithGaiaDR2Id(dataSetName, nameToSearch.trim());
            }
            case GAIADR3 -> {
                log.info("Selected: GAIADR3");
                starObject = service.findWithGaiaDR3Id(dataSetName, nameToSearch.trim());
            }
            case GAIAEDR3 -> {
                log.info("Selected: GAIAEDR3");
                starObject = service.findWithGaiaEDR3Id(dataSetName, nameToSearch.trim());
            }
            case TYCHO2 -> {
                log.info("Selected: TYCHO2");
                starObject = service.findWithTychoId(dataSetName, nameToSearch.trim());
            }
            case CSI -> {
                log.info("Selected: CSI");
                starObject = service.findWithCsiId(dataSetName, nameToSearch.trim());
            }
            case TWO_MASS -> {
                log.info("Selected: TWO_MASS");
                starObject = service.findWithTwoMassId(dataSetName, nameToSearch.trim());
            }
            default ->
                // Handle default or other case
                    log.info("Selected: Other or unrecognized catalog");
        }
        if (starObject != null) {
            StarSearchResults findResults = StarSearchResults.builder()
                    .starsFound(true)
                    .nameToSearch(nameToSearch)
                    .dataSetName(dataSetName)
                    .starObject(starObject)
                    .build();
            setResult(findResults);
        } else {
            showErrorAlert("find star", "No star found with that Id");
        }

    }


}
