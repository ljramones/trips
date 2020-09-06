package com.teamgannon.trips.dialogs.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Map;


public class RouteDialog extends Dialog<RouteDescriptor> {

    public RouteDialog(StarDisplayRecord starDisplayRecord) {
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
        TextField routeName = new TextField();
        grid.add(routeNameLabel, 1, 1);
        grid.add(routeName, 2, 1);

        Label routeColorLabel = new Label("Route Color: ");
        ColorPicker colorPicker = new ColorPicker();
        grid.add(routeColorLabel, 1, 2);
        grid.add(colorPicker, 2, 2);

        Label routeStartLabel = new Label("Route Starts at: ");
        Label routeStart = new Label(starName);
        grid.add(routeStartLabel, 1, 3);
        grid.add(routeStart, 2, 3);

        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        Label routeStartCoordinates = new Label(String.format("x(%.2f), y(%.2f), z(%.2f)", x, y, z));
        grid.add(routeStartCoordinatesLabel, 1, 4);
        grid.add(routeStartCoordinates, 2, 4);

        Label notesLabel= new Label("Notes: ");
        TextArea notes = new TextArea();
        grid.add(notesLabel, 1, 5);
        grid.add(notes, 2, 5);

        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);

        setResultConverter(b -> {

            if (b == buttonTypeOk) {

                return RouteDescriptor.builder()
                        .name(routeName.getText())
                        .color(colorPicker.getValue())
                        .startStar(starName)
                        .lineSegments(new ArrayList<>())
                        .routeNotes(notes.getText())
                        .routeList(new ArrayList<>())
                        .build();
            }

            return null;
        });
    }

}
