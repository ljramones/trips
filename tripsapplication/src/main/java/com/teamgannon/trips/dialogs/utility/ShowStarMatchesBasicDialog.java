package com.teamgannon.trips.dialogs.utility;

import com.teamgannon.trips.dialogs.utility.model.StarSelectionObject;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ShowStarMatchesBasicDialog extends Dialog<StarSelectionObject> {

    private StarSelectionObject starSelectionObject = StarSelectionObject.builder().selected(false).build();

    private final List<StarObject> starObjects;

    private final TableView<StarObject> tableView = new TableView<>();
    private final TableColumn<StarObject, String> displayNameCol = new TableColumn<>("Display Name");
    private final TableColumn<StarObject, Double> catalogIdCol = new TableColumn<>("Catalog Id List");
    private final TableColumn<StarObject, Double> distanceToEarthCol = new TableColumn<>("Distance to Earth(ly)");
    private final TableColumn<StarObject, String> spectraCol = new TableColumn<>("Spectra");
    private final TableColumn<StarObject, Double> radiusCol = new TableColumn<>("Radius");
    private final TableColumn<StarObject, Double> raCol = new TableColumn<>("RA");
    private final TableColumn<StarObject, Double> decCol = new TableColumn<>("Declination");
    private final TableColumn<StarObject, Double> paraCol = new TableColumn<>("Parallax");
    private final TableColumn<StarObject, Double> xCoordCol = new TableColumn<>("X");
    private final TableColumn<StarObject, Double> yCoordCol = new TableColumn<>("Y");
    private final TableColumn<StarObject, Double> zCoordCol = new TableColumn<>("Z");
    private final TableColumn<StarObject, String> realCol = new TableColumn<>("Real");
    private final TableColumn<StarObject, String> commentCol = new TableColumn<>("comment");

    public ShowStarMatchesBasicDialog(List<StarObject> starObjects) {
        this.starObjects = starObjects;

        this.setTitle("Show discovered stars");
        this.setHeight(700);
        this.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(tableView);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button selectDataSetButton = new Button("Select");
        selectDataSetButton.setOnAction(this::selectStar);
        hBox2.getChildren().add(selectDataSetButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // setup the table structure
        setupTable();

        // load data
        loadData();

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

    }

    private void selectStar(ActionEvent actionEvent) {
        final StarObject starObject = tableView.getSelectionModel().getSelectedItem();
        if (starObject!=null) {
            starSelectionObject.setStar(starObject);
            starSelectionObject.setSelected(true);
            setResult(starSelectionObject);
        }
    }

    private void setupTable() {
        // allow the table to be editable
        tableView.setPlaceholder(new Label("No rows to display"));

        // set selection model
        setSelectionModel();

        // setup context menu
        setupContextMenu();

        // set table columns
        setupTableColumns();
    }


    private void setupContextMenu() {
        final ContextMenu tableContextMenu = new ContextMenu();

        final MenuItem chooseSelectedMenuItem = new MenuItem("Choose");
        chooseSelectedMenuItem.setOnAction(event -> {
            final StarObject starObject = tableView.getSelectionModel().getSelectedItem();
            starSelectionObject.setStar(starObject);
            starSelectionObject.setSelected(true);
            setResult(starSelectionObject);
        });
        tableContextMenu.getItems().add(chooseSelectedMenuItem);

        tableView.setContextMenu(tableContextMenu);
    }

    private void setupTableColumns() {
        displayNameCol.setMinWidth(100);
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));

        catalogIdCol.setMinWidth(100);
        catalogIdCol.setCellValueFactory(new PropertyValueFactory<>("catalogIdList"));

        distanceToEarthCol.setMinWidth(120);
        distanceToEarthCol.setCellValueFactory(new PropertyValueFactory<>("distance"));

        spectraCol.setMinWidth(70);
        spectraCol.setCellValueFactory(new PropertyValueFactory<>("spectralClass"));

        radiusCol.setMinWidth(100);
        radiusCol.setCellValueFactory(new PropertyValueFactory<>("radius"));

        raCol.setMinWidth(50);
        raCol.setCellValueFactory(new PropertyValueFactory<>("ra"));

        decCol.setMinWidth(70);
        decCol.setCellValueFactory(new PropertyValueFactory<>("declination"));

        paraCol.setMinWidth(70);
        paraCol.setCellValueFactory(new PropertyValueFactory<>("parallax"));

        xCoordCol.setMinWidth(50);
        xCoordCol.setCellValueFactory(new PropertyValueFactory<>("x"));

        yCoordCol.setMinWidth(50);
        yCoordCol.setCellValueFactory(new PropertyValueFactory<>("y"));

        zCoordCol.setMinWidth(50);
        zCoordCol.setCellValueFactory(new PropertyValueFactory<>("z"));

        realCol.setMinWidth(100);
        realCol.setCellValueFactory(new PropertyValueFactory<>("realStar"));

        commentCol.setMinWidth(200);
        commentCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        commentCol.setSortable(false);

        // add the columns
        tableView.getColumns()
                .addAll(displayNameCol, catalogIdCol, distanceToEarthCol, spectraCol,
                        radiusCol, raCol, decCol, paraCol,
                        xCoordCol, yCoordCol, zCoordCol,
                        realCol, commentCol
                );
    }

    private void setSelectionModel() {
        TableView.TableViewSelectionModel<StarObject> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<StarObject> selectedItems = selectionModel.getSelectedItems();

        selectedItems.addListener((ListChangeListener<StarObject>) change ->
                log.info("Selection changed: " + change.getList()));
    }

    /**
     * load the data
     */
    private void loadData() {

        for (StarObject starObject : starObjects) {
            // check for a crap record
            if (starObject.getDisplayName() == null) {
                continue;
            }
            if (!starObject.getDisplayName().equalsIgnoreCase("name")) {
                tableView.getItems().add(starObject);
            }
        }
    }

    private void close(WindowEvent windowEvent) {
        setResult(starSelectionObject);
        close();
    }

    private void close(ActionEvent actionEvent) {
        setResult(starSelectionObject);
        close();
    }
}
