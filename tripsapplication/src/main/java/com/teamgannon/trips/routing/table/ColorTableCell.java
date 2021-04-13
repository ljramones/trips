package com.teamgannon.trips.routing.table;

import com.teamgannon.trips.routing.sidepanel.RoutingCallback;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class ColorTableCell<RouteTree> extends TableCell<RouteTree, Color> {
    private final ColorPicker colorPicker;
    private RoutingCallback routingPanel;

    public ColorTableCell(TableColumn<RouteTree, Color> column) {
        this.colorPicker = new ColorPicker();
        this.colorPicker.editableProperty().bind(column.editableProperty());
        this.colorPicker.disableProperty().bind(column.editableProperty().not());
        this.colorPicker.setOnShowing(event -> {
            final TableView<RouteTree> tableView = getTableView();
            tableView.getSelectionModel().select(getTableRow().getIndex());
            tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
        });
        this.colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isEditing()) {
                commitEdit(newValue);
                final TableView<RouteTree> tableView = getTableView();
                routingPanel.triggerColorChange(tableView.getSelectionModel().getSelectedItem(), newValue);
            }
        });
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        if (empty) {
            setGraphic(null);
        } else {
            this.colorPicker.setValue(item);
            this.setGraphic(this.colorPicker);
        }
    }

    public void setRoutingCallback(RoutingCallback routingPanel) {
        this.routingPanel = routingPanel;
    }
}
