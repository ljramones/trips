package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
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

import java.util.ArrayList;
import java.util.List;

/**
 * The spreadsheet table for
 * <p>
 * Created by larrymitchell on 2017-04-01.
 */
@Slf4j
public class DataSetTable {

    /**
     * the actual ui component to hold these entries
     */
    private Dialog dialog = new Dialog();

    /**
     * the underlying windows component that the dialog belongs to
     */
    private Window window;


    private Stage stage;
    /**
     * the list of records to show/edit
     */
    private List<AstrographicObject> astrographicObjects;

    /**
     * the constructure that we use to show the data
     *
     * @param stage
     * @param AstrographicObjects the lsit of records that we display
     */
    public DataSetTable(Stage stage, List<AstrographicObject> AstrographicObjects) {
        this.stage = stage;
        this.astrographicObjects = AstrographicObjects;

        dialog = new Dialog();

        dialog.setTitle("Astrographic Records Table");
        // set the dimensions
        dialog.setHeight(600);
        dialog.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(getPanel(stage));

        Button addButton = new Button();
        addButton.setText("Add Entry");
        addButton.setOnAction(event -> addNewDataEntry());
        addButton.setDisable(true);

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


    /**
     * add a new entry
     *
     * @TODO implemnt
     */
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

        int rowCount = astrographicObjects.size();
        int columnCount = 9;
        GridBase grid = new GridBase(rowCount, columnCount);
        normalGrid(grid);

        SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
        spreadSheetView.getGrid().getColumnHeaders().setAll(
                "Id", "Simbad Id", "Star Name", "Spectral Class", "Distance", "X", "Y", "Z", "Notes"
        );

        borderPane.setCenter(spreadSheetView);

        borderPane.setLeft(buildCommonControlGrid(spreadSheetView));

        return borderPane;
    }

    /**
     * Build a common control Grid with some options on the left to control the
     * SpreadsheetViewInternal
     *
     * @param spv the spread sheet view
     * @return a grid pane
     */
    private GridPane buildCommonControlGrid(
            final SpreadsheetView spv) {

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

        grid.add(rowHeader, 1, 1);
        grid.add(columnHeader, 1, 2);

        return grid;
    }


    private void normalGrid(GridBase grid) {
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>(grid.getRowCount());
        int row = 0;
        for (AstrographicObject astrographicObject : astrographicObjects) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            dataRow.add(generateCell(astrographicObject.getId().toString(), row, 0));
            dataRow.add(generateCell(astrographicObject.getSimbadId(), row, 1));
            dataRow.add(generateCell(astrographicObject.getDisplayName(), row, 2));
            dataRow.add(generateCell(astrographicObject.getSpectralClass(), row, 3));
            dataRow.add(generateCell(String.valueOf(astrographicObject.getDistance()), row, 4));

            String x = String.format("%1$,.2f", astrographicObject.getCoordinates()[0]);
            dataRow.add(generateCell(x, row, 5));
            String y = String.format("%1$,.2f", astrographicObject.getCoordinates()[1]);
            dataRow.add(generateCell(y, row, 6));
            String z = String.format("%1$,.2f", astrographicObject.getCoordinates()[2]);
            dataRow.add(generateCell(z, row, 7));

            dataRow.add(generateCell(astrographicObject.getNotes(), row, 8));
            rows.add(dataRow);
            row++;
        }
        grid.setRows(rows);
    }

    /**
     * Randomly generate a dataCell(list or text)
     *
     * @param row    the row
     * @param column the column
     * @return the spread sheet cell
     */
    private SpreadsheetCell generateCell(String value, int row, int column) {
        SpreadsheetCell cell;


        cell = SpreadsheetCellType.STRING.createCell(
                row, column, 1, 1, value);

        // Styling for preview
        if (row % 5 == 0) {
            cell.getStyleClass().add("five_rows");
        }
        if (column == 0) {
            cell.getStyleClass().add("row_header");
        }
        if (row == 0) {
            cell.getStyleClass().add("col_header");
        }
        return cell;
    }

}
