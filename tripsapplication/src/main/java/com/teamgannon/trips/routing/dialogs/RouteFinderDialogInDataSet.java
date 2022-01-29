package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.measure.PerformanceMeasure;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinderDialogInDataSet extends Dialog<RouteFindingOptions> {

    /*
     * the combobox for selection
     */
    private final TextField originCmb;
    private final TextField destinationCmb;

    private StarObject originStar;
    private StarObject destinationStar;

    private final TextField upperLengthLengthTextField = new TextField();
    private final TextField lowerLengthLengthTextField = new TextField();

    private final TextField numPathsToFindTextField = new TextField();
    private final TextField lineWidthTextField = new TextField();

    private final ColorPicker colorPicker = new ColorPicker();
    private final String currentDataSet;
    private final StarMeasurementService starMeasurementService;
    private final DatabaseManagementService databaseManagementService;

   private Button colorButton = new Button("color");

    /**
     * this constructor is used when we search an entire database
     *
     * @param currentDataSet            the dataset to use
     * @param databaseManagementService the database management service
     */
    public RouteFinderDialogInDataSet(String currentDataSet,
                                      StarMeasurementService starMeasurementService,
                                      @NotNull DatabaseManagementService databaseManagementService) {

        this.currentDataSet = currentDataSet;
        this.starMeasurementService = starMeasurementService;
        this.databaseManagementService = databaseManagementService;

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();

        Font font = topOfPanel(vBox, gridPane);

        originCmb = new TextField();
        originCmb.setPromptText("origin star");
        gridPane.add(originCmb, 1, 1);
        Button lookupButton1 = new Button("Lookup");
        lookupButton1.setOnAction(event -> {
            StarSearchResults starObject = lookupStarName(originCmb.getText());
            if (starObject != null) {
                originCmb.setText(starObject.getNameToSearch());
                originStar = starObject.getStarObject();
            }
        });
        gridPane.add(lookupButton1, 2, 1);

        destinationCmb = new TextField();
        destinationCmb.setPromptText("destination star");
        gridPane.add(destinationCmb, 1, 2);
        Button lookupButton2 = new Button("Lookup");
        lookupButton2.setOnAction(event -> {

            StarSearchResults starName = lookupStarName(destinationCmb.getText());
            if (starName != null) {
                destinationCmb.setText(starName.getNameToSearch());
                destinationStar = starName.getStarObject();
            }
        });
        gridPane.add(lookupButton2, 2, 2);

        setupRestOfPanel(vBox, gridPane, font);
    }


    private StarSearchResults lookupStarName(String starToFind) {
        if (starToFind.isEmpty()) {
            showErrorAlert("Star to Lookup", "Please enter a star name");
            return null;
        } else {
            LookupStarDialog lookupStarDialog = new LookupStarDialog(starToFind, currentDataSet, databaseManagementService);
            Stage theStage = (Stage) lookupStarDialog.getDialogPane().getScene().getWindow();
            theStage.setAlwaysOnTop(true);
            theStage.toFront();
            Optional<StarSearchResults> optStarSelect = lookupStarDialog.showAndWait();
            if (optStarSelect.isPresent()) {
                StarSearchResults starSelected = optStarSelect.get();
                if (starSelected.isStarsFound()) {
                    return starSelected;
                }
            }
            return null;
        }
    }


    ////////////////////////////////

    private Font topOfPanel(VBox vBox, GridPane gridPane) {
        this.setTitle("Enter parameters for Route location");

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        Label originStar = new Label("Origin Star");
        originStar.setFont(font);
        gridPane.add(originStar, 0, 1);

        Label destinationStar = new Label("Destination Star");
        destinationStar.setFont(font);
        gridPane.add(destinationStar, 0, 2);
        return font;
    }

    private void setupRestOfPanel(VBox vBox, GridPane gridPane, Font font) {
        Label upperBound = new Label("Upper limit for route length");
        upperBound.setFont(font);
        gridPane.add(upperBound, 0, 3);
        gridPane.add(upperLengthLengthTextField, 1, 3);
        upperLengthLengthTextField.setText("8");

        Label lowerBound = new Label("lower limit for route length");
        lowerBound.setFont(font);
        gridPane.add(lowerBound, 0, 4);
        gridPane.add(lowerLengthLengthTextField, 1, 4);
        lowerLengthLengthTextField.setText("3");

        Label lineWidth = new Label("route line width");
        lineWidth.setFont(font);
        gridPane.add(lineWidth, 0, 5);
        gridPane.add(lineWidthTextField, 1, 5);
        lineWidthTextField.setText("0.5");

        Label routeColor = new Label("route color");
        routeColor.setFont(font);
        gridPane.add(routeColor, 0, 6);
        colorPicker.setValue(Color.AQUA);
        colorButton.setOnAction(this::setColor);
        gridPane.add(colorButton, 1, 6);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText("3");

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button howLongBtn = new Button("How Long will this take?");
        howLongBtn.setOnAction(this::findOutHowLong);
        hBox.getChildren().add(howLongBtn);

        Button resetBtn = new Button("Find Route(s)");
        resetBtn.setOnAction(this::findRoutesClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Cancel");
        addBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }


    private void setColor(ActionEvent actionEvent) {
        ColorChoiceDialog colorChoiceDialog = new ColorChoiceDialog();
        Optional<ColorChoice> colorChoiceOptional = colorChoiceDialog.showAndWait();
        if (colorChoiceOptional.isPresent()) {
            ColorChoice colorChoice = colorChoiceOptional.get();
            if (colorChoice.isSelected()) {
                colorPicker.setValue(colorChoice.getSwatch());
                colorButton.setTextFill(colorChoice.getSwatch());
            }
        }
    }


    private void close(WindowEvent windowEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
    }


    private void cancelClicked(ActionEvent actionEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
        log.info("cancel find routes clicked");
    }


    private void findOutHowLong(ActionEvent actionEvent) {
        if (originStar == null || destinationStar == null) {
            showErrorAlert("Error in long route determination", "Both origin and destination should be set");
            log.error("Error in long route determination, both origin and destination should be set");
        } else {
            log.info("find out how many stars this query will take");
            double[] originCoords = originStar.getCoordinates();
            double[] destinationCoords = destinationStar.getCoordinates();
            double distance = starMeasurementService.calculateDistance(originCoords, destinationCoords);
            log.info("distance between {} and {} is {} ly", originStar.getDisplayName(), destinationStar.getDisplayName(), distance);

            long starCount = databaseManagementService.getCountOfDatasetWithinLimit(currentDataSet, distance);
            log.info("number of stars:{}", starCount);
            PerformanceMeasure performanceMeasure = starMeasurementService.calculateTimeToDoSearch(starCount);
            performanceMeasure.setDistance(distance);
            log.info("time required to find a route through {} stars is {} secs",
                    String.format("%,d", starCount),
                    String.format("%,.2f", performanceMeasure.getTimeToDoRouteSearch())
            );
            SearchPerformanceDialog searchPerformanceDialog = new SearchPerformanceDialog(performanceMeasure);
            searchPerformanceDialog.showAndWait();
        }

    }

    private void findRoutesClicked(ActionEvent actionEvent) {
        try {
            String originStarSelected = originCmb.getText();
            String destinationStarSelected = destinationCmb.getText();
            double maxDistance = 20;

            setResult(
                    RouteFindingOptions
                            .builder()
                            .selected(true)
                            .originStarName(originStarSelected)
                            .originStar(originStar)
                            .destinationStarName(destinationStarSelected)
                            .destinationStar(destinationStar)
                            .upperBound(Double.parseDouble(upperLengthLengthTextField.getText()))
                            .lowerBound(Double.parseDouble(lowerLengthLengthTextField.getText()))
                            .lineWidth(Double.parseDouble(lineWidthTextField.getText()))
                            .color(colorPicker.getValue())
                            .maxDistance(maxDistance)
                            .numberPaths(Integer.parseInt(numPathsToFindTextField.getText()))
                            .build()
            );
            log.info("cancel clicked");
        } catch (NumberFormatException nfe) {
            showErrorAlert("Route Finder", "bad floating point");
            log.error("bad floating point");
        }
    }

}