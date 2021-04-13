package com.teamgannon.trips.routing.sidepanel;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteChange;
import com.teamgannon.trips.routing.dialogs.RouteEditDialog;
import com.teamgannon.trips.routing.table.ColorTableCell;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
     * the set of routings
     */
    private final TableView<RouteTree> routingTableView = new TableView<>();
    private final RouteUpdaterListener routeUpdaterListener;

    /**
     * the constructor
     */
    public RoutingPanel(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;

        routingTableView.setPlaceholder(new Label("No routes in this dataset"));
        routingTableView.setEditable(true);
        routingTableView.setPrefHeight(800);
        routingTableView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);

        TableColumn<RouteTree, Boolean> showRouteCol = getCheckBoxColumn();

        TableColumn<RouteTree, Color> colorCol = getRouteColorTableColumn();

        TableColumn<RouteTree, String> routeCol = getRouteTableColumn();

        routingTableView.getColumns().addAll(showRouteCol, colorCol, routeCol);

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
    private TableColumn<RouteTree, String> getRouteTableColumn() {
        TableColumn<RouteTree, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        return routeCol;
    }

    @NotNull
    private TableColumn<RouteTree, Color> getRouteColorTableColumn() {
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

    private TableColumn<RouteTree, Boolean> getCheckBoxColumn() {
        TableColumn<RouteTree, Boolean> tableColumn = new TableColumn<>("Show?");
        tableColumn.setCellFactory(column -> {
            CheckBoxTableCell<RouteTree, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
            final BooleanProperty selected = new SimpleBooleanProperty(true);
            checkBoxTableCell.setSelectedStateCallback(index -> selected);
            selected.addListener((obs, wasSelected, isSelected) -> {
                final RouteTree routeTree = routingTableView.getSelectionModel().getSelectedItem();
                if (routeTree != null) {
                    routeUpdaterListener.displayRoute(RouteTree.toRouteDescriptor(routeTree), isSelected);
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
        });
        return tableColumn;
    }

    private void routingChanged(Observable observable) {
        log.info("routing selected");
    }

    /**
     * set the dataset context
     *
     * @param descriptor        the descriptor
     * @param routeVisiblityMap a map of which routes are visible
     */
    public void setContext(@Nullable DataSetDescriptor descriptor, Map<UUID, Boolean> routeVisiblityMap) {

        routingTableView.getItems().clear();

        if (descriptor != null) {
            List<Route> routeList = descriptor.getRoutes();
            if (routeList.size() != 0) {
                for (Route route : routeList) {
                    RouteTree routeTree = RouteTree.createRouteTree(route);
                    boolean willShow = routeVisiblityMap.get(route.getUuid());
                    if (willShow) {
                        routingTableView.getItems().add(routeTree);
                    }
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
}
