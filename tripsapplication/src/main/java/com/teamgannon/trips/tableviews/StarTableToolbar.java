package com.teamgannon.trips.tableviews;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Toolbar component for the star table view.
 * Contains action buttons, column visibility menu, and page navigation.
 */
public class StarTableToolbar extends HBox {

    @Getter
    private final MenuButton columnsMenu;
    private final StarTableColumnConfig columnConfig;
    private final StarTableColumnFactory columnFactory;
    private final TableView<StarEditRecord> tableView;

    /**
     * Create a new star table toolbar.
     *
     * @param columnConfig   the column configuration
     * @param columnFactory  the column factory
     * @param tableView      the table view to control
     * @param pagination     the pagination control
     * @param onAddStar      callback when Add Star is clicked
     * @param onExportPage   callback when Export Page is clicked
     * @param onExportAll    callback when Export All is clicked
     */
    public StarTableToolbar(StarTableColumnConfig columnConfig,
                            StarTableColumnFactory columnFactory,
                            TableView<StarEditRecord> tableView,
                            Pagination pagination,
                            Runnable onAddStar,
                            Runnable onExportPage,
                            Runnable onExportAll) {
        this.columnConfig = columnConfig;
        this.columnFactory = columnFactory;
        this.tableView = tableView;

        setSpacing(10);
        setPadding(new Insets(5));
        setAlignment(Pos.CENTER_LEFT);

        // Add Star button
        Button addButton = new Button("Add Star");
        addButton.setOnAction(e -> onAddStar.run());

        // Export buttons
        Button exportPageButton = new Button("Export Page CSV");
        exportPageButton.setOnAction(e -> onExportPage.run());

        Button exportAllButton = new Button("Export All CSV");
        exportAllButton.setOnAction(e -> onExportAll.run());

        // Columns menu
        columnsMenu = createColumnsMenu();

        // Jump to page controls
        Label jumpLabel = new Label("Go to page:");
        TextField pageField = new TextField();
        pageField.setPrefWidth(60);
        pageField.setOnAction(e -> {
            try {
                int pageNum = Integer.parseInt(pageField.getText()) - 1;
                if (pageNum >= 0 && pageNum < pagination.getPageCount()) {
                    pagination.setCurrentPageIndex(pageNum);
                }
            } catch (NumberFormatException ex) {
                // Ignore invalid input
            }
            pageField.clear();
        });

        // Spacer to push page controls to the right
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                addButton,
                new Separator(),
                exportPageButton,
                exportAllButton,
                new Separator(),
                columnsMenu,
                spacer,
                jumpLabel,
                pageField
        );
    }

    private MenuButton createColumnsMenu() {
        MenuButton menuButton = new MenuButton("Columns...");

        for (String columnId : StarTableColumnConfig.getAllColumnIds()) {
            CheckMenuItem item = new CheckMenuItem(StarTableColumnConfig.getDisplayName(columnId));
            TableColumn<StarEditRecord, ?> column = columnFactory.getColumn(columnId);

            // Set initial checkbox state based on column visibility
            if (column != null) {
                item.setSelected(column.isVisible());
            } else {
                item.setSelected(columnConfig.isColumnVisible(columnId));
            }

            if (column != null) {
                item.setOnAction(e -> {
                    columnConfig.setColumnVisible(columnId, item.isSelected());
                    column.setVisible(item.isSelected());
                    tableView.refresh();
                });
            }
            menuButton.getItems().add(item);
        }

        menuButton.getItems().add(new SeparatorMenuItem());

        MenuItem resetItem = new MenuItem("Reset to Defaults");
        resetItem.setOnAction(e -> resetColumnsToDefaults(menuButton));
        menuButton.getItems().add(resetItem);

        return menuButton;
    }

    private void resetColumnsToDefaults(MenuButton menuButton) {
        columnConfig.resetToDefaults();
        for (MenuItem mi : menuButton.getItems()) {
            if (mi instanceof CheckMenuItem cmi) {
                String text = cmi.getText();
                for (String colId : StarTableColumnConfig.getAllColumnIds()) {
                    if (StarTableColumnConfig.getDisplayName(colId).equals(text)) {
                        TableColumn<StarEditRecord, ?> col = columnFactory.getColumn(colId);
                        boolean shouldBeVisible = columnConfig.isColumnVisible(colId);
                        cmi.setSelected(shouldBeVisible);
                        if (col != null) {
                            col.setVisible(shouldBeVisible);
                        }
                        break;
                    }
                }
            }
        }
        tableView.refresh();
    }

    /**
     * Update a checkbox item's selected state by column ID.
     */
    public void setColumnCheckState(String columnId, boolean selected) {
        String displayName = StarTableColumnConfig.getDisplayName(columnId);
        for (MenuItem mi : columnsMenu.getItems()) {
            if (mi instanceof CheckMenuItem cmi && cmi.getText().equals(displayName)) {
                cmi.setSelected(selected);
                break;
            }
        }
    }
}
