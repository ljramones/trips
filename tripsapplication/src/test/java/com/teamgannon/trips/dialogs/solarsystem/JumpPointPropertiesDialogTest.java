package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.model.FeatureDescription;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase E.1 Step 9 — verifies {@link JumpPointPropertiesDialog} construction with the
 * fixtures the user-facing path actually produces:
 * <ul>
 *   <li>single-star fixture (name="Jump Point", parentStarName="Sol")</li>
 *   <li>multi-star fixture (name="Alpha Centauri A Jump Point", parentStarName="Alpha Centauri A")</li>
 *   <li>null parentStarName (resolveParentStarName returned null — should show "N/A")</li>
 *   <li>missing feature name (defensive — should fall back to "Jump Point" in the title)</li>
 * </ul>
 *
 * <p>The dialog is read-only with just an OK button and three TitledPanes (Identity, Position,
 * Routing). The result type is {@link ButtonType} (no edit-result struct because Phase E.1 ships
 * no editable fields — Phase E.3 will extend if routing data becomes editable).
 *
 * <p>Dialog construction must run on the JavaFX Application Thread (it creates a {@code Window}
 * internally), so every test wraps the dialog operations via {@link #runOnFxThread(FxAction)}.
 * Pattern mirrors {@code StarEditComboConfigTest.runOnFxThread}.
 */
class JumpPointPropertiesDialogTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized (re-entry from another test class)
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void requireFx() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
    }

    private FeatureDescription singleStarFixture() {
        FeatureDescription jp = new FeatureDescription();
        jp.setId("feat-single");
        jp.setName("Jump Point");
        jp.setFeatureType("JUMP_POINT");
        jp.setOrbitalRadiusAU(38.5);
        jp.setOrbitalAngleDeg(127.4);
        jp.setOrbitalHeightAU(2.3);
        jp.setParentBodyId("star-sol");
        return jp;
    }

    private FeatureDescription multiStarFixture() {
        FeatureDescription jp = new FeatureDescription();
        jp.setId("feat-multi");
        jp.setName("Alpha Centauri A Jump Point");
        jp.setFeatureType("JUMP_POINT");
        jp.setOrbitalRadiusAU(34.1);
        jp.setOrbitalAngleDeg(45.0);
        jp.setOrbitalHeightAU(-1.8);
        jp.setParentBodyId("star-alpha-cen-a");
        return jp;
    }

    @Test
    @DisplayName("single-star fixture: title contains feature name, identity shows parent star name")
    void singleStarConstructs() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(singleStarFixture(), "Sol");
            assertEquals("Jump Point Properties: Jump Point", dialog.getTitle());
            assertTrue(dialog.getDialogPane().getButtonTypes().contains(ButtonType.OK),
                    "dialog must have an OK button");
            List<String> identityValues = readIdentityValues(dialog);
            assertEquals("Jump Point", identityValues.get(0), "Name row");
            assertEquals("Jump Point", identityValues.get(1), "Type row (display string)");
            assertEquals("Sol", identityValues.get(2), "Parent Star row resolves from constructor arg");
        });
    }

    @Test
    @DisplayName("multi-star fixture: title shows star-qualified name, Parent Star resolves correctly")
    void multiStarConstructs() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(multiStarFixture(), "Alpha Centauri A");
            assertEquals("Jump Point Properties: Alpha Centauri A Jump Point", dialog.getTitle());
            List<String> identityValues = readIdentityValues(dialog);
            assertEquals("Alpha Centauri A Jump Point", identityValues.get(0));
            assertEquals("Alpha Centauri A", identityValues.get(2));
        });
    }

    @Test
    @DisplayName("null parentStarName falls back to N/A in the Parent Star row")
    void nullParentStarShowsNA() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(singleStarFixture(), null);
            List<String> identityValues = readIdentityValues(dialog);
            assertEquals("N/A", identityValues.get(2), "null parentStarName -> N/A");
        });
    }

    @Test
    @DisplayName("blank parentStarName also falls back to N/A")
    void blankParentStarShowsNA() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(singleStarFixture(), "   ");
            List<String> identityValues = readIdentityValues(dialog);
            assertEquals("N/A", identityValues.get(2));
        });
    }

    @Test
    @DisplayName("feature with null name falls back to 'Jump Point' label so the dialog stays usable")
    void nullFeatureNameFallsBackToDisplayType() throws Exception {
        runOnFxThread(() -> {
            FeatureDescription jp = singleStarFixture();
            jp.setName(null);
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(jp, "Sol");
            assertEquals("Jump Point Properties: Jump Point", dialog.getTitle());
        });
    }

    @Test
    @DisplayName("position section formats AU + degrees with units")
    void positionSectionShowsValuesWithUnits() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(singleStarFixture(), "Sol");
            List<String> positionValues = readSectionValues(dialog, "Position");
            assertTrue(positionValues.get(0).endsWith("AU"),
                    "Orbital Radius shows AU unit: " + positionValues.get(0));
            assertTrue(positionValues.get(0).contains("38.5"),
                    "Orbital Radius shows the value: " + positionValues.get(0));
            assertTrue(positionValues.get(1).endsWith("°"),
                    "Orbital Angle shows degrees unit: " + positionValues.get(1));
            assertTrue(positionValues.get(2).endsWith("AU"),
                    "Orbital Height shows AU unit");
        });
    }

    @Test
    @DisplayName("routing section shows N/A placeholders for Network and Reachable Destinations (Phase E.3)")
    void routingPlaceholdersArePresent() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(singleStarFixture(), "Sol");
            List<String> routingValues = readSectionValues(dialog, "Routing (Phase E.3)");
            assertEquals(2, routingValues.size(), "Network + Reachable Destinations rows");
            assertEquals("N/A", routingValues.get(0));
            assertEquals("N/A", routingValues.get(1));
        });
    }

    @Test
    @DisplayName("dialog content layout: 3 TitledPanes (Identity, Position, Routing) in a VBox")
    void dialogLayoutHasThreeTitledPanes() throws Exception {
        runOnFxThread(() -> {
            JumpPointPropertiesDialog dialog = new JumpPointPropertiesDialog(singleStarFixture(), "Sol");
            VBox content = (VBox) dialog.getDialogPane().getContent();
            assertNotNull(content);
            List<TitledPane> panes = content.getChildren().stream()
                    .filter(TitledPane.class::isInstance)
                    .map(TitledPane.class::cast)
                    .toList();
            assertEquals(3, panes.size());
            assertEquals("Identity", panes.get(0).getText());
            assertEquals("Position", panes.get(1).getText());
            assertEquals("Routing (Phase E.3)", panes.get(2).getText());
        });
    }

    // ----- helpers -----

    /** Pulls the value-column labels from the Identity TitledPane. */
    private List<String> readIdentityValues(JumpPointPropertiesDialog dialog) {
        return readSectionValues(dialog, "Identity");
    }

    /**
     * Pulls the value-column labels (column 1) from the GridPane inside the named TitledPane.
     * The dialog layout puts the bold label in column 0 and the value Label in column 1.
     */
    private List<String> readSectionValues(JumpPointPropertiesDialog dialog, String titlePaneName) {
        VBox content = (VBox) dialog.getDialogPane().getContent();
        for (var node : content.getChildren()) {
            if (node instanceof TitledPane pane && titlePaneName.equals(pane.getText())) {
                GridPane grid = (GridPane) pane.getContent();
                List<String> values = new ArrayList<>();
                for (var child : grid.getChildren()) {
                    Integer col = GridPane.getColumnIndex(child);
                    if (col != null && col == 1 && child instanceof Label l) {
                        values.add(l.getText());
                    }
                }
                return values;
            }
        }
        throw new IllegalStateException("No TitledPane named " + titlePaneName);
    }

    /** Functional interface that allows checked exceptions to propagate out of FX-thread actions. */
    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }

    /**
     * Run {@code action} on the JavaFX Application Thread and wait for it to complete.
     * Mirrors the pattern in {@code StarEditComboConfigTest.runOnFxThread}.
     */
    private void runOnFxThread(FxAction action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX operation timed out");

        Throwable t = error.get();
        if (t != null) {
            if (t instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(t);
        }
    }
}
