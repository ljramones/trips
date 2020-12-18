package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dataset.enums.SortParameterEnum;
import com.teamgannon.trips.jpa.model.AstrographicObject;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class ShowStarMatchesDialog extends Dialog<String> {


    private final TableView<AstrographicObject> tableView = new TableView<>();
    private final TableColumn<AstrographicObject, String> displayNameCol = new TableColumn<>("Display Name");
    private final TableColumn<AstrographicObject, Double> distanceToEarthCol = new TableColumn<>("Distance to Earth(ly)");
    private final TableColumn<AstrographicObject, String> spectraCol = new TableColumn<>("Spectra");
    private final TableColumn<AstrographicObject, Double> radiusCol = new TableColumn<>("Radius");
    private final TableColumn<AstrographicObject, Double> raCol = new TableColumn<>("RA");
    private final TableColumn<AstrographicObject, Double> decCol = new TableColumn<>("Declination");
    private final TableColumn<AstrographicObject, Double> paraCol = new TableColumn<>("Parallax");
    private final TableColumn<AstrographicObject, Double> xCoordCol = new TableColumn<>("X");
    private final TableColumn<AstrographicObject, Double> yCoordCol = new TableColumn<>("Y");
    private final TableColumn<AstrographicObject, Double> zCoordCol = new TableColumn<>("Z");
    private final TableColumn<AstrographicObject, String> realCol = new TableColumn<>("Real");
    private final TableColumn<AstrographicObject, String> commentCol = new TableColumn<>("comment");

    private @NotNull SortParameterEnum currentSortStrategy = SortParameterEnum.NAME;
    private TableColumn.@NotNull SortType sortDirection = TableColumn.SortType.ASCENDING;

    private DatabaseManagementService databaseManagementService;
    private final List<AstrographicObject> astrographicObjects;

    public ShowStarMatchesDialog(DatabaseManagementService databaseManagementService, List<AstrographicObject> astrographicObjects) {
        this.databaseManagementService = databaseManagementService;
        this.astrographicObjects = astrographicObjects;
        this.setTitle("Show discovered stars");
        this.setHeight(700);
        this.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(tableView);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // setup the table structure
        setupTable();

        // load data
        loadData();

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private void close(WindowEvent windowEvent) {
        setResult("dismiss");
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
                .addAll(displayNameCol, distanceToEarthCol, spectraCol,
                        radiusCol, raCol, decCol, paraCol,
                        xCoordCol, yCoordCol, zCoordCol,
                        realCol, commentCol
                );
    }

    private void setupContextMenu() {
        final ContextMenu tableContextMenu = new ContextMenu();

        final MenuItem editSelectedMenuItem = new MenuItem("Edit selected");
        editSelectedMenuItem.setOnAction(event -> {
            final AstrographicObject astrographicObject = tableView.getSelectionModel().getSelectedItem();
            StarEditDialog starEditDialog = new StarEditDialog(astrographicObject);

            Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
            if (statusOptional.isPresent()) {
                StarEditStatus starEditStatus = statusOptional.get();
                if (starEditStatus.isChanged()) {
                    // update the database
                    databaseManagementService.updateStar(starEditStatus.getRecord());
                    // load data base on were we are
                    loadData();
                }
            }

        });

        final MenuItem deleteSelectedMenuItem = new MenuItem("Delete selected");
        deleteSelectedMenuItem.setOnAction(event -> {
            final AstrographicObject astrographicObject = tableView.getSelectionModel().getSelectedItem();
            tableView.getItems().remove(astrographicObject);
            removeFromDB(astrographicObject);
        });
        tableContextMenu.getItems().addAll(editSelectedMenuItem, deleteSelectedMenuItem);

        tableView.setContextMenu(tableContextMenu);
    }

    /**
     * remove an entry from the DB
     *
     * @param astrographicObject the record to remove
     */
    private void removeFromDB(@NotNull AstrographicObject astrographicObject) {

        UUID id = astrographicObject.getId();

        // remove from DB
        databaseManagementService.removeStar(id);

        loadData();

        log.info("Removed from DB");
    }

    private void setSelectionModel() {
        TableView.TableViewSelectionModel<AstrographicObject> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<AstrographicObject> selectedItems = selectionModel.getSelectedItems();

        selectedItems.addListener((ListChangeListener<AstrographicObject>) change ->
                log.info("Selection changed: " + change.getList()));
    }

    private void close(ActionEvent actionEvent) {
        setResult("dismiss");
    }

    /**
     * load the data
     */
    private void loadData() {

        for (AstrographicObject astrographicObject : astrographicObjects) {
            // check for a crap record
            if (astrographicObject.getDisplayName() == null) {
                continue;
            }
            if (!astrographicObject.getDisplayName().equalsIgnoreCase("name")) {
                tableView.getItems().add(astrographicObject);
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
    public void sortByName(TableColumn.@NotNull SortType sortOrder) {
        currentSortStrategy = SortParameterEnum.NAME;
        sortDirection = sortOrder;
        if (sortOrder.equals(TableColumn.SortType.ASCENDING)) {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDisplayName));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDistance).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDistance));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDistance).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getSpectralClass));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getSpectralClass).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getRadius));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getSpectralClass).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getRa));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getRa).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDeclination));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getDeclination).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getParallax));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getParallax).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getX));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getX).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getY));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getY).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getZ));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::getZ).reversed());
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
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::isRealStar));
        } else {
            astrographicObjects.sort(Comparator.comparing(AstrographicObject::isRealStar).reversed());
        }
    }
}
