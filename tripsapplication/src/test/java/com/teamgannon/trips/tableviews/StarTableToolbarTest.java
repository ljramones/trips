package com.teamgannon.trips.tableviews;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarTableToolbar.
 * Most tests require JavaFX to be initialized.
 */
class StarTableToolbarTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available, StarTableToolbar tests will be skipped: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @Nested
    @DisplayName("Toolbar construction tests")
    class ToolbarConstructionTests {

        @Test
        @DisplayName("should create toolbar with children")
        void shouldCreateToolbarWithChildren() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<StarTableToolbar> toolbarRef = new AtomicReference<>();
            runOnFxThread(() -> {
                toolbarRef.set(createToolbar());
            });

            assertNotNull(toolbarRef.get());
            assertFalse(toolbarRef.get().getChildren().isEmpty());
        }

        @Test
        @DisplayName("should have columns menu")
        void shouldHaveColumnsMenu() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<MenuButton> menuRef = new AtomicReference<>();
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                menuRef.set(toolbar.getColumnsMenu());
            });

            assertNotNull(menuRef.get());
            assertEquals("Columns...", menuRef.get().getText());
        }

        @Test
        @DisplayName("should contain buttons")
        void shouldContainButtons() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Long> buttonCountRef = new AtomicReference<>();
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                long count = toolbar.getChildren().stream()
                        .filter(node -> node instanceof Button)
                        .count();
                buttonCountRef.set(count);
            });

            assertTrue(buttonCountRef.get() >= 3); // Add Star, Export Page, Export All
        }

        @Test
        @DisplayName("should contain separators")
        void shouldContainSeparators() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Long> separatorCountRef = new AtomicReference<>();
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                long count = toolbar.getChildren().stream()
                        .filter(node -> node instanceof Separator)
                        .count();
                separatorCountRef.set(count);
            });

            assertTrue(separatorCountRef.get() >= 2);
        }
    }

    @Nested
    @DisplayName("Button action tests")
    class ButtonActionTests {

        @Test
        @DisplayName("Add Star button should trigger callback")
        void addStarButtonShouldTriggerCallback() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean addStarCalled = new AtomicBoolean(false);

            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar(
                        () -> addStarCalled.set(true),
                        () -> {},
                        () -> {}
                );
                Button addButton = findButtonByText(toolbar, "Add Star");
                if (addButton != null) {
                    addButton.fire();
                }
            });

            assertTrue(addStarCalled.get());
        }

        @Test
        @DisplayName("Export Page button should trigger callback")
        void exportPageButtonShouldTriggerCallback() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean exportPageCalled = new AtomicBoolean(false);

            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar(
                        () -> {},
                        () -> exportPageCalled.set(true),
                        () -> {}
                );
                Button button = findButtonByText(toolbar, "Export Page CSV");
                if (button != null) {
                    button.fire();
                }
            });

            assertTrue(exportPageCalled.get());
        }

        @Test
        @DisplayName("Export All button should trigger callback")
        void exportAllButtonShouldTriggerCallback() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean exportAllCalled = new AtomicBoolean(false);

            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar(
                        () -> {},
                        () -> {},
                        () -> exportAllCalled.set(true)
                );
                Button button = findButtonByText(toolbar, "Export All CSV");
                if (button != null) {
                    button.fire();
                }
            });

            assertTrue(exportAllCalled.get());
        }
    }

    @Nested
    @DisplayName("Columns menu tests")
    class ColumnsMenuTests {

        @Test
        @DisplayName("should have menu items for all columns")
        void shouldHaveMenuItemsForAllColumns() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Long> checkMenuItemCountRef = new AtomicReference<>();
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                MenuButton columnsMenu = toolbar.getColumnsMenu();
                long count = columnsMenu.getItems().stream()
                        .filter(item -> item instanceof CheckMenuItem)
                        .count();
                checkMenuItemCountRef.set(count);
            });

            assertEquals(StarTableColumnConfig.getAllColumnIds().size(), checkMenuItemCountRef.get().intValue());
        }

        @Test
        @DisplayName("should have Reset to Defaults menu item")
        void shouldHaveResetToDefaultsMenuItem() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean hasResetItem = new AtomicBoolean(false);
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                MenuButton columnsMenu = toolbar.getColumnsMenu();
                boolean found = columnsMenu.getItems().stream()
                        .filter(item -> item instanceof MenuItem && !(item instanceof CheckMenuItem))
                        .filter(item -> !(item instanceof SeparatorMenuItem))
                        .anyMatch(item -> "Reset to Defaults".equals(item.getText()));
                hasResetItem.set(found);
            });

            assertTrue(hasResetItem.get());
        }

        @Test
        @DisplayName("should have separator before Reset item")
        void shouldHaveSeparatorBeforeResetItem() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean hasSeparator = new AtomicBoolean(false);
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                MenuButton columnsMenu = toolbar.getColumnsMenu();
                boolean found = columnsMenu.getItems().stream()
                        .anyMatch(item -> item instanceof SeparatorMenuItem);
                hasSeparator.set(found);
            });

            assertTrue(hasSeparator.get());
        }
    }

    @Nested
    @DisplayName("Page navigation tests")
    class PageNavigationTests {

        @Test
        @DisplayName("should contain page navigation label")
        void shouldContainPageNavigationLabel() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicBoolean hasLabel = new AtomicBoolean(false);
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                boolean found = toolbar.getChildren().stream()
                        .filter(node -> node instanceof Label)
                        .map(node -> (Label) node)
                        .anyMatch(label -> "Go to page:".equals(label.getText()));
                hasLabel.set(found);
            });

            assertTrue(hasLabel.get());
        }

        @Test
        @DisplayName("should contain page navigation text field")
        void shouldContainPageNavigationTextField() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Long> textFieldCount = new AtomicReference<>();
            runOnFxThread(() -> {
                StarTableToolbar toolbar = createToolbar();
                long count = toolbar.getChildren().stream()
                        .filter(node -> node instanceof TextField)
                        .count();
                textFieldCount.set(count);
            });

            assertEquals(1, textFieldCount.get().intValue());
        }
    }

    // Helper methods

    private StarTableToolbar createToolbar() {
        return createToolbar(() -> {}, () -> {}, () -> {});
    }

    private StarTableToolbar createToolbar(Runnable onAddStar, Runnable onExportPage, Runnable onExportAll) {
        StarTableColumnConfig columnConfig = StarTableColumnConfig.defaults();
        StarTableColumnFactory columnFactory = new StarTableColumnFactory();
        TableView<StarEditRecord> tableView = new TableView<>();
        Pagination pagination = new Pagination();
        pagination.setPageCount(10);

        columnFactory.configureColumns(tableView);

        return new StarTableToolbar(
                columnConfig,
                columnFactory,
                tableView,
                pagination,
                onAddStar,
                onExportPage,
                onExportAll
        );
    }

    private Button findButtonByText(StarTableToolbar toolbar, String text) {
        return toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(button -> text.equals(button.getText()))
                .findFirst()
                .orElse(null);
    }

    private void runOnFxThread(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }
    }
}
