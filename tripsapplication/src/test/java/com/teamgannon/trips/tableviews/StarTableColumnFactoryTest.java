package com.teamgannon.trips.tableviews;

import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarTableColumnFactory.
 */
class StarTableColumnFactoryTest {

    private static boolean javaFxInitialized = false;
    private StarTableColumnFactory factory;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available, some tests will be skipped: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        factory = new StarTableColumnFactory();
    }

    @Nested
    @DisplayName("Column map initialization tests")
    class ColumnMapInitializationTests {

        @Test
        @DisplayName("should initialize column map with all columns")
        void shouldInitializeColumnMapWithAllColumns() {
            Map<String, TableColumn<StarEditRecord, ?>> columnMap = factory.getColumnMap();

            assertNotNull(columnMap);
            assertFalse(columnMap.isEmpty());
        }

        @Test
        @DisplayName("should contain main visible columns")
        void shouldContainMainVisibleColumns() {
            Map<String, TableColumn<StarEditRecord, ?>> columnMap = factory.getColumnMap();

            assertTrue(columnMap.containsKey("displayName"));
            assertTrue(columnMap.containsKey("distanceToEarth"));
            assertTrue(columnMap.containsKey("spectra"));
            assertTrue(columnMap.containsKey("radius"));
            assertTrue(columnMap.containsKey("mass"));
            assertTrue(columnMap.containsKey("luminosity"));
            assertTrue(columnMap.containsKey("ra"));
            assertTrue(columnMap.containsKey("declination"));
            assertTrue(columnMap.containsKey("parallax"));
            assertTrue(columnMap.containsKey("xCoord"));
            assertTrue(columnMap.containsKey("yCoord"));
            assertTrue(columnMap.containsKey("zCoord"));
            assertTrue(columnMap.containsKey("real"));
            assertTrue(columnMap.containsKey("comment"));
        }

        @Test
        @DisplayName("should contain hidden columns")
        void shouldContainHiddenColumns() {
            Map<String, TableColumn<StarEditRecord, ?>> columnMap = factory.getColumnMap();

            assertTrue(columnMap.containsKey("commonName"));
            assertTrue(columnMap.containsKey("constellationName"));
            assertTrue(columnMap.containsKey("polity"));
            assertTrue(columnMap.containsKey("temperature"));
        }

        @Test
        @DisplayName("should have 18 total columns")
        void shouldHave18TotalColumns() {
            Map<String, TableColumn<StarEditRecord, ?>> columnMap = factory.getColumnMap();

            assertEquals(18, columnMap.size());
        }
    }

    @Nested
    @DisplayName("GetColumn tests")
    class GetColumnTests {

        @Test
        @DisplayName("should return column for valid ID")
        void shouldReturnColumnForValidId() {
            TableColumn<StarEditRecord, ?> column = factory.getColumn("displayName");

            assertNotNull(column);
            assertEquals("Display Name", column.getText());
        }

        @Test
        @DisplayName("should return null for invalid ID")
        void shouldReturnNullForInvalidId() {
            TableColumn<StarEditRecord, ?> column = factory.getColumn("invalidColumn");

            assertNull(column);
        }

        @Test
        @DisplayName("should return correct column for each ID")
        void shouldReturnCorrectColumnForEachId() {
            assertEquals("Distance (LY)", factory.getColumn("distanceToEarth").getText());
            assertEquals("Spectra", factory.getColumn("spectra").getText());
            assertEquals("X", factory.getColumn("xCoord").getText());
            assertEquals("Y", factory.getColumn("yCoord").getText());
            assertEquals("Z", factory.getColumn("zCoord").getText());
        }
    }

    @Nested
    @DisplayName("GetColumnId tests")
    class GetColumnIdTests {

        @Test
        @DisplayName("should return ID for known column")
        void shouldReturnIdForKnownColumn() {
            TableColumn<StarEditRecord, ?> column = factory.getColumn("displayName");
            String id = factory.getColumnId(column);

            assertEquals("displayName", id);
        }

        @Test
        @DisplayName("should return null for unknown column")
        void shouldReturnNullForUnknownColumn() {
            TableColumn<StarEditRecord, String> unknownColumn = new TableColumn<>("Unknown");
            String id = factory.getColumnId(unknownColumn);

            assertNull(id);
        }
    }

    @Nested
    @DisplayName("Column visibility tests")
    class ColumnVisibilityTests {

        @Test
        @DisplayName("should set column visible")
        void shouldSetColumnVisible() {
            factory.setColumnVisible("commonName", true);

            assertTrue(factory.isColumnVisible("commonName"));
        }

        @Test
        @DisplayName("should set column invisible")
        void shouldSetColumnInvisible() {
            factory.setColumnVisible("displayName", false);

            assertFalse(factory.isColumnVisible("displayName"));
        }

        @Test
        @DisplayName("should handle invalid column ID gracefully")
        void shouldHandleInvalidColumnIdGracefully() {
            // Should not throw
            assertDoesNotThrow(() -> factory.setColumnVisible("invalidColumn", true));
            assertFalse(factory.isColumnVisible("invalidColumn"));
        }
    }

    @Nested
    @DisplayName("ConfigureColumns tests (requires JavaFX)")
    class ConfigureColumnsTests {

        @Test
        @DisplayName("should add all columns to table view")
        void shouldAddAllColumnsToTableView() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Integer> columnCount = new AtomicReference<>();
            runOnFxThread(() -> {
                TableView<StarEditRecord> tableView = new TableView<>();
                factory.configureColumns(tableView);
                columnCount.set(tableView.getColumns().size());
            });

            assertEquals(18, columnCount.get());
        }

        @Test
        @DisplayName("should hide optional columns by default")
        void shouldHideOptionalColumnsByDefault() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                TableView<StarEditRecord> tableView = new TableView<>();
                factory.configureColumns(tableView);
            });

            // commonName, constellationName, polity, temperature should be hidden
            assertFalse(factory.isColumnVisible("commonName"));
            assertFalse(factory.isColumnVisible("constellationName"));
            assertFalse(factory.isColumnVisible("polity"));
            assertFalse(factory.isColumnVisible("temperature"));
        }

        @Test
        @DisplayName("should show main columns by default")
        void shouldShowMainColumnsByDefault() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                TableView<StarEditRecord> tableView = new TableView<>();
                factory.configureColumns(tableView);
            });

            assertTrue(factory.isColumnVisible("displayName"));
            assertTrue(factory.isColumnVisible("distanceToEarth"));
            assertTrue(factory.isColumnVisible("spectra"));
        }
    }

    @Nested
    @DisplayName("Column text tests")
    class ColumnTextTests {

        @Test
        @DisplayName("mass column should have solar mass symbol")
        void massColumnShouldHaveSolarMassSymbol() {
            TableColumn<StarEditRecord, ?> massCol = factory.getColumn("mass");

            assertTrue(massCol.getText().contains("M"));
            assertTrue(massCol.getText().contains("\u2609")); // Solar symbol
        }

        @Test
        @DisplayName("coordinate columns should have short names")
        void coordinateColumnsShouldHaveShortNames() {
            assertEquals("X", factory.getColumn("xCoord").getText());
            assertEquals("Y", factory.getColumn("yCoord").getText());
            assertEquals("Z", factory.getColumn("zCoord").getText());
        }
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
