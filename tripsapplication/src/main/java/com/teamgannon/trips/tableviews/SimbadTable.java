package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.jpa.model.SimbadEntry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;


/**
 * This is dialog that is used to show the Simbad data
 * <p>
 * Created by larrymitchell on 2017-02-25.
 */
public class SimbadTable {

    /**
     * we encapsulate the dialog
     */
    private Dialog dialog = new Dialog();

    /**
     * the enclosing window for the dialog
     */
    private final Window window;

    /**
     * the table that we use to show the data
     */
    private final TableView table = new TableView();

    /**
     * backing collection for the data to be displayed
     */
    private List<SimbadEntry> simbadEntries = new ArrayList<>();

    /**
     * the default constructor
     *
     * @param simbadEntries the list of simbad entries
     */
    public SimbadTable(List<SimbadEntry> simbadEntries) {

        this.simbadEntries = simbadEntries;

        dialog = new Dialog();

        dialog.setTitle("Simbad Records Table");
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
