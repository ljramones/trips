package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.model.FeatureDescription;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only properties dialog for JUMP_POINT system features.
 *
 * <p>v2 Phase E.1 Step 9 — clicking a JUMP_POINT sphere opens this dialog showing the
 * jump point's identity, position, and placeholder rows for Phase E.3 routing data
 * (network membership, reachable destinations).
 *
 * <p>Per the Phase E.1 Step 9 design discussion (Q3 ratification): per-type dialog, not a
 * generic FeaturePropertiesDialog. Other feature types will grow their own dialogs as their
 * data models mature — ASTEROID_BELT, JUMP_GATE, SHIPYARD all want different fields.
 *
 * <p>Pattern mirrors {@code PlanetPropertiesDialog}: programmatic {@code Dialog<T>}
 * subclass, raw construction (no FxWeaver) since the caller passes per-edit state. There's
 * nothing to edit yet — the result type is the OK/Cancel {@link ButtonType} — but the
 * scaffolding is in place for Phase E.3 to introduce editable fields without restructuring.
 */
@Slf4j
public class JumpPointPropertiesDialog extends Dialog<ButtonType> {

    private static final String DISPLAY_TYPE = "Jump Point";
    private static final String NOT_AVAILABLE = "N/A";

    public JumpPointPropertiesDialog(@NotNull FeatureDescription feature, @Nullable String parentStarName) {
        setTitle("Jump Point Properties: " + safeName(feature));

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().add(createIdentitySection(feature, parentStarName));
        content.getChildren().add(createPositionSection(feature));
        content.getChildren().add(createRoutingSection());

        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(420);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }

    private TitledPane createIdentitySection(FeatureDescription feature, String parentStarName) {
        GridPane grid = createGrid();
        addRow(grid, 0, "Name:", safeName(feature));
        addRow(grid, 1, "Type:", DISPLAY_TYPE);
        addRow(grid, 2, "Parent Star:", parentStarName != null && !parentStarName.isBlank()
                ? parentStarName : NOT_AVAILABLE);
        TitledPane pane = new TitledPane("Identity", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createPositionSection(FeatureDescription feature) {
        GridPane grid = createGrid();
        addRow(grid, 0, "Orbital Radius:", formatAu(feature.getOrbitalRadiusAU()));
        addRow(grid, 1, "Orbital Angle:", formatDegrees(feature.getOrbitalAngleDeg()));
        addRow(grid, 2, "Orbital Height:", formatAu(feature.getOrbitalHeightAU()));
        TitledPane pane = new TitledPane("Position", grid);
        pane.setCollapsible(false);
        return pane;
    }

    /**
     * Placeholders for Phase E.3 — drive-capability matching will populate network membership
     * and reachable destinations from the GateNetwork canon + the spaceship's
     * defaultAccessibleNetworkIds.
     */
    private TitledPane createRoutingSection() {
        GridPane grid = createGrid();
        addRow(grid, 0, "Network:", NOT_AVAILABLE);
        addRow(grid, 1, "Reachable Destinations:", NOT_AVAILABLE);
        TitledPane pane = new TitledPane("Routing (Phase E.3)", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label value = new Label(valueText);
        value.setAlignment(Pos.CENTER_LEFT);
        grid.add(label, 0, row);
        grid.add(value, 1, row);
    }

    private String safeName(FeatureDescription feature) {
        String name = feature.getName();
        return name != null && !name.isBlank() ? name : DISPLAY_TYPE;
    }

    private String formatAu(double value) {
        return "%.4f AU".formatted(value);
    }

    private String formatDegrees(double value) {
        return "%.2f°".formatted(value);
    }
}
