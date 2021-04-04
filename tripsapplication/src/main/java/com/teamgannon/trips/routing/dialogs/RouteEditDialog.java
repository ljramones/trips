package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteChange;
import javafx.event.ActionEvent;
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

public class RouteEditDialog extends Dialog<RouteChange> {

    private final Route route;

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final TextField routeNameTextField = new TextField();

    private final TextArea routeNotesTextArea = new TextArea();

    private final ColorPicker routeColorPicker = new ColorPicker();


    public RouteEditDialog(Route route) {
        this.route = route;

        this.setTitle("Editing Route for " + route.getRouteName());
        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        GridPane gridPane = createEditPane(route);
        vBox.getChildren().add(gridPane);

        HBox buttonBox = new HBox();
        vBox.getChildren().add(buttonBox);

        Button choosePlotButton = new Button("Save Changes");
        choosePlotButton.setFont(font);
        choosePlotButton.setOnAction(this::change);
        buttonBox.getChildren().add(choosePlotButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(font);
        cancelButton.setOnAction(this::close);
        buttonBox.getChildren().add(cancelButton);

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private void change(ActionEvent actionEvent) {
        route.setRouteNotes(routeNotesTextArea.getText());
        route.setRouteName(routeNameTextField.getText());
        route.setRouteColor(routeColorPicker.getValue().toString());
        this.setResult(RouteChange.builder().changed(true).route(route).build());
    }

    private void close(ActionEvent actionEvent) {
        this.setResult(RouteChange.builder().changed(false).build());
    }

    private void close(WindowEvent windowEvent) {
        this.setResult(RouteChange.builder().changed(false).build());
    }

    private GridPane createEditPane(Route route) {
        GridPane pane = new GridPane();

        routeNameTextField.setText(route.getRouteName());
        pane.add(new Label("Route Name"), 0, 0);
        pane.add(routeNameTextField, 1, 0);

        routeNotesTextArea.setText(route.getRouteNotes());
        pane.add(new Label("Route Notes"), 0, 1);
        pane.add(routeNotesTextArea, 1, 1);

        routeColorPicker.setValue(Color.valueOf(route.getRouteColor()));
        pane.add(new Label("Route Color"), 0, 2);
        pane.add(routeColorPicker, 1, 2);

        return pane;
    }

}
