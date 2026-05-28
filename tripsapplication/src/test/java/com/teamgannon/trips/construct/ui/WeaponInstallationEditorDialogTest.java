package com.teamgannon.trips.construct.ui;

import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.WeaponInstallation;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponInstallationEditorDialogTest {

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

    @Test
    @DisplayName("edit-dialog reads Catalog.SAPL — every field round-trips")
    void editDialogReadsSAPL() throws InterruptedException {
        WeaponInstallation sapl = (WeaponInstallation) Catalog.SAPL;
        AtomicReference<WeaponInstallation> draft = new AtomicReference<>();
        onFx(() -> {
            WeaponInstallationEditorDialog dialog = new WeaponInstallationEditorDialog(sapl);
            assertFalse(dialog.okButtonForTesting().isDisable(), "SAPL must validate cleanly");
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        WeaponInstallation result = draft.get();
        assertNotNull(result);
        assertEquals(sapl.id(), result.id());
        assertEquals(sapl.installationType(), result.installationType());
        assertEquals(sapl.emplacement(), result.emplacement());
        assertEquals(sapl.mobile(), result.mobile());
        assertEquals(sapl.armaments(), result.armaments());
        assertEquals(sapl.operationalState(), result.operationalState());
    }

    @Test
    @DisplayName("edit-dialog reads Catalog.SHEVA_GUN — mobile=true round-trips")
    void editDialogReadsShevaGun() throws InterruptedException {
        WeaponInstallation sheva = (WeaponInstallation) Catalog.SHEVA_GUN;
        AtomicReference<WeaponInstallation> draft = new AtomicReference<>();
        onFx(() -> {
            WeaponInstallationEditorDialog dialog = new WeaponInstallationEditorDialog(sheva);
            assertFalse(dialog.okButtonForTesting().isDisable());
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        WeaponInstallation result = draft.get();
        assertNotNull(result);
        assertEquals(sheva.id(), result.id());
        assertTrue(result.mobile(), "SheVa is ground-mobile; mobile=true must round-trip");
    }

    @Test
    @DisplayName("mutate-and-save: concealed + operational state + mobile changes all flow through draft")
    void mutateThenSave() throws InterruptedException {
        WeaponInstallation sapl = (WeaponInstallation) Catalog.SAPL;
        AtomicReference<WeaponInstallation> draft = new AtomicReference<>();
        onFx(() -> {
            WeaponInstallationEditorDialog dialog = new WeaponInstallationEditorDialog(sapl);
            CheckBox concealed = (CheckBox) lookup(dialog,
                    n -> n instanceof CheckBox cb && ConstructLabels.get("editor.field.concealed").equals(cb.getAccessibleText()));
            CheckBox mobile = (CheckBox) lookup(dialog,
                    n -> n instanceof CheckBox cb && ConstructLabels.get("editor.weapon.field.mobile").equals(cb.getAccessibleText()));
            @SuppressWarnings("unchecked")
            ComboBox<OperationalState> opState = (ComboBox<OperationalState>) lookup(dialog,
                    n -> n instanceof ComboBox<?> c && ConstructLabels.get("editor.field.operationalState").equals(c.getAccessibleText()));

            concealed.setSelected(true);
            mobile.setSelected(true);
            opState.setValue(OperationalState.DAMAGED);

            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        WeaponInstallation result = draft.get();
        assertNotNull(result);
        assertEquals(sapl.id(), result.id());
        assertTrue(result.concealed());
        assertTrue(result.mobile());
        assertEquals(OperationalState.DAMAGED, result.operationalState());
    }

    @ParameterizedTest
    @EnumSource(InstallationType.class)
    @DisplayName("every InstallationType round-trips through the editor draft")
    void everyInstallationTypeRoundTrips(InstallationType type) throws InterruptedException {
        AtomicReference<WeaponInstallation> draft = new AtomicReference<>();
        onFx(() -> {
            WeaponInstallationEditorDialog dialog = new WeaponInstallationEditorDialog();
            ((TextField) lookup(dialog,
                    n -> n instanceof TextField tf && ConstructLabels.get("editor.field.name").equals(tf.getAccessibleText())))
                    .setText("Coverage Battery");
            @SuppressWarnings("unchecked")
            ComboBox<InstallationType> typeCombo = (ComboBox<InstallationType>) lookup(dialog,
                    n -> n instanceof ComboBox<?> c && ConstructLabels.get("editor.weapon.field.installationType").equals(c.getAccessibleText()));
            typeCombo.setValue(type);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        WeaponInstallation result = draft.get();
        assertNotNull(result);
        assertEquals(type, result.installationType());
    }

    @ParameterizedTest
    @EnumSource(Emplacement.class)
    @DisplayName("every Emplacement round-trips through the editor draft")
    void everyEmplacementRoundTrips(Emplacement emp) throws InterruptedException {
        AtomicReference<WeaponInstallation> draft = new AtomicReference<>();
        onFx(() -> {
            WeaponInstallationEditorDialog dialog = new WeaponInstallationEditorDialog();
            ((TextField) lookup(dialog,
                    n -> n instanceof TextField tf && ConstructLabels.get("editor.field.name").equals(tf.getAccessibleText())))
                    .setText("Coverage Battery");
            @SuppressWarnings("unchecked")
            ComboBox<Emplacement> empCombo = (ComboBox<Emplacement>) lookup(dialog,
                    n -> n instanceof ComboBox<?> c && ConstructLabels.get("editor.weapon.field.emplacement").equals(c.getAccessibleText()));
            empCombo.setValue(emp);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        WeaponInstallation result = draft.get();
        assertNotNull(result);
        assertEquals(emp, result.emplacement());
    }

    private static Node lookup(WeaponInstallationEditorDialog dialog, Predicate<Node> p) {
        return walk(dialog.getDialogPane(), p);
    }

    private static Node walk(Node node, Predicate<Node> p) {
        if (node == null) {
            return null;
        }
        if (p.test(node)) {
            return node;
        }
        if (node instanceof DialogPane dp) {
            Node found = walk(dp.getContent(), p);
            if (found != null) {
                return found;
            }
        }
        if (node instanceof ScrollPane sp) {
            Node found = walk(sp.getContent(), p);
            if (found != null) {
                return found;
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Node found = walk(child, p);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
