package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
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
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

@Slf4j
public class ContextManualRoutingDialog extends Dialog<Boolean> {

    private final RouteManager routeManager;

    private final DataSetDescriptor currentDataSet;

    private StarDisplayRecord starDisplayRecord;

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final TextField routeName = new TextField();
    private final TextField lineWidthTextField = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private final TextArea notes = new TextArea();

    private int rowToAdd = 1;
    private final int anchorRow = 7;

    private final GridPane grid = new GridPane();

    private final Button finishBtn = new Button("Finish route");

    private boolean startRouting = false;

    private final Stage stage;

    private final List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    private final Set<StarDisplayRecord> routeSet = new HashSet<>();

    private RouteDescriptor routeDescriptor;

    /**
     * true means this plot was part of an in plot context call
     * false means it was called from outside of the plot
     */
    private final boolean plottingContextMode;

    private ComboBox<String> originDisplayCmb;

    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    boolean firstTime = true;

    private final Label startStarLabel = new Label();

    private final Label routeStartCoordinates = new Label("0, 0, 0");

    private final Button startButton = new Button("Start route");

    private final Button removeLastBtn = new Button("Remove last segment");

    private final Button colorButton = new Button("Color");

    public ContextManualRoutingDialog(@NotNull RouteManager routeManager,
                                      @NotNull DataSetDescriptor currentDataSet,
                                      @NotNull List<StarDisplayRecord> starsInView) {

        this.plottingContextMode = false;

        this.routeManager = routeManager;
        this.currentDataSet = currentDataSet;

        Set<String> searchValues = convertList(starsInView);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);
        vBox.getChildren().add(grid);

        this.setTitle("Route Creation Dialog");
        this.setHeaderText("Create an initial Route");

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label routeNameLabel = new Label("Route Name: ");
        routeNameLabel.setFont(font);

        grid.add(routeNameLabel, 1, 1);
        routeName.setText("Route A");
        grid.add(routeName, 2, 1);

        Label routeColorLabel = new Label("Route Color: ");
        routeColorLabel.setFont(font);

        grid.add(routeColorLabel, 1, 2);
        colorPicker.setValue(Color.CYAN);
        colorButton.setOnAction(this::setColor);
        grid.add(colorButton, 2, 2);

        Label routeLineWidthLabel = new Label("Route Line Width: ");
        routeLineWidthLabel.setFont(font);

        grid.add(routeLineWidthLabel, 1, 3);
        lineWidthTextField.setText("0.5");
        grid.add(lineWidthTextField, 2, 3);

        Label routeStartLabel = new Label("Route Starts at: ");
        routeStartLabel.setFont(font);

        originDisplayCmb = new ComboBox<>();
        originDisplayCmb.setPromptText("start typing");
        originDisplayCmb.getItems().addAll(searchValues);
        originDisplayCmb.setEditable(true);
        originDisplayCmb.setOnAction(this::selectStar);
        TextFields.bindAutoCompletion(originDisplayCmb.getEditor(), originDisplayCmb.getItems());

        grid.add(routeStartLabel, 1, 4);
        grid.add(originDisplayCmb, 2, 4);

        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        routeStartCoordinatesLabel.setFont(font);
        grid.add(routeStartCoordinatesLabel, 1, 5);
        grid.add(routeStartCoordinates, 2, 5);

        Label notesLabel = new Label("Notes: ");
        notesLabel.setFont(font);
        grid.add(notesLabel, 1, 6);
        grid.add(notes, 2, 6);

        Label startSegmentLabel = new Label("Start:");
        startSegmentLabel.setFont(font);
        grid.add(startSegmentLabel, 1, 7);
        grid.add(startStarLabel, 2, 7);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        startButton.setOnAction(this::startRouteClicked);
        hBox.getChildren().add(startButton);

        finishBtn.setDisable(true);
        finishBtn.setOnAction(this::finishRouteClicked);
        hBox.getChildren().add(finishBtn);

        Button resetBtn = new Button("Reset route");
        resetBtn.setOnAction(this::resetRoute);
        hBox.getChildren().add(resetBtn);

        removeLastBtn.setOnAction(this::removeRouteSegment);
        hBox.getChildren().add(removeLastBtn);
        removeLastBtn.setDisable(true);

        Button addBtn = new Button("Close");
        addBtn.setOnAction(this::closeClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        // set the dialog as a utility
        stage = (Stage) this.getDialogPane().getScene().getWindow();
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


    public ContextManualRoutingDialog(@NotNull RouteManager routeManager,
                                      @NotNull DataSetDescriptor currentDataSet,
                                      @NotNull StarDisplayRecord starDisplayRecord) {

        this.plottingContextMode = true;

        this.routeManager = routeManager;
        this.currentDataSet = currentDataSet;

        this.starDisplayRecord = starDisplayRecord;
        starDisplayRecordList.add(starDisplayRecord);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);
        vBox.getChildren().add(grid);

        this.setTitle("Route Creation Dialog");
        this.setHeaderText("Create an initial Route");

        String starName = starDisplayRecord.getStarName();
        double x = starDisplayRecord.getX();
        double y = starDisplayRecord.getY();
        double z = starDisplayRecord.getZ();

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label routeNameLabel = new Label("Route Name: ");
        routeNameLabel.setFont(font);

        grid.add(routeNameLabel, 1, 1);
        routeName.setText("Route A");
        grid.add(routeName, 2, 1);

        Label routeColorLabel = new Label("Route Color: ");
        routeColorLabel.setFont(font);

        grid.add(routeColorLabel, 1, 2);
        colorPicker.setValue(Color.CYAN);

        colorButton.setOnAction(this::setColor);
        grid.add(colorButton, 2, 2);

        Label routeLineWidthLabel = new Label("Route Line Width: ");
        routeLineWidthLabel.setFont(font);

        grid.add(routeLineWidthLabel, 1, 3);
        lineWidthTextField.setText("0.5");
        grid.add(lineWidthTextField, 2, 3);

        Label routeStartLabel = new Label("Route Starts at: ");
        routeStartLabel.setFont(font);
        Label routeStart = new Label(starName);
        grid.add(routeStartLabel, 1, 4);
        grid.add(routeStart, 2, 4);

        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        routeStartCoordinatesLabel.setFont(font);
        Label routeStartCoordinates = new Label(String.format("x(%.2f), y(%.2f), z(%.2f)", x, y, z));
        grid.add(routeStartCoordinatesLabel, 1, 5);
        grid.add(routeStartCoordinates, 2, 5);

        Label notesLabel = new Label("Notes: ");
        notesLabel.setFont(font);
        grid.add(notesLabel, 1, 6);
        grid.add(notes, 2, 6);

        Label startSegmentLabel = new Label("Start:");
        startSegmentLabel.setFont(font);
        grid.add(startSegmentLabel, 1, 7);
        grid.add(new Label(starDisplayRecord.getStarName()), 2, 7);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        startButton.setOnAction(this::startRouteClicked);
        hBox.getChildren().add(startButton);

        Button resetBtn = new Button("Reset route");
        resetBtn.setOnAction(this::resetRoute);
        hBox.getChildren().add(resetBtn);

        removeLastBtn.setOnAction(this::removeRouteSegment);
        hBox.getChildren().add(removeLastBtn);
        removeLastBtn.setDisable(true);

        finishBtn.setDisable(true);
        finishBtn.setOnAction(this::finishRouteClicked);
        hBox.getChildren().add(finishBtn);

        Button addBtn = new Button("Close");
        addBtn.setOnAction(this::closeClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        // set the dialog as a utility
        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }


    private void resetRoute(ActionEvent actionEvent) {
        if (!plottingContextMode) {
            originDisplayCmb.setDisable(false);
            originDisplayCmb.setValue("");
            routeStartCoordinates.setText("0, 0, 0");
            startButton.setDisable(false);
        }
        clearRoute();
        for (int i = 1; i <= rowToAdd; i++) {
            // removing rows
            int rowNumber = anchorRow + i;
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) == rowNumber);
        }
        stage.sizeToScene();
        finishBtn.setDisable(true);
        routeManager.startRoute(currentDataSet, routeDescriptor, starDisplayRecord);
        removeLastBtn.setDisable(true);
    }


    private void removeRouteSegment(ActionEvent actionEvent) {
        log.info("remove last star segment");
        StarDisplayRecord lastStar = routeManager.removeLastSegment();
        boolean wasThere = routeSet.remove(lastStar);
        log.info("removing {}, was there={}", lastStar.getStarName(), wasThere);
        grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) == (anchorRow + rowToAdd - 1));
        rowToAdd--;
        stage.sizeToScene();
    }

    private void clearRoute() {
        routeSet.clear();
        routeManager.resetRoute();
        removeLastBtn.setDisable(true);
    }

    private void selectStar(ActionEvent actionEvent) {
        if (firstTime) {
            String selectedStar = originDisplayCmb.getValue();
            starDisplayRecord = starLookup.get(selectedStar);
            startStarLabel.setText(starDisplayRecord.getStarName());

            double x = starDisplayRecord.getX();
            double y = starDisplayRecord.getY();
            double z = starDisplayRecord.getZ();
            routeStartCoordinates.setText(String.format("x(%.2f), y(%.2f), z(%.2f)", x, y, z));

            log.info("start star:{}", starDisplayRecord);
            originDisplayCmb.setDisable(true);
            firstTime = false;
            removeLastBtn.setDisable(false);
        }
    }

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        starsInView.forEach(record -> starLookup.put(record.getStarName(), record));
        return starLookup.keySet();
    }

    private void finishRouteClicked(ActionEvent actionEvent) {
        log.info("save route");
        routeManager.finishRoute();
        removeLastBtn.setDisable(true);
        setResult(true);
    }

    private void closeClicked(ActionEvent actionEvent) {
        if (routeManager.isManualRoutingActive()) {
            routeManager.setManualRoutingActive(false);
        }
        setResult(false);
    }


    private void close(WindowEvent windowEvent) {
        if (routeManager.isManualRoutingActive()) {
            routeManager.setManualRoutingActive(false);
        }
        setResult(false);
    }

    public void addStar(StarDisplayRecord record) {
        log.info("request to add star to manual plot:{}", record.getStarName());
        if (startRouting) {
            if (routeSet.contains(record)) {
                // can't use this route
                showErrorAlert("Manual Route finding", "You already have this star in the route.");
            } else {
                routeManager.continueRoute(record);
                routeSet.add(record);
                starDisplayRecordList.add(record);
                Label segmentNameLabel = new Label("segment" + rowToAdd + ":");
                segmentNameLabel.setFont(font);
                grid.add(segmentNameLabel, 1, anchorRow + rowToAdd);
                grid.add(new Label(record.getStarName()), 2, anchorRow + rowToAdd);
                rowToAdd++;
                stage.sizeToScene();
                finishBtn.setDisable(false);
                removeLastBtn.setDisable(false);
            }
        } else {
            showWarningMessage("Add Route", "Please press start to select parameters for route.");
        }
    }

    private void startRouteClicked(ActionEvent actionEvent) {
        double lineWidth = 0.5;
        try {
            lineWidth = Double.parseDouble(lineWidthTextField.getText());
        } catch (NumberFormatException nfe) {
            log.error(
                    "{} is not a valid double so defaulting to 0.5",
                    lineWidthTextField.getText());
        }
        routeDescriptor = RouteDescriptor.builder()
                .name(routeName.getText())
                .color(colorPicker.getValue())
                .startStar(starDisplayRecord.getStarName())
                .routeCoordinates(new ArrayList<>())
                .lineWidth(lineWidth)
                .visibility(RouteVisibility.FULL)
                .routeNotes(notes.getText())
                .routeList(new ArrayList<>())
                .build();
        routeSet.add(starDisplayRecord);
        routeManager.startRoute(currentDataSet, routeDescriptor, starDisplayRecord);
        startRouting = true;
        startButton.setDisable(true);
    }

}
