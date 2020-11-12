package com.teamgannon.trips.progress;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EndStatusPane extends Pane {

    private Stage mainStage;
    private Label label;
    private String loadMessage;

    public EndStatusPane(String loadMessage) {
        this.loadMessage = loadMessage;
        mainStage = new Stage(StageStyle.DECORATED);
        BorderPane.setMargin(this, new Insets(12, 12, 12, 12));
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        label = new Label("");
        label.setMinSize(300, 50);
        label.setAlignment(Pos.CENTER);
        vBox.getChildren().add(label);
        Button dismiss = new Button("Dismiss");
        dismiss.setOnAction(event -> {
            mainStage.close();
        });
        vBox.getChildren().add(dismiss);
        vBox.getChildren().add(new Label("  "));
        this.getChildren().add(vBox);
    }

    public void show(Integer recordsCount) {
        label.setText(recordsCount + loadMessage);
        mainStage.setScene(new Scene(this));
        mainStage.show();
    }

}
