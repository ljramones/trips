package com.teamgannon.trips.transits;

import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Dialog for configuring transit band definitions.
 * Allows users to define distance ranges for finding transits between stars.
 */
@Slf4j
public class FindTransitsBetweenStarsDialog extends Dialog<TransitDefinitions> {

    private static final int MAX_BANDS = 20;
    private static final Font HEADER_FONT = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12);

    private final Map<UUID, TransitBandEditor> bandEditors = new HashMap<>();
    private final DatasetService datasetService;
    private final TransitDefinitions transitDefinitions;
    private final VBox bandsContainer = new VBox(5);

    public FindTransitsBetweenStarsDialog(DatasetService datasetService,
                                          TransitDefinitions transitDefinitions) {
        this.datasetService = datasetService;
        this.transitDefinitions = transitDefinitions;

        setTitle("Select a Range to Find Transits");
        setHeight(400);
        setWidth(700);

        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));

        mainContainer.getChildren().add(new Separator());
        mainContainer.getChildren().add(createHeaderRow());
        mainContainer.getChildren().add(bandsContainer);

        setupInitialBands(transitDefinitions);

        HBox addRowBox = new HBox();
        addRowBox.setAlignment(Pos.CENTER);
        addRowBox.setPadding(new Insets(10, 0, 10, 0));
        Button addRowButton = new Button("Add Row");
        addRowButton.setOnAction(this::addRow);
        addRowBox.getChildren().add(addRowButton);
        mainContainer.getChildren().add(addRowBox);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button generateButton = new Button("Generate Transits");
        generateButton.setOnAction(this::generateTransits);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(this::cancel);
        buttonBox.getChildren().addAll(generateButton, cancelButton);
        mainContainer.getChildren().add(buttonBox);

        getDialogPane().setContent(mainContainer);
        DialogUtils.bindCloseHandler(this, this::closeWindow);
    }

    private HBox createHeaderRow() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(5));
        header.setAlignment(Pos.CENTER_LEFT);

        Label useLabel = createHeaderLabel("Use?", 30);
        Label nameLabel = createHeaderLabel("Band Name", 80);
        Label lowerLabel = createHeaderLabel("Lower", 60);
        Label sliderLabel = createHeaderLabel("Range Slider", 180);
        Label upperLabel = createHeaderLabel("Upper", 60);
        Label widthLabel = createHeaderLabel("Width", 50);
        Label colorLabel = createHeaderLabel("Color", 60);

        HBox.setHgrow(sliderLabel, Priority.ALWAYS);

        header.getChildren().addAll(useLabel, nameLabel, lowerLabel, sliderLabel, upperLabel, widthLabel, colorLabel);
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setFont(HEADER_FONT);
        label.setPrefWidth(width);
        return label;
    }

    private void setupInitialBands(TransitDefinitions transitDefinitions) {
        List<TransitRangeDef> existingBands = transitDefinitions.getTransitRangeDefs();
        existingBands.sort(Comparator.comparing(TransitRangeDef::getBandName));

        if (!existingBands.isEmpty()) {
            for (TransitRangeDef def : existingBands) {
                addBand(def);
            }
        } else {
            // Create default bands
            addBand(createDefaultBand("band 1", Color.AQUAMARINE));
            addBand(createDefaultBand("band 2", Color.BLUEVIOLET));
            addBand(createDefaultBand("band 3", Color.GREEN));
            addBand(createDefaultBand("band 4", Color.YELLOW));
            addBand(createDefaultBand("band 5", Color.RED));
        }
    }

    private TransitRangeDef createDefaultBand(String name, Color color) {
        TransitRangeDef def = new TransitRangeDef();
        def.setBandId(UUID.randomUUID());
        def.setBandName(name);
        def.setEnabled(false);
        def.setBandColor(color);
        def.setLineWidth(TransitConstants.DEFAULT_BAND_LINE_WIDTH);
        return def;
    }

    private void addBand(TransitRangeDef def) {
        TransitBandEditor editor = new TransitBandEditor(def);
        bandEditors.put(def.getBandId(), editor);
        bandsContainer.getChildren().add(editor.getRoot());
    }

    private void addRow(ActionEvent event) {
        if (bandEditors.size() >= MAX_BANDS) {
            showErrorAlert("Add Transit Definition", "Maximum of " + MAX_BANDS + " bands allowed");
            return;
        }

        int bandNumber = bandEditors.size() + 1;
        TransitRangeDef def = createDefaultBand("band " + bandNumber, Color.WHEAT);
        addBand(def);
        sizeToScene();
    }

    private void generateTransits(ActionEvent event) {
        List<TransitRangeDef> defs = collectValues();
        if (defs == null) {
            showErrorAlert("Transit Definitions", "Invalid transit definition");
            return;
        }

        transitDefinitions.setTransitRangeDefs(defs);
        transitDefinitions.setSelected(true);
        datasetService.setTransitPreferences(transitDefinitions);
        setResult(transitDefinitions);
    }

    private List<TransitRangeDef> collectValues() {
        try {
            List<TransitRangeDef> defs = new ArrayList<>();
            for (TransitBandEditor editor : bandEditors.values()) {
                defs.add(editor.getValue());
            }
            validateTransitRanges(defs);
            return defs;
        } catch (Exception e) {
            log.error("Transit definition invalid: {}", e.getMessage());
            return null;
        }
    }

    private void validateTransitRanges(List<TransitRangeDef> defs) {
        for (TransitRangeDef outer : defs) {
            if (!outer.isEnabled()) {
                continue;
            }
            for (TransitRangeDef inner : defs) {
                if (!inner.isEnabled() || inner.getBandId().equals(outer.getBandId())) {
                    continue;
                }
                if (rangesOverlap(outer, inner)) {
                    String message = outer.getBandName() + " overlaps with " + inner.getBandName();
                    showErrorAlert("Transit Definitions", message);
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    private boolean rangesOverlap(TransitRangeDef a, TransitRangeDef b) {
        return (b.getLowerRange() > a.getLowerRange() && b.getLowerRange() < a.getUpperRange()) ||
               (b.getUpperRange() > a.getLowerRange() && b.getUpperRange() < a.getUpperRange());
    }

    private void cancel(ActionEvent event) {
        cancelAndClose();
    }

    private void closeWindow(WindowEvent event) {
        cancelAndClose();
    }

    private void cancelAndClose() {
        TransitDefinitions result = new TransitDefinitions();
        result.setSelected(false);
        setResult(result);
    }

    private void sizeToScene() {
        if (getDialogPane().getScene() != null) {
            getDialogPane().getScene().getWindow().sizeToScene();
        }
    }
}
