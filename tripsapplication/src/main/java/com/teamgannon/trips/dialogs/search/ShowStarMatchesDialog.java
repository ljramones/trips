package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dataset.enums.SortParameterEnum;
import com.teamgannon.trips.dialogs.search.model.SingleStarSelection;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class ShowStarMatchesDialog extends Dialog<SingleStarSelection> {


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
    private final List<StarObject> starObjects;
    private @NotNull SortParameterEnum currentSortStrategy = SortParameterEnum.NAME;
    private TableColumn.@NotNull SortType sortDirection = TableColumn.SortType.ASCENDING;
    private final DatabaseManagementService databaseManagementService;

    public ShowStarMatchesDialog(DatabaseManagementService databaseManagementService, List<StarObject> starObjects) {
        this.databaseManagementService = databaseManagementService;
        this.starObjects = starObjects;
        this.setTitle("Show discovered stars");
        this.setHeight(700);
        this.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(tableView);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button selectButton = new Button("Select");
        selectButton.setOnAction(this::select);
        hBox2.getChildren().add(selectButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set up the table structure
        setupTable();

        // load data
        loadData();

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private void select(ActionEvent actionEvent) {

        StarObject starObject = tableView.getSelectionModel().getSelectedItem();
        if (starObject != null) {
            SingleStarSelection singleStarSelection = new SingleStarSelection(true, starObject);
            setResult(singleStarSelection);
        }
    }

    private void close(WindowEvent windowEvent) {
        SingleStarSelection singleStarSelection = new SingleStarSelection(false, null);
        setResult(singleStarSelection);
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

    private void setupTableColumns() {
        displayNameCol.setMinWidth(100);
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        displayNameCol.setSortType(TableColumn.SortType.ASCENDING);
        displayNameCol.sortTypeProperty().addListener(this::displayNameSortOrderChange);

        catalogIdCol.setMinWidth(100);
        catalogIdCol.setCellValueFactory(new PropertyValueFactory<>("catalogIdList"));
        catalogIdCol.setSortType(TableColumn.SortType.ASCENDING);
        catalogIdCol.sortTypeProperty().addListener(this::catalogIdSortOrderChange);

        distanceToEarthCol.setMinWidth(120);
        distanceToEarthCol.setCellValueFactory(new PropertyValueFactory<>("distance"));
        distanceToEarthCol.sortTypeProperty().addListener(this::distanceSortOrderChange);

        spectraCol.setMinWidth(70);
        spectraCol.setCellValueFactory(new PropertyValueFactory<>("spectralClass"));
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
        xCoordCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        xCoordCol.sortTypeProperty().addListener(this::xSortOrderChange);

        yCoordCol.setMinWidth(50);
        yCoordCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        yCoordCol.sortTypeProperty().addListener(this::ySortOrderChange);

        zCoordCol.setMinWidth(50);
        zCoordCol.setCellValueFactory(new PropertyValueFactory<>("z"));
        zCoordCol.sortTypeProperty().addListener(this::zSortOrderChange);

        realCol.setMinWidth(100);
        realCol.setCellValueFactory(new PropertyValueFactory<>("realStar"));
        realCol.sortTypeProperty().addListener(this::realSortOrderChange);

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


    private void setupContextMenu() {
        final ContextMenu tableContextMenu = new ContextMenu();

        final MenuItem editSelectedMenuItem = new MenuItem("Edit selected");
        editSelectedMenuItem.setOnAction(event -> {
            final StarObject starObject = tableView.getSelectionModel().getSelectedItem();
            Optional<StarObject> starObjectOptional = databaseManagementService.findId(starObject.getId());
            if (starObjectOptional.isPresent()) {
                StarEditDialog starEditDialog = new StarEditDialog(starObjectOptional.get());

                Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
                if (statusOptional.isPresent()) {
                    StarEditStatus starEditStatus = statusOptional.get();
                    if (starEditStatus.isChanged()) {
                        // update the database
                        databaseManagementService.updateStar(starEditStatus.getRecord());
                        // load database on were we are
                        loadData();
                    }
                }

            } else {
                log.error("Could not find star by id:{}, should never happen", starObject.getId());
            }
        });

        final MenuItem deleteSelectedMenuItem = new MenuItem("Delete selected");
        deleteSelectedMenuItem.setOnAction(event -> {
            final StarObject starObject = tableView.getSelectionModel().getSelectedItem();
            tableView.getItems().remove(starObject);
            removeFromDB(starObject);
        });
        tableContextMenu.getItems().addAll(editSelectedMenuItem, deleteSelectedMenuItem);

        tableView.setContextMenu(tableContextMenu);
    }

    /**
     * remove an entry from the DB
     *
     * @param starObject the record to remove
     */
    private void removeFromDB(@NotNull StarObject starObject) {

        String id = starObject.getId();

        // remove from DB
        databaseManagementService.removeStar(id);

        loadData();

        log.info("Removed from DB");
    }

    private void setSelectionModel() {
        TableView.TableViewSelectionModel<StarObject> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<StarObject> selectedItems = selectionModel.getSelectedItems();

        selectedItems.addListener((ListChangeListener<StarObject>) change -> log.info("Selection changed: " + change.getList()));
    }

    private void close(ActionEvent actionEvent) {
        SingleStarSelection singleStarSelection = new SingleStarSelection(false, null);
        setResult(singleStarSelection);
    }

    /**
     * load the data
     */
    private void loadData() {
        tableView.getItems().clear();
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


    ///////////////////  Sorting operators   ///////////////

    /**
     * display name sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void displayNameSortOrderChange(Observable o) {
        TableColumn.SortType sortType = displayNameCol.getSortType();
        sortByName(sortType);
        log.info("re-sorted by display name ");
    }

    private void catalogIdSortOrderChange(Observable observable) {
        TableColumn.SortType sortType = catalogIdCol.getSortType();
        sortByName(sortType);
        log.info("re-sorted by catalogId name ");
    }


    /**
     * distance sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void distanceSortOrderChange(Observable o) {
        TableColumn.SortType sortType = distanceToEarthCol.getSortType();
        sortByDistance(sortType);
        log.info("re-sorted by distance ");
    }

    /**
     * spectra sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void spectraSortOrderChange(Observable o) {
        TableColumn.SortType sortType = spectraCol.getSortType();
        sortBySpectra(sortType);
        log.info("re-sorted by spectra ");
    }

    /**
     * radius sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void radiusSortOrderChange(Observable o) {
        TableColumn.SortType sortType = radiusCol.getSortType();
        sortByRadius(sortType);
        log.info("re-sorted by radius ");
    }

    /**
     * RA sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void raSortOrderChange(Observable o) {
        TableColumn.SortType sortType = raCol.getSortType();
        sortByRa(sortType);
        log.info("re-sorted by RA ");
    }

    /**
     * declination sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void declinationSortOrderChange(Observable o) {
        TableColumn.SortType sortType = decCol.getSortType();
        sortByDeclination(sortType);
        log.info("re-sorted by declination ");
    }

    /**
     * parallax sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void parallaxSortOrderChange(Observable o) {
        TableColumn.SortType sortType = paraCol.getSortType();
        sortByParallax(sortType);
        log.info("re-sorted by parallax ");
    }

    /**
     * X sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void xSortOrderChange(Observable o) {
        TableColumn.SortType sortType = xCoordCol.getSortType();
        sortByX(sortType);
        log.info("re-sorted by X ");
    }

    /**
     * Y sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void ySortOrderChange(Observable o) {
        TableColumn.SortType sortType = yCoordCol.getSortType();
        sortByY(sortType);
        log.info("re-sorted by Y ");
    }

    /**
     * Z sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void zSortOrderChange(Observable o) {
        TableColumn.SortType sortType = zCoordCol.getSortType();
        sortByZ(sortType);
        log.info("re-sorted by Z ");
    }

    /**
     * real sort event trigger
     *
     * @param o observable that triggered the event
     */
    private void realSortOrderChange(Observable o) {
        TableColumn.SortType sortType = realCol.getSortType();
        sortByReal(sortType);
        log.info("re-sorted by real flag");
    }

    /**
     * resort the list with the current sorting strategy and direction
     */
    private void reSort() {
        switch (Objects.requireNonNull(currentSortStrategy)) {
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
    public void sortByName(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.NAME;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByDistance(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.DISTANCE;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortBySpectra(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.SPECTRA;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByRadius(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.RADIUS;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByRa(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.RA;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByDeclination(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.DECLINATION;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByParallax(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.PARALLAX;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByX(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.X;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByY(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.Y;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByZ(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.Z;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
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
    public void sortByReal(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.REAL;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
            starObjects.sort(Comparator.comparing(StarObject::isRealStar));
        } else {
            starObjects.sort(Comparator.comparing(StarObject::isRealStar).reversed());
        }
    }
}
