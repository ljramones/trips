package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.starplotting.StarPlotManager;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

@Slf4j
public class ContextManualRoutingDialog extends Dialog<Boolean> {

    private final RouteManager routeManager;
    private final DataSetDescriptor currentDataSet;
    private final StarDisplayRecord starDisplayRecord;

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final TextField routeName = new TextField();
    private final TextField lineWidthTextField = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private final TextArea notes = new TextArea();

    private int rowToAdd = 1;

    private final GridPane grid = new GridPane();

    private final Button finishBtn = new Button("Finish route");

    private boolean startRouting = false;

    private final Stage stage;

    private final List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    private final Set<StarDisplayRecord> routeSet = new HashSet<>();

    public ContextManualRoutingDialog(@NotNull StarPlotManager plotManager,
                                      @NotNull RouteManager routeManager,
                                      @NotNull DataSetDescriptor currentDataSet,
                                      @NotNull StarDisplayRecord starDisplayRecord,
                                      @NotNull List<StarDisplayRecord> starsInView) {

        this.routeManager = routeManager;
        this.currentDataSet = currentDataSet;

        this.starDisplayRecord = starDisplayRecord;
        starDisplayRecordList.add(starDisplayRecord);

        // set the dialog as a utility
        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

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
        grid.add(colorPicker, 2, 2);

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

        Button resetBtn = new Button("Start route");
        resetBtn.setOnAction(this::startRouteClicked);
        hBox.getChildren().add(resetBtn);

        finishBtn.setDisable(true);
        finishBtn.setOnAction(this::finishRouteClicked);
        hBox.getChildren().add(finishBtn);

        Button addBtn = new Button("Close");
        addBtn.setOnAction(this::closeClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

    }

    private void finishRouteClicked(ActionEvent actionEvent) {
        log.info("save route");
        routeManager.finishRoute();
    }

    private void closeClicked(ActionEvent actionEvent) {
        routeManager.setRoutingActive(false);
        setResult(false);
    }


    private void close(WindowEvent windowEvent) {
        routeManager.setRoutingActive(false);
        setResult(false);
    }

    public void addStar(StarDisplayRecord record) {
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
                grid.add(segmentNameLabel, 1, 7 + rowToAdd);
                grid.add(new Label(record.getStarName()), 2, 7 + rowToAdd);
                rowToAdd++;
                stage.sizeToScene();
                finishBtn.setDisable(false);
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
        RouteDescriptor routeDescriptor = RouteDescriptor.builder()
                .name(routeName.getText())
                .color(colorPicker.getValue())
                .startStar(starDisplayRecord.getStarName())
                .lineSegments(new ArrayList<>())
                .lineWidth(lineWidth)
                .routeNotes(notes.getText())
                .routeList(new ArrayList<>())
                .build();
        routeManager.startRoute(currentDataSet, routeDescriptor, starDisplayRecord);
        startRouting = true;
    }


}
