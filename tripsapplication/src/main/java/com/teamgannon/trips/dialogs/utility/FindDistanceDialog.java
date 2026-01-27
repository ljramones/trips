package com.teamgannon.trips.dialogs.utility;


import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.dialogs.utility.model.DistanceCalculationObject;
import com.teamgannon.trips.dialogs.utility.model.StarSelectionObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

public class FindDistanceDialog extends Dialog<DistanceCalculationObject> {

    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    private final DistanceCalculationObject distanceCalculationObject = DistanceCalculationObject.builder().build();

    private final TextField fromStarName = new TextField();
    private final TextField toStarName = new TextField();

    private final Label distanceLabel = new Label("0.0");
    private final Label fromStarNameLabel = new Label("Not Selected Yet");
    private final Label toStarNameLabel = new Label("Not Selected Yet");

    private final ChoiceBox<String> datasets = new ChoiceBox<>();

    public FindDistanceDialog(List<String> datasetNames,
                              DataSetDescriptor dataSetDescriptor,
                              DatabaseManagementService databaseManagementService,
                              StarService starService) {
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;

        datasets.getItems().addAll(datasetNames);
        datasets.getSelectionModel().select(dataSetDescriptor.getDataSetName());

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

        this.setTitle("Calculate the Distance between stars");
        this.setHeight(600);
        this.setWidth(500);

        Insets insets1 = new Insets(6.0, 6.0, 6.0, 6.0);

        VBox vBox = new VBox(5);
        vBox.setPadding(insets1);
        this.getDialogPane().setContent(vBox);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(insets1);
        vBox.getChildren().add(gridPane);

        Label starFromSearchLabel = new Label("Please enter the partial name \nto search from star: ");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        starFromSearchLabel.setFont(font);
        Button fromStarButton = new Button("Find from Star");
        fromStarButton.setOnAction(this::fromButtonClicked);
        gridPane.add(starFromSearchLabel, 0, 0);
        gridPane.add(fromStarName, 1, 0);
        gridPane.add(fromStarButton, 2, 0);

        Label starToSearchLabel = new Label("Please enter the partial name \nto search to star: ");
        starToSearchLabel.setFont(font);
        Button toStarButton = new Button("Find to Star");
        toStarButton.setOnAction(this::toButtonClicked);
        gridPane.add(starToSearchLabel, 0, 1);
        gridPane.add(toStarName, 1, 1);
        gridPane.add(toStarButton, 2, 1);

        Label datasetLabel = new Label("Please enter dataset name: ");
        datasetLabel.setFont(font);
        gridPane.add(datasetLabel, 0, 2);
        gridPane.add(datasets, 1, 2);

        gridPane.add(new Separator(), 0, 3, 2, 1);

        Label fSLabel = new Label("From Star: ");
        fSLabel.setFont(font);
        gridPane.add(fSLabel, 0, 4);
        gridPane.add(fromStarNameLabel, 1, 4);

        Label tSLabel = new Label("To Star: ");
        tSLabel.setFont(font);
        gridPane.add(tSLabel, 0, 5);
        gridPane.add(toStarNameLabel, 1, 5);

        Label distLabel = new Label("Distance");
        distLabel.setFont(font);
        gridPane.add(distLabel, 0, 6);
        gridPane.add(distanceLabel, 1, 6);

        // button box
        HBox hBox = new HBox(5);
        hBox.setPadding(insets1);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Button calculateCoordinatesButton = new Button("Calculate Distance");
        calculateCoordinatesButton.setOnAction(this::calculate);
        hBox.getChildren().add(calculateCoordinatesButton);

        Button acceptButton = new Button("Accept");
        acceptButton.setOnAction(this::accept);
        hBox.getChildren().add(acceptButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::close);
        hBox.getChildren().add(cancelDataSetButton);

        gridPane.add(hBox, 0, 7, 2, 1);

    }

    private void toButtonClicked(ActionEvent actionEvent) {
        String toStar = toStarName.getText();
        if (toStar.isEmpty()) {
            showErrorAlert("Find Distance Dialog", "To star can't be blank!");
        } else {
            List<StarObject> starObjectList = starService.findStarsWithName(datasets.getValue(), toStar);
            // select the star we want
            ShowStarMatchesBasicDialog dialog = new ShowStarMatchesBasicDialog(starObjectList);
            Optional<StarSelectionObject> optionalStarSelectionObject = dialog.showAndWait();
            if (optionalStarSelectionObject.isPresent()) {
                StarSelectionObject starSelectionObject = optionalStarSelectionObject.get();
                if (starSelectionObject.isSelected()) {
                    distanceCalculationObject.setToStar(starSelectionObject.getStar());
                    toStarNameLabel.setText(starSelectionObject.getStar().getDisplayName());
                }
            }
        }
    }

    private void fromButtonClicked(ActionEvent actionEvent) {
        String fromStar = fromStarName.getText();
        if (fromStar.isEmpty()) {
            showErrorAlert("Find Distance Dialog", "From star can't be blank!");
        } else {
            List<StarObject> starObjectList = starService.findStarsWithName(datasets.getValue(), fromStar);
            // select the star we want
            ShowStarMatchesBasicDialog dialog = new ShowStarMatchesBasicDialog(starObjectList);
            Optional<StarSelectionObject> optionalStarSelectionObject = dialog.showAndWait();
            if (optionalStarSelectionObject.isPresent()) {
                StarSelectionObject starSelectionObject = optionalStarSelectionObject.get();
                if (starSelectionObject.isSelected()) {
                    distanceCalculationObject.setFromStar(starSelectionObject.getStar());
                    fromStarNameLabel.setText(starSelectionObject.getStar().getDisplayName());
                }
            }

        }
    }


    private void calculate(ActionEvent actionEvent) {
        if (distanceCalculationObject.getFromStar() != null && distanceCalculationObject.getToStar() != null) {
            double[] fromStarCoordinates = distanceCalculationObject.getFromStar().getCoordinates();
            double[] toStarCoordinates = distanceCalculationObject.getToStar().getCoordinates();

            double distance = StarMath.getDistance(fromStarCoordinates, toStarCoordinates);
            distanceLabel.setText("%.3f".formatted(distance));
            distanceCalculationObject.setDistance(distance);
            distanceCalculationObject.setCalculated(true);
        } else {
            showErrorAlert("Find Distance", "the To and From stars can't be null!");
        }
    }

    private void close(WindowEvent windowEvent) {
        setResult(distanceCalculationObject);
        close();
    }

    private void close(ActionEvent actionEvent) {
        setResult(distanceCalculationObject);
        close();
    }

    private void accept(ActionEvent actionEvent) {
        setResult(distanceCalculationObject);
    }

}
