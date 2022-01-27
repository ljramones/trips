package com.teamgannon.trips.dialogs.inventory;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.validation.constraints.NotNull;

public class ComputerInventoryDialog extends Dialog<InventoryReport> {

    private final InventoryReport inventory;

    public Button changeButton = new Button("Save");

    public ComputerInventoryDialog(@NotNull InventoryReport inventory) {
        this.inventory = inventory;

        this.setTitle("Computer Detailed Description");
        this.setHeight(300);
        this.setWidth(400);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);

        TextArea reportArea = new TextArea();
        reportArea.setText(inventory.getInventoryDescription());
        vBox.getChildren().add(reportArea);

        HBox prefsBox = new HBox();
        vBox.getChildren().add(prefsBox);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        changeButton.setOnAction(this::saveClicked);
        hBox.getChildren().add(changeButton);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::cancel);
        hBox.getChildren().add(cancelBtn);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        inventory.setSave(false);
        setResult(inventory);
    }

    private void cancel(ActionEvent actionEvent) {
        inventory.setSave(false);
        setResult(inventory);
    }

    private void saveClicked(ActionEvent actionEvent) {
        inventory.setSave(true);
        setResult(inventory);
    }
}
