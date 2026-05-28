package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SpaceshipDesign;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase A0 Step 5 coverage for the {@code concealed} and {@code operationalState} controls on
 * {@link SpaceshipEditorDialog}.
 *
 * <p>Three things to pin: defaults for a new design, round-trip from an existing design, and the
 * mutate-then-save path. The dialog is constructed without being shown — {@code Platform.startup}
 * boots the JavaFX toolkit, {@code Platform.runLater} pumps the work on the FX thread, and a
 * {@link CountDownLatch} blocks the test thread until the assertions are made. This mirrors the
 * lightweight bootstrap already in use by {@code StarEditFormBinderTest}.
 *
 * <p>This is a headless smoke test, not a full TestFX harness. The Phase A0 prompt explicitly said
 * not to retrofit dialog test infrastructure in this step.
 */
class SpaceshipEditorDialogFieldsTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {
            });
            javaFxInitialized = true;
        } catch (IllegalStateException alreadyStarted) {
            javaFxInitialized = true;
        } catch (Exception e) {
            javaFxInitialized = false;
        }
    }

    private static SpaceshipDesign sampleDesign() {
        return SpaceshipBuilder.create("Test Vessel")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(200)
                .engineTons(150)
                .propellantTons(300)
                .payloadTons(50)
                .crewTons(20)
                .radiatorTons(120)
                .crew(4)
                .lengthMeters(50)
                .build();
    }

    /**
     * Recursively walks the dialog's scene-graph subtree (depth-first), collecting every node of the
     * requested {@code type} that sits in a GridPane row whose first column carries a Label matching
     * {@code labelText}. The dialog is unattached to a Scene during the test, so we cannot rely on
     * CSS lookups — only direct child traversal works.
     */
    private static <T> Optional<T> findControlByLabel(DialogPane pane, String labelText, Class<T> type) {
        Predicate<javafx.scene.Node> isLabel = n -> n instanceof Label l && labelText.equals(l.getText());
        java.util.List<javafx.scene.Node> all = new java.util.ArrayList<>();
        collect(pane.getContent(), all);
        return all.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .filter(t -> {
                    javafx.scene.Node node = (javafx.scene.Node) t;
                    javafx.scene.Parent p = node.getParent();
                    if (!(p instanceof javafx.scene.layout.GridPane grid)) {
                        return false;
                    }
                    Integer row = javafx.scene.layout.GridPane.getRowIndex(node);
                    if (row == null) {
                        return false;
                    }
                    return grid.getChildren().stream().anyMatch(sibling -> {
                        Integer siblingRow = javafx.scene.layout.GridPane.getRowIndex(sibling);
                        Integer siblingCol = javafx.scene.layout.GridPane.getColumnIndex(sibling);
                        return siblingRow != null
                                && siblingRow.intValue() == row.intValue()
                                && (siblingCol == null || siblingCol == 0)
                                && isLabel.test(sibling);
                    });
                })
                .findFirst();
    }

    private static void collect(javafx.scene.Node node, java.util.List<javafx.scene.Node> out) {
        if (node == null) {
            return;
        }
        out.add(node);
        if (node instanceof javafx.scene.control.ScrollPane sp) {
            collect(sp.getContent(), out);
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                collect(child, out);
            }
        }
    }

    private void onFx(Runnable r) throws InterruptedException {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test ran past 10s on the FX thread");
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    @DisplayName("New-design dialog defaults to concealed=false and operationalState=OPERATIONAL")
    void newDialogDefaults() throws InterruptedException {
        onFx(() -> {
            SpaceshipEditorDialog dialog = new SpaceshipEditorDialog(null);

            Optional<CheckBox> concealed = findControlByLabel(dialog.getDialogPane(), "Concealed", CheckBox.class);
            Optional<ComboBox> operational = findControlByLabel(dialog.getDialogPane(), "Operational state", ComboBox.class);

            assertTrue(concealed.isPresent(), "Concealed checkbox should be present on the basic-info grid");
            assertTrue(operational.isPresent(), "Operational state combo should be present on the basic-info grid");
            assertFalse(concealed.get().isSelected(), "new design defaults to not concealed");
            assertEquals(OperationalState.OPERATIONAL, operational.get().getValue(),
                    "new design defaults to OPERATIONAL");
        });
    }

    @Test
    @DisplayName("Edit dialog reads concealed=true + operationalState=DERELICT from an existing design")
    void editDialogReadsExisting() throws InterruptedException {
        SpaceshipDesign base = sampleDesign();
        SpaceshipDesign concealedDerelict = new SpaceshipDesign(
                base.id(),
                base.name(),
                base.designation(),
                base.shipClass(),
                base.driveType(),
                base.massBudget(),
                base.crewComplement(),
                base.lengthMeters(),
                base.carriedCraft(),
                base.armaments(),
                base.iconPath(),
                base.description(),
                base.sourceType(),
                base.sourceUniverse(),
                base.faction(),
                true,
                OperationalState.DERELICT,
                base.era(),
                base.createdAt());

        onFx(() -> {
            SpaceshipEditorDialog dialog = new SpaceshipEditorDialog(concealedDerelict);

            Optional<CheckBox> concealed = findControlByLabel(dialog.getDialogPane(), "Concealed", CheckBox.class);
            Optional<ComboBox> operational = findControlByLabel(dialog.getDialogPane(), "Operational state", ComboBox.class);

            assertTrue(concealed.isPresent());
            assertTrue(operational.isPresent());
            assertTrue(concealed.get().isSelected(), "concealed=true should round-trip into the checkbox");
            assertEquals(OperationalState.DERELICT, operational.get().getValue(),
                    "operationalState=DERELICT should round-trip into the combo");
        });
    }

    @Test
    @DisplayName("Mutating controls and invoking the save converter produces a design carrying both values")
    @SuppressWarnings("unchecked")
    void mutateThenSaveProducesUpdatedDesign() throws InterruptedException {
        SpaceshipDesign base = sampleDesign();
        AtomicReference<SpaceshipDesign> saved = new AtomicReference<>();

        onFx(() -> {
            SpaceshipEditorDialog dialog = new SpaceshipEditorDialog(base);

            Optional<CheckBox> concealed = findControlByLabel(dialog.getDialogPane(), "Concealed", CheckBox.class);
            Optional<ComboBox> operational = findControlByLabel(dialog.getDialogPane(), "Operational state", ComboBox.class);
            assertTrue(concealed.isPresent());
            assertTrue(operational.isPresent());

            concealed.get().setSelected(true);
            operational.get().setValue(OperationalState.UNDER_CONSTRUCTION);

            SpaceshipDesign result = dialog.getResultConverter().call(ButtonType.OK);
            assertNotNull(result, "OK converter should produce a SpaceshipDesign for a valid draft");
            saved.set(result);
        });

        SpaceshipDesign result = saved.get();
        assertEquals(base.id(), result.id(), "edit path preserves the original id");
        assertTrue(result.concealed(), "save path carries concealed=true");
        assertEquals(OperationalState.UNDER_CONSTRUCTION, result.operationalState(),
                "save path carries operationalState=UNDER_CONSTRUCTION");
    }
}
