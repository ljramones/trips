package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.SearchPane;
import com.teamgannon.trips.search.StellarDataUpdater;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
public class QueryDialog extends Dialog<AstroSearchQuery> {

    private final SearchContext searchContext;

    private final SearchPane searchPane;

    public final Button runQueryButton = new Button("Run Query");

    public QueryDialog(SearchContext searchContext, StellarDataUpdater updater) {
        this.searchContext = searchContext;
        this.setTitle("Query And Search for Stars");

        searchPane = new SearchPane(this.searchContext, updater, false);

        this.setHeight(800);
        this.setWidth(500);

        VBox vBox = new VBox();
        vBox.getChildren().add(searchPane);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox5 = new HBox();
        hBox5.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox5);

        runQueryButton.setOnAction(this::addDataSetClicked);
        hBox5.getChildren().add(runQueryButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox5.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(ActionEvent actionEvent) {
        setResult(searchContext.getAstroSearchQuery());
    }

    private void close(WindowEvent we) {
        setResult(searchContext.getAstroSearchQuery());
    }

    private void addDataSetClicked(ActionEvent actionEvent) {
        searchPane.handleButtonAction(actionEvent);
    }

}
