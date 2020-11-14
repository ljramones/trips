package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

public class ShowStarMatchesDialog extends Dialog<String> {

    private TableView<AstrographicObject> starTable = new TableView<>();

    private final List<AstrographicObject> starList;

    public ShowStarMatchesDialog(List<AstrographicObject> starList) {
        this.starList = starList;
        this.setTitle("Show discovered stars");
        this.setHeight(700);
        this.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(starTable);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

    }

    private void close(ActionEvent actionEvent) {
        setResult("dismiss");
    }
}
