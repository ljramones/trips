package com.teamgannon.trips.dialogs.solarsystem;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Builds the read-only "Legend" {@link TitledPane} for the procedural-planet
 * viewer dialog — a colour key for elevation bands plus an optional plate /
 * boundary key.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation. Stateless builder; the only context input is
 * whether the plate-boundary key should be appended.
 */
public final class PlanetLegendSection {

    /** Elevation legend rows: human label → hex colour. */
    private static final String[][] ELEVATION_ITEMS = {
            {"Snow Peak", "#FFFFFF"},
            {"Mountain", "#8B7355"},
            {"Highland", "#9B8B5B"},
            {"Lowland", "#6B8E23"},
            {"Coast", "#90B060"},
            {"Shallow", "#5090C0"},
            {"Ocean", "#3070A0"},
            {"Deep Sea", "#204080"},
            {"Abyss", "#102050"}
    };

    /** Plate-boundary legend rows. */
    private static final String[][] PLATE_ITEMS = {
            {"Convergent", "#DC3C3C"},
            {"Divergent", "#3CC8B4"},
            {"Transform", "#DCB43C"},
            {"Inactive", "#787878"}
    };

    private static final String SECTION_TITLE_STYLE =
            "-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 10;";
    private static final String LEGEND_ROW_STYLE =
            "-fx-text-fill: #555555; -fx-font-size: 9;";

    private PlanetLegendSection() {
    }

    /**
     * Build the legend pane.
     *
     * @param includePlateBoundaries whether to append the plate-boundary key
     *                               (typically true iff the active planet has
     *                               a plate assignment + boundary analysis).
     * @return a collapsed-by-default {@link TitledPane} ready for the side panel.
     */
    public static TitledPane create(boolean includePlateBoundaries) {
        VBox content = new VBox(3);
        content.setPadding(new Insets(5));

        Label elevTitle = new Label("Elevation");
        elevTitle.setStyle(SECTION_TITLE_STYLE);
        content.getChildren().add(elevTitle);
        for (String[] item : ELEVATION_ITEMS) {
            content.getChildren().add(buildLegendRow(item[0], item[1], 14, 10, true));
        }

        if (includePlateBoundaries) {
            content.getChildren().add(new Separator());
            Label plateTitle = new Label("Boundaries");
            plateTitle.setStyle(SECTION_TITLE_STYLE);
            content.getChildren().add(plateTitle);
            for (String[] item : PLATE_ITEMS) {
                content.getChildren().add(buildLegendRow(item[0], item[1], 14, 4, false));
            }
        }

        TitledPane pane = new TitledPane("Legend", content);
        pane.setExpanded(false);   // Collapsed by default
        pane.setCollapsible(true);
        return pane;
    }

    private static HBox buildLegendRow(String label, String hexColour,
                                       double swatchWidth, double swatchHeight,
                                       boolean withStroke) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        Rectangle swatch = new Rectangle(swatchWidth, swatchHeight);
        swatch.setFill(Color.web(hexColour));
        if (withStroke) {
            swatch.setStroke(Color.gray(0.5));
            swatch.setStrokeWidth(0.5);
        }

        Label text = new Label(label);
        text.setStyle(LEGEND_ROW_STYLE);
        row.getChildren().addAll(swatch, text);
        return row;
    }
}
