package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.dialogs.search.ComboBoxAutoComplete;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.*;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class ContextAutomatedRoutingDialog extends Dialog<Boolean> {

    /*
     * the combobox for selection
     */
    private final Label fromStar = new Label();
    private ComboBox<String> destinationDisplayCmb;

    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    private final TextField upperLengthLengthTextField = new TextField();
    private final TextField lowerLengthLengthTextField = new TextField();

    private final TextField numPathsToFindTextField = new TextField();
    private final TextField lineWidthTextField = new TextField();

    private final ColorPicker colorPicker = new ColorPicker();

    private final Set<String> searchValues;

    private final Stage stage;

    // star types
    private final CheckBox oCheckBox = new CheckBox("O");
    private final CheckBox bCheckBox = new CheckBox("B");
    private final CheckBox aCheckBox = new CheckBox("A");
    private final CheckBox fCheckBox = new CheckBox("F");
    private final CheckBox gCheckBox = new CheckBox("G");
    private final CheckBox kCheckBox = new CheckBox("K");
    private final CheckBox mCheckBox = new CheckBox("M");
    private final CheckBox wCheckBox = new CheckBox("W");
    private final CheckBox lCheckBox = new CheckBox("L");
    private final CheckBox tCheckBox = new CheckBox("T");
    private final CheckBox yCheckBox = new CheckBox("Y");
    private final CheckBox cCheckBox = new CheckBox("C");
    private final CheckBox sCheckBox = new CheckBox("S");

    private final CheckBox terranCheckBox = new CheckBox("Terran");
    private final CheckBox dornaniCheckBox = new CheckBox("Dornani");
    private final CheckBox ktorCheckBox = new CheckBox("Ktor");
    private final CheckBox aratKurCheckBox = new CheckBox("Arat kur");
    private final CheckBox hkhRkhCheckBox = new CheckBox("Hkh'Rkh");
    private final CheckBox slassrithiCheckBox = new CheckBox("Slaasriithi");
    private final CheckBox other1CheckBox = new CheckBox("Other 1");
    private final CheckBox other2CheckBox = new CheckBox("Other 2");
    private final CheckBox other3CheckBox = new CheckBox("Other 3");
    private final CheckBox other4CheckBox = new CheckBox("Other 4");


    private static final int GRAPH_THRESHOLD = 1500;

    Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final StarPlotManager plotManager;
    private final RouteManager routeManager;
    private final DataSetDescriptor currentDataSet;
    private final List<StarDisplayRecord> starsInView;

    public ContextAutomatedRoutingDialog(@NotNull StarPlotManager plotManager,
                                         @NotNull RouteManager routeManager,
                                         @NotNull DataSetDescriptor currentDataSet,
                                         @NotNull StarDisplayRecord fromStarDisplayRecord,
                                         @NotNull List<StarDisplayRecord> starsInView) {


        this.plotManager = plotManager;
        this.routeManager = routeManager;
        this.currentDataSet = currentDataSet;
        this.starsInView = starsInView;

        // set the dialog as a utility
        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);


        searchValues = convertList(starsInView);

        fromStar.setText(fromStarDisplayRecord.getStarName());

        this.setTitle("Enter parameters for Route location");

        Tab primaryTab = new Tab();
        setupPrimaryTab(primaryTab);
        TabPane routeSelectionPane = new TabPane();
        routeSelectionPane.getTabs().add(primaryTab);

        Tab starTab = new Tab();
        setupStarTab(starTab);
        routeSelectionPane.getTabs().add(starTab);

        Tab polityTab = new Tab();
        setupPolityTab(polityTab);
        routeSelectionPane.getTabs().add(polityTab);

        VBox vBox = new VBox();
        vBox.getChildren().add(routeSelectionPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Find Route(s)");
        resetBtn.setOnAction(this::findRoutesClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Close");
        addBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);
    }

    /**
     * set the starName
     *
     * @param starName the star name
     */
    public void setToStar(String starName) {
        destinationDisplayCmb.setValue(starName);
        stage.toFront();
    }

    private void cancelClicked(ActionEvent actionEvent) {
        plotManager.clearRoutingFlag();
        setResult(false);
    }

    private void findRoutesClicked(ActionEvent actionEvent) {

        double maxDistance = 20;
        String destinationStarSelected = destinationDisplayCmb.getValue();
        if (!searchValues.contains(destinationStarSelected)) {
            showErrorAlert("Find Route", String.format("Destination star <%s> is not present in view", destinationStarSelected));
            return;
        }

        RouteFindingOptions routeFindingOptions = RouteFindingOptions
                .builder()
                .selected(true)
                .originStar(fromStar.getText())
                .destinationStar(destinationStarSelected)
                .upperBound(Double.parseDouble(upperLengthLengthTextField.getText()))
                .lowerBound(Double.parseDouble(lowerLengthLengthTextField.getText()))
                .lineWidth(Double.parseDouble(lineWidthTextField.getText()))
                .starExclusions(getStarExclusions())
                .polityExclusions(getPolityExclusions())
                .color(colorPicker.getValue())
                .maxDistance(maxDistance)
                .numberPaths(Integer.parseInt(numPathsToFindTextField.getText()))
                .build();

        // get the route location parameters from the dialog
        processRouteRequest(currentDataSet, stage, routeFindingOptions);
    }


    private void processRouteRequest(DataSetDescriptor currentDataSet, Stage theStage, RouteFindingOptions routeFindingOptions) {

        // if we actually selected the option to route then do it
        if (routeFindingOptions.isSelected()) {
            try {
                log.info("find route between stars");

                // setup our initials
                String origin = routeFindingOptions.getOriginStar();
                String destination = routeFindingOptions.getDestinationStar();

                List<StarDisplayRecord> prunedStars = prune(starsInView, routeFindingOptions);

                if (prunedStars.size() > GRAPH_THRESHOLD) {
                    showErrorAlert("Route Finder", "There are too many stars to plan a route");
                }

                RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(prunedStars);

                // check if the start star is present
                if (!routeBuilderHelper.has(origin)) {
                    showErrorAlert("Route Finder", "The start star is not in route");
                }

                // check if the destination star is present
                if (!routeBuilderHelper.has(destination)) {
                    showErrorAlert("Route Finder", "The destination star is not in route");
                }

                // calculate the transits based on upper and lower bounds
                StarMeasurementService starMeasurementService = new StarMeasurementService();
                DistanceRoutes distanceRoutes = DistanceRoutes
                        .builder()
                        .upperDistance(routeFindingOptions.getUpperBound())
                        .lowerDistance((routeFindingOptions.getLowerBound()))
                        .build();
                List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, prunedStars);
                log.info("transits calculated");

                // create a graph based on the transits available
                RouteGraph routeGraph = new RouteGraph(transitRoutes);
                try {
                    // check if the origin star and destination star are connected to each other
                    if (routeGraph.isConnected(origin, destination)) {
                        determineRoutesAndPlotOne(currentDataSet, theStage, routeFindingOptions, origin, destination, routeBuilderHelper, routeGraph);
                    } else {
                        log.error("Source and destination stars do not have a path");
                        showErrorAlert("Route Finder from A to B",
                                "Unable to find a route between source and destination based on supplied parameters.");
                    }
                } catch (Exception e) {
                    showErrorAlert("Route Finder from A to B",
                            "Unable to find a route between source and destination based on supplied parameters.");
                }
            } catch (Exception e) {
                log.error("failed to find routes:", e);
            }
        }
    }


    private void determineRoutesAndPlotOne(DataSetDescriptor currentDataSet, Stage theStage, RouteFindingOptions routeFindingOptions, String origin, String destination, RouteBuilderHelper routeBuilderHelper, RouteGraph routeGraph) {
        log.info("Source and destination stars have a path");

        // find the k shortest paths. We add one because the first is null
        List<String> kShortestPaths = routeGraph.findKShortestPaths(
                origin, destination, routeFindingOptions.getNumberPaths() + 1);
//                        kShortestPaths.forEach(System.out::println);

        PossibleRoutes possibleRoutes = new PossibleRoutes();
        possibleRoutes.setDesiredPath(String.format("Route %s to %s", origin, destination));

        List<RouteDescriptor> routeList = new ArrayList<>();
        List<String> pathToPlot = new ArrayList<>(kShortestPaths);
        int i = 1;
        // for each of our paths create a route
        for (String path : pathToPlot) {
            if (path.contains("null")) {
                // this is a dead path
                continue;
            }

            Color color = routeFindingOptions.getColor();
            if (i > 1) {
                color = Color.color(Math.random(), Math.random(), Math.random());
            }

            RouteDescriptor route = routeBuilderHelper.buildPath(
                    origin, destination, Integer.toString(i++),
                    color, routeFindingOptions.getLineWidth(), path);

            route.setDescriptor(currentDataSet);
            routeList.add(route);

            RoutingMetric routingMetric = RoutingMetric
                    .builder()
                    .totalLength(route.getTotalLength())
                    .routeDescriptor(route)
                    .path(path)
                    .rank(i - 1)
                    .numberOfSegments(route.getLineSegments().size())
                    .build();
            possibleRoutes.getRoutes().add(routingMetric);
        }

        DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(theStage, possibleRoutes);
        Stage stage = (Stage) displayAutoRoutesDialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
        if (optionalRoutingMetrics.isPresent()) {
            List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
            if (selectedRoutingMetrics.size() > 0) {
                log.info("plotting selected routes:{}", selectedRoutingMetrics);
                // plot the routes found
                plot(currentDataSet, selectedRoutingMetrics);
            }
        }
    }

    private List<StarDisplayRecord> prune(List<StarDisplayRecord> starsInView, RouteFindingOptions routeFindingOptions) {
        List<StarDisplayRecord> prunedStars = new ArrayList<>();
        for (StarDisplayRecord starDisplayRecord : starsInView) {
            if (routeFindingOptions.getStarExclusions().contains(starDisplayRecord.getSpectralClass().substring(0, 1))) {
                continue;
            }
            if (routeFindingOptions.getPolityExclusions().contains(starDisplayRecord.getPolity())) {
                continue;
            }
            prunedStars.add(starDisplayRecord);
        }
        return prunedStars;
    }


    /**
     * plot the routes found
     *
     * @param currentDataSet the data descriptor
     * @param routeList      the routes to plot
     */
    private void plot(DataSetDescriptor currentDataSet, List<RoutingMetric> routeList) {
        routeManager.plotRouteDescriptors(currentDataSet, routeList);
    }

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        starsInView.forEach(record -> starLookup.put(record.getStarName(), record));
        return starLookup.keySet();
    }

    private void setupPrimaryTab(Tab primaryTab) {
        VBox vBox = new VBox();
        primaryTab.setContent(vBox);
        primaryTab.setText("Primary");
        GridPane gridPane = new GridPane();

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        Label originStar = new Label("Origin Star");
        originStar.setFont(font);
        gridPane.add(originStar, 0, 1);

        Label destinationStar = new Label("Destination Star");
        destinationStar.setFont(font);
        gridPane.add(destinationStar, 0, 2);

        gridPane.add(fromStar, 1, 1);

        destinationDisplayCmb = new ComboBox<>();
        destinationDisplayCmb.setPromptText("start typing");
        destinationDisplayCmb.setTooltip(new Tooltip());
        destinationDisplayCmb.getItems().addAll(searchValues);
        destinationDisplayCmb.setEditable(true);
        TextFields.bindAutoCompletion(destinationDisplayCmb.getEditor(), destinationDisplayCmb.getItems());
        gridPane.add(destinationDisplayCmb, 1, 2);

        Label upperBound = new Label("Upper limit for route length");
        upperBound.setFont(font);
        gridPane.add(upperBound, 0, 3);
        gridPane.add(upperLengthLengthTextField, 1, 3);
        upperLengthLengthTextField.setText("8");

        Label lowerBound = new Label("lower limit for route length");
        lowerBound.setFont(font);
        gridPane.add(lowerBound, 0, 4);
        gridPane.add(lowerLengthLengthTextField, 1, 4);
        lowerLengthLengthTextField.setText("3");

        Label lineWidth = new Label("route line width");
        lineWidth.setFont(font);
        gridPane.add(lineWidth, 0, 5);
        gridPane.add(lineWidthTextField, 1, 5);
        lineWidthTextField.setText("0.5");

        Label routeColor = new Label("route color");
        routeColor.setFont(font);
        gridPane.add(routeColor, 0, 6);
        gridPane.add(colorPicker, 1, 6);
        colorPicker.setValue(Color.AQUA);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText("3");

    }

    private void setupStarTab(Tab starTab) {
        VBox vBox = new VBox();
        starTab.setContent(vBox);
        starTab.setText("Star Exclusions");
        Label titleLabel = new Label("Select stars to exclude in our route finding");
        titleLabel.setFont(font);
        vBox.getChildren().add(titleLabel);
        vBox.getChildren().add(new Separator());

        HBox hBox = new HBox();
        vBox.getChildren().add(hBox);

        VBox vBox1 = new VBox();
        hBox.getChildren().add(vBox1);
        oCheckBox.setMinWidth(100);
        vBox1.getChildren().add(oCheckBox);
        bCheckBox.setMinWidth(100);
        vBox1.getChildren().add(bCheckBox);
        aCheckBox.setMinWidth(100);
        vBox1.getChildren().add(aCheckBox);
        fCheckBox.setMinWidth(100);
        vBox1.getChildren().add(fCheckBox);

        VBox vBox2 = new VBox();
        hBox.getChildren().add(vBox2);
        gCheckBox.setMinWidth(100);
        vBox2.getChildren().add(gCheckBox);
        kCheckBox.setMinWidth(100);
        vBox2.getChildren().add(kCheckBox);
        mCheckBox.setMinWidth(100);
        vBox2.getChildren().add(mCheckBox);
        wCheckBox.setMinWidth(100);
        vBox2.getChildren().add(wCheckBox);

        VBox vBox3 = new VBox();
        hBox.getChildren().add(vBox3);
        lCheckBox.setMinWidth(100);
        vBox3.getChildren().add(lCheckBox);
        tCheckBox.setMinWidth(100);
        vBox3.getChildren().add(tCheckBox);
        yCheckBox.setMinWidth(100);
        vBox3.getChildren().add(yCheckBox);
        cCheckBox.setMinWidth(100);
        vBox3.getChildren().add(cCheckBox);
        sCheckBox.setMinWidth(100);
        vBox3.getChildren().add(sCheckBox);

    }

    private void setupPolityTab(Tab polityTab) {
        VBox vBox = new VBox();
        polityTab.setContent(vBox);
        polityTab.setText("Polity Exclusions");
        Label titleLabel = new Label("Select polities to exclude in our route finding");
        titleLabel.setFont(font);
        vBox.getChildren().add(titleLabel);
        vBox.getChildren().add(new Separator());

        HBox hBox = new HBox();
        vBox.getChildren().add(hBox);

        VBox vBox1 = new VBox();
        hBox.getChildren().add(vBox1);
        terranCheckBox.setMinWidth(100);
        vBox1.getChildren().add(terranCheckBox);

        dornaniCheckBox.setMinWidth(100);
        vBox1.getChildren().add(dornaniCheckBox);

        ktorCheckBox.setMinWidth(100);
        vBox1.getChildren().add(ktorCheckBox);

        aratKurCheckBox.setMinWidth(100);
        vBox1.getChildren().add(aratKurCheckBox);

        hkhRkhCheckBox.setMinWidth(100);
        vBox1.getChildren().add(hkhRkhCheckBox);

        slassrithiCheckBox.setMinWidth(100);
        vBox1.getChildren().add(slassrithiCheckBox);

        VBox vBox2 = new VBox();
        hBox.getChildren().add(vBox2);

        other1CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other1CheckBox);

        other2CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other2CheckBox);

        other3CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other3CheckBox);

        other4CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other4CheckBox);

    }

    private Set<String> getStarExclusions() {
        Set<String> starExclusions = new HashSet<>();
        if (oCheckBox.isSelected()) {
            starExclusions.add("O");
        }
        if (bCheckBox.isSelected()) {
            starExclusions.add("B");
        }
        if (aCheckBox.isSelected()) {
            starExclusions.add("A");
        }
        if (fCheckBox.isSelected()) {
            starExclusions.add("F");
        }
        if (gCheckBox.isSelected()) {
            starExclusions.add("G");
        }
        if (kCheckBox.isSelected()) {
            starExclusions.add("K");
        }
        if (mCheckBox.isSelected()) {
            starExclusions.add("M");
        }
        if (wCheckBox.isSelected()) {
            starExclusions.add("W");
        }
        if (lCheckBox.isSelected()) {
            starExclusions.add("L");
        }
        if (tCheckBox.isSelected()) {
            starExclusions.add("T");
        }
        if (yCheckBox.isSelected()) {
            starExclusions.add("Y");
        }
        if (cCheckBox.isSelected()) {
            starExclusions.add("C");
        }
        if (sCheckBox.isSelected()) {
            starExclusions.add("S");
        }

        return starExclusions;
    }

    private Set<String> getPolityExclusions() {
        Set<String> exclusions = new HashSet<>();
        if (terranCheckBox.isSelected()) {
            exclusions.add("Terran");
        }
        if (dornaniCheckBox.isSelected()) {
            exclusions.add("Dornani");
        }
        if (ktorCheckBox.isSelected()) {
            exclusions.add("Ktor");
        }
        if (aratKurCheckBox.isSelected()) {
            exclusions.add("Arat Kur");
        }
        if (hkhRkhCheckBox.isSelected()) {
            exclusions.add("Hkh'rkh");
        }
        if (slassrithiCheckBox.isSelected()) {
            exclusions.add("slassrithi");
        }
        if (other1CheckBox.isSelected()) {
            exclusions.add("Other 1");
        }
        if (other2CheckBox.isSelected()) {
            exclusions.add("Other 2");
        }
        if (other3CheckBox.isSelected()) {
            exclusions.add("Other 3");
        }
        if (other4CheckBox.isSelected()) {
            exclusions.add("Other4");
        }

        return exclusions;
    }

    private void close(WindowEvent windowEvent) {
        plotManager.clearRoutingFlag();
        setResult(false);
    }

}
