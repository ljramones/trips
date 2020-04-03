package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.elasticsearch.model.ChViewRecord;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is a popup dependent window that we use to host a table for CH View data
 * <p>
 * Created by larrymitchell on 2017-02-25.
 */
@Slf4j
public class CHViewTable {

    /**
     * the actual ui component to hold these entries
     */
    private Dialog dialog = new Dialog();

    /**
     * the underlying windows component that the dialog belongs to
     */
    private Window window;

    /**
     * the table that we use to show the data
     */
    private TableView table = new TableView();

    private Stage stage;
    /**
     * the list of records to show/edit
     */
    private Map<Integer, ChViewRecord> chViewRecords;

    /**
     * the constructure that we use to show the data
     *
     * @param stage
     * @param chViewRecords the lsit of records that we display
     */
    public CHViewTable(Stage stage, Map<Integer, ChViewRecord> chViewRecords) {
        this.stage = stage;
        this.chViewRecords = chViewRecords;

//        int rowCount = 45;
//        int columnCount = 10;
//        GridBase grid = new GridBase(rowCount, columnCount);
//
//        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
//        for (int row = 0; row < grid.getRowCount(); ++row) {
//            final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
//            for (int column = 0; column < grid.getColumnCount(); ++column) {
//                list.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,"value"));
//            }
//            rows.add(list);
//        }
//        grid.setRows(rows);
//
//        SpreadsheetView spv = new SpreadsheetView(grid);

        dialog = new Dialog();

        dialog.setTitle("CH Records Table");
        // set the dimensions
        dialog.setHeight(600);
        dialog.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(getPanel(stage));

        Button addButton = new Button();
        addButton.setText("Add Entry");
        addButton.setOnAction(event -> addNewDataEntry());

        Button dismissButton = new Button();
        dismissButton.setText("Dismiss");
        dismissButton.setOnAction(event -> window.hide());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(addButton, new Separator(), dismissButton);
        Pane bottomPane = new Pane();
        bottomPane.getChildren().addAll(hBox);
        vBox.getChildren().add(bottomPane);

        // set the windows close
        window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        dialog.getDialogPane().setContent(vBox);
        dialog.show();
    }


    private void addNewDataEntry() {

    }

    private void setupTable(TableView table) {

        // allow the table to be edittable
        table.setEditable(true);

        // set table columns
        TableColumn properPlaceNameCol = new TableColumn("Proper Place Name");
        properPlaceNameCol.setMinWidth(100);
        TableColumn starNameCol = new TableColumn("Star Name");
        starNameCol.setMinWidth(100);
        TableColumn distanceToEarthCol = new TableColumn("Distance to Earth");
        distanceToEarthCol.setMinWidth(100);

        TableColumn spectraCol = new TableColumn("Spectra");
        spectraCol.setMinWidth(100);
        TableColumn collapsedMassCol = new TableColumn("Collapsed mass");
        collapsedMassCol.setMinWidth(100);
        TableColumn uncollapsedMassCol = new TableColumn("Uncollapsed mass");
        uncollapsedMassCol.setMinWidth(100);

        TableColumn xCoordCol = new TableColumn("X");
        xCoordCol.setMinWidth(50);
        TableColumn yCoordCol = new TableColumn("Y");
        yCoordCol.setMinWidth(50);
        TableColumn zCoordCol = new TableColumn("Z");
        zCoordCol.setMinWidth(50);

        TableColumn constellationCol = new TableColumn("constellation");
        constellationCol.setMinWidth(100);

        TableColumn commentCol = new TableColumn("comment");
        commentCol.setMinWidth(100);


        // add the columns
        table.getColumns()
                .addAll(properPlaceNameCol, starNameCol, distanceToEarthCol, spectraCol,
                        collapsedMassCol, uncollapsedMassCol,
                        xCoordCol, yCoordCol, zCoordCol,
                        constellationCol, commentCol
                );
    }

    // ----------------------------- //

    public Node getPanel(Stage stage) {
        BorderPane borderPane = new BorderPane();

        int rowCount = 50;
        int columnCount = 10;
        GridBase grid = new GridBase(rowCount, columnCount);
        normalGrid(grid);
        buildBothGrid(grid);

        SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
//        spreadSheetView.getGrid().getColumnHeaders().setAll(...);

        borderPane.setCenter(spreadSheetView);

        borderPane.setLeft(buildCommonControlGrid(spreadSheetView, borderPane, "Both"));

        return borderPane;
    }

    /**
     * Build a common control Grid with some options on the left to control the
     * SpreadsheetViewInternal
     *
     * @param gridType a grid pane
     * @param spv      the spread sheet view
     * @return a grid pane
     */
    private GridPane buildCommonControlGrid(
            final SpreadsheetView spv,
            final BorderPane borderPane,
            String gridType) {

        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));

        final CheckBox rowHeader = new CheckBox("Row Header");
        rowHeader.setSelected(true);
        rowHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean arg1, Boolean arg2) {
                spv.setShowRowHeader(arg2);
            }
        });

        final CheckBox columnHeader = new CheckBox("Column Header");
        columnHeader.setSelected(true);

        /**
         * FIXME It's not working right now, see in SpreadsheetViewSkin in LayoutChildren
         */
        columnHeader.setDisable(true);

        columnHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(
                    ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
//				spv.setShowColumnHeader(arg2);
            }
        });

        //In order to change the span style more easily
        final ChoiceBox<String> typeOfGrid = new ChoiceBox<>(FXCollections.observableArrayList("Normal", "Both"));
        typeOfGrid.setValue(gridType);
        typeOfGrid.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            public void changed(
                    ObservableValue<? extends Number> arg0,
                    Number arg1, Number arg2) {

                if (arg2.equals(0)) {
                    int rowCount = 50;
                    int columnCount = 10;
                    GridBase grid = new GridBase(rowCount, columnCount);
                    normalGrid(grid);

                    SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
                    borderPane.setCenter(spreadSheetView);
                    borderPane.setLeft(buildCommonControlGrid(spreadSheetView, borderPane, "Normal"));
                } else {
                    int rowCount = 50;
                    int columnCount = 10;
                    GridBase grid = new GridBase(rowCount, columnCount);
                    normalGrid(grid);
                    buildBothGrid(grid);

                    SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
                    borderPane.setCenter(spreadSheetView);
                    borderPane.setLeft(buildCommonControlGrid(spreadSheetView, borderPane, "Both"));
                }
            }
        });

        grid.add(rowHeader, 1, 1);
        grid.add(columnHeader, 1, 2);
        grid.add(new Label("Span model:"), 1, 3);
        grid.add(typeOfGrid, 1, 4);

        return grid;
    }


    private void normalGrid(GridBase grid) {
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>(grid.getRowCount());
        for (int row = 0; row < grid.getRowCount(); ++row) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList(); //new DataRow(row, grid.getColumnCount());
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                dataRow.add(generateCell(row, column, 1, 1));
            }
            rows.add(dataRow);
        }
        grid.setRows(rows);
    }

    /**
     * Randomly generate a dataCell(list or text)
     *
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return
     */
    private SpreadsheetCell generateCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell;
        List<String> stringListTextCell = Arrays.asList(
                "Shanghai", "Paris", "New York City", "Bangkok", "Singapore",
                "Johannesburg", "Berlin", "Wellington", "London", "Montreal"
        );
        final double random = Math.random();
        if (random < 0.10) {
            List<String> stringList = Arrays.asList(
                    "China", "France", "New Zealand", "United States", "Germany", "Canada"
            );
            cell = SpreadsheetCellType.LIST(stringList).createCell(row, column, rowSpan, colSpan, null);
        } else if (random >= 0.10 && random < 0.25) {
            cell = SpreadsheetCellType.STRING.createCell(
                    row, column, rowSpan, colSpan, stringListTextCell.get((int) (Math.random() * 10)));
        } else if (random >= 0.25 && random < 0.75) {
            cell = SpreadsheetCellType.DOUBLE.createCell(
                    row, column, rowSpan, colSpan, (double) Math.round((Math.random() * 100) * 100) / 100);
        } else {
            cell = SpreadsheetCellType.DATE.createCell(
                    row, column, rowSpan, colSpan, LocalDate.now().plusDays((int) (Math.random() * 10)));
        }

        // Styling for preview
        if (row % 5 == 0) {
            cell.getStyleClass().add("five_rows");
        }
        if (column == 0 && rowSpan == 1) {
            cell.getStyleClass().add("row_header");
        }
        if (row == 0) {
            cell.getStyleClass().add("col_header");
        }
        return cell;
    }

    /**
     * Build a sample RowSpan and ColSpan grid
     *
     * @param grid the grid
     */
    private void buildBothGrid(GridBase grid) {
        grid.spanRow(2, 2, 2);
        grid.spanColumn(2, 2, 2);

        grid.spanRow(4, 2, 4);

        grid.spanColumn(5, 8, 2);

        grid.spanRow(15, 3, 8);

        grid.spanRow(3, 5, 5);
        grid.spanColumn(3, 5, 5);

        grid.spanRow(2, 10, 4);
        grid.spanColumn(3, 10, 4);

        grid.spanRow(2, 12, 3);
        grid.spanColumn(3, 22, 3);

        grid.spanRow(1, 27, 4);

        grid.spanColumn(4, 30, 3);
        grid.spanRow(4, 30, 3);
    }

}
