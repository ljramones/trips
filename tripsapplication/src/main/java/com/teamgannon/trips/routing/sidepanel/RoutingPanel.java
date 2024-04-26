package com.teamgannon.trips.routing.sidepanel;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.ClearDataEvent;
import com.teamgannon.trips.events.RoutingPanelUpdateEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.dialogs.RouteEditDialog;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RouteChange;
import com.teamgannon.trips.routing.table.ColorTableCell;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Component
public class RoutingPanel extends Pane implements RoutingCallback {

    /**
     * the table view of the routes
     */
    private final TableView<RouteTree> routingTableView = new TableView<>();

    /**
     * route update listener - used to handle route events
     */
    @Setter
    private RouteUpdaterListener routeUpdaterListener;

    /**
     * status update listener - used to handle status events
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * the constructor
     */
    public RoutingPanel(ApplicationEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;

        routingTableView.setPlaceholder(new Label("No routes in this dataset"));
        routingTableView.setEditable(true);
        routingTableView.setPrefHeight(800);
        routingTableView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);

        TableColumn<RouteTree, Boolean> showRouteCol = createCheckBoxColumn();
        showRouteCol.setPrefWidth(80);

        TableColumn<RouteTree, String> showStatusCol = createStatusColumn();
        showRouteCol.setPrefWidth(40);

        TableColumn<RouteTree, Color> colorCol = createRouteColorTableColumn();
        colorCol.setPrefWidth(60);

        TableColumn<RouteTree, String> routeCol = createRouteTableColumn();
        routeCol.setPrefWidth(300);

        routingTableView.getColumns().add(showRouteCol);
        routingTableView.getColumns().add(showStatusCol);
        routingTableView.getColumns().add(colorCol);
        routingTableView.getColumns().add(routeCol);

        setSelectionModel();

        setTableContextMenu();

        this.getChildren().add(routingTableView);
    }


    private void setTableContextMenu() {
        final ContextMenu tableContextMenu = new ContextMenu();

        final MenuItem editRouteMenuItem = new MenuItem("Edit route");
        editRouteMenuItem.setOnAction(event -> {
            final RouteTree routeTree = routingTableView.getSelectionModel().getSelectedItem();
            if (routeTree != null) {
                RouteEditDialog routeEditDialog = new RouteEditDialog(routeTree);
                Optional<RouteChange> routeChangeOptional = routeEditDialog.showAndWait();
                if (routeChangeOptional.isPresent()) {
                    RouteChange routeChange = routeChangeOptional.get();
                    if (routeChange.isChanged()) {
                        routeUpdaterListener.updateRoute(RouteTree.toRouteDescriptor(routeTree));
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, routeTree.getRouteName() + " was edited"));
                    }
                }
            } else {
                showErrorAlert("Edit Star",
                        "record: could not be found. This is odd");
            }
        });

        final MenuItem deleteRouteMenuItem = new MenuItem("Delete selected");
        deleteRouteMenuItem.setOnAction(event -> {
            final RouteTree routeTree = routingTableView.getSelectionModel().getSelectedItem();
            if (routeTree != null) {
                Optional<ButtonType> buttonTypeOptional = showConfirmationAlert(
                        "Route display",
                        "delete route",
                        "Are you sure that you want to delete this route?");
                if (buttonTypeOptional.isPresent()) {
                    ButtonType buttonType = buttonTypeOptional.get();
                    if (buttonType.equals(ButtonType.OK)) {
                        routeUpdaterListener.deleteRoute(RouteTree.toRouteDescriptor(routeTree));
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, routeTree.getRouteName() + " was deleted"));
                    }
                }
            } else {
                showErrorAlert("Delete Star",
                        "record: could not be found. This is odd");
            }
        });

        tableContextMenu.getItems().addAll(editRouteMenuItem, deleteRouteMenuItem);
        routingTableView.setContextMenu(tableContextMenu);
    }

    private void setSelectionModel() {
        TableView.TableViewSelectionModel<RouteTree> selectionModel = routingTableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<RouteTree> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<RouteTree>) change ->
                log.info("Selection changed: " + change.getList()));
    }

    @NotNull
    private TableColumn<RouteTree, String> createRouteTableColumn() {
        TableColumn<RouteTree, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        return routeCol;
    }


    private TableColumn<RouteTree, String> createStatusColumn() {
        TableColumn<RouteTree, String> routeCol = new TableColumn<>("Visibility");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("visibility"));
        return routeCol;
    }

    @NotNull
    private TableColumn<RouteTree, Color> createRouteColorTableColumn() {
        TableColumn<RouteTree, Color> colorCol = new TableColumn<>("Color");
        colorCol.setPrefWidth(65);
        colorCol.setCellValueFactory(new PropertyValueFactory<>("routeColor"));
        colorCol.setCellFactory(column -> {
            ColorTableCell<RouteTree> colorTableCell = new ColorTableCell<>(column);
            colorTableCell.setRoutingCallback(this);
            return colorTableCell;
        });
        return colorCol;
    }

    private TableColumn<RouteTree, Boolean> createCheckBoxColumn() {
        TableColumn<RouteTree, Boolean> tableColumn = new TableColumn<>("Show?");
        tableColumn.setCellFactory(column -> {
            CheckBoxTableCell<RouteTree, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
            final BooleanProperty selected = new SimpleBooleanProperty(true);
            checkBoxTableCell.setSelectedStateCallback(index -> selected);

            selected.addListener((obs, wasSelected, isSelected) -> {
                RouteTree routeTree;
                routeTree = routingTableView.getSelectionModel().getSelectedItem();
                if (routeTree == null) {
                    TableRow<RouteTree> tableRow = checkBoxTableCell.getTableRow();
                    if (tableRow != null) {
                        routeTree = tableRow.getItem();
                    }
                }
                if (routeTree != null) {
                    routeUpdaterListener.displayRoute(RouteTree.toRouteDescriptor(routeTree), isSelected);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, routeTree.getRouteName() + " is " + (isSelected ? "shown" : "not shown")));
                }
            });
            return checkBoxTableCell;
        });
        tableColumn.setEditable(true);
        tableColumn.setOnEditCommit(event -> {
            log.info("edit commit checkbox");
            RouteTree routeTree = event.getRowValue();
            RouteDescriptor routeDescriptor = RouteDescriptor.toRouteDescriptor(routeTree);
            routeUpdaterListener.displayRoute(routeDescriptor, routeTree.isChecked());
            eventPublisher.publishEvent(new StatusUpdateEvent(this, routeTree.getRouteName() + " was edited"));
        });
        return tableColumn;
    }

    private void routingChanged(Observable observable) {
        log.info("routing selected");
    }

    /**
     * set the dataset context
     *
     * @param descriptor         the descriptor
     * @param routeVisibilityMap a map of which routes are visible
     */
    public void setContext(@Nullable DataSetDescriptor descriptor,
                           Map<UUID, RouteVisibility> routeVisibilityMap) {

        routingTableView.getItems().clear();

        if (descriptor != null) {
            List<Route> routeList = descriptor.getRoutes();
            if (!routeList.isEmpty()) {
                // we compare the descriptor routes to what we have actually plotted
                // some of all of the potential routes might be present
                // we also want to note routes that aren't plotted
                for (Route route : routeList) {
                    // iterate each of the DB routes, look up the associated plotted route and mark visiblity
                    // if a db route is not in the visibility list, then mark it as not visible
                    // get the visibility list
                    //     a route plotted in full has the original UUID
                    //     a partial route has a new id since there ma
                    RouteVisibility routeVisibility = routeVisibilityMap.get(route.getUuid());
                    RouteTree routeTree = RouteTree.createRouteTree(route, routeVisibility);
                    routingTableView.getItems().add(routeTree);
                }
                log.info("adding routes");
            }

        } else {
            log.error("This dataset should be there but we couldn't find it");
        }
    }

    public void clearData() {
        routingTableView.getItems().clear();
    }

    @Override
    public void triggerColorChange(Object object, Color color) {
        RouteTree routeTree = (RouteTree) object;
        routeTree.setRouteColor(color);
        routeUpdaterListener.updateRoute(RouteTree.toRouteDescriptor(routeTree));
        log.info(routeTree.getRoute());
    }

    private void handleCheckboxEvent(ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean
            isSelected) {
        RouteTree routeTree;
        if (wasSelected) {
            routeTree = routingTableView.getSelectionModel().getSelectedItem();
        } else {
            routeTree = null;
        }
        if (routeTree != null) {
            routeUpdaterListener.displayRoute(RouteTree.toRouteDescriptor(routeTree), isSelected);
        }
    }


    /**
     * This method is an event listener that is triggered when a clear data event occurs.
     * It runs on the JavaFX Application thread and calls the clearData() method.
     */
    @EventListener
    public void onClearDataEvent(ClearDataEvent event) {
        Platform.runLater(this::clearData);
    }

    @EventListener
    public void onRoutingPanelUpdateEvent(RoutingPanelUpdateEvent event) {
        Platform.runLater(() -> {
            log.info("ROUTING PANEL UPDATE event received ::: descriptor:{}", event.getDataSetDescriptor().getDataSetName());
            this.setContext(event.getDataSetDescriptor(), event.getRouteVisibilityMap());
        });
    }
}


