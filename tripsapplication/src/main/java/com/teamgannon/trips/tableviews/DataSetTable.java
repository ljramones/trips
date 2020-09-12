package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.dataset.AddStarDialog;
import com.teamgannon.trips.dialogs.dataset.TableEditResult;
import com.teamgannon.trips.dialogs.support.EditTypeEnum;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.DatabaseListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
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
import static java.lang.Math.ceil;
import static java.lang.Math.floor;


/**
 * The spreadsheet table for
 * <p>
 * Created by larrymitchell on 2017-04-01.
 */
@Slf4j
public class DataSetTable {

    private final int PAGE_SIZE = 50;

    private int pageNumber = 1;

    private int totalPages = 1;

    private int currentPosition = 0;

    /**
     * the table view object
     */
    private final TableView<StarEditRecord> tableView = new TableView<>();
    private final TableColumn<StarEditRecord, String> displayNameCol = new TableColumn<>("Display Name");
    private final TableColumn<StarEditRecord, Double> distanceToEarthCol = new TableColumn<>("Distance to Earth(ly)");
    private final TableColumn<StarEditRecord, String> spectraCol = new TableColumn<>("Spectra");
    private final TableColumn<StarEditRecord, Double> radiusCol = new TableColumn<>("Radius");
    private final TableColumn<StarEditRecord, Double> raCol = new TableColumn<>("RA");
    private final TableColumn<StarEditRecord, Double> decCol = new TableColumn<>("Declination");
    private final TableColumn<StarEditRecord, Double> paraCol = new TableColumn<>("Parallax");
    private final TableColumn<StarEditRecord, Double> xCoordCol = new TableColumn<>("X");
    private final TableColumn<StarEditRecord, Double> yCoordCol = new TableColumn<>("Y");
    private final TableColumn<StarEditRecord, Double> zCoordCol = new TableColumn<>("Z");
    private final TableColumn<StarEditRecord, String> realCol = new TableColumn<>("Real");
    private final TableColumn<StarEditRecord, String> commentCol = new TableColumn<>("comment");

    private final Map<UUID, AstrographicObject> astrographicObjectMap = new HashMap<>();

    /**
     * the underlying windows component that the dialog belongs to
     */
    private Window window;
    private final DatabaseListener databaseUpdater;
    private List<AstrographicObject> astrographicObjects;

    private final String dataSetName;

    private final Dialog<String> dialog;

    /**
     * the constructor that we use to show the data
     *
     * @param databaseUpdater     the reference back to search for more stars if needs be
     * @param astrographicObjects the list of objects
     */
    public DataSetTable(DatabaseListener databaseUpdater,
                        List<AstrographicObject> astrographicObjects) {

        this.databaseUpdater = databaseUpdater;
        this.astrographicObjects = astrographicObjects;
        if (!astrographicObjects.isEmpty()) {
            MapUtils.populateMap(astrographicObjectMap,
                    astrographicObjects,
                    AstrographicObject::getId);
        }

        // the actual ui component to hold these entries
        dialog = new Dialog<>();

        dataSetName = astrographicObjects.get(0).getDataSetName();
        totalPages = (int) ceil(astrographicObjects.size() / (float) PAGE_SIZE);  // bump to next integer

        setTitle();

        // set the dimensions
        dialog.setHeight(600);
        dialog.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(getPanel());

        Button firstButton = new Button();
        firstButton.setText("Begin");
        firstButton.setOnAction(event -> moveFirst());

        Button previousButton = new Button();
        previousButton.setText("<<<");
        previousButton.setOnAction(event -> moveBack());

        Button forwardButton = new Button();
        forwardButton.setText(">>>");
        forwardButton.setOnAction(event -> moveForward());

        Button lastButton = new Button();
        lastButton.setText("End");
        lastButton.setOnAction(event -> moveEnd());

        Button addButton = new Button();
        addButton.setText("Add Entry");
        addButton.setOnAction(event -> addNewDataEntry());

        Button dismissButton = new Button();
        dismissButton.setText("Dismiss");
        dismissButton.setOnAction(event -> window.hide());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(
                firstButton,
                previousButton,
                forwardButton,
                lastButton,
                new Separator(),
                addButton,
                new Separator(),
                dismissButton);
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

    private void moveFirst() {
        currentPosition = 0;
        pageNumber = 1;
        clearData();
        loadData();
    }

    private void moveEnd() {
        currentPosition = astrographicObjectMap.size() - PAGE_SIZE;
        pageNumber = (int) floor(astrographicObjectMap.size() / (float) PAGE_SIZE);
        clearData();
        loadData();
    }


    /**
     * update the title
     */
    public void setTitle() {
        dialog.setTitle("Astrographic Records Table: for dataset:: " +
                dataSetName + " - page: " + pageNumber + " of " + totalPages + " pages");
    }

    /**
     * move forward by a page which is 50
     */
    private void moveForward() {
        log.info("move forward");
        if (astrographicObjectMap.size() < PAGE_SIZE) {
            return;
        }
        if ((currentPosition + PAGE_SIZE) > astrographicObjectMap.size()) {
            currentPosition = astrographicObjectMap.size() - PAGE_SIZE;
        } else {
            currentPosition += PAGE_SIZE;
            if (pageNumber < totalPages) {
                pageNumber++;
            }
        }
        setTitle();
        clearData();
        loadData();
    }

    /**
     * move back a page which is 50
     */
    private void moveBack() {
        log.info("move back");
        if (astrographicObjectMap.size() < PAGE_SIZE) {
            return;
        }
        if (currentPosition < PAGE_SIZE) {
            currentPosition = 0;
        } else {
            currentPosition -= PAGE_SIZE;
            pageNumber--;
        }

        setTitle();
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

    /**
     * setup the table
     */
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

        displayNameCol.setMinWidth(100);
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        displayNameCol.setSortType(SortType.ASCENDING);
        displayNameCol.sortTypeProperty().addListener(this::displayNameSortOrderChange);

        distanceToEarthCol.setMinWidth(120);
        distanceToEarthCol.setCellValueFactory(new PropertyValueFactory<>("distanceToEarth"));
        distanceToEarthCol.sortTypeProperty().addListener(this::distanceSortOrderChange);

        spectraCol.setMinWidth(70);
        spectraCol.setCellValueFactory(new PropertyValueFactory<>("spectra"));
        spectraCol.sortTypeProperty().addListener(this::spectraSortOrderChange);

        radiusCol.setMinWidth(100);
        radiusCol.setCellValueFactory(new PropertyValueFactory<>("radius"));
        radiusCol.sortTypeProperty().addListener(this::radiusSortOrderChange);

        raCol.setMinWidth(50);
        raCol.setCellValueFactory(new PropertyValueFactory<>("ra"));
        raCol.sortTypeProperty().addListener(this::raSortOrderChange);

        decCol.setMinWidth(70);
        decCol.setCellValueFactory(new PropertyValueFactory<>("declination"));
        decCol.sortTypeProperty().addListener(this::declinationSortOrderChange);

        paraCol.setMinWidth(70);
        paraCol.setCellValueFactory(new PropertyValueFactory<>("parallax"));
        paraCol.sortTypeProperty().addListener(this::parallaxSortOrderChange);

        xCoordCol.setMinWidth(50);
        xCoordCol.setCellValueFactory(new PropertyValueFactory<>("xCoord"));
        xCoordCol.sortTypeProperty().addListener(this::xSortOrderChange);

        yCoordCol.setMinWidth(50);
        yCoordCol.setCellValueFactory(new PropertyValueFactory<>("yCoord"));
        yCoordCol.sortTypeProperty().addListener(this::ySortOrderChange);

        zCoordCol.setMinWidth(50);
        zCoordCol.setCellValueFactory(new PropertyValueFactory<>("zCoord"));
        zCoordCol.sortTypeProperty().addListener(this::zSortOrderChange);

        realCol.setMinWidth(100);
        realCol.setCellValueFactory(new PropertyValueFactory<>("real"));
        realCol.sortTypeProperty().addListener(this::realSortOrderChange);

        commentCol.setMinWidth(200);
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setSortable(false);

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
        databaseUpdater.updateStar(astrographicObject);

        resetList();
    }


    private void addToDB(StarEditRecord starEditRecord) {

        AstrographicObject astrographicObjectNew = convertToAstrographicObject(starEditRecord);
        astrographicObjectMap.put(astrographicObjectNew.getId(), astrographicObjectNew);

        // add to DB
        databaseUpdater.updateStar(astrographicObjectNew);

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

    /**
     * reset the list
     */
    private void resetList() {
        astrographicObjects = new ArrayList<>(astrographicObjectMap.values());

    }

    /**
     * update the record
     *
     * @param starEditRecord     the star display reocrd
     * @param astrographicObject the db record
     */
    private void updateObject(StarEditRecord starEditRecord,
                              AstrographicObject astrographicObject) {
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

    /**
     * load the data
     */
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

    /**
     * clear the data
     */
    private void clearData() {
        tableView.getItems().clear();
    }

    /**
     * convert an astro record to a StarDisplayrecord
     *
     * @param astrographicObject the DB record
     * @return the star display record
     */
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

    ///////////////////  Sorting operators   ///////////////

    private void displayNameSortOrderChange(Observable o) {
        SortType sortType = displayNameCol.getSortType();
        sortByName(sortType);
        log.info("re-sorted by display name ");
    }

    private void distanceSortOrderChange(Observable o) {
        SortType sortType = distanceToEarthCol.getSortType();
        sortByDistance(sortType);
        log.info("re-sorted by distance ");
    }

    private void spectraSortOrderChange(Observable o) {
        SortType sortType = spectraCol.getSortType();
        sortBySpectra(sortType);
        log.info("re-sorted by spectra ");
    }

    private void radiusSortOrderChange(Observable o) {
        SortType sortType = radiusCol.getSortType();
        sortByRadius(sortType);
        log.info("re-sorted by radius ");
    }

    private void raSortOrderChange(Observable o) {
        SortType sortType = raCol.getSortType();
        sortByRa(sortType);
        log.info("re-sorted by RA ");
    }

    private void declinationSortOrderChange(Observable o) {
        SortType sortType = decCol.getSortType();
        sortByDeclination(sortType);
        log.info("re-sorted by declination ");
    }

    private void parallaxSortOrderChange(Observable o) {
        SortType sortType = paraCol.getSortType();
        sortByParallax(sortType);
        log.info("re-sorted by parallax ");
    }

    private void xSortOrderChange(Observable o) {
        SortType sortType = xCoordCol.getSortType();
        sortByX(sortType);
        log.info("re-sorted by X ");
    }

    private void ySortOrderChange(Observable o) {
        SortType sortType = yCoordCol.getSortType();
        sortByY(sortType);
        log.info("re-sorted by Y ");
    }

    private void zSortOrderChange(Observable o) {
        SortType sortType = zCoordCol.getSortType();
        sortByZ(sortType);
        log.info("re-sorted by Z ");
    }

    private void realSortOrderChange(Observable o) {
        SortType sortType = realCol.getSortType();
        sortByReal(sortType);
        log.info("re-sorted by real flag");
    }

    public void sortByName(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDisplayName));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDistance).reversed());
        }
    }

    public void sortByDistance(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDistance));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDistance).reversed());
        }
    }

    public void sortBySpectra(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getSpectralClass));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getSpectralClass).reversed());
        }
    }

    public void sortByRadius(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getRadius));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getSpectralClass).reversed());
        }
    }

    public void sortByRa(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getRa));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getRa).reversed());
        }
    }

    public void sortByDeclination(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDeclination));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDeclination).reversed());
        }
    }

    public void sortByParallax(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getParallax));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getParallax).reversed());
        }
    }

    public void sortByX(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getX));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getX).reversed());
        }
    }

    public void sortByY(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getY));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getY).reversed());
        }
    }

    public void sortByZ(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getZ));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getZ).reversed());
        }
    }

    public void sortByReal(SortType sortOrder) {
        if (sortOrder.equals(SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::isRealStar));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::isRealStar).reversed());
        }
    }

}
