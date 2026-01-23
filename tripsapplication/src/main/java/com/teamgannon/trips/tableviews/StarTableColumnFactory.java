package com.teamgannon.trips.tableviews;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Factory for creating and configuring TableView columns for star data display.
 * Handles column creation, formatting, and cell factories.
 */
@Slf4j
public class StarTableColumnFactory {

    // Decimal formatters for cleaner display
    private static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("0.000");
    private static final DecimalFormat RA_DEC_FORMAT = new DecimalFormat("0.0000");
    private static final DecimalFormat GENERAL_FORMAT = new DecimalFormat("0.00");

    /**
     * All columns indexed by their ID.
     */
    @Getter
    private final Map<String, TableColumn<StarEditRecord, ?>> columnMap = new LinkedHashMap<>();

    // Columns - visible by default
    private final TableColumn<StarEditRecord, String> displayNameCol = new TableColumn<>("Display Name");
    private final TableColumn<StarEditRecord, Double> distanceToEarthCol = new TableColumn<>("Distance (LY)");
    private final TableColumn<StarEditRecord, String> spectraCol = new TableColumn<>("Spectra");
    private final TableColumn<StarEditRecord, Double> radiusCol = new TableColumn<>("Radius");
    private final TableColumn<StarEditRecord, Double> massCol = new TableColumn<>("Mass (M\u2609)");
    private final TableColumn<StarEditRecord, String> luminosityCol = new TableColumn<>("Luminosity");
    private final TableColumn<StarEditRecord, Double> raCol = new TableColumn<>("RA");
    private final TableColumn<StarEditRecord, Double> decCol = new TableColumn<>("Declination");
    private final TableColumn<StarEditRecord, Double> paraCol = new TableColumn<>("Parallax");
    private final TableColumn<StarEditRecord, Double> xCoordCol = new TableColumn<>("X");
    private final TableColumn<StarEditRecord, Double> yCoordCol = new TableColumn<>("Y");
    private final TableColumn<StarEditRecord, Double> zCoordCol = new TableColumn<>("Z");
    private final TableColumn<StarEditRecord, String> realCol = new TableColumn<>("Real");
    private final TableColumn<StarEditRecord, String> commentCol = new TableColumn<>("Comment");

    // Columns - hidden by default
    private final TableColumn<StarEditRecord, String> commonNameCol = new TableColumn<>("Common Name");
    private final TableColumn<StarEditRecord, String> constellationNameCol = new TableColumn<>("Constellation");
    private final TableColumn<StarEditRecord, String> polityCol = new TableColumn<>("Polity");
    private final TableColumn<StarEditRecord, Double> temperatureCol = new TableColumn<>("Temperature");

    public StarTableColumnFactory() {
        initializeColumnMap();
    }

    private void initializeColumnMap() {
        columnMap.put("displayName", displayNameCol);
        columnMap.put("distanceToEarth", distanceToEarthCol);
        columnMap.put("spectra", spectraCol);
        columnMap.put("radius", radiusCol);
        columnMap.put("mass", massCol);
        columnMap.put("luminosity", luminosityCol);
        columnMap.put("ra", raCol);
        columnMap.put("declination", decCol);
        columnMap.put("parallax", paraCol);
        columnMap.put("xCoord", xCoordCol);
        columnMap.put("yCoord", yCoordCol);
        columnMap.put("zCoord", zCoordCol);
        columnMap.put("real", realCol);
        columnMap.put("comment", commentCol);
        // Additional columns (hidden by default)
        columnMap.put("commonName", commonNameCol);
        columnMap.put("constellationName", constellationNameCol);
        columnMap.put("polity", polityCol);
        columnMap.put("temperature", temperatureCol);
    }

    /**
     * Configure all columns and add them to the table.
     *
     * @param tableView the table view to configure
     */
    public void configureColumns(TableView<StarEditRecord> tableView) {
        configureVisibleColumns();
        configureHiddenColumns();
        addColumnsToTable(tableView);
    }

    private void configureVisibleColumns() {
        // Display Name - text column
        configureTextColumn(displayNameCol, "displayName", 120, 140, true);

        // Distance - formatted decimal
        configureDoubleColumn(distanceToEarthCol, "distanceToEarth", 100, 110, DISTANCE_FORMAT, true);

        // Spectra - text column
        configureTextColumn(spectraCol, "spectra", 60, 70, true);

        // Radius - formatted decimal
        configureDoubleColumn(radiusCol, "radius", 70, 80, GENERAL_FORMAT, true);

        // Mass - formatted decimal
        configureDoubleColumn(massCol, "mass", 80, 90, GENERAL_FORMAT, true);

        // Luminosity - text column (already stored as formatted string)
        configureTextColumn(luminosityCol, "luminosity", 80, 90, true);

        // RA - formatted decimal with more precision
        configureDoubleColumn(raCol, "ra", 90, 100, RA_DEC_FORMAT, true);

        // Declination - formatted decimal with more precision
        configureDoubleColumn(decCol, "declination", 90, 100, RA_DEC_FORMAT, true);

        // Parallax - formatted decimal
        configureDoubleColumn(paraCol, "parallax", 80, 90, GENERAL_FORMAT, true);

        // X, Y, Z coordinates - formatted decimal
        configureDoubleColumn(xCoordCol, "xCoord", 80, 90, COORD_FORMAT, true);
        configureDoubleColumn(yCoordCol, "yCoord", 80, 90, COORD_FORMAT, true);
        configureDoubleColumn(zCoordCol, "zCoord", 80, 90, COORD_FORMAT, true);

        // Real - boolean as text
        configureTextColumn(realCol, "real", 50, 60, true);

        // Comment - text column
        configureTextColumn(commentCol, "comment", 150, 200, false);
    }

    private void configureHiddenColumns() {
        // Common Name - text column (hidden)
        configureTextColumn(commonNameCol, "commonName", 100, 120, true);
        commonNameCol.setVisible(false);

        // Constellation - text column (hidden)
        configureTextColumn(constellationNameCol, "constellationName", 100, 120, true);
        constellationNameCol.setVisible(false);

        // Polity - text column (hidden)
        configureTextColumn(polityCol, "polity", 80, 100, true);
        polityCol.setVisible(false);

        // Temperature - formatted decimal (hidden)
        configureDoubleColumn(temperatureCol, "temperature", 90, 100, GENERAL_FORMAT, true);
        temperatureCol.setVisible(false);
    }

    private void configureTextColumn(TableColumn<StarEditRecord, String> column,
                                     String property, int minWidth, int prefWidth, boolean sortable) {
        column.setMinWidth(minWidth);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setSortable(sortable);
    }

    private void configureDoubleColumn(TableColumn<StarEditRecord, Double> column,
                                       String property, int minWidth, int prefWidth,
                                       DecimalFormat format, boolean sortable) {
        column.setMinWidth(minWidth);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setCellFactory(createFormattedDoubleCellFactory(format));
        column.setSortable(sortable);
    }

    private void addColumnsToTable(TableView<StarEditRecord> tableView) {
        tableView.getColumns().addAll(
                displayNameCol, distanceToEarthCol, spectraCol,
                radiusCol, massCol, luminosityCol, raCol, decCol, paraCol,
                xCoordCol, yCoordCol, zCoordCol, realCol, commentCol,
                // Hidden by default
                commonNameCol, constellationNameCol, polityCol, temperatureCol
        );
    }

    /**
     * Create a cell factory that formats Double values using the given DecimalFormat.
     */
    private Callback<TableColumn<StarEditRecord, Double>, TableCell<StarEditRecord, Double>> createFormattedDoubleCellFactory(DecimalFormat format) {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        };
    }

    /**
     * Setup server-side sorting for the table.
     *
     * @param tableView           the table view
     * @param currentSortColumn   initial sort column
     * @param currentSortAscending initial sort direction
     * @param isLoadingSupplier   supplier to check if loading
     * @param onSortChanged       callback when sort changes (columnId, ascending)
     */
    public void setupServerSideSorting(TableView<StarEditRecord> tableView,
                                       String currentSortColumn,
                                       boolean currentSortAscending,
                                       java.util.function.BooleanSupplier isLoadingSupplier,
                                       BiConsumer<String, Boolean> onSortChanged) {

        final String[] sortCol = {currentSortColumn};
        final boolean[] sortAsc = {currentSortAscending};

        tableView.setSortPolicy(tv -> {
            if (isLoadingSupplier.getAsBoolean()) {
                return false;
            }
            ObservableList<TableColumn<StarEditRecord, ?>> sortOrder = tv.getSortOrder();
            if (!sortOrder.isEmpty()) {
                TableColumn<StarEditRecord, ?> column = sortOrder.get(0);
                String columnId = getColumnId(column);
                if (columnId != null && StarTableColumnConfig.isSortable(columnId)) {
                    boolean ascending = column.getSortType() == TableColumn.SortType.ASCENDING;
                    if (!columnId.equals(sortCol[0]) || ascending != sortAsc[0]) {
                        sortCol[0] = columnId;
                        sortAsc[0] = ascending;
                        onSortChanged.accept(columnId, ascending);
                    }
                }
            }
            return true;
        });
    }

    /**
     * Get the column ID for a given column.
     */
    public String getColumnId(TableColumn<StarEditRecord, ?> column) {
        for (Map.Entry<String, TableColumn<StarEditRecord, ?>> entry : columnMap.entrySet()) {
            if (entry.getValue() == column) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get a column by its ID.
     */
    public TableColumn<StarEditRecord, ?> getColumn(String columnId) {
        return columnMap.get(columnId);
    }

    /**
     * Set column visibility.
     */
    public void setColumnVisible(String columnId, boolean visible) {
        TableColumn<StarEditRecord, ?> column = columnMap.get(columnId);
        if (column != null) {
            column.setVisible(visible);
        }
    }

    /**
     * Check if a column is visible.
     */
    public boolean isColumnVisible(String columnId) {
        TableColumn<StarEditRecord, ?> column = columnMap.get(columnId);
        return column != null && column.isVisible();
    }
}
