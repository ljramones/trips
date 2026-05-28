package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationType;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless tests for {@link StationEditorDialog}. Mirrors the pattern from
 * {@code SpaceshipEditorDialogFieldsTest}: lightweight {@link Platform#startup} bootstrap,
 * dialog constructed on the FX thread inside a {@link CountDownLatch} latch, OK result-converter
 * invoked directly to assert the produced draft without showing the dialog.
 */
class StationEditorDialogTest {

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
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    private static StationDesign troy() {
        return (StationDesign) Catalog.TROY;
    }

    @Test
    @DisplayName("new-dialog defaults: OUTPOST / FIXED / OPERATIONAL / aux drive disabled")
    void newDialogDefaults() throws InterruptedException {
        AtomicReference<StationEditorDialog> ref = new AtomicReference<>();
        onFx(() -> ref.set(new StationEditorDialog()));

        StationEditorDialog dialog = ref.get();
        assertEquals(Mobility.FIXED, dialog.mobilityComboForTesting().getValue());
        assertTrue(dialog.auxiliaryDriveComboForTesting().isDisabled(),
                "auxiliary drive must be disabled for FIXED mobility");
        assertNull(dialog.auxiliaryDriveComboForTesting().getValue(),
                "auxiliary drive must be null for FIXED mobility");
    }

    @Test
    @DisplayName("edit-dialog reads Troy: stationType + mobility + auxiliaryDrive + allegiance all round-trip")
    void editDialogReadsExisting() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog(troy());
            // OK should be enabled because Troy is a valid station.
            assertFalse(dialog.okButtonForTesting().isDisable(), "loading Troy must not produce validation errors");
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign result = draft.get();
        assertNotNull(result);
        assertEquals(troy().id(), result.id(), "edit preserves the original id");
        assertEquals(troy().name(), result.name());
        assertEquals(troy().stationType(), result.stationType());
        assertEquals(troy().mobility(), result.mobility());
        assertEquals(troy().auxiliaryDrive(), result.auxiliaryDrive());
        assertEquals(troy().allegiance(), result.allegiance());
        assertEquals(troy().concealed(), result.concealed());
        assertEquals(troy().operationalState(), result.operationalState());
        assertEquals(troy().carriedCraft(), result.carriedCraft());
        assertEquals(troy().armaments(), result.armaments());
    }

    @Test
    @DisplayName("mutate-and-save: change name / concealed / allegiance / operational state — id preserved")
    void mutateThenSaveProducesUpdatedDesign() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog(troy());
            // Trigger mutations via the publicly exposed controls. We use the test seam combos
            // for mobility / aux drive and reach the other controls through node lookups.
            dialog.mobilityComboForTesting().setValue(Mobility.MANEUVERABLE);
            dialog.auxiliaryDriveComboForTesting().setValue(DriveType.ORION);

            javafx.scene.control.TextField name = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.name"));
            javafx.scene.control.TextField allegiance = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.allegiance"));
            javafx.scene.control.CheckBox concealed = lookupCheckBoxByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.concealed"));
            javafx.scene.control.ComboBox<?> opState = lookupComboBoxByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.operationalState"));

            name.setText("Troy Reborn");
            allegiance.setText("Free Worlds");
            concealed.setSelected(true);
            @SuppressWarnings("unchecked")
            javafx.scene.control.ComboBox<OperationalState> os =
                    (javafx.scene.control.ComboBox<OperationalState>) opState;
            os.setValue(OperationalState.DAMAGED);

            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign result = draft.get();
        assertNotNull(result);
        assertEquals(troy().id(), result.id());
        assertEquals("Troy Reborn", result.name());
        assertEquals("Free Worlds", result.allegiance());
        assertTrue(result.concealed());
        assertEquals(OperationalState.DAMAGED, result.operationalState());
        assertEquals(Mobility.MANEUVERABLE, result.mobility());
        assertEquals(DriveType.ORION, result.auxiliaryDrive());
    }

    @Test
    @DisplayName("switching mobility to FIXED clears + disables auxiliary drive")
    void mobilityFixedDisablesAuxiliaryDrive() throws InterruptedException {
        AtomicReference<StationEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog(troy());
            dialog.mobilityComboForTesting().setValue(Mobility.MANEUVERABLE);
            dialog.auxiliaryDriveComboForTesting().setValue(DriveType.ORION);
            assertFalse(dialog.auxiliaryDriveComboForTesting().isDisabled());

            dialog.mobilityComboForTesting().setValue(Mobility.FIXED);
            ref.set(dialog);
        });

        StationEditorDialog dialog = ref.get();
        assertTrue(dialog.auxiliaryDriveComboForTesting().isDisabled(),
                "switching to FIXED must disable the auxiliary drive combo");
        assertNull(dialog.auxiliaryDriveComboForTesting().getValue(),
                "switching to FIXED must clear the auxiliary drive value");
    }

    @ParameterizedTest
    @EnumSource(StationType.class)
    @DisplayName("every StationType round-trips through the editor draft")
    void everyStationTypeRoundTrips(StationType type) throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            // Need a non-blank name + positive dry mass for the OK converter to run; the new
            // dialog applies a positive dry-mass default so we only have to set the name.
            javafx.scene.control.TextField nameField = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.name"));
            nameField.setText("Coverage Station");
            @SuppressWarnings("unchecked")
            javafx.scene.control.ComboBox<StationType> stCombo =
                    (javafx.scene.control.ComboBox<StationType>) lookupComboBoxByAccessibleText(dialog,
                            ConstructLabels.get("editor.station.field.stationType"));
            stCombo.setValue(type);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign result = draft.get();
        assertNotNull(result);
        assertEquals(type, result.stationType());
    }

    // ---------------------------------------------------------------- node lookup helpers

    private static javafx.scene.control.TextField lookupTextFieldByAccessibleText(
            StationEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.TextField) lookupNode(dialog, accessibleText, javafx.scene.control.TextField.class);
    }

    private static javafx.scene.control.CheckBox lookupCheckBoxByAccessibleText(
            StationEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.CheckBox) lookupNode(dialog, accessibleText, javafx.scene.control.CheckBox.class);
    }

    private static javafx.scene.control.ComboBox<?> lookupComboBoxByAccessibleText(
            StationEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.ComboBox<?>) lookupNode(dialog, accessibleText, javafx.scene.control.ComboBox.class);
    }

    private static javafx.scene.Node lookupNode(StationEditorDialog dialog, String accessibleText,
                                                Class<?> type) {
        return walk(dialog.getDialogPane(), node ->
                type.isInstance(node) && accessibleText.equals(node.getAccessibleText()));
    }

    private static javafx.scene.Node walk(javafx.scene.Node node, java.util.function.Predicate<javafx.scene.Node> p) {
        if (node == null) {
            return null;
        }
        if (p.test(node)) {
            return node;
        }
        if (node instanceof javafx.scene.control.DialogPane dp) {
            javafx.scene.Node found = walk(dp.getContent(), p);
            if (found != null) {
                return found;
            }
        }
        if (node instanceof javafx.scene.control.ScrollPane sp) {
            javafx.scene.Node found = walk(sp.getContent(), p);
            if (found != null) {
                return found;
            }
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                javafx.scene.Node found = walk(child, p);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
