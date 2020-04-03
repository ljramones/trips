package com.teamgannon.trips.dialog;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Map;


public class RouteDialog extends Dialog<RouteDescriptor> {

    public RouteDialog(Map<String, String> properties) {
        this.setTitle("Route Creation Dialog");
        this.setHeaderText("Create an initial Route");

        String starName = properties.get("name");
        String x = properties.get("x");
        String y = properties.get("y");
        String z = properties.get("z");

        Label routeNameLabel = new Label("Route Name: ");
        TextField routeName = new TextField();

        Label routeColorLabel = new Label("Route Color: ");
        ColorPicker colorPicker = new ColorPicker();

        Label routeStartLabel = new Label("Route Starts at: ");
        Label routeStart = new Label(starName);

        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        Label routeStartCoordinates = new Label(String.format("x(%s), y(%s), z(%s)", x, y, z));

        GridPane grid = new GridPane();
        grid.add(routeNameLabel, 1, 1);
        grid.add(routeName, 2, 1);
        grid.add(routeColorLabel, 1, 2);
        grid.add(colorPicker, 2, 2);
        grid.add(routeStartLabel, 1, 3);
        grid.add(routeStart, 2, 3);
        grid.add(routeStartCoordinatesLabel, 1, 4);
        grid.add(routeStartCoordinates, 2, 4);


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
                        .routeList(new ArrayList<>())
                        .build();
            }

            return null;
        });
    }

}
