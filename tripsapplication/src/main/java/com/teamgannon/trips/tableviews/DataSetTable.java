package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.dataset.AddStarDialog;
import com.teamgannon.trips.dialogs.dataset.TableEditResult;
import com.teamgannon.trips.dialogs.support.EditTypeEnum;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.DatabaseListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


/**
 * The spreadsheet table for
 * <p>
 * Created by larrymitchell on 2017-04-01.
 */
@Slf4j
public class DataSetTable {

    private final int PAGE_SIZE = 100;

    /**
     * the table view object
     */
    private final TableView<StarEditRecord> tableView = new TableView<>();
    private final Map<UUID, AstrographicObject> astrographicObjectMap = new HashMap<>();
    /**
     * the underlying windows component that the dialog belongs to
     */
    private Window window;
    private DatabaseListener databaseUpdater;
    private List<AstrographicObject> astrographicObjects;

    private int currentPosition = 0;

    /**
     * the constructor that we use to show the data
     *
     * @param databaseUpdater     the reference back to search for more stars if needs be
     * @param astrographicObjects the list of objects
     */

    public DataSetTable(DatabaseListener databaseUpdater, List<AstrographicObject> astrographicObjects) {
        this.databaseUpdater = databaseUpdater;
        this.astrographicObjects = astrographicObjects;
        if (!astrographicObjects.isEmpty()) {
            MapUtils.populateMap(astrographicObjectMap, astrographicObjects, AstrographicObject::getId);
        }

        // the actual ui component to hold these entries
        Dialog dialog = new Dialog();

        dialog.setTitle("Astrographic Records Table");

        // set the dimensions
        dialog.setHeight(600);
        dialog.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(getPanel());

        Button previousButton = new Button();
        previousButton.setText("<<<");
        previousButton.setOnAction(event -> moveBack());

        Button forwardButton = new Button();
        forwardButton.setText(">>>");
        forwardButton.setOnAction(event -> moveForward());

        Button addButton = new Button();
        addButton.setText("Add Entry");
        addButton.setOnAction(event -> addNewDataEntry());

        Button dismissButton = new Button();
        dismissButton.setText("Dismiss");
        dismissButton.setOnAction(event -> window.hide());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(previousButton, forwardButton, new Separator(), addButton, new Separator(), dismissButton);
        Pane bottomPane = new Pane();
        bottomPane.getChildren().addAll(hBox);
        vBox.getChildren().add(bottomPane);

        // set the windows close
        window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        dialog.getDialogPane().setContent(vBox);
        dialog.initModality(Modality.NONE);
        dialog.show();
    }

    public void loadData(List<AstrographicObject> astrographicObjects) {
        astrographicObjectMap.clear();
        if (!astrographicObjects.isEmpty()) {
            MapUtils.populateMap(astrographicObjectMap, astrographicObjects, AstrographicObject::getId);
        }
    }

    private void moveForward() {
        log.info("move forward");
        if (astrographicObjectMap.size() < PAGE_SIZE) {
            return;
        }
        if ((currentPosition + PAGE_SIZE) > astrographicObjectMap.size()) {
            currentPosition = astrographicObjectMap.size() - PAGE_SIZE;
        } else {
            currentPosition += 100;
        }
        clearData();
        loadData();
    }

    private void moveBack() {
        log.info("move back");
        if (astrographicObjectMap.size() < PAGE_SIZE) {
            return;
        }
        if (currentPosition < PAGE_SIZE) {
            currentPosition = 0;
        } else {
            currentPosition -= PAGE_SIZE;
        }
        clearData();
        loadData();
    }


    /**
     * add a new entry
     */
    private void addNewDataEntry() {
        AddStarDialog addStarDialog = new AddStarDialog();
        Optional<TableEditResult> optionalDataSet = addStarDialog.showAndWait();
        if (optionalDataSet.isPresent()) {
            TableEditResult editResult = optionalDataSet.get();
            if (editResult.getEditType().equals(EditTypeEnum.UPDATE)) {
                StarEditRecord starEditRecord = editResult.getStarEditRecord();
                log.info("star created={}", starEditRecord);
                tableView.getItems().add(starEditRecord);
                addToDB(starEditRecord);
            }
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

        final MenuItem editSelectedMenuItem = new MenuItem("Edit selected");
        editSelectedMenuItem.setOnAction(event -> {
            final StarEditRecord selectedStarEditRecord = tableView.getSelectionModel().getSelectedItem();
            AddStarDialog addStarDialog = new AddStarDialog(selectedStarEditRecord);
            Optional<TableEditResult> optionalDataSet = addStarDialog.showAndWait();
            if (optionalDataSet.isPresent()) {
                TableEditResult editResult = optionalDataSet.get();
                if (editResult.getEditType().equals(EditTypeEnum.UPDATE)) {
                    StarEditRecord starEditRecord = editResult.getStarEditRecord();
                    log.info("star created={}", starEditRecord);
                    tableView.getItems().add(starEditRecord);
                    updateEntry(starEditRecord);
                }
            }
        });


        final MenuItem deleteSelectedMenuItem = new MenuItem("Delete selected");
        deleteSelectedMenuItem.setOnAction(event -> {
            final StarEditRecord selectedStarEditRecords = tableView.getSelectionModel().getSelectedItem();
            tableView.getItems().remove(selectedStarEditRecords);
            removeFromDB(selectedStarEditRecords);
        });
        tableContextMenu.getItems().addAll(editSelectedMenuItem, deleteSelectedMenuItem);

        tableView.setContextMenu(tableContextMenu);
    }


    private void setSelectionModel() {
        TableViewSelectionModel<StarEditRecord> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<StarEditRecord> selectedItems = selectionModel.getSelectedItems();

        selectedItems.addListener((ListChangeListener<StarEditRecord>) change ->
                log.info("Selection changed: " + change.getList()));
    }

    private void setupTableColumns() {
        TableColumn<StarEditRecord, String> displayNameCol = new TableColumn<>("Display Name");
        displayNameCol.setMinWidth(100);
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        displayNameCol.setSortType(TableColumn.SortType.ASCENDING);

        TableColumn<StarEditRecord, Double> distanceToEarthCol = new TableColumn<>("Distance to Earth(ly)");
        distanceToEarthCol.setMinWidth(120);
        distanceToEarthCol.setCellValueFactory(new PropertyValueFactory<>("distanceToEarth"));

        TableColumn<StarEditRecord, String> spectraCol = new TableColumn<>("Spectra");
        spectraCol.setMinWidth(70);
        spectraCol.setCellValueFactory(new PropertyValueFactory<>("spectra"));

        TableColumn<StarEditRecord, Double> radiusCol = new TableColumn<>("Radius");
        radiusCol.setMinWidth(100);
        radiusCol.setCellValueFactory(new PropertyValueFactory<>("radius"));

        TableColumn<StarEditRecord, Double> raCol = new TableColumn<>("RA");
        raCol.setMinWidth(50);
        raCol.setCellValueFactory(new PropertyValueFactory<>("ra"));

        TableColumn<StarEditRecord, Double> decCol = new TableColumn<>("Declination");
        decCol.setMinWidth(70);
        decCol.setCellValueFactory(new PropertyValueFactory<>("declination"));

        TableColumn<StarEditRecord, Double> paraCol = new TableColumn<>("Parallax");
        paraCol.setMinWidth(70);
        paraCol.setCellValueFactory(new PropertyValueFactory<>("parallax"));

        TableColumn<StarEditRecord, Double> xCoordCol = new TableColumn<>("X");
        xCoordCol.setMinWidth(50);
        xCoordCol.setCellValueFactory(new PropertyValueFactory<>("xCoord"));

        TableColumn<StarEditRecord, Double> yCoordCol = new TableColumn<>("Y");
        yCoordCol.setMinWidth(50);
        yCoordCol.setCellValueFactory(new PropertyValueFactory<>("yCoord"));

        TableColumn<StarEditRecord, Double> zCoordCol = new TableColumn<>("Z");
        zCoordCol.setMinWidth(50);
        zCoordCol.setCellValueFactory(new PropertyValueFactory<>("zCoord"));

        TableColumn<StarEditRecord, String> realCol = new TableColumn<>("Real");
        realCol.setMinWidth(100);
        realCol.setCellValueFactory(new PropertyValueFactory<>("real"));

        TableColumn<StarEditRecord, String> commentCol = new TableColumn<>("comment");
        commentCol.setMinWidth(200);
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));

        // add the columns
        tableView.getColumns()
                .addAll(displayNameCol, distanceToEarthCol, spectraCol,
                        radiusCol, raCol, decCol, paraCol,
                        xCoordCol, yCoordCol, zCoordCol,
                        realCol, commentCol
                );
    }

    private void updateEntry(StarEditRecord starEditRecord) {
        UUID id = starEditRecord.getId();
        AstrographicObject astrographicObject = astrographicObjectMap.get(id);
        updateObject(starEditRecord, astrographicObject);

        // updated star
        databaseUpdater.updateNotesForStar(astrographicObject);

        resetList();
    }


    private void addToDB(StarEditRecord starEditRecord) {

        AstrographicObject astrographicObjectNew = convertToAstrographicObject(starEditRecord);
        astrographicObjectMap.put(astrographicObjectNew.getId(), astrographicObjectNew);

        // add to DB
        databaseUpdater.updateNotesForStar(astrographicObjectNew);

        resetList();

        log.info("Added to DB");
    }

    private AstrographicObject convertToAstrographicObject(StarEditRecord starEditRecord) {
        AstrographicObject astrographicObject = new AstrographicObject();
        updateObject(starEditRecord, astrographicObject);
        astrographicObject.setId(UUID.randomUUID());
        return astrographicObject;
    }

    private void removeFromDB(StarEditRecord starEditRecord) {

        UUID id = starEditRecord.getId();
        AstrographicObject astrographicObject = astrographicObjectMap.get(id);
        astrographicObjectMap.remove(id);

        // remove from DB
        databaseUpdater.removeStar(astrographicObject);

        resetList();

        log.info("Removed from DB");
    }

    private void resetList() {
        astrographicObjects = new ArrayList<>(astrographicObjectMap.values());

    }

    private void updateObject(StarEditRecord starEditRecord, AstrographicObject astrographicObject) {
        if (starEditRecord.getDisplayName() == null) {
            showErrorAlert("Update Star", "Star name cannot be empty!");
            return;
        }
        astrographicObject.setDisplayName(starEditRecord.getDisplayName());
        astrographicObject.setDistance(starEditRecord.getDistanceToEarth());
        astrographicObject.setSpectralClass(starEditRecord.getSpectra());
        astrographicObject.setRadius(starEditRecord.getRadius());
        astrographicObject.setRa(starEditRecord.getRa());
        astrographicObject.setDeclination(starEditRecord.getDeclination());
        if (starEditRecord.getParallax() != null) {
            astrographicObject.setParallax(starEditRecord.getParallax());
        }
        astrographicObject.setX(starEditRecord.getXCoord());
        astrographicObject.setY(starEditRecord.getYCoord());
        astrographicObject.setZ(starEditRecord.getZCoord());
        astrographicObject.setRealStar(starEditRecord.isReal());
        astrographicObject.setNotes(starEditRecord.getComment());
    }

    // ----------------------------- //

    public Node getPanel() {
        BorderPane borderPane = new BorderPane();

        // setup the table structure
        setupTable();
        borderPane.setCenter(tableView);

        // load an initial page of data
        loadData();

        return borderPane;
    }


    private void loadData() {
        int pageSize = PAGE_SIZE;
        int diff = astrographicObjects.size() - currentPosition;
        if (diff < pageSize) {
            pageSize = diff;
        }
        for (int i = currentPosition; i < (currentPosition + pageSize); i++) {
            AstrographicObject object = astrographicObjects.get(i);
            // check for a crap record
            if (object.getDisplayName() == null) {
                continue;
            }
            if (!object.getDisplayName().equalsIgnoreCase("name")) {
                StarEditRecord starEditRecord = convertToStarEditRecord(object);
                tableView.getItems().add(starEditRecord);
            }
        }
    }

    private void clearData() {
        tableView.getItems().clear();
    }

    private StarEditRecord convertToStarEditRecord(AstrographicObject astrographicObject) {
        StarEditRecord starEditRecord = new StarEditRecord();

        starEditRecord.setId(astrographicObject.getId());
        starEditRecord.setDisplayName(astrographicObject.getDisplayName());
        starEditRecord.setDistanceToEarth(astrographicObject.getDistance());
        starEditRecord.setSpectra(astrographicObject.getSpectralClass());
        starEditRecord.setRadius(astrographicObject.getRadius());
        starEditRecord.setRa(astrographicObject.getRa());
        starEditRecord.setDeclination(astrographicObject.getDeclination());

        starEditRecord.setXCoord(astrographicObject.getX());
        starEditRecord.setYCoord(astrographicObject.getY());
        starEditRecord.setZCoord(astrographicObject.getZ());

        starEditRecord.setReal(astrographicObject.isRealStar());

        starEditRecord.setComment(astrographicObject.getNotes());

        starEditRecord.setDirty(false);

        return starEditRecord;
    }

}
