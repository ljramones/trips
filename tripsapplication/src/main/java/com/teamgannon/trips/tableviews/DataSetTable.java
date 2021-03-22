package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.dataset.enums.SortParameterEnum;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.service.DatabaseManagementService;
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
import org.jetbrains.annotations.NotNull;

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
    private final Map<UUID, StarObject> astrographicObjectMap = new HashMap<>();
    private final DatabaseManagementService databaseManagementService;
    private final String dataSetName;
    private final @NotNull Dialog<String> dialog;
    private int pageNumber = 1;
    private int totalPages = 1;
    private int currentPosition = 0;
    /**
     * the underlying windows component that the dialog belongs to
     */
    private Window window;
    private List<StarObject> starObjects;
    private @NotNull SortParameterEnum currentSortStrategy = SortParameterEnum.NAME;
    private @NotNull SortType sortDirection = SortType.ASCENDING;

    /**
     * the constructor that we use to show the data
     *
     * @param databaseManagementService the database management service
     * @param starObjects               the list of objects
     */
    public DataSetTable(DatabaseManagementService databaseManagementService,
                        @NotNull List<StarObject> starObjects) {

        this.databaseManagementService = databaseManagementService;

        this.starObjects = starObjects;
        if (!starObjects.isEmpty()) {
            MapUtils.populateMap(astrographicObjectMap,
                    starObjects,
                    StarObject::getId);
        }


        // the actual ui component to hold these entries
        dialog = new Dialog<>();

        dataSetName = starObjects.get(0).getDataSetName();
        totalPages = (int) ceil(starObjects.size() / (float) PAGE_SIZE);  // bump to next integer

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

        Button cancelButton = new Button();
        cancelButton.setText("Cancel");
        cancelButton.setOnAction(event -> window.hide());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(
                firstButton,
                previousButton,
                forwardButton,
                lastButton,
                new Separator(),
                addButton,
                new Separator(),
                cancelButton);

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
            StarObject starObject = databaseManagementService.getStar(selectedStarEditRecord.getId());
            if (starObject != null) {
                StarEditDialog starEditDialog = new StarEditDialog(starObject);

                Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
                if (statusOptional.isPresent()) {
                    StarEditStatus starEditStatus = statusOptional.get();
                    if (starEditStatus.isChanged()) {
                        // update the database
                        databaseManagementService.updateStar(starEditStatus.getRecord());
                        // resort
                        reSort();
                        // load data base on were we are
                        loadData();
                    }
                }
            } else {
                showErrorAlert("Edit Star",
                        "record:" + selectedStarEditRecord.getId() + " could not be found. This is odd");

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

    /**
     * update the title
     */
    private void setTitle() {
        dialog.setTitle("Astrographic Records Table: for dataset:: " +
                dataSetName + " - page: " + pageNumber + " of " + totalPages + " pages");
    }


    public @NotNull Node getPanel() {
        BorderPane borderPane = new BorderPane();

        // setup the table structure
        setupTable();
        borderPane.setCenter(tableView);

        // load an initial page of data
        loadData();

        return borderPane;
    }

    //////////////// Navigation functions   ///////////////////////

    /**
     * move to page one
     */
    private void moveFirst() {
        currentPosition = 0;
        pageNumber = 1;
        clearData();
        loadData();
        setTitle();
    }

    /**
     * move to the last page
     */
    private void moveEnd() {
        currentPosition = astrographicObjectMap.size() - PAGE_SIZE;
        pageNumber = (int) floor(astrographicObjectMap.size() / (float) PAGE_SIZE);
        clearData();
        loadData();
        setTitle();
    }

    /**
     * move forward by a page which is defined by PAGE_SIZE
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
     * move back a page which is defined by PAGE_SIZE
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
        StarObject starObject = new StarObject();
        starObject.setId(UUID.randomUUID());
        starObject.setDataSetName(dataSetName);
        StarEditDialog editDialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> status = editDialog.showAndWait();
        if (status.isPresent()) {
            StarEditStatus editResult = status.get();
            if (editResult.isChanged()) {
                StarObject astro = editResult.getRecord();
                log.info("star created={}", astro);
                // add to the backing list for table view
                starObjects.add(astro);
                // add to our map which backs the list
                astrographicObjectMap.put(astro.getId(), astro);
                // add to the database
                databaseManagementService.addStar(astro);
                // now that we added an entry, we resort and reset the page views
                reSort();
                moveFirst();
            }
        }
    }


    /**
     * remove an entry from the DB
     *
     * @param starEditRecord the record to remove
     */
    private void removeFromDB(@NotNull StarEditRecord starEditRecord) {

        UUID id = starEditRecord.getId();
        astrographicObjectMap.remove(id);

        // remove from DB
        databaseManagementService.removeStar(id);

        resetList();
        reSort();
        loadData();

        log.info("Removed from DB");
    }

    /**
     * reset the list
     */
    private void resetList() {
        starObjects = new ArrayList<>(astrographicObjectMap.values());
    }


    // ----------------------------- //

    /**
     * load the data
     */
    private void loadData() {
        int pageSize = PAGE_SIZE;
        int diff = starObjects.size() - currentPosition;
        if (diff < pageSize) {
            pageSize = diff;
        }
        for (int i = currentPosition; i < (currentPosition + pageSize); i++) {
            StarObject object = starObjects.get(i);
            // check for a crap record
            if (object.getDisplayName() == null) {
                continue;
            }
            if (!object.getDisplayName().equalsIgnoreCase("name")) {
                StarEditRecord starEditRecord = StarEditRecord.fromAstrographicObject(object);
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

    ///////////////////  Sorting operators   ///////////////

    /**
     * display name sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void displayNameSortOrderChange(Observable o) {
        SortType sortType = displayNameCol.getSortType();
        sortByName(sortType);
        log.info("re-sorted by display name ");
    }

    /**
     * distance sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void distanceSortOrderChange(Observable o) {
        SortType sortType = distanceToEarthCol.getSortType();
        sortByDistance(sortType);
        log.info("re-sorted by distance ");
    }

    /**
     * spectra sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void spectraSortOrderChange(Observable o) {
        SortType sortType = spectraCol.getSortType();
        sortBySpectra(sortType);
        moveFirst();
        log.info("re-sorted by spectra ");
    }

    /**
     * radius sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void radiusSortOrderChange(Observable o) {
        SortType sortType = radiusCol.getSortType();
        sortByRadius(sortType);
        moveFirst();
        log.info("re-sorted by radius ");
    }

    /**
     * RA sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void raSortOrderChange(Observable o) {
        SortType sortType = raCol.getSortType();
        sortByRa(sortType);
        moveFirst();
        log.info("re-sorted by RA ");
    }

    /**
     * declination sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void declinationSortOrderChange(Observable o) {
        SortType sortType = decCol.getSortType();
        sortByDeclination(sortType);
        moveFirst();
        log.info("re-sorted by declination ");
    }

    /**
     * parallax sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void parallaxSortOrderChange(Observable o) {
        SortType sortType = paraCol.getSortType();
        sortByParallax(sortType);
        moveFirst();
        log.info("re-sorted by parallax ");
    }

    /**
     * X sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void xSortOrderChange(Observable o) {
        SortType sortType = xCoordCol.getSortType();
        sortByX(sortType);
        moveFirst();
        log.info("re-sorted by X ");
    }

    /**
     * Y sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void ySortOrderChange(Observable o) {
        SortType sortType = yCoordCol.getSortType();
        sortByY(sortType);
        moveFirst();
        log.info("re-sorted by Y ");
    }

    /**
     * Z sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void zSortOrderChange(Observable o) {
        SortType sortType = zCoordCol.getSortType();
        sortByZ(sortType);
        moveFirst();
        log.info("re-sorted by Z ");
    }

    /**
     * real sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void realSortOrderChange(Observable o) {
        SortType sortType = realCol.getSortType();
        sortByReal(sortType);
        moveFirst();
        log.info("re-sorted by real flag");
    }

    /**
     * resort the list with the current sorting strategy and direction
     */
    private void reSort() {
        switch (currentSortStrategy) {
            case NAME -> sortByName(sortDirection);
            case DISTANCE -> sortByDistance(sortDirection);
            case SPECTRA -> sortBySpectra(sortDirection);
            case RADIUS -> sortByRadius(sortDirection);
            case RA -> sortByRa(sortDirection);
            case DECLINATION -> sortByDeclination(sortDirection);
            case PARALLAX -> sortByParallax(sortDirection);
            case X -> sortByX(sortDirection);
            case Y -> sortByY(sortDirection);
            case Z -> sortByZ(sortDirection);
            case REAL -> sortByReal(sortDirection);
            default -> log.error("Unexpected value: " + currentSortStrategy);
        }
    }

    /**
     * sort the list by name attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByName(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.NAME;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getDisplayName));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getDistance).reversed());
        }
    }

    /**
     * sort the list by distance attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByDistance(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.DISTANCE;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getDistance));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getDistance).reversed());
        }
    }

    /**
     * sort the list by spectra attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortBySpectra(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.SPECTRA;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getSpectralClass));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getSpectralClass).reversed());
        }
    }

    /**
     * sort the list by radius attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByRadius(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.RADIUS;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getRadius));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getSpectralClass).reversed());
        }
    }

    /**
     * sort the list by ra attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByRa(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.RA;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getRa));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getRa).reversed());
        }
    }

    /**
     * sort the list by declination attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByDeclination(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.DECLINATION;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getDeclination));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getDeclination).reversed());
        }
    }

    /**
     * sort the list by parallax attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByParallax(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.PARALLAX;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getParallax));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getParallax).reversed());
        }
    }

    /**
     * sort the list by X attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByX(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.X;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getX));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getX).reversed());
        }
    }

    /**
     * sort the list by Y attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByY(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.Y;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getY));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getY).reversed());
        }
    }

    /**
     * sort the list by Z attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByZ(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.Z;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::getZ));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::getZ).reversed());
        }
    }

    /**
     * sort the list by real attribute
     *
     * @param sortOrder the sorting order - up or down
     */
    public void sortByReal(@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.REAL;
        sortDirection = sortOrder;
        if (sortOrder.equals(SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::isRealStar));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::isRealStar).reversed());
        }
    }

}
