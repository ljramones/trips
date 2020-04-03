package com.teamgannon.trips.dead;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controls.ApplicationPreferencesPane;
import com.teamgannon.trips.dialog.ProgressDialog;
import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import com.teamgannon.trips.elasticsearch.model.DataSetDescriptor;
import com.teamgannon.trips.elasticsearch.repository.AstrographicObjectRepository;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.file.excel.model.RBExcelFile;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.operators.*;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.model.StarBase;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.SearchPane;
import com.teamgannon.trips.search.StellarDataUpdater;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.Simulator;
import com.teamgannon.trips.tableviews.DataSetTable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.*;

@Slf4j
public class MainPane extends Pane implements
        ListUpdater,
        StellarPropertiesDisplayer,
        StellarDataUpdater,
        ContextSelector,
        RouteUpdater,
        RedrawListener {

    ////////////////////////////////


    ///////////////////// Spring components /////////

    private int depth;
    private int spacing;
    private DatabaseManagementService databaseManagementService;

    private ApplicationContext appContext;

    private ChviewReader chviewReader;

    private ExcelReader excelReader;

    private AstrographicPlotter astrographicPlotter;

    private StarBase starBase;

    private SearchContext searchContext;

    private TripsContext tripsContext;

    private AstrographicObjectRepository astrographicObjectRepository;


    //////////////  java fx fields  //////////////

    public VBox settingsPane;

    public VBox mainPanel;


    public ToolBar toolBar;

    public HBox statusBar;

    public GridPane propertiesPane;

    public GridPane tripsPropertiesPane;

    public Label databaseStatus;

    public Accordion propertiesAccordion;

    public StackPane leftDisplayPane;

    public SplitPane mainSplitPane;

    public ToggleButton toggleSettings;

    //////////// accordion items

    public TitledPane viewPreferencesPane;

    public TitledPane stellarObjectPane;

    public TitledPane objectsViewPane;

    public TitledPane searchPane;

    public TitledPane routingPane;

    public ScrollPane queryScrollPane;

    ////////////////////////////////

    private InterstellarSpacePane interstellarSpacePane;

    private boolean gridOn = true;
    private boolean extensionsOn = true;
    private boolean starsOn = true;
    private boolean scaleOn = true;
    private boolean routesOn = true;

    private Simulator simulator;


    /**
     * solar system panes for showing the details of various solar systems
     */
    protected SolarSystemSpacePane solarSystemSpacePane;

    /**
     * temporary data for chview data testing
     */
    private ChViewFile chViewFile;


    /////// data objects ///////////

    /**
     * backing array for listable stellar objects - the M for the MVC of Listview
     */
    private List<Map<String, String>> objectsInView = new ArrayList<>();

    /**
     * observable list that provides the C for the MVC of the ListView
     */
    private ObservableList<Map<String, String>> stellarObjectList = FXCollections.observableArrayList(objectsInView);

    /**
     * the ListView UI control for displaying lists - the V for the MVC of Listview
     */
    private ListView<Map<String, String>> stellarObjectsListView = new ListView<>(stellarObjectList);


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * constructor for the star pane
     *
     * @param width   width
     * @param height  height
     * @param depth   depth
     * @param spacing spacing of grid
     */
    public MainPane(int width, int height, int depth, int spacing,
                    DatabaseManagementService databaseManagementService,
                    ApplicationContext appContext,
                    ChviewReader chviewReader,
                    ExcelReader excelReader,
                    AstrographicPlotter astrographicPlotter,
                    StarBase starBase,
                    TripsContext tripsContext,
                    AstrographicObjectRepository astrographicObjectRepository
    ) {
        this.depth = depth;
        this.spacing = spacing;

        this.databaseManagementService = databaseManagementService;
        this.appContext = appContext;
        this.chviewReader = chviewReader;
        this.excelReader = excelReader;
        this.astrographicPlotter = astrographicPlotter;
        this.starBase = starBase;
        this.tripsContext = tripsContext;
        this.astrographicObjectRepository = astrographicObjectRepository;
        this.searchContext = tripsContext.getSearchContext();

        this.setMinHeight(height);
        this.setMinWidth(width);

        mainPanel = new VBox();
        mainPanel.setPrefWidth(1100);
        mainPanel.setPrefHeight(700);

        this.getChildren().add(mainPanel);

        // menu and tool bar setup
        createMenu(mainPanel);
        createToolBar(mainPanel);

        // main split pane
        mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.8);
        mainSplitPane.setFocusTraversable(true);

        // left display
        createLeftDisplay();

        // right display
        createRightDisplay();

        mainPanel.getChildren().add(mainSplitPane);

        ////////////////////
        // create main graphics display pane
        // @TODO this has to be changed
//        interstellarSpacePane = new InterstellarSpacePane(width, height, depth, spacing);
//        mainPanel.getChildren().add(interstellarSpacePane);
        ///////////////////

        statusBar = new HBox();
        statusBar.setAlignment(Pos.CENTER);
        statusBar.setSpacing(5.0);
        Insets insets1 = new Insets(3.0, 3.0, 3.0, 3.0);
        statusBar.setPadding(insets1);
        databaseStatus = new Label("No Database Loaded");
        databaseStatus.setFont(new Font("Arial", 11));
        databaseStatus.setTextFill(Color.color(0.625, 0.625, 0.625));
        statusBar.getChildren().add(databaseStatus);

        mainPanel.getChildren().add(statusBar);

        // setup simulator
        simulator = new Simulator(interstellarSpacePane, width, height, depth);

        log.info("\n\n\n--------- initializing main controller    ---------");
        showStatus("No database loaded");

//        settingsPane.setMinWidth(0);
//
//        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
//        splitPaneDividerPosition.addListener((obs, oldPos, newPos) -> toggleSettings.setSelected(newPos.doubleValue() < 0.95));

        viewPreferencesPane.setContent(new ApplicationPreferencesPane(tripsContext));

        queryScrollPane.setContent(new SearchPane(searchContext.getAstroSearchQuery(), this));

        // setup the object in view
        setupStellarObjectListView();

        // setup graphics panes for display
        setupInterstellarPane();

        // setup solar system panes
//        setupSolarSystemPane();

        // put the interstellar space on top and the solarsystem to the back
        interstellarSpacePane.toFront();

        // set the interstellar pane to be the drawing surface
        astrographicPlotter.setInterstellarPane(interstellarSpacePane);
    }

    private void createLeftDisplay() {
        // note that the two graphics parts need to be here
        leftDisplayPane = new StackPane();
        leftDisplayPane.setMinWidth(780);
        leftDisplayPane.setMinHeight(680);
        mainSplitPane.getItems().add(leftDisplayPane);
    }

    private void createRightDisplay() {
        settingsPane = new VBox();
        propertiesAccordion = new Accordion();

        viewPreferencesPane = new TitledPane();
        viewPreferencesPane.setText("TRIPS Preferences");
        tripsPropertiesPane = new GridPane();
        viewPreferencesPane.setContent(tripsPropertiesPane);
        propertiesAccordion.getPanes().add(viewPreferencesPane);

        searchPane = new TitledPane();
        searchPane.setText("DB Query");
        queryScrollPane = new ScrollPane();
        searchPane.setContent(queryScrollPane);
        propertiesAccordion.getPanes().add(searchPane);

        objectsViewPane = new TitledPane();
        objectsViewPane.setText("Objects in View");
        propertiesAccordion.getPanes().add(objectsViewPane);

        stellarObjectPane = new TitledPane();
        stellarObjectPane.setText("Stellar Object Properties");
        propertiesPane = new GridPane();
        stellarObjectPane.setContent(propertiesPane);
        propertiesAccordion.getPanes().add(stellarObjectPane);

        routingPane = new TitledPane();
        routingPane.setText("Star Routing");
        propertiesAccordion.getPanes().add(routingPane);

        settingsPane.getChildren().add(propertiesAccordion);
        mainSplitPane.getItems().add(settingsPane);
    }


    /**
     * initialize the view controller
     */
    public void initialize() {
        log.info("\n\n\n--------- initializing main controller    ---------");
        showStatus("No database loaded");

//        settingsPane.setMinWidth(0);
//
//        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
//        splitPaneDividerPosition.addListener((obs, oldPos, newPos) -> toggleSettings.setSelected(newPos.doubleValue() < 0.95));

        viewPreferencesPane.setContent(new ApplicationPreferencesPane(tripsContext));

        queryScrollPane.setContent(new SearchPane(searchContext.getAstroSearchQuery(), this));

        // setup the object in view
        setupStellarObjectListView();

        // setup graphics panes for display
        setupInterstellarPane();

        // setup solar system panes
        setupSolarSystemPane();

        // put the interstellar space on top and the solarsystem to the back
        interstellarSpacePane.toFront();

        // set the interstellar pane to be the drawing surface
        astrographicPlotter.setInterstellarPane(interstellarSpacePane);

    }


    /**
     * sets up a graphical plane to show a solar system of a specified star or multiple star system
     */
    private void setupSolarSystemPane() {
        solarSystemSpacePane = new SolarSystemSpacePane(leftDisplayPane.getMaxWidth(), leftDisplayPane.getMaxHeight());
        solarSystemSpacePane.setContextUpdater(this);
        leftDisplayPane.getChildren().add(solarSystemSpacePane);
    }

    /**
     * setup a graphical panes to display interstellar space
     */
    private void setupInterstellarPane() {
        interstellarSpacePane = new InterstellarSpacePane(
                (int) leftDisplayPane.getMaxWidth(),
                (int) leftDisplayPane.getMaxHeight(),
                depth,
                spacing
        );

        // setup event listeners
        interstellarSpacePane.setListUpdater(this);
        interstellarSpacePane.setContextUpdater(this);
        interstellarSpacePane.setStellarObjectDisplayer(this);
        interstellarSpacePane.setRouteUpdater(this);
        interstellarSpacePane.setRedrawListener(this);

        leftDisplayPane.getChildren().add(interstellarSpacePane);
    }


    /**
     * sets up list view for stellar objects
     */
    private void setupStellarObjectListView() {

        // initially greyed
//        objectsViewPane.setDisable(true);

        // setup model to display in case we turn on
        objectsViewPane.setContent(stellarObjectsListView);

        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Map<String, String>> ov, Map<String, String> old_val, Map<String, String> newSelection) -> {
                    displayProperties(newSelection);
                    log.info("Object Selected is:" + newSelection);
                });

        stellarObjectsListView.setCellFactory(new Callback<ListView<Map<String, String>>, ListCell<Map<String, String>>>() {

            @Override
            public ListCell<Map<String, String>> call(ListView<Map<String, String>> p) {

                objectsViewPane.setDisable(false);
                searchPane.setDisable(false);

                return new ListCell<Map<String, String>>() {

                    @Override
                    protected void updateItem(Map<String, String> objectProperties, boolean bln) {
                        super.updateItem(objectProperties, bln);
                        if (objectProperties != null) {
                            setText(objectProperties.get("name"));
                        }
                    }

                };
            }
        });
    }

    /**
     * Displays the properties of the astronomical object of name given as a parameter.
     *
     * @param properties Astronomical object unique name.
     */
    private void displayProperties(Map<String, String> properties) {
        String objectId = properties.get("name");
        String record = properties.get("recordNumber");
        UUID recordNumber = UUID.fromString(record);
        Map<String, String> characteristics = starBase.getRecordFields(recordNumber);
        propertiesPane.getChildren().clear();
        int i = 0;
        for (String key : characteristics.keySet()) {
            Label keyLabel = new Label(key + ":");
            keyLabel.setWrapText(true);
            keyLabel.setTextAlignment(TextAlignment.JUSTIFY);
            Label charLabel = new Label(characteristics.get(key));
            charLabel.setWrapText(true);
            charLabel.setTextAlignment(TextAlignment.JUSTIFY);
            charLabel.setPrefWidth(200);
            propertiesPane.addRow(i++, keyLabel, charLabel);
        }

        stellarObjectPane.setDisable(false);
        propertiesAccordion.setExpandedPane(stellarObjectPane);
    }


    ////////////// toolbar ///////////////////////////////

    private void createToolBar(VBox vBox) {
        ToolBar toolBar = new ToolBar();

        Button starButton = new Button("Stars");
        starButton.setOnAction(e -> {
            toggleStars();
        });
        toolBar.getItems().add(starButton);

        Button gridButton = new Button("Grid");
        gridButton.setOnAction(e -> {
            toggleGrid();
        });
        toolBar.getItems().add(gridButton);

        Button extensionsButton = new Button("Extensions");
        extensionsButton.setOnAction(e -> {
            toggleExtensions();
        });
        toolBar.getItems().add(extensionsButton);

        Button scaleButton = new Button("Scale");
        scaleButton.setOnAction(e -> {
            toggleScale();
        });
        toolBar.getItems().add(scaleButton);

        Button zoomInButton = new Button("+");
        zoomInButton.setOnAction(e -> {
            interstellarSpacePane.zoomIn();
        });
        toolBar.getItems().add(zoomInButton);

        Button zoomOutButton = new Button("-");
        zoomOutButton.setOnAction(e -> {
            interstellarSpacePane.zoomOut();
        });
        toolBar.getItems().add(zoomOutButton);

        vBox.getChildren().add(toolBar);
    }


    /////////////////  Menu Construction  ////////

    private void createMenu(VBox vBox) {
        MenuBar menuBar = new MenuBar();
        vBox.getChildren().add(menuBar);

        menuBar.getMenus().add(createGeneralMenu());
        menuBar.getMenus().add(createDisplayMenu());
        menuBar.getMenus().add(createFileImportExportMenu());
        menuBar.getMenus().add(createEditDisplayMenu());
//        menuBar.getMenus().add(createShowStarsMenu());
        menuBar.getMenus().add(createDatabaseMenu());
//        menuBar.getMenus().add(createSettingsMenu());
        menuBar.getMenus().add(createReportMenu());
        menuBar.getMenus().add(createPlotMenu());
        menuBar.getMenus().add(createAnimationsMenu());
        menuBar.getMenus().add(createRouteMenu());
        menuBar.getMenus().add(createHelpMenu());
    }


    /////////////// General Menu items
    private Menu createGeneralMenu() {
        Menu menu = new Menu("General");
        MenuItem quiteMenuitem = new MenuItem("Quit");
        quiteMenuitem.setOnAction(e -> {
            shutdown();
        });
        menu.getItems().add(quiteMenuitem);
        return menu;
    }

    private void shutdown() {
        log.debug("Exit selection");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Exit Application?");
        alert.setContentText("Are you sure you want to leave?");
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            initiateShutdown(0);
        }
    }

    /**
     * shutdown the application
     *
     * @param returnCode shoudl be a return code of zero meaning success
     */
    private void initiateShutdown(int returnCode) {
        // close the spring context which invokes all the bean destroy methods
        SpringApplication.exit(appContext, () -> returnCode);
        // now exit the application
        System.exit(returnCode);
    }

    /////////////////////////////////


    private Menu createDisplayMenu() {
        Menu menu = new Menu("Display");

        MenuItem toggleGrid = new MenuItem("Toggle Grid");
        toggleGrid.setOnAction(e -> {
            toggleGrid();
        });
        menu.getItems().add(toggleGrid);

        MenuItem toggleExtensions = new MenuItem("Toggle Grid Extensions");
        toggleExtensions.setOnAction(e -> {
            toggleExtensions();
        });
        menu.getItems().add(toggleExtensions);

        MenuItem stars = new MenuItem("Toggle Stars");
        stars.setOnAction(e -> {
            toggleStars();
        });
        menu.getItems().add(stars);

        MenuItem scale = new MenuItem("Toggle Scale");
        scale.setOnAction(e -> {
            toggleScale();
        });
        menu.getItems().add(scale);

        MenuItem routes = new MenuItem("Toggle Routes");
        routes.setOnAction(e -> {
            toggleRoutes();
        });
        menu.getItems().add(routes);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem sidepaneItem = new MenuItem("Toggle Side Pane");
        sidepaneItem.setOnAction(e -> {
            toggleSidePanel();
        });
        menu.getItems().add(sidepaneItem);

        MenuItem toolbarItem = new MenuItem("Toggle toolbar");
        toolbarItem.setOnAction(e -> {
            toggleToolbar();
        });
        menu.getItems().add(toolbarItem);

        MenuItem statusbarItem = new MenuItem("Toggle status bar");
        statusbarItem.setOnAction(e -> {
            toggleStatusBar();
        });
        menu.getItems().add(statusbarItem);

        return menu;
    }

    ////////////////// event actions /////////////////

    private void toggleGrid() {
        gridOn = !gridOn;
        interstellarSpacePane.toggleGrid(gridOn);
    }

    private void toggleExtensions() {
        extensionsOn = !extensionsOn;
        interstellarSpacePane.toggleExtensions(extensionsOn);
    }

    private void toggleStars() {
        starsOn = !starsOn;
        interstellarSpacePane.toggleStars(starsOn);
    }

    private void toggleScale() {
        scaleOn = !scaleOn;
        interstellarSpacePane.toggleScale(scaleOn);
    }

    private void toggleRoutes() {
        routesOn = !routesOn;
        interstellarSpacePane.toggleRoutes(routesOn);
    }

    /**
     * toggle the setting side panel
     */
    private void toggleSidePanel() {
        if (toggleSettings.isSelected()) {
            mainSplitPane.setDividerPositions(0.8);
        } else {
            mainSplitPane.setDividerPositions(1.0);
        }
    }

    private void toggleToolbar() {
        if (toolBar.isVisible()) {
            toolBar.setVisible(false);
        } else {
            toolBar.setVisible(true);
        }
    }

    private void toggleStatusBar() {
        if (statusBar.isVisible()) {
            statusBar.setVisible(false);
        } else {
            statusBar.setVisible(true);
        }
    }

    ////////////////////////////////

    private Menu createFileImportExportMenu() {
        Menu menu = new Menu("File Import/Export");
        menu.getItems().add(createImportFile());
        menu.getItems().add(createExportFile());
        return menu;
    }

    private MenuItem createImportFile() {
        Menu menu = new Menu("Import File");

        MenuItem importChvFileItem = new MenuItem("Import CHV file…");
        importChvFileItem.setOnAction(e -> {
            importCHV();
        });
        menu.getItems().add(importChvFileItem);


        MenuItem importSimbadItem = new MenuItem("Import Simbad File…");
        importSimbadItem.setOnAction(e -> {
            importSimbad();
        });
        menu.getItems().add(importSimbadItem);


        MenuItem importHipparcos3 = new MenuItem("Import Hipparcos3 file…");
        importHipparcos3.setOnAction(e -> {
            importHipparcos3();
        });
        menu.getItems().add(importHipparcos3);

        return menu;
    }

    private MenuItem createExportFile() {
        Menu menu = new Menu("Export Data");

        MenuItem exportJsonItem = new MenuItem("Export JSON");
        exportJsonItem.setOnAction(e -> {
            exportJSON();
        });
        menu.getItems().add(exportJsonItem);

        return menu;
    }

    // ------------------ File Loaders and Exporters ----------------------------- //

    /**
     * load a CH View binary file
     */
    public void importCHV() {
        log.debug("Import a CHV format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CHV file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CHV Files", "chv");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            // load chview file
            chViewFile = chviewReader.loadFile(file);
            try {
                databaseManagementService.loadCHFile(chViewFile);
            } catch (Exception e) {
                loadErrorAlert();
            }
        } else {
            log.warn("file selection cancelled");
        }
    }


    public void importRBExcel() {
        log.debug("Import a RB Excel format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XLSX Files", "xlsx");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            // load RB excel file
            RBExcelFile excelFile = excelReader.loadFile(file);
            try {
                databaseManagementService.loadRBStarSet(excelFile);
            } catch (Exception e) {
                loadErrorAlert();
            }
        } else {
            log.warn("file selection cancelled");
        }
    }

    private void loadErrorAlert() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Duplicate Dataset");
        String s = "This dataset was already loaded in the system ";
        alert.setContentText(s);
        alert.showAndWait();
        log.error("This dataset was already loaded in the system");
    }

    public void exportJSON() {
        log.debug("Export as a CNV format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select JSON file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON Files", "json");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            chviewReader.exportJson(file, chViewFile);
        } else {
            log.warn("file selection cancelled");
        }
    }


    public void importSimbad() {
        showUnsupportedMessage();
//        log.debug("Import a Simbad CSV format file");
//        final FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select CSV file to import");
//        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CHV Files", "chv");
//        fileChooser.setSelectedExtensionFilter(filter);
//        File file = fileChooser.showOpenDialog(getStage());
//        if (file != null) {
//            // load simbad file
//            chViewFile = chviewReader.loadFile(file);
//
//        } else {
//            log.warn("file selection cancelled");
//        }
    }

    public void exportSimbad() {
        showUnsupportedMessage();
    }

    public void importHipparcos3() {
        showUnsupportedMessage();
//        log.debug("Import a Hipparcos 3 CSV format file");
//        final FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select CHV file to import");
//        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CHV Files", "chv");
//        fileChooser.setSelectedExtensionFilter(filter);
//        File file = fileChooser.showOpenDialog(getStage());
//        if (file != null) {
//            // loaf chview file
//            chViewFile = chviewReader.loadFile(file);
//        } else {
//            log.warn("file selection cancelled");
//        }
    }

    public void exportHipparcos3() {
        showUnsupportedMessage();
    }

    //////////////////////////////


    private Menu createEditDisplayMenu() {
        Menu menu = new Menu("Edit/Display Data");

        MenuItem plotDataItem = new MenuItem("Plot Stars");
        plotDataItem.setOnAction(e -> {
            showPlot();
        });
        menu.getItems().add(plotDataItem);

        MenuItem editDataItem = new MenuItem("View/Edit Star Data");
        editDataItem.setOnAction(e -> {
            showTableData();
        });
        menu.getItems().add(editDataItem);

        return menu;
    }

    private void showTableData() {
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSetIds();

        List<String> dialogData = new ArrayList<>();
        for (DataSetDescriptor dataSetDescriptor : datasets) {
            dialogData.add(dataSetDescriptor.getDataSetName());
        }

        ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);
        dialog.setTitle("Choice Data set to display");
        dialog.setHeaderText("Select your choice");

        Optional<String> result = dialog.showAndWait();
        String selected = "cancelled.";

        if (result.isPresent()) {
            selected = result.get();

            DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
            if (dataSetDescriptor == null) {
                log.error("How the hell did this happen");
                return;
            }

            List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(dataSetDescriptor, 15.0);
            if (!astrographicObjects.isEmpty()) {
                new DataSetTable(getStage(), astrographicObjects);
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Astrographic data view error");
                String s = "No Astrographic data was loaded ";
                alert.setContentText(s);
                alert.showAndWait();
            }
        }
    }

    private void showPlot() {
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSetIds();

        List<String> dialogData = new ArrayList<>();
        for (DataSetDescriptor dataSetDescriptor : datasets) {
            dialogData.add(dataSetDescriptor.getDataSetName());
        }

        ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);
        dialog.setTitle("Choice Data set to display");
        dialog.setHeaderText("Select your choice - (Default display is 15 light years from Earth, use Show Stars filter to change)");

        Optional<String> result = dialog.showAndWait();
        String selected = "cancelled.";

        if (result.isPresent()) {
            selected = result.get();

            DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
            if (dataSetDescriptor == null) {
                log.error("How the hell did this happen");
                return;
            }

            List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(dataSetDescriptor, searchContext.getAstroSearchQuery().getDistanceFromCenterStar());
            log.info("DB Query returns {} stars", astrographicObjects.size());

            if (!astrographicObjects.isEmpty()) {
                astrographicPlotter.drawAstrographicData(astrographicObjects, searchContext.getAstroSearchQuery().getCenterCoordinates());
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Astrographic data view error");
                String s = "No Astrographic data was loaded ";
                alert.setContentText(s);
                alert.showAndWait();
            }
        }
    }


    private DataSetDescriptor findFromDataSet(String selected, List<DataSetDescriptor> datasets) {
        for (DataSetDescriptor dataSetDescriptor : datasets) {
            if (dataSetDescriptor.getDataSetName().equals(selected)) {
                return dataSetDescriptor;
            }
        }
        return null;
    }

    //////////////////////////////


    private Menu createDatabaseMenu() {
        Menu menu = new Menu("Database");

        MenuItem dropDatabaseItem = new MenuItem("Drop database");
        dropDatabaseItem.setOnAction(e -> {
            dropDatabase();
        });
        menu.getItems().add(dropDatabaseItem);

        MenuItem importDatabaseItem = new MenuItem("Import database");
        importDatabaseItem.setOnAction(e -> {
            importDatabase();
        });
        menu.getItems().add(importDatabaseItem);

        MenuItem exportDatabaseItem = new MenuItem("Export database");
        exportDatabaseItem.setOnAction(e -> {
            exportDatabase();
        });
        menu.getItems().add(exportDatabaseItem);

        return menu;
    }

    private void dropDatabase() {
        log.debug("Drop Database");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Drop Database");
        alert.setHeaderText("Drop Database?");
        alert.setContentText("Warning, this will clear the existing database and cannot be undone!");
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

            ProgressDialog progressDialog = new ProgressDialog("Dropping Database");

            // In real life this task would do something useful and return
            // some meaningful result:
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    for (int i = 0; i < 10; i++) {
                        updateProgress(i, 10);
                        Thread.sleep(200);
                    }
                    updateProgress(10, 10);
                    return null;
                }
            };

            // binds progress of progress bars to progress of task:
            progressDialog.activateProgressBar(task);

            // in real life this method would get the result of the task
            // and update the UI based on its value:
            task.setOnSucceeded(event -> {
                progressDialog.getDialogStage().close();
                showStatus("database was dropped");
            });

            progressDialog.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.start();
            boolean dropStarted = true;
            databaseManagementService.dropDatabase();
            dropStarted = false;
            showStatus("database was dropped");
        }
    }

    private void importDatabase() {
        log.debug("This is will import a csv file");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Import Database");
        alert.setHeaderText("Import Database?");
        alert.setContentText("Warning, this will overwrite the existing database and cannot be undone!");
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Database to import");
            File file = fileChooser.showOpenDialog(getStage());
            if (file != null) {
                importDatabase(file);
            } else {
                log.warn("file selection cancelled");
            }
        } else {
            log.warn("file selection cancelled");
        }
    }

    private void exportDatabase() {
        log.debug("Export the existing database as a CSV file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database to export as a CSV file");
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            exportDB(file);
        } else {
            log.warn("file selection cancelled");
        }
    }


    /**
     * call the database import method
     *
     * @param file the file to load
     */
    private void importDatabase(File file) {
        databaseManagementService.importDatabase(file);
        log.info("File selection is:" + file.getAbsolutePath());
        showStatus("Database loaded:" + file.getName());

    }


    /**
     * export the database as a CSV file
     *
     * @param file the file to export
     */
    private void exportDB(File file) {
        databaseManagementService.exportDatabase(file);
        log.info("File selection is:" + file.getAbsolutePath());
    }


    //////////////////////////////


    private Menu createReportMenu() {
        Menu menu = new Menu("Reports");

        MenuItem distanceReportItem = new MenuItem("Distance ...");
        distanceReportItem.setOnAction(e -> {
            distanceReport();
        });
        menu.getItems().add(distanceReportItem);

        MenuItem densityReportItem = new MenuItem("Density ...");
        densityReportItem.setOnAction(e -> {
            densityReport();
        });
        menu.getItems().add(densityReportItem);

        MenuItem cubicDensityReportItem = new MenuItem("Cubic Density ...");
        cubicDensityReportItem.setOnAction(e -> {
            cubicDensityReport();
        });
        menu.getItems().add(cubicDensityReportItem);

        MenuItem clustersReportItem = new MenuItem("Clusters ...");
        clustersReportItem.setOnAction(e -> {
            clustersReport();
        });
        menu.getItems().add(clustersReportItem);

        MenuItem routeFinderReportItem = new MenuItem("Route Finder ...");
        routeFinderReportItem.setOnAction(e -> {
            routeFinderReport();
        });
        menu.getItems().add(routeFinderReportItem);

        MenuItem crossroadsReportItem = new MenuItem("Crossroads ...");
        crossroadsReportItem.setOnAction(e -> {
            crossRoadsReport();
        });
        menu.getItems().add(crossroadsReportItem);

        MenuItem civilizationsReportItem = new MenuItem("Civilizations ...");
        civilizationsReportItem.setOnAction(e -> {
            civilizationsReport();
        });
        menu.getItems().add(civilizationsReportItem);


        return menu;
    }

    private void civilizationsReport() {
        showUnsupportedMessage();
    }

    private void crossRoadsReport() {
        showUnsupportedMessage();
    }

    private void routeFinderReport() {

    }

    private void clustersReport() {
        showUnsupportedMessage();
    }

    private void cubicDensityReport() {

    }

    private void densityReport() {
        showUnsupportedMessage();
    }

    private void distanceReport() {
        showUnsupportedMessage();
    }

    ///////////////////////////////


    private Menu createHelpMenu() {
        Menu menu = new Menu("Help");

        MenuItem aboutItem = new MenuItem("About Terran Republic Viewer");
        aboutItem.setOnAction(e -> {
            aboutTRIPS();
        });
        menu.getItems().add(aboutItem);

        MenuItem supportItem = new MenuItem("How to get support");
        supportItem.setOnAction(e -> {
            support();
        });
        menu.getItems().add(supportItem);


        MenuItem checkForUpdateItem = new MenuItem("Check for update");
        checkForUpdateItem.setOnAction(e -> {
            checkForUpdate();
        });
        menu.getItems().add(checkForUpdateItem);


        return menu;
    }

    private void checkForUpdate() {
        showUnsupportedMessage();
    }

    private void support() {
        showUnsupportedMessage();
    }

    private void aboutTRIPS() {
        showUnsupportedMessage();
    }


    //////////////////////////////
    //////////////////////////////


    ///////////////////////////////

    private Menu createRouteMenu() {
        Menu routeMenu = new Menu("Route");

//        MenuItem createRoute = new MenuItem("Create route");
//        createRoute.setOnAction(e -> {
//            interstellarSpacePane.createRoute(currentRoute);
//        });
//        routeMenu.getItems().add(createRoute);

        MenuItem finishRoute = new MenuItem("Complete route");
        finishRoute.setOnAction(e -> {
            interstellarSpacePane.completeRoute();
        });
        routeMenu.getItems().add(finishRoute);
//
//        MenuItem plotRoute = new MenuItem("Plot routes");
//        plotRoute.setOnAction(e -> {
////            interstellarSpacePane.plotRoutes(routeList);
//            interstellarSpacePane.plotRoute(createRoute());
//        });
//        routeMenu.getItems().add(plotRoute);

        return routeMenu;
    }

    private Menu createAnimationsMenu() {
        Menu animationsMenu = new Menu("Animate");

        MenuItem startYaxisRotation = new MenuItem("Start Y-axis rotation");
        startYaxisRotation.setOnAction(e -> {
            interstellarSpacePane.startYrotate();
        });
        animationsMenu.getItems().add(startYaxisRotation);

        MenuItem stopYaxisRotation = new MenuItem("Stop Y-axis rotation");
        stopYaxisRotation.setOnAction(e -> {
            interstellarSpacePane.stopYrotate();
        });
        animationsMenu.getItems().add(stopYaxisRotation);

        return animationsMenu;
    }

    private Menu createPlotMenu() {
        Menu plotMenu = new Menu("Plot");

        MenuItem simulate = new MenuItem("simulate");
        simulate.setOnAction(e -> {
            simulator.simulate();
        });
        plotMenu.getItems().add(simulate);

//        MenuItem plotStars = new MenuItem("Plot Stars");
//        plotStars.setOnAction(e -> {
//            interstellarSpacePane.plotStars(recordList);
//        });
//        plotMenu.getItems().add(plotStars);
//
//        MenuItem plotRoutes = new MenuItem("Plot Routes");
//        plotStars.setOnAction(e -> {
//            interstellarSpacePane.plotRoutes(routeList);
//        });
//        plotMenu.getItems().add(plotRoutes);

        MenuItem clearStars = new MenuItem("Clear Stars");
        clearStars.setOnAction(e -> {
            interstellarSpacePane.clearStars();
        });
        plotMenu.getItems().add(clearStars);

        MenuItem clearRoutes = new MenuItem("Clear Routes");
        clearRoutes.setOnAction(e -> {
            interstellarSpacePane.clearRoutes();
        });
        plotMenu.getItems().add(clearRoutes);

        return plotMenu;
    }


    ///////////////////////   Listener Updates  /////////////////////

    /**
     * select a interstellar system space
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectInterstellarSpace(Map<String, String> objectProperties) {

    }

    /**
     * select a solar system
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectSolarSystemSpace(Map<String, String> objectProperties) {

    }

    /**
     * update the list
     *
     * @param listItem the list item
     */
    @Override
    public void updateList(Map<String, String> listItem) {

    }

    /**
     * clear the entire list
     */
    @Override
    public void clearList() {

    }

    @Override
    public void displayStellarProperties(Map<String, String> properties) {

    }

    @Override
    public void showNewStellarData(AstroSearchQuery searchQuery) {

    }

    /**
     * triggered when a new route is created
     *
     * @param routeDescriptor the route descriptor
     */
    @Override
    public void newRoute(RouteDescriptor routeDescriptor) {

    }

    /**
     * triggered when an existing route changes
     *
     * @param routeDescriptor the route descriptor
     */
    @Override
    public void updateRoute(RouteDescriptor routeDescriptor) {

    }

    /**
     * triggered when a route is removed
     *
     * @param routeDescriptor the route descriptor
     */
    @Override
    public void deleteRoute(RouteDescriptor routeDescriptor) {

    }

    @Override
    public void recenter(StarDisplayRecord starId) {

    }

    /////////////////////////////////////////

    private void showUnsupportedMessage() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Work in progress");
        String s = "not supported yet ";
        alert.setContentText(s);
        alert.showAndWait();
    }

    /**
     * get the parent window for this application
     *
     * @return the primary primaryStage
     */
    private Stage getStage() {
        return (Stage) mainPanel.getScene().getWindow();
    }

    /**
     * show a status message
     *
     * @param message the status message
     */
    private void showStatus(String message) {
        databaseStatus.setText(message);
    }


}
