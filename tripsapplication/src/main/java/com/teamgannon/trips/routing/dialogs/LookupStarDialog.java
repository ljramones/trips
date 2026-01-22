package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public class LookupStarDialog extends Dialog<StarSearchResults> {

    private final TableView<StarObject> tableView = new TableView<>();
    private final TableColumn<StarObject, String> displayNameCol = new TableColumn<>("Display Name");
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
    private final String datasetName;
    private final List<StarObject> starsFound;


    public LookupStarDialog(String starToLookup,
                            String datasetName,
                            @NotNull DatabaseManagementService databaseManagementService,
                            @NotNull StarService starService) {
        this.datasetName = datasetName;

        starsFound = starService.findStarsWithName(datasetName, starToLookup);

        this.setTitle("Show stars that match: " + starToLookup);
        this.setHeight(700);
        this.setWidth(1000);

        VBox vBox = new VBox();
        vBox.getChildren().add(tableView);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.getChildren().add(buttonBox);

        Button selectButton = new Button("Select");
        selectButton.setOnAction(event -> selectName());
        buttonBox.getChildren().add(selectButton);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(event -> dismiss());
        buttonBox.getChildren().add(dismissButton);

        getDialogPane().setContent(vBox);

        // setup the table structure
        setupTable();

        // load data
        loadData(starsFound);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void loadData(List<StarObject> starsFound) {
        for (StarObject starObject : starsFound) {
            // check for a crap record
            if (starObject.getDisplayName() == null) {
                continue;
            }
            if (!starObject.getDisplayName().equalsIgnoreCase("name")) {
                tableView.getItems().add(starObject);
            }
        }
    }

    private void setupTable() {
        // allow the table to be editable
        tableView.setPlaceholder(new Label("No rows to display"));

        // set selection model
        setSelectionModel();

        // set table columns
        setupTableColumns();
    }


    private void setSelectionModel() {
        TableView.TableViewSelectionModel<StarObject> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<StarObject> selectedItems = selectionModel.getSelectedItems();

        selectedItems.addListener((ListChangeListener<StarObject>) change ->
                log.info("Selection changed: " + change.getList()));
    }


    private void setupTableColumns() {
        displayNameCol.setMinWidth(100);
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));

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
                .addAll(displayNameCol, distanceToEarthCol, spectraCol,
                        radiusCol, raCol, decCol, paraCol,
                        xCoordCol, yCoordCol, zCoordCol,
                        realCol, commentCol
                );
    }

    private void close(WindowEvent windowEvent) {
        StarSearchResults results = StarSearchResults.builder().starsFound(false).build();
        setResult(results);
    }

    private void dismiss() {
        StarSearchResults results = StarSearchResults.builder().starsFound(false).build();
        setResult(results);

    }

    private void selectName() {
        StarObject starObject = tableView.getSelectionModel().getSelectedItem();
        StarSearchResults results =  StarSearchResults.builder()
                .starsFound(true)
                .dataSetName(datasetName)
                .starObject(starObject)
                .nameToSearch(starObject.getDisplayName())
                .build();
        setResult(results);
    }
}
