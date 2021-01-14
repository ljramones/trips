package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.service.DatabaseManagementService;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinderDialog extends Dialog<RouteFindingOptions> {

    /*
     * the combobox for selection
     */
//    private final @NotNull ComboBox<String> originCmb;
//    private final @NotNull ComboBox<String> destinationCmb;

    private final TextField originCmb;
    private final TextField destinationCmb;

    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    private final TextField upperLengthLengthTextField = new TextField();
    private final TextField lowerLengthLengthTextField = new TextField();

    private final TextField numPathsToFindTextField = new TextField();
    private final TextField lineWidthTextField = new TextField();

    private final ColorPicker colorPicker = new ColorPicker();

    private String currentDataSet;

    private DatabaseManagementService databaseManagementService;

    private final boolean offlineFlag;

    private Set<String> searchValues;

    public RouteFinderDialog(String currentDataSet,
                             @NotNull DatabaseManagementService databaseManagementService) {

        this.currentDataSet = currentDataSet;
        this.databaseManagementService = databaseManagementService;
        this.offlineFlag = true;

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();

        Font font = topOfPanel(vBox, gridPane);

        originCmb = new TextField();
        originCmb.setPromptText("origin star");
        gridPane.add(originCmb, 1, 1);
        Button lookupButton1 = new Button("Lookup");
        lookupButton1.setOnAction(event -> {
            String starName = lookupStarName(originCmb.getText());
            if (starName != null) {
                originCmb.setText(starName);
            }
        });
        gridPane.add(lookupButton1, 2, 1);

        destinationCmb = new TextField();
        destinationCmb.setPromptText("destination star");
        gridPane.add(destinationCmb, 1, 2);
        Button lookupButton2 = new Button("Lookup");
        lookupButton2.setOnAction(event -> {

            String starName = lookupStarName(destinationCmb.getText());
            if (starName != null) {
                destinationCmb.setText(starName);
            }
        });
        gridPane.add(lookupButton2, 2, 2);

        setupRestOfPanel(vBox, gridPane, font);
    }

    private String lookupStarName(String starToFind) {
        LookupStarDialog lookupStarDialog = new LookupStarDialog(starToFind, currentDataSet, databaseManagementService);
        Stage theStage = (Stage) lookupStarDialog.getDialogPane().getScene().getWindow();
        theStage.setAlwaysOnTop(true);
        theStage.toFront();
        Optional<String> optStarSelect = lookupStarDialog.showAndWait();
        if (optStarSelect.isPresent()) {
            String starSelected = optStarSelect.get();
            if (!starSelected.equals("dismiss")) {
                return starSelected;
            }
        }
        return null;
    }


    public RouteFinderDialog(@NotNull List<StarDisplayRecord> starsInView) {
        this.offlineFlag = false;
        this.setTitle("Enter parameters for Route location");

        searchValues = convertList(starsInView);

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();

        Font font = topOfPanel(vBox, gridPane);

        originCmb = new TextField();
        originCmb.setPromptText("origin star");

//        originCmb = new ComboBox<>();
//        originCmb.setPromptText("start typing");
//        originCmb.setTooltip(new Tooltip());
//        originCmb.getItems().addAll(searchValues);
//        new ComboBoxAutoComplete<>(stage, originCmb);
        gridPane.add(originCmb, 1, 1);

        destinationCmb = new TextField();
        destinationCmb.setPromptText("destination star");

//        destinationCmb = new ComboBox<>();
//        destinationCmb.setPromptText("start typing");
//        destinationCmb.setTooltip(new Tooltip());
//        destinationCmb.getItems().addAll(searchValues);
//        new ComboBoxAutoComplete<>(stage, destinationCmb);
        gridPane.add(destinationCmb, 1, 2);

        setupRestOfPanel(vBox, gridPane, font);
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
        gridPane.add(colorPicker, 1, 6);
        colorPicker.setValue(Color.AQUA);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText("3");

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Find Route(s)");
        resetBtn.setOnAction(this::findRoutesClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Cancel");
        addBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);
    }


    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        for (StarDisplayRecord record : starsInView) {
            starLookup.put(record.getStarName(), record);
        }
        return starLookup.keySet();
    }

    private void cancelClicked(ActionEvent actionEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
        log.info("cancel find routes clicked");
    }

    private void findRoutesClicked(ActionEvent actionEvent) {
        try {
            String originStarSelected = originCmb.getText();
            String destinationStarSelected = destinationCmb.getText();
            double maxDistance = 20;

            // this section is only used if we are searching offline and for this we need to search outside of the view
            if (offlineFlag) {
                if (originStarSelected.isEmpty()) {
                    showErrorAlert("Route Finding", "Origin is blank");
                    return;
                }
                if (destinationStarSelected.isEmpty()) {
                    showErrorAlert("Route Finding", "Destination is blank");
                    return;
                }
                List<AstrographicObject> originStarList = databaseManagementService.findStarsWithName(currentDataSet, originStarSelected);
                double originStarDistance = originStarList.get(0).getDistance();
                if (originStarDistance > maxDistance) {
                    maxDistance = originStarDistance;
                }
                List<AstrographicObject> destStarList = databaseManagementService.findStarsWithName(currentDataSet, destinationStarSelected);
                double destStarDistance = destStarList.get(0).getDistance();
                if (destStarDistance > maxDistance) {
                    maxDistance = destStarDistance;
                }
                // we add 10 ly to the radius
                maxDistance += 10;
            } else {
                if (!searchValues.contains(originStarSelected)) {
                    showErrorAlert("Find Route", String.format("Origin star <%s> is not present in view", originStarSelected));
                    return;
                }
                if (!searchValues.contains(destinationStarSelected)) {
                    showErrorAlert("Find Route", String.format("Destination star <%s> is not present in view", destinationStarSelected));
                    return;
                }
            }

//            String originStarSelected = originCmb.getValue();
//            String destinationStarSelected = destinationCmb.getValue();

            setResult(
                    RouteFindingOptions
                            .builder()
                            .selected(true)
                            .originStar(originStarSelected)
                            .destinationStar(destinationStarSelected)
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
