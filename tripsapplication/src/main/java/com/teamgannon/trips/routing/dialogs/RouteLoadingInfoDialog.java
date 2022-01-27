package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.graphsearch.GraphRouteResult;
import com.teamgannon.trips.service.graphsearch.GraphSearchComplete;
import com.teamgannon.trips.service.graphsearch.LargeGraphSearchService;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteLoadingInfoDialog extends Dialog<GraphRouteResult> implements GraphSearchComplete {

    private StatusUpdaterListener statusUpdaterListener;

    private final ProgressBar searchProgressBar = new ProgressBar();
    private final Label searchProgressText = new Label("waiting for graph selection");

    private LargeGraphSearchService largeGraphSearchService;

    private final Button finishBtn = new Button("Finish");
    private final Button cancelBtn = new Button("Cancel");

    public RouteLoadingInfoDialog(DataSetDescriptor currentDataset,
                                  DatabaseManagementService databaseManagementService,
                                  LargeGraphSearchService largeGraphSearchService,
                                  StatusUpdaterListener statusUpdaterListener,
                                  RouteFindingOptions routeFindingOptions) {

        this.largeGraphSearchService = largeGraphSearchService;
        this.statusUpdaterListener = statusUpdaterListener;

        VBox vBox = new VBox();

        // setup report info
        GridPane gridPane = new GridPane();
        gridPane.setMinWidth(600);

        Label statusButtonLabel = new Label("Status: ");
        statusButtonLabel.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 12));

        gridPane.add(statusButtonLabel, 0, 0);
        gridPane.add(searchProgressText, 1, 0);
        searchProgressBar.setProgress(0);
        searchProgressBar.setMinWidth(500);
        gridPane.add(searchProgressBar, 0, 1, 2, 1);
        vBox.getChildren().add(gridPane);

        // define buttons
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        finishBtn.setDisable(true);
        finishBtn.setOnAction(this::finishClicked);
        hBox.getChildren().add(finishBtn);

        cancelBtn.setDisable(false);
        cancelBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(cancelBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

        startSearch(currentDataset, databaseManagementService, routeFindingOptions);

    }

    private void startSearch(DataSetDescriptor currentDataset,
                             DatabaseManagementService databaseManagementService,
                             RouteFindingOptions routeFindingOptions) {

        // always reset the task state
        largeGraphSearchService.reset();
//        largeGraphSearchService.restart();

        // start the background task running
        largeGraphSearchService.processGraphSearch(
                routeFindingOptions,
                currentDataset,
                databaseManagementService,
                statusUpdaterListener,
                this,
                searchProgressText,
                searchProgressBar,
                cancelBtn);
        largeGraphSearchService.start();
    }


    private void finishClicked(ActionEvent actionEvent) {
        GraphRouteResult routeResult = largeGraphSearchService.getValue();
        setResult(routeResult);
        log.info("finish find routes clicked");
    }

    private void cancelClicked(ActionEvent actionEvent) {
        boolean cancelled = largeGraphSearchService.cancelSearch();
        log.info("search task was cancelled = {}", cancelled);
        setResult(
                GraphRouteResult
                        .builder()
                        .routeFound(false)
                        .searchCancelled(true)
                        .build()
        );
        log.info("cancel find routes clicked");
    }

    @Override
    public void complete(boolean status, String errorMessage) {
        log.info("graph search complete with status={}, message ={}", status, errorMessage);
        finishBtn.setDisable(false);
    }
}
