package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.RoutingConstants;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
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
public class RouteFinderDialogInView extends Dialog<RouteFindingOptions> {

    /*
     * the combobox for selection
     */
    private ComboBox<String> originDisplayCmb;
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

    // Polity exclusion checkboxes (terranCheckBox, dornaniCheckBox, ktorCheckBox,
    // aratKurCheckBox, hkhRkhCheckBox, slassrithiCheckBox, other1-4 CheckBox) removed by
    // the Worldbuilding Data Model Normalization task. F.3 reintroduces faction-based
    // exclusions via FactionAssignment.


    Font font = RoutingConstants.createDialogFont();

    Button colorButton = new Button("color");

    /**
     * this data set is used when we want to search the stars in the 3d display
     *
     * @param starsInView the set of visible stars on the display
     */
    public RouteFinderDialogInView(@NotNull List<StarDisplayRecord> starsInView) {
        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        searchValues = convertList(starsInView);

        this.setTitle("Enter parameters for Route location");

        Tab primaryTab = new Tab();
        setupPrimaryTab(primaryTab);
        TabPane routeSelectionPane = new TabPane();
        routeSelectionPane.getTabs().add(primaryTab);

        Tab starTab = new Tab();
        setupStarTab(starTab);
        routeSelectionPane.getTabs().add(starTab);

        // Polity exclusions tab removed by the Worldbuilding Data Model Normalization task.

        VBox vBox = new VBox();
        vBox.getChildren().add(routeSelectionPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Find Route(s)");
        resetBtn.setOnAction(this::findRoutesClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Cancel");
        addBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

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

        originDisplayCmb = new ComboBox<>();
        originDisplayCmb.setPromptText("start typing");
        originDisplayCmb.getItems().addAll(searchValues);
        originDisplayCmb.setEditable(true);
        TextFields.bindAutoCompletion(originDisplayCmb.getEditor(), originDisplayCmb.getItems());
        gridPane.add(originDisplayCmb, 1, 1);

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
        upperLengthLengthTextField.setText(RoutingConstants.DEFAULT_UPPER_BOUND_TEXT);

        Label lowerBound = new Label("lower limit for route length");
        lowerBound.setFont(font);
        gridPane.add(lowerBound, 0, 4);
        gridPane.add(lowerLengthLengthTextField, 1, 4);
        lowerLengthLengthTextField.setText(RoutingConstants.DEFAULT_LOWER_BOUND_TEXT);

        Label lineWidth = new Label("route line width");
        lineWidth.setFont(font);
        gridPane.add(lineWidth, 0, 5);
        gridPane.add(lineWidthTextField, 1, 5);
        lineWidthTextField.setText(RoutingConstants.DEFAULT_LINE_WIDTH_TEXT);


        Label routeColor = new Label("route color");
        routeColor.setFont(font);

        colorButton.setOnAction(this::pickColor);

        gridPane.add(routeColor, 0, 6);
        gridPane.add(colorButton, 1, 6);
        colorPicker.setValue(Color.AQUA);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText(RoutingConstants.DEFAULT_NUM_PATHS_TEXT);

        // Bucket-A (Issue 49 + 35): accessibility + input hints for primary route-finder tab.
        originDisplayCmb.setTooltip(new Tooltip("Type a star name from the current view; auto-complete suggests matches"));
        originDisplayCmb.setAccessibleText("Origin star for the route, chosen from stars currently in view");
        destinationDisplayCmb.setTooltip(new Tooltip("Type a star name from the current view; auto-complete suggests matches"));
        destinationDisplayCmb.setAccessibleText("Destination star for the route, chosen from stars currently in view");
        upperLengthLengthTextField.setPromptText("light years");
        upperLengthLengthTextField.setTooltip(new Tooltip("Maximum allowed length, in light years, for any single transit segment"));
        upperLengthLengthTextField.setAccessibleText("Upper bound for transit length, in light years");
        lowerLengthLengthTextField.setPromptText("light years");
        lowerLengthLengthTextField.setTooltip(new Tooltip("Minimum allowed length, in light years, for any single transit segment"));
        lowerLengthLengthTextField.setAccessibleText("Lower bound for transit length, in light years");
        lineWidthTextField.setPromptText("e.g. 0.5");
        lineWidthTextField.setTooltip(new Tooltip("Line width used to render route segments in the 3D plot"));
        lineWidthTextField.setAccessibleText("Route line width for 3D rendering");
        numPathsToFindTextField.setPromptText("e.g. 3");
        numPathsToFindTextField.setTooltip(new Tooltip("Number of alternate shortest paths to find (Yen's K-shortest paths algorithm)"));
        numPathsToFindTextField.setAccessibleText("Number of alternate routes to find");
        colorButton.setAccessibleText("Choose the colour used to render the route");
    }

    private void pickColor(ActionEvent actionEvent) {
        ColorChoiceDialog dialog = new ColorChoiceDialog();
        Optional<ColorChoice> colorChoiceOptional = dialog.showAndWait();
        if (colorChoiceOptional.isPresent()) {
            ColorChoice colorChoice = colorChoiceOptional.get();
            if (colorChoice.isSelected()) {
                colorPicker.setValue(colorChoice.getSwatch());
                colorButton.setTextFill(colorChoice.getSwatch());
            }
        }
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
        oCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox1.getChildren().add(oCheckBox);
        bCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox1.getChildren().add(bCheckBox);
        aCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox1.getChildren().add(aCheckBox);
        fCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox1.getChildren().add(fCheckBox);

        VBox vBox2 = new VBox();
        hBox.getChildren().add(vBox2);
        gCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox2.getChildren().add(gCheckBox);
        kCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox2.getChildren().add(kCheckBox);
        mCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox2.getChildren().add(mCheckBox);
        wCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox2.getChildren().add(wCheckBox);

        VBox vBox3 = new VBox();
        hBox.getChildren().add(vBox3);
        lCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox3.getChildren().add(lCheckBox);
        tCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox3.getChildren().add(tCheckBox);
        yCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox3.getChildren().add(yCheckBox);
        cCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox3.getChildren().add(cCheckBox);
        sCheckBox.setMinWidth(RoutingConstants.CHECKBOX_WIDTH);
        vBox3.getChildren().add(sCheckBox);

    }

    // setupPolityTab removed by the Worldbuilding Data Model Normalization task.

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

    // getPolityExclusions removed by the Worldbuilding Data Model Normalization task.

    private void close(WindowEvent windowEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
    }

    ////////////////////////////////

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        for (StarDisplayRecord record : starsInView) {
            starLookup.put(record.getStarName(), record);
        }
        return starLookup.keySet();
    }

    private void cancelClicked(ActionEvent actionEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
        log.info("cancel find routes clicked");
    }

    private void findRoutesClicked(ActionEvent actionEvent) {
        try {
            String originStarSelected = originDisplayCmb.getValue();
            String destinationStarSelected = destinationDisplayCmb.getValue();
            double maxDistance = RoutingConstants.MAX_VALIDATION_DISTANCE;

            if (!searchValues.contains(originStarSelected)) {
                showErrorAlert("Find Route", "Origin star <%s> is not present in view".formatted(originStarSelected));
                return;
            }
            if (!searchValues.contains(destinationStarSelected)) {
                showErrorAlert("Find Route", "Destination star <%s> is not present in view".formatted(destinationStarSelected));
                return;
            }

            setResult(
                    RouteFindingOptions
                            .builder()
                            .selected(true)
                            .originStarName(originStarSelected)
                            .destinationStarName(destinationStarSelected)
                            .upperBound(Double.parseDouble(upperLengthLengthTextField.getText()))
                            .lowerBound(Double.parseDouble(lowerLengthLengthTextField.getText()))
                            .lineWidth(Double.parseDouble(lineWidthTextField.getText()))
                            .starExclusions(getStarExclusions())
                            .color(colorPicker.getValue())
                            .maxDistance(maxDistance)
                            .numberPaths(Integer.parseInt(numPathsToFindTextField.getText()))
                            .build()
            );
            log.info("cancel clicked");
        } catch (NumberFormatException nfe) {
            showErrorAlert("Route Finder", "bad floating point");
            log.error("bad floating point");
        }
    }

}
