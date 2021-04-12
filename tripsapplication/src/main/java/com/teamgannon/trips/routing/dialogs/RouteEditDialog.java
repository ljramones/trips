package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.routing.RouteChange;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class RouteEditDialog extends Dialog<RouteChange> {

    private final RouteTree routeTree;

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final TextField routeNameTextField = new TextField();

    private final TextArea routeNotesTextArea = new TextArea();

    private final ColorPicker routeColorPicker = new ColorPicker();


    public RouteEditDialog(RouteTree routeTree) {
        this.routeTree = routeTree;

        this.setTitle("Editing Route for " + routeTree.getRouteName());
        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        GridPane gridPane = createEditPane(routeTree);
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
        routeTree.setRouteNotes(routeNotesTextArea.getText());
        routeTree.setRouteName(routeNameTextField.getText());
        routeTree.setRouteColor(routeColorPicker.getValue());
        this.setResult(RouteChange.builder().changed(true).routeTree(routeTree).build());
    }

    private void close(ActionEvent actionEvent) {
        this.setResult(RouteChange.builder().changed(false).build());
    }

    private void close(WindowEvent windowEvent) {
        this.setResult(RouteChange.builder().changed(false).build());
    }

    private GridPane createEditPane(RouteTree routeTree) {
        GridPane pane = new GridPane();

        routeNameTextField.setText(routeTree.getRouteName());
        pane.add(new Label("Route Name"), 0, 0);
        pane.add(routeNameTextField, 1, 0);

        routeNotesTextArea.setText(routeTree.getRouteNotes());
        pane.add(new Label("Route Notes"), 0, 1);
        pane.add(routeNotesTextArea, 1, 1);

        routeColorPicker.setValue(routeTree.getRouteColor());
        pane.add(new Label("Route Color"), 0, 2);
        pane.add(routeColorPicker, 1, 2);

        return pane;
    }

}
