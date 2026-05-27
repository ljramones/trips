package com.teamgannon.trips.javafxsupport;

import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins {@link ResponsiveLayouts}: column-width bindings track the table's
 * width, illegal argument counts and types fail loudly. Issue 34.
 */
class ResponsiveLayoutsTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { });
            javaFxInitialized = true;
        } catch (IllegalStateException already) {
            javaFxInitialized = true;
        } catch (Exception e) {
            javaFxInitialized = false;
        }
    }

    @Nested
    @DisplayName("bindColumnPercentages")
    class BindColumnPercentages {

        @Test
        @DisplayName("columns track the table width proportionally")
        void columnsTrackWidth() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<double[]> widths = new AtomicReference<>();

            runOnFx(() -> {
                TableView<Object> table = new TableView<>();
                TableColumn<Object, String> a = new TableColumn<>("A");
                TableColumn<Object, String> b = new TableColumn<>("B");
                TableColumn<Object, String> c = new TableColumn<>("C");
                table.getColumns().addAll(a, b, c);

                ResponsiveLayouts.bindColumnPercentages(table,
                        a, 0.50,
                        b, 0.30,
                        c, 0.20);

                table.resize(1000, 400);
                // Columns are bound; their prefWidth should now reflect the percentages.
                widths.set(new double[]{
                        a.getPrefWidth(), b.getPrefWidth(), c.getPrefWidth()
                });
            });

            assertEquals(500.0, widths.get()[0], 0.5);
            assertEquals(300.0, widths.get()[1], 0.5);
            assertEquals(200.0, widths.get()[2], 0.5);
        }

        @Test
        @DisplayName("rejects an odd number of arguments")
        void rejectsOddArgCount() {
            TableView<Object> table = new TableView<>();
            TableColumn<Object, String> col = new TableColumn<>();
            assertThrows(IllegalArgumentException.class,
                    () -> ResponsiveLayouts.bindColumnPercentages(table, col, 0.5, col));
        }

        @Test
        @DisplayName("rejects non-TableColumn at column position")
        void rejectsNonColumnArg() {
            TableView<Object> table = new TableView<>();
            assertThrows(IllegalArgumentException.class,
                    () -> ResponsiveLayouts.bindColumnPercentages(table, "not-a-column", 0.5));
        }

        @Test
        @DisplayName("rejects non-Number at percentage position")
        void rejectsNonNumberArg() {
            TableView<Object> table = new TableView<>();
            TableColumn<Object, String> col = new TableColumn<>();
            assertThrows(IllegalArgumentException.class,
                    () -> ResponsiveLayouts.bindColumnPercentages(table, col, "fifty"));
        }
    }

    @Nested
    @DisplayName("fill helpers")
    class FillHelpers {

        @Test
        @DisplayName("fillVerticalSpace sets VBox.vgrow = ALWAYS")
        void verticalGrow() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
            AtomicReference<javafx.scene.layout.Priority> got = new AtomicReference<>();
            runOnFx(() -> {
                TableView<Object> table = new TableView<>();
                ResponsiveLayouts.fillVerticalSpace(table);
                got.set(javafx.scene.layout.VBox.getVgrow(table));
            });
            assertEquals(javafx.scene.layout.Priority.ALWAYS, got.get());
        }

        @Test
        @DisplayName("fillHorizontalSpace sets HBox.hgrow = ALWAYS")
        void horizontalGrow() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
            AtomicReference<javafx.scene.layout.Priority> got = new AtomicReference<>();
            runOnFx(() -> {
                TableView<Object> table = new TableView<>();
                ResponsiveLayouts.fillHorizontalSpace(table);
                got.set(javafx.scene.layout.HBox.getHgrow(table));
            });
            assertEquals(javafx.scene.layout.Priority.ALWAYS, got.get());
        }
    }

    // ---- helpers ----

    private static void runOnFx(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                err.set(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX action timed out");
        if (err.get() != null) throw err.get();
    }
}
