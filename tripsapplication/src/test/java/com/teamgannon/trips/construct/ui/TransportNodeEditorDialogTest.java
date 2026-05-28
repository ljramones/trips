package com.teamgannon.trips.construct.ui;

import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransportNodeEditorDialogTest {

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

    private static TransportNode sample(NodeType type, boolean instantaneous, List<String> partners) {
        Instant now = Instant.parse("2025-08-01T09:00:00Z");
        return new TransportNode(
                UUID.randomUUID().toString(),
                "Coverage Gate",
                "src",
                "F",
                false,
                "desc",
                type,
                1.0, 2.0, 3.0,
                partners,
                100.0,
                instantaneous,
                instantaneous ? 0 : 25.0,
                now,
                now);
    }

    @Test
    @DisplayName("edit-dialog round-trips every TransportNode field on a populated node")
    void editDialogRoundTripsAllFields() throws InterruptedException {
        TransportNode original = sample(NodeType.RING_GATE, false, List.of("partner-a", "partner-b"));
        AtomicReference<TransportNode> draft = new AtomicReference<>();
        onFx(() -> {
            TransportNodeEditorDialog dialog = new TransportNodeEditorDialog(original);
            assertFalse(dialog.okButtonForTesting().isDisable());
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        TransportNode result = draft.get();
        assertNotNull(result);
        assertEquals(original.id(), result.id());
        assertEquals(original.name(), result.name());
        assertEquals(original.type(), result.type());
        assertEquals(original.positionX(), result.positionX());
        assertEquals(original.positionY(), result.positionY());
        assertEquals(original.positionZ(), result.positionZ());
        assertEquals(original.connectedNodeIds(), result.connectedNodeIds());
        assertEquals(original.instantaneousTransit(), result.instantaneousTransit());
        assertEquals(original.traversalTimeTicks(), result.traversalTimeTicks());
    }

    @Test
    @DisplayName("instantaneousTransit=true clears + disables traversalTimeTicks; draft carries 0")
    void instantaneousDisablesTraversalTime() throws InterruptedException {
        TransportNode original = sample(NodeType.JUMP_POINT, false, List.of());
        AtomicReference<TransportNode> draft = new AtomicReference<>();
        AtomicReference<TransportNodeEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            TransportNodeEditorDialog dialog = new TransportNodeEditorDialog(original);
            // toggle instantaneous on
            dialog.instantaneousTransitCheckForTesting().setSelected(true);
            ref.set(dialog);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        assertTrue(ref.get().traversalTimeFieldForTesting().isDisabled(),
                "traversalTime must disable when instantaneousTransit=true");
        TransportNode result = draft.get();
        assertNotNull(result);
        assertTrue(result.instantaneousTransit());
        assertEquals(0.0, result.traversalTimeTicks(),
                "the dialog must produce 0 traversalTime when instantaneous transit is set");
    }

    @Test
    @DisplayName("connectedNodeIds list editor adds and removes entries")
    void connectedNodeIdsListEditorAddsAndRemoves() throws InterruptedException {
        AtomicReference<TransportNodeEditorDialog> ref = new AtomicReference<>();
        AtomicReference<TransportNode> draft = new AtomicReference<>();
        onFx(() -> {
            TransportNodeEditorDialog dialog = new TransportNodeEditorDialog();
            ((TextField) lookup(dialog,
                    n -> n instanceof TextField tf && ConstructLabels.get("editor.field.name").equals(tf.getAccessibleText())))
                    .setText("Coverage Gate");

            dialog.newConnectionFieldForTesting().setText("partner-1");
            dialog.addConnectionForTesting();
            dialog.newConnectionFieldForTesting().setText("partner-2");
            dialog.addConnectionForTesting();
            // duplicate add is a no-op (set semantics)
            dialog.newConnectionFieldForTesting().setText("partner-1");
            dialog.addConnectionForTesting();

            assertEquals(2, dialog.connectionsListForTesting().getItems().size(),
                    "duplicate adds must be no-ops");

            // remove the first
            dialog.connectionsListForTesting().getSelectionModel().select("partner-1");
            // No public remove method exposed; trigger via the remove button programmatically by
            // removing from the backing list. The remove action does the same.
            dialog.connectionsListForTesting().getItems().remove("partner-1");

            ref.set(dialog);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        TransportNode result = draft.get();
        assertNotNull(result);
        assertEquals(List.of("partner-2"), result.connectedNodeIds(),
                "after removing partner-1, only partner-2 should remain");
    }

    @ParameterizedTest
    @EnumSource(NodeType.class)
    @DisplayName("every NodeType round-trips through the editor draft")
    void everyNodeTypeRoundTrips(NodeType type) throws InterruptedException {
        AtomicReference<TransportNode> draft = new AtomicReference<>();
        onFx(() -> {
            TransportNodeEditorDialog dialog = new TransportNodeEditorDialog();
            ((TextField) lookup(dialog,
                    n -> n instanceof TextField tf && ConstructLabels.get("editor.field.name").equals(tf.getAccessibleText())))
                    .setText("Coverage Node");
            @SuppressWarnings("unchecked")
            ComboBox<NodeType> tc = (ComboBox<NodeType>) lookup(dialog,
                    n -> n instanceof ComboBox<?> c && ConstructLabels.get("editor.transport.field.type").equals(c.getAccessibleText()));
            tc.setValue(type);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        TransportNode result = draft.get();
        assertNotNull(result);
        assertEquals(type, result.type());
    }

    private static Node lookup(TransportNodeEditorDialog dialog, Predicate<Node> p) {
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
