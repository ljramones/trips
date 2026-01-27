package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.routing.RoutingConstants;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RoutingMetric;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Non-modal dialog for selecting and previewing routes.
 * Allows user to interact with the map while comparing routes.
 */
public class DisplayAutoRoutesDialog {

    private final Stage stage;
    private final TableView<RoutingMetric> routingChoicesTable = new TableView<>();

    private final List<RoutingMetric> selectedRoutingMetrics = new ArrayList<>();
    private final List<RoutingMetric> finalSelectedRoutes = new ArrayList<>();
    private final PossibleRoutes possibleRoutes;

    // Callbacks for route actions
    private final Consumer<List<RoutingMetric>> previewCallback;
    private final Consumer<List<RoutingMetric>> acceptCallback;
    private Runnable cancelCallback;

    // Number of stars per line in path display
    private static final int STARS_PER_LINE = 4;

    /**
     * Create non-modal dialog with preview and accept callbacks.
     *
     * @param possibleRoutes  the routes to display
     * @param previewCallback callback invoked when user clicks "Preview" (can be null)
     * @param acceptCallback  callback invoked when user clicks "Accept" with final selection
     */
    public DisplayAutoRoutesDialog(@NotNull PossibleRoutes possibleRoutes,
                                   @Nullable Consumer<List<RoutingMetric>> previewCallback,
                                   @NotNull Consumer<List<RoutingMetric>> acceptCallback) {
        this.possibleRoutes = possibleRoutes;
        this.previewCallback = previewCallback;
        this.acceptCallback = acceptCallback;

        // Create non-modal stage
        stage = new Stage();
        stage.initStyle(StageStyle.DECORATED);
        stage.initModality(Modality.NONE);  // Non-modal: user can interact with main window
        stage.setTitle("Select Routes to Plot");
        stage.setResizable(true);
        stage.setMinWidth(700);
        stage.setMinHeight(400);

        // Keep on top but allow interaction with main window
        stage.setAlwaysOnTop(true);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        Font font = RoutingConstants.createDialogFont();

        // Add instruction label
        Label instructionLabel = new Label(
                """
                Select route(s) and click 'Preview' to see them on the map. \
                You can interact with the map while this window is open. \
                Click 'Accept' when done.\
                """);
        instructionLabel.setFont(font);
        instructionLabel.setWrapText(true);
        vBox.getChildren().add(instructionLabel);

        createTable(vBox);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button previewButton = new Button("Preview Selected");
        previewButton.setFont(font);
        previewButton.setOnAction(this::previewSelected);
        buttonBox.getChildren().add(previewButton);

        Button acceptButton = new Button("Accept && Close");
        acceptButton.setFont(font);
        acceptButton.setOnAction(this::acceptSelected);
        buttonBox.getChildren().add(acceptButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(font);
        cancelButton.setOnAction(this::cancelSelected);
        buttonBox.getChildren().add(cancelButton);

        vBox.getChildren().add(buttonBox);

        Scene scene = new Scene(vBox, 950, 550);
        stage.setScene(scene);

        updateTable(possibleRoutes);

        // Handle window close
        stage.setOnCloseRequest(event -> {
            if (cancelCallback != null) {
                cancelCallback.run();
            }
        });
    }

    /**
     * Set callback for cancel action.
     */
    public void setOnCancel(Runnable callback) {
        this.cancelCallback = callback;
    }

    /**
     * Show the dialog (non-blocking).
     */
    public void show() {
        stage.show();
        stage.toFront();
    }

    /**
     * Close the dialog.
     */
    public void close() {
        stage.close();
    }

    /**
     * Get the stage for positioning purposes.
     */
    public Stage getStage() {
        return stage;
    }

    private void updateTable(@NotNull PossibleRoutes possibleRoutes) {
        routingChoicesTable.getItems().clear();
        possibleRoutes.getRoutes().forEach(metric -> routingChoicesTable.getItems().add(metric));
    }

    private void createTable(@NotNull VBox vBox) {
        // Make table resize with dialog
        routingChoicesTable.setPrefWidth(RoutingConstants.ROUTE_TABLE_WIDTH);
        routingChoicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(routingChoicesTable, Priority.ALWAYS);

        // Enable row height to accommodate multiline text
        routingChoicesTable.setFixedCellSize(-1);

        TableColumn<RoutingMetric, Integer> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankColumn.setPrefWidth(50);
        rankColumn.setMinWidth(40);
        rankColumn.setMaxWidth(80);
        routingChoicesTable.getColumns().add(rankColumn);

        // Path column with multiline wrapping
        TableColumn<RoutingMetric, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathColumn.setPrefWidth(550);
        pathColumn.setCellFactory(createMultilinePathCellFactory());
        routingChoicesTable.getColumns().add(pathColumn);

        TableColumn<RoutingMetric, Integer> numSegmentsColumn = new TableColumn<>("Segments");
        numSegmentsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfSegments"));
        numSegmentsColumn.setPrefWidth(70);
        numSegmentsColumn.setMinWidth(60);
        numSegmentsColumn.setMaxWidth(90);
        routingChoicesTable.getColumns().add(numSegmentsColumn);

        TableColumn<RoutingMetric, Double> totalLengthColumn = new TableColumn<>("Length (ly)");
        totalLengthColumn.setCellValueFactory(new PropertyValueFactory<>("totalLength"));
        totalLengthColumn.setPrefWidth(90);
        totalLengthColumn.setMinWidth(70);
        totalLengthColumn.setMaxWidth(120);
        // Format to 2 decimal places
        totalLengthColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("%.2f".formatted(value));
                }
            }
        });
        routingChoicesTable.getColumns().add(totalLengthColumn);

        // Set the default
        routingChoicesTable.setPlaceholder(new Label("No routes to display"));
        TableView.TableViewSelectionModel<RoutingMetric> selectionModel = routingChoicesTable.getSelectionModel();

        // Set selection mode to multiple
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        ObservableList<RoutingMetric> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener(this::onChanged);

        vBox.getChildren().add(routingChoicesTable);

        // Select first row by default
        selectionModel.selectFirst();
        if (!possibleRoutes.getRoutes().isEmpty()) {
            selectedRoutingMetrics.add(possibleRoutes.getRoutes().get(0));
        }
    }

    /**
     * Create a cell factory that formats the path as multiline text.
     * Stars are grouped with STARS_PER_LINE per line for readability.
     */
    private Callback<TableColumn<RoutingMetric, String>, TableCell<RoutingMetric, String>> createMultilinePathCellFactory() {
        return column -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                setGraphic(text);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null) {
                    text.setText(null);
                    setTooltip(null);
                } else {
                    // Format path with line breaks every STARS_PER_LINE stars
                    String formattedPath = formatPathMultiline(path);
                    text.setText(formattedPath);
                    // Full path in tooltip for reference
                    Tooltip tooltip = new Tooltip(path);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(600);
                    setTooltip(tooltip);
                }
            }
        };
    }

    /**
     * Format a comma-separated path string into multiple lines.
     * Groups stars with arrows for visual clarity.
     */
    private String formatPathMultiline(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        // Split on comma and trim
        String[] stars = path.split(",\\s*");
        if (stars.length <= STARS_PER_LINE) {
            // Short path - join with arrows
            return String.join(" → ", stars);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars.length; i++) {
            if (i > 0) {
                if (i % STARS_PER_LINE == 0) {
                    sb.append(" →\n  → ");  // New line with continuation arrow
                } else {
                    sb.append(" → ");
                }
            }
            sb.append(stars[i].trim());
        }
        return sb.toString();
    }

    private void onChanged(ListChangeListener.@NotNull Change<? extends RoutingMetric> change) {
        selectedRoutingMetrics.clear();
        ObservableList<RoutingMetric> selectedItems = (ObservableList<RoutingMetric>) change.getList();
        if (!selectedItems.isEmpty()) {
            selectedRoutingMetrics.addAll(selectedItems);
        }
    }

    /**
     * Preview selected routes without closing the dialog.
     */
    private void previewSelected(ActionEvent actionEvent) {
        if (!selectedRoutingMetrics.isEmpty()) {
            // Remember the current selection for final accept
            finalSelectedRoutes.clear();
            finalSelectedRoutes.addAll(selectedRoutingMetrics);
            // Plot the routes via callback (dialog stays open)
            if (previewCallback != null) {
                previewCallback.accept(new ArrayList<>(selectedRoutingMetrics));
            } else {
                // If no preview callback, use accept callback for preview
                acceptCallback.accept(new ArrayList<>(selectedRoutingMetrics));
            }
        }
    }

    /**
     * Accept the currently selected routes and close the dialog.
     */
    private void acceptSelected(ActionEvent actionEvent) {
        List<RoutingMetric> routesToAccept;
        if (!finalSelectedRoutes.isEmpty()) {
            routesToAccept = new ArrayList<>(finalSelectedRoutes);
        } else if (!selectedRoutingMetrics.isEmpty()) {
            routesToAccept = new ArrayList<>(selectedRoutingMetrics);
        } else {
            routesToAccept = new ArrayList<>();
        }

        acceptCallback.accept(routesToAccept);
        stage.close();
    }

    private void cancelSelected(ActionEvent actionEvent) {
        if (cancelCallback != null) {
            cancelCallback.run();
        }
        stage.close();
    }
}
