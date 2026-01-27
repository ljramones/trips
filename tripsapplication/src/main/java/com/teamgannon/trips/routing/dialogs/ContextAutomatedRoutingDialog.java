package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.RoutingConstants;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
import com.teamgannon.trips.routing.dialogs.components.ExclusionCheckboxPanel;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.starplotting.StarPlotManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

    private Button colorButton = new Button("Color");

    private final Set<String> searchValues;

    private final Stage stage;

    // Exclusion panels (replaces individual checkboxes)
    private ExclusionCheckboxPanel spectralClassPanel;
    private ExclusionCheckboxPanel polityPanel;

    private final Font font = RoutingConstants.createDialogFont();

    private final StarPlotManager plotManager;
    private final RouteManager routeManager;
    private final RouteFindingService routeFindingService;
    private final DataSetDescriptor currentDataSet;
    private final List<StarDisplayRecord> starsInView;

    public ContextAutomatedRoutingDialog(@NotNull StarPlotManager plotManager,
                                         @NotNull RouteManager routeManager,
                                         @NotNull RouteFindingService routeFindingService,
                                         @NotNull DataSetDescriptor currentDataSet,
                                         @NotNull StarDisplayRecord fromStarDisplayRecord,
                                         @NotNull List<StarDisplayRecord> starsInView) {


        this.plotManager = plotManager;
        this.routeManager = routeManager;
        this.routeFindingService = routeFindingService;
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

        double maxDistance = RoutingConstants.MAX_VALIDATION_DISTANCE;
        String destinationStarSelected = destinationDisplayCmb.getValue();
        if (!searchValues.contains(destinationStarSelected)) {
            showErrorAlert("Find Route", "Destination star <%s> is not present in view".formatted(destinationStarSelected));
            return;
        }

        RouteFindingOptions routeFindingOptions = RouteFindingOptions
                .builder()
                .selected(true)
                .originStarName(fromStar.getText())
                .destinationStarName(destinationStarSelected)
                .upperBound(Double.parseDouble(upperLengthLengthTextField.getText()))
                .lowerBound(Double.parseDouble(lowerLengthLengthTextField.getText()))
                .lineWidth(Double.parseDouble(lineWidthTextField.getText()))
                .starExclusions(getStarExclusions())
                .polityExclusions(getPolityExclusions())
                .color(colorPicker.getValue())
                .maxDistance(maxDistance)
                .numberPaths(Integer.parseInt(numPathsToFindTextField.getText()))
                .build();

        // Delegate to service
        processRouteRequest(routeFindingOptions);
    }


    private void processRouteRequest(RouteFindingOptions routeFindingOptions) {
        if (!routeFindingOptions.isSelected()) {
            return;
        }

        // Delegate to service for route finding
        RouteFindingResult result = routeFindingService.findRoutes(
                routeFindingOptions, starsInView, currentDataSet);

        if (!result.isSuccess()) {
            showErrorAlert("Route Finder", result.getErrorMessage());
            return;
        }

        if (!result.hasRoutes()) {
            showErrorAlert("Route Finder", "No routes found with the given parameters.");
            return;
        }

        // Display the found routes for user selection
        displayFoundRoutes(result);
    }

    private void displayFoundRoutes(@NotNull RouteFindingResult result) {
        // Create non-modal dialog with preview and accept callbacks
        DisplayAutoRoutesDialog dialog = new DisplayAutoRoutesDialog(
                result.getRoutes(),
                // Preview callback: plot routes without closing dialog
                selectedRoutes -> {
                    log.info("Previewing {} routes", selectedRoutes.size());
                    plot(currentDataSet, selectedRoutes);
                },
                // Accept callback: final plot when user clicks Accept
                selectedRoutes -> {
                    if (!selectedRoutes.isEmpty()) {
                        log.info("Accepted {} routes", selectedRoutes.size());
                        plot(currentDataSet, selectedRoutes);
                        setResult(true);
                    }
                }
        );

        // Show non-modal dialog (user can interact with map)
        dialog.show();
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

        gridPane.setPadding(new Insets(RoutingConstants.GRID_PADDING));
        gridPane.setVgap(RoutingConstants.BUTTON_SPACING);
        gridPane.setHgap(RoutingConstants.BUTTON_SPACING);
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
        destinationDisplayCmb.getItems().addAll(searchValues);
        destinationDisplayCmb.setEditable(true);
        TextFields.bindAutoCompletion(destinationDisplayCmb.getEditor(), destinationDisplayCmb.getItems());
        gridPane.add(destinationDisplayCmb, 1, 2);

        Label upperBound = new Label("Upper limit for route length");
        upperBound.setFont(font);
        gridPane.add(upperBound, 0, 3);
        gridPane.add(upperLengthLengthTextField, 1, 3);
        upperLengthLengthTextField.setText(String.valueOf(RoutingConstants.DEFAULT_UPPER_DISTANCE));

        Label lowerBound = new Label("lower limit for route length");
        lowerBound.setFont(font);
        gridPane.add(lowerBound, 0, 4);
        gridPane.add(lowerLengthLengthTextField, 1, 4);
        lowerLengthLengthTextField.setText(RoutingConstants.DEFAULT_LOWER_BOUND_TEXT);

        Label lineWidth = new Label("route line width");
        lineWidth.setFont(font);
        gridPane.add(lineWidth, 0, 5);
        gridPane.add(lineWidthTextField, 1, 5);
        lineWidthTextField.setText(String.valueOf(RoutingConstants.DEFAULT_LINE_WIDTH));

        Label routeColor = new Label("route color");
        routeColor.setFont(font);
        gridPane.add(routeColor, 0, 6);

        colorPicker.setValue(Color.AQUA);
        colorButton.setOnAction(this::setColor);
        gridPane.add(colorButton, 1, 6);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText(String.valueOf(RoutingConstants.DEFAULT_NUMBER_PATHS));

    }

    private void setColor(ActionEvent actionEvent) {
        ColorChoiceDialog colorChoiceDialog = new ColorChoiceDialog();
        Optional<ColorChoice> colorChoiceOptional = colorChoiceDialog.showAndWait();
        if (colorChoiceOptional.isPresent()) {
            ColorChoice colorChoice = colorChoiceOptional.get();
            if (colorChoice.isSelected()) {
                colorPicker.setValue(colorChoice.getSwatch());
                colorButton.setTextFill(colorChoice.getSwatch());
            }
        }
    }

    private void setupStarTab(Tab starTab) {
        starTab.setText("Star Exclusions");
        spectralClassPanel = ExclusionCheckboxPanel.createSpectralClassPanel();
        starTab.setContent(spectralClassPanel);
    }

    private void setupPolityTab(Tab polityTab) {
        polityTab.setText("Polity Exclusions");
        polityPanel = ExclusionCheckboxPanel.createPolityPanel();
        polityTab.setContent(polityPanel);
    }

    private Set<String> getStarExclusions() {
        return spectralClassPanel.getSelectedExclusions();
    }

    private Set<String> getPolityExclusions() {
        return polityPanel.getSelectedExclusions();
    }

    private void close(WindowEvent windowEvent) {
        plotManager.clearRoutingFlag();
        setResult(false);
    }

}
