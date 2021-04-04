package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ContextManualRoutingDialog extends Dialog<Boolean> {

    private final StarDisplayRecord starDisplayRecord;

    private TextField routeName = new TextField();
    private TextField lineWidthTextField = new TextField();
    private ColorPicker colorPicker = new ColorPicker();
    private TextArea notes = new TextArea();

    private final Stage stage;

    private RouteDescriptor routeDescriptor;

    private List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    public ContextManualRoutingDialog(StarDisplayRecord starDisplayRecord) {

        this.starDisplayRecord = starDisplayRecord;
        starDisplayRecordList.add(starDisplayRecord);

        // set the dialog as a utility
        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        VBox vBox = new VBox();

        this.setTitle("Route Creation Dialog");
        this.setHeaderText("Create an initial Route");

        String starName = starDisplayRecord.getStarName();
        double x = starDisplayRecord.getX();
        double y = starDisplayRecord.getY();
        double z = starDisplayRecord.getZ();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label routeNameLabel = new Label("Route Name: ");

        grid.add(routeNameLabel, 1, 1);
        grid.add(routeName, 2, 1);

        Label routeColorLabel = new Label("Route Color: ");

        grid.add(routeColorLabel, 1, 2);
        grid.add(colorPicker, 2, 2);

        Label routeLineWidthLabel = new Label("Route Line Width: ");

        grid.add(routeLineWidthLabel, 1, 3);
        grid.add(lineWidthTextField, 2, 3);

        Label routeStartLabel = new Label("Route Starts at: ");
        Label routeStart = new Label(starName);
        grid.add(routeStartLabel, 1, 4);
        grid.add(routeStart, 2, 4);

        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        Label routeStartCoordinates = new Label(String.format("x(%.2f), y(%.2f), z(%.2f)", x, y, z));
        grid.add(routeStartCoordinatesLabel, 1, 5);
        grid.add(routeStartCoordinates, 2, 5);

        Label notesLabel = new Label("Notes: ");
        grid.add(notesLabel, 1, 6);
        grid.add(notes, 2, 6);

        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Start route");
        resetBtn.setOnAction(this::startRouteClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Close");
        addBtn.setOnAction(this::closeClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

    }

    private void closeClicked(ActionEvent actionEvent) {
        setResult(false);
    }


    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void addStar(StarDisplayRecord record) {
        starDisplayRecordList.add(record);
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
                .lineSegments(new ArrayList<>())
                .lineWidth(lineWidth)
                .routeNotes(notes.getText())
                .routeList(new ArrayList<>())
                .build();
    }


}
