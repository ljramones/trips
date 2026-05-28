package com.teamgannon.trips.accessibility;

import com.teamgannon.trips.dialogs.solarsystem.AddPlanetDialog;
import com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipEditorDialog;
import com.teamgannon.trips.test.TestFXBase;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DialogAccessibilityPolishTest extends TestFXBase {

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Group(), 1, 1));
        stage.show();
    }

    @Test
    @DisplayName("AddPlanetDialog annotates interactive controls for assistive tech")
    void addPlanetDialogAnnotatesInteractiveControls() {
        AtomicReference<AddPlanetDialog> ref = new AtomicReference<>();
        interact(() -> ref.set(new AddPlanetDialog(null, false, null)));

        assertInteractiveControlsAnnotated(ref.get());
    }

    @Test
    @DisplayName("SpaceshipEditorDialog annotates interactive controls for assistive tech")
    void spaceshipEditorDialogAnnotatesInteractiveControls() {
        AtomicReference<SpaceshipEditorDialog> ref = new AtomicReference<>();
        interact(() -> ref.set(new SpaceshipEditorDialog(null)));

        assertInteractiveControlsAnnotated(ref.get());
    }

    private static void assertInteractiveControlsAnnotated(Dialog<?> dialog) {
        List<Node> nodes = new ArrayList<>();
        if (dialog.getDialogPane().getContent() != null) {
            nodes.addAll(descendants(dialog.getDialogPane().getContent()));
        }
        for (ButtonType buttonType : dialog.getDialogPane().getButtonTypes()) {
            Node button = dialog.getDialogPane().lookupButton(buttonType);
            if (button != null) {
                nodes.add(button);
            }
        }

        List<Node> controls = nodes.stream()
                .filter(DialogAccessibilityPolishTest::isInteractiveControl)
                .toList();

        assertFalse(controls.isEmpty(), "test should find interactive controls");
        assertAll(controls.stream().map(control -> () -> {
            String accessibleText = control.getAccessibleText();
            assertFalse(accessibleText == null || accessibleText.isBlank(),
                    () -> "missing accessible text on " + describe(control));
        }));
    }

    private static List<Node> descendants(Node root) {
        List<Node> out = new ArrayList<>();
        collect(root, out);
        return out;
    }

    private static void collect(Node node, List<Node> out) {
        out.add(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, out);
            }
        }
    }

    private static boolean isInteractiveControl(Node node) {
        return node instanceof TextInputControl
                || node instanceof ComboBoxBase<?>
                || node instanceof ButtonBase
                || node instanceof TableView<?>
                || node instanceof ListView<?>
                || node instanceof TabPane;
    }

    private static String describe(Node node) {
        if (node instanceof Control control && control.getTooltip() != null) {
            return node.getClass().getSimpleName() + "[" + control.getTooltip().getText() + "]";
        }
        return node.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(node));
    }
}
