package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.elasticsearch.model.Star;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/**
 * A table view component for displaying hipparcos 3 data
 * <p>
 * Created by larrymitchell on 2017-02-25.
 */
public class Hipparcos3Table {

    /**
     * we encapsulate the dialog
     */
    private Dialog dialog = new Dialog();

    /**
     * the enclosing window for the dialog
     */
    private Window window;

    /**
     * the table that we use to show the data
     */
    private TableView table = new TableView();

    /**
     * backing collection for the data displayed
     */
    private List<Star> hipparcos3EntryList;


    public Hipparcos3Table(List<Star> hipparcos3EntryList) {

        this.hipparcos3EntryList = hipparcos3EntryList;

        dialog = new Dialog();

        dialog.setTitle("Hipparcos 3 Records Table");
        // set the dimensions
        dialog.setHeight(600);
        dialog.setWidth(1000);

        VBox vBox = new VBox();

        ScrollPane scrollPane = new ScrollPane();
        vBox.getChildren().add(scrollPane);

        Button dismissButton = new Button();
        dismissButton.setText("Dismiss");
        dismissButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                window.hide();
            }
        });

        Pane bottomPane = new Pane();
        bottomPane.getChildren().add(dismissButton);
        vBox.getChildren().add(bottomPane);

        // set the windows close
        window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        scrollPane.setContent(table);

        dialog.getDialogPane().setContent(vBox);
        dialog.show();
    }
}
