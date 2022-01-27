package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RoutingMetric;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DisplayAutoRoutesDialog extends Dialog<List<RoutingMetric>> {

    private final TableView<RoutingMetric> routingChoicesTable = new TableView<>();

    private final List<RoutingMetric> selectedRoutingMetrics = new ArrayList<>();
    private final PossibleRoutes possibleRoutes;

    public DisplayAutoRoutesDialog(@NotNull Stage stage, @NotNull PossibleRoutes possibleRoutes) {
        this.possibleRoutes = possibleRoutes;
        this.setTitle("Select Routes to Plot");

        VBox vBox = new VBox();
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        createTable(vBox);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(5);

        Button choosePlotButton = new Button("Plot Selected route(s)");
        choosePlotButton.setFont(font);
        choosePlotButton.setOnAction(this::plotSelected);
        buttonBox.getChildren().add(choosePlotButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(font);
        cancelButton.setOnAction(this::cancelSelected);
        buttonBox.getChildren().add(cancelButton);

        vBox.getChildren().add(buttonBox);

        this.getDialogPane().setContent(vBox);

        updateTable(possibleRoutes);

        // set the dialog as a utility
        stage.setOnCloseRequest(this::close);
    }


    private void updateTable(@NotNull PossibleRoutes possibleRoutes) {
        routingChoicesTable.getItems().clear();
        possibleRoutes.getRoutes().forEach(metric -> routingChoicesTable.getItems().add(metric));

    }

    private void createTable(@NotNull VBox vBox) {
        routingChoicesTable.setPrefWidth(750);

        TableColumn<RoutingMetric, String> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        routingChoicesTable.getColumns().add(rankColumn);

        TableColumn<RoutingMetric, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        routingChoicesTable.getColumns().add(pathColumn);

        TableColumn<RoutingMetric, String> numSegmentsColumn = new TableColumn<>("Number of Segments");
        numSegmentsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfSegments"));
        routingChoicesTable.getColumns().add(numSegmentsColumn);

        TableColumn<RoutingMetric, String> totalLengthColumn = new TableColumn<>("Total Path Length in ly");
        totalLengthColumn.setCellValueFactory(new PropertyValueFactory<>("totalLength"));
        routingChoicesTable.getColumns().add(totalLengthColumn);

        // set the default
        routingChoicesTable.setPlaceholder(new Label("No rows to display"));
        TableView.TableViewSelectionModel<RoutingMetric> selectionModel = routingChoicesTable.getSelectionModel();

        // set selection mode to multiple
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        ObservableList<RoutingMetric> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener(this::onChanged);

        vBox.getChildren().add(routingChoicesTable);

        selectionModel.selectFirst();
        selectedRoutingMetrics.add(possibleRoutes.getRoutes().get(1));
    }

    private void onChanged(ListChangeListener.@NotNull Change<? extends RoutingMetric> change) {
        selectedRoutingMetrics.clear();
        ObservableList<RoutingMetric> selectedItems = (ObservableList<RoutingMetric>) change.getList();
        if (selectedItems.size() != 0) {
            selectedRoutingMetrics.addAll(selectedItems);
        }
    }

    private void plotSelected(ActionEvent actionEvent) {
        setResult(selectedRoutingMetrics);
    }


    private void cancelSelected(ActionEvent actionEvent) {
        setResult(new ArrayList<>());
    }

    private void close(WindowEvent windowEvent) {
        setResult(new ArrayList<>());
    }

}
