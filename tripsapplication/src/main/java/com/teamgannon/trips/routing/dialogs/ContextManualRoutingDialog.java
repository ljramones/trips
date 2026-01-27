package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RoutingConstants;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

/**
 * Dialog for creating manual routes by selecting stars.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li>Plotting context mode: Started from a selected star in the plot</li>
 *   <li>Non-plotting context mode: User selects starting star from a list</li>
 * </ul>
 */
@Slf4j
public class ContextManualRoutingDialog extends Dialog<Boolean> {

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final RouteManager routeManager;
    private final DataSetDescriptor currentDataSet;

    // =========================================================================
    // UI Components
    // =========================================================================

    private final Font font = RoutingConstants.createDialogFont();
    private final TextField routeName = new TextField();
    private final TextField lineWidthTextField = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private final TextArea notes = new TextArea();
    private final GridPane grid = new GridPane();
    private final Button finishBtn = new Button("Finish route");
    private final Button startButton = new Button("Start route");
    private final Button removeLastBtn = new Button("Remove last segment");
    private final Button colorButton = new Button("Color");
    private final Label startStarLabel = new Label();
    private final Label routeStartCoordinates = new Label("0, 0, 0");
    private final Stage stage;

    /** ComboBox for star selection (only used in non-plotting context mode) */
    private ComboBox<String> originDisplayCmb;

    // =========================================================================
    // State
    // =========================================================================

    /** The currently selected star for route origin */
    private StarDisplayRecord starDisplayRecord;

    /** The route descriptor being built */
    private RouteDescriptor routeDescriptor;

    /** True if this dialog was opened from a selected star in the plot */
    private final boolean plottingContextMode;

    /** Maps star names to their display records for lookup */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    /** Stars that have been added to the current route */
    private final Set<StarDisplayRecord> routeSet = new HashSet<>();

    /** Ordered list of stars in the route */
    private final List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    /** Whether route creation has started */
    private boolean startRouting = false;

    /** Whether this is the first star selection */
    private boolean firstTime = true;

    /** Current row for adding route segment labels */
    private int rowToAdd = 1;

    /** The anchor row where selected stars are displayed */
    private static final int ANCHOR_ROW = 7;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Constructor for non-plotting context mode.
     * User will select the starting star from a list.
     *
     * @param routeManager   the route manager
     * @param currentDataSet the current dataset
     * @param starsInView    the list of stars available for selection
     */
    public ContextManualRoutingDialog(@NotNull RouteManager routeManager,
                                      @NotNull DataSetDescriptor currentDataSet,
                                      @NotNull List<StarDisplayRecord> starsInView) {
        this.plottingContextMode = false;
        this.routeManager = routeManager;
        this.currentDataSet = currentDataSet;

        Set<String> searchValues = convertList(starsInView);

        VBox vBox = initializeDialogPane();
        setupCommonGridLayout();
        setupStarSelectionComboBox(searchValues);
        setupCoordinatesDisplay();
        setupNotesAndStartSegment(null);
        HBox buttonBox = createButtonBox();
        vBox.getChildren().add(buttonBox);

        stage = initializeStage();
        clearRoute();
    }

    /**
     * Constructor for plotting context mode.
     * The starting star is already selected.
     *
     * @param routeManager      the route manager
     * @param currentDataSet    the current dataset
     * @param starDisplayRecord the pre-selected starting star
     */
    public ContextManualRoutingDialog(@NotNull RouteManager routeManager,
                                      @NotNull DataSetDescriptor currentDataSet,
                                      @NotNull StarDisplayRecord starDisplayRecord) {
        this.plottingContextMode = true;
        this.routeManager = routeManager;
        this.currentDataSet = currentDataSet;
        this.starDisplayRecord = starDisplayRecord;

        starDisplayRecordList.add(starDisplayRecord);

        VBox vBox = initializeDialogPane();
        setupCommonGridLayout();
        setupFixedStarDisplay(starDisplayRecord);
        setupCoordinatesDisplayForStar(starDisplayRecord);
        setupNotesAndStartSegment(starDisplayRecord.getStarName());
        HBox buttonBox = createButtonBox();
        vBox.getChildren().add(buttonBox);

        stage = initializeStage();
    }

    // =========================================================================
    // Initialization Helpers
    // =========================================================================

    private VBox initializeDialogPane() {
        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);
        vBox.getChildren().add(grid);
        this.setTitle("Route Creation Dialog");
        this.setHeaderText("Create an initial Route");
        return vBox;
    }

    private void setupCommonGridLayout() {
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        // Row 1: Route Name
        Label routeNameLabel = new Label("Route Name: ");
        routeNameLabel.setFont(font);
        grid.add(routeNameLabel, 1, 1);
        routeName.setText("Route A");
        grid.add(routeName, 2, 1);

        // Row 2: Route Color
        Label routeColorLabel = new Label("Route Color: ");
        routeColorLabel.setFont(font);
        grid.add(routeColorLabel, 1, 2);
        colorPicker.setValue(Color.CYAN);
        colorButton.setOnAction(this::setColor);
        grid.add(colorButton, 2, 2);

        // Row 3: Line Width
        Label routeLineWidthLabel = new Label("Route Line Width: ");
        routeLineWidthLabel.setFont(font);
        grid.add(routeLineWidthLabel, 1, 3);
        lineWidthTextField.setText(String.valueOf(RoutingConstants.DEFAULT_LINE_WIDTH));
        grid.add(lineWidthTextField, 2, 3);
    }

    private void setupStarSelectionComboBox(Set<String> searchValues) {
        Label routeStartLabel = new Label("Route Starts at: ");
        routeStartLabel.setFont(font);

        originDisplayCmb = new ComboBox<>();
        originDisplayCmb.setPromptText("start typing");
        originDisplayCmb.getItems().addAll(searchValues);
        originDisplayCmb.setEditable(true);
        originDisplayCmb.setOnAction(this::selectStar);
        TextFields.bindAutoCompletion(originDisplayCmb.getEditor(), originDisplayCmb.getItems());

        grid.add(routeStartLabel, 1, 4);
        grid.add(originDisplayCmb, 2, 4);
    }

    private void setupFixedStarDisplay(StarDisplayRecord star) {
        Label routeStartLabel = new Label("Route Starts at: ");
        routeStartLabel.setFont(font);
        Label routeStart = new Label(star.getStarName());
        grid.add(routeStartLabel, 1, 4);
        grid.add(routeStart, 2, 4);
    }

    private void setupCoordinatesDisplay() {
        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        routeStartCoordinatesLabel.setFont(font);
        grid.add(routeStartCoordinatesLabel, 1, 5);
        grid.add(routeStartCoordinates, 2, 5);
    }

    private void setupCoordinatesDisplayForStar(StarDisplayRecord star) {
        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        routeStartCoordinatesLabel.setFont(font);
        Label coordsLabel = new Label(formatCoordinates(star.getX(), star.getY(), star.getZ()));
        grid.add(routeStartCoordinatesLabel, 1, 5);
        grid.add(coordsLabel, 2, 5);
    }

    private void setupNotesAndStartSegment(@Nullable String starName) {
        // Row 6: Notes
        Label notesLabel = new Label("Notes: ");
        notesLabel.setFont(font);
        grid.add(notesLabel, 1, 6);
        grid.add(notes, 2, 6);

        // Row 7: Start segment
        Label startSegmentLabel = new Label("Start:");
        startSegmentLabel.setFont(font);
        grid.add(startSegmentLabel, 1, 7);

        if (starName != null) {
            grid.add(new Label(starName), 2, 7);
        } else {
            grid.add(startStarLabel, 2, 7);
        }
    }

    private HBox createButtonBox() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        startButton.setOnAction(this::startRouteClicked);
        hBox.getChildren().add(startButton);

        finishBtn.setDisable(true);
        finishBtn.setOnAction(this::finishRouteClicked);
        hBox.getChildren().add(finishBtn);

        Button resetBtn = new Button("Reset route");
        resetBtn.setOnAction(this::resetRoute);
        hBox.getChildren().add(resetBtn);

        removeLastBtn.setOnAction(this::removeRouteSegment);
        hBox.getChildren().add(removeLastBtn);
        removeLastBtn.setDisable(true);

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(this::closeClicked);
        hBox.getChildren().add(closeBtn);

        return hBox;
    }

    private Stage initializeStage() {
        Stage stg = (Stage) this.getDialogPane().getScene().getWindow();
        stg.setOnCloseRequest(this::close);
        return stg;
    }

    // =========================================================================
    // Star Selection
    // =========================================================================

    private void selectStar(ActionEvent actionEvent) {
        if (!firstTime) {
            return;
        }

        String selectedStar = originDisplayCmb.getValue();
        if (selectedStar == null || selectedStar.trim().isEmpty()) {
            showWarningMessage("Star Selection", "Please select a star from the list.");
            return;
        }

        StarDisplayRecord selectedRecord = starLookup.get(selectedStar);
        if (selectedRecord == null) {
            showWarningMessage("Star Selection",
                    "Star '" + selectedStar + "' not found. Please select from the available stars.");
            return;
        }

        starDisplayRecord = selectedRecord;
        startStarLabel.setText(starDisplayRecord.getStarName());
        routeStartCoordinates.setText(formatCoordinates(
                starDisplayRecord.getX(),
                starDisplayRecord.getY(),
                starDisplayRecord.getZ()));

        log.info("start star:{}", starDisplayRecord);
        originDisplayCmb.setDisable(true);
        firstTime = false;
        removeLastBtn.setDisable(false);
    }

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        starsInView.forEach(record -> starLookup.put(record.getStarName(), record));
        return starLookup.keySet();
    }

    // =========================================================================
    // Route Management
    // =========================================================================

    /**
     * Add a star to the current route.
     *
     * @param record the star to add
     */
    public void addStar(StarDisplayRecord record) {
        log.info("request to add star to manual plot:{}", record.getStarName());
        if (!startRouting) {
            showWarningMessage("Add Route", "Please press start to select parameters for route.");
            return;
        }

        if (routeSet.contains(record)) {
            showErrorAlert("Manual Route finding", "You already have this star in the route.");
            return;
        }

        routeManager.continueRoute(record);
        routeSet.add(record);
        starDisplayRecordList.add(record);

        Label segmentNameLabel = new Label("segment" + rowToAdd + ":");
        segmentNameLabel.setFont(font);
        grid.add(segmentNameLabel, 1, ANCHOR_ROW + rowToAdd);
        grid.add(new Label(record.getStarName()), 2, ANCHOR_ROW + rowToAdd);
        rowToAdd++;

        stage.sizeToScene();
        finishBtn.setDisable(false);
        removeLastBtn.setDisable(false);
    }

    private void startRouteClicked(ActionEvent actionEvent) {
        if (starDisplayRecord == null) {
            showWarningMessage("Start Route", "Please select a starting star first.");
            return;
        }

        double lineWidth = parseLineWidth();
        routeDescriptor = RouteDescriptor.builder()
                .name(routeName.getText())
                .color(colorPicker.getValue())
                .startStar(starDisplayRecord.getStarName())
                .routeCoordinates(new ArrayList<>())
                .lineWidth(lineWidth)
                .visibility(RouteVisibility.FULL)
                .routeNotes(notes.getText())
                .routeList(new ArrayList<>())
                .build();

        routeSet.add(starDisplayRecord);
        routeManager.startRoute(currentDataSet, routeDescriptor, starDisplayRecord);
        startRouting = true;
        startButton.setDisable(true);
    }

    private void finishRouteClicked(ActionEvent actionEvent) {
        log.info("save route");
        routeManager.finishRoute();
        removeLastBtn.setDisable(true);
        setResult(true);
    }

    private void resetRoute(ActionEvent actionEvent) {
        if (!plottingContextMode) {
            originDisplayCmb.setDisable(false);
            originDisplayCmb.setValue("");
            routeStartCoordinates.setText("0, 0, 0");
            startButton.setDisable(false);
        }
        clearRoute();

        // Remove added segment rows
        for (int i = 1; i <= rowToAdd; i++) {
            int rowNumber = ANCHOR_ROW + i;
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) == rowNumber);
        }

        stage.sizeToScene();
        finishBtn.setDisable(true);
        routeManager.startRoute(currentDataSet, routeDescriptor, starDisplayRecord);
        removeLastBtn.setDisable(true);
    }

    private void removeRouteSegment(ActionEvent actionEvent) {
        log.info("remove last star segment");
        StarDisplayRecord lastStar = routeManager.removeLastSegment();
        if (lastStar != null) {
            boolean wasThere = routeSet.remove(lastStar);
            log.info("removing {}, was there={}", lastStar.getStarName(), wasThere);
        }
        grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) == (ANCHOR_ROW + rowToAdd - 1));
        rowToAdd--;
        stage.sizeToScene();
    }

    private void clearRoute() {
        routeSet.clear();
        routeManager.resetRoute();
        removeLastBtn.setDisable(true);
    }

    // =========================================================================
    // Dialog Actions
    // =========================================================================

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

    private void closeClicked(ActionEvent actionEvent) {
        if (routeManager.isManualRoutingActive()) {
            routeManager.setManualRoutingActive(false);
        }
        setResult(false);
    }

    private void close(WindowEvent windowEvent) {
        if (routeManager.isManualRoutingActive()) {
            routeManager.setManualRoutingActive(false);
        }
        setResult(false);
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    private String formatCoordinates(double x, double y, double z) {
        return "x(%.2f), y(%.2f), z(%.2f)".formatted(x, y, z);
    }

    private double parseLineWidth() {
        try {
            return Double.parseDouble(lineWidthTextField.getText());
        } catch (NumberFormatException nfe) {
            log.error("{} is not a valid double so defaulting to {}",
                    lineWidthTextField.getText(), RoutingConstants.DEFAULT_LINE_WIDTH);
            return RoutingConstants.DEFAULT_LINE_WIDTH;
        }
    }
}
