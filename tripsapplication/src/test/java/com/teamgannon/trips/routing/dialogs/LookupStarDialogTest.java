package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(MockitoExtension.class)
class LookupStarDialogTest {

    @Mock
    private DatabaseManagementService databaseManagementService;

    @Mock
    private StarService starService;

    private LookupStarDialog dialog;
    private List<StarObject> testStars;

    @BeforeAll
    static void initToolkit() {
        // Ensure JavaFX toolkit is initialized
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() {
        testStars = createTestStars();
    }

    @Start
    void start(Stage stage) {
        // TestFX start method - called before each test
    }

    @Test
    void testDialogCreation() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                when(starService.findStarsWithName(anyString(), anyString())).thenReturn(testStars);

                dialog = new LookupStarDialog("Alpha", "TestDataset", databaseManagementService, starService);
                assertNotNull(dialog, "Dialog should be created");
                assertTrue(dialog.getTitle().contains("Alpha"), "Title should contain search term");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testTableHasCorrectColumns() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                when(starService.findStarsWithName(anyString(), anyString())).thenReturn(testStars);

                dialog = new LookupStarDialog("Alpha", "TestDataset", databaseManagementService, starService);

                // Get the TableView from the dialog
                TableView<StarObject> tableView = findTableView();
                assertNotNull(tableView, "TableView should exist");

                // Verify expected columns
                List<String> columnNames = tableView.getColumns().stream()
                        .map(TableColumn::getText)
                        .toList();

                assertTrue(columnNames.contains("Display Name"), "Should have Display Name column");
                assertTrue(columnNames.contains("Distance to Earth(ly)"), "Should have Distance column");
                assertTrue(columnNames.contains("Spectra"), "Should have Spectra column");
                assertTrue(columnNames.contains("Radius"), "Should have Radius column");
                assertTrue(columnNames.contains("RA"), "Should have RA column");
                assertTrue(columnNames.contains("Declination"), "Should have Declination column");
                assertTrue(columnNames.contains("Parallax"), "Should have Parallax column");
                assertTrue(columnNames.contains("X"), "Should have X column");
                assertTrue(columnNames.contains("Y"), "Should have Y column");
                assertTrue(columnNames.contains("Z"), "Should have Z column");
                assertTrue(columnNames.contains("Real"), "Should have Real column");
                assertTrue(columnNames.contains("comment"), "Should have comment column");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testDataIsLoaded() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                when(starService.findStarsWithName(anyString(), anyString())).thenReturn(testStars);

                dialog = new LookupStarDialog("Alpha", "TestDataset", databaseManagementService, starService);

                TableView<StarObject> tableView = findTableView();
                assertNotNull(tableView, "TableView should exist");

                // Verify data is loaded (excluding invalid records)
                assertEquals(3, tableView.getItems().size(), "Should have 3 valid stars loaded");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testInvalidRecordsAreFiltered() throws Exception {
        // Add invalid records to test data
        StarObject nullNameStar = new StarObject();
        nullNameStar.setId(UUID.randomUUID().toString());
        nullNameStar.setDisplayName(null);
        testStars.add(nullNameStar);

        StarObject headerRowStar = new StarObject();
        headerRowStar.setId(UUID.randomUUID().toString());
        headerRowStar.setDisplayName("name"); // Header row marker
        testStars.add(headerRowStar);

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                when(starService.findStarsWithName(anyString(), anyString())).thenReturn(testStars);

                dialog = new LookupStarDialog("Alpha", "TestDataset", databaseManagementService, starService);

                TableView<StarObject> tableView = findTableView();
                assertNotNull(tableView, "TableView should exist");

                // Should still only have 3 valid stars (invalid ones filtered out)
                assertEquals(3, tableView.getItems().size(), "Invalid records should be filtered");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testColumnsAreSortable() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                when(starService.findStarsWithName(anyString(), anyString())).thenReturn(testStars);

                dialog = new LookupStarDialog("Alpha", "TestDataset", databaseManagementService, starService);

                TableView<StarObject> tableView = findTableView();
                assertNotNull(tableView, "TableView should exist");

                // Check that sortable columns are sortable
                for (TableColumn<StarObject, ?> column : tableView.getColumns()) {
                    String colName = column.getText();
                    if (!"comment".equals(colName)) {
                        assertTrue(column.isSortable(), "Column " + colName + " should be sortable");
                    }
                }

                // Comment column should not be sortable
                TableColumn<StarObject, ?> commentCol = tableView.getColumns().stream()
                        .filter(c -> "comment".equals(c.getText()))
                        .findFirst()
                        .orElse(null);
                if (commentCol != null) {
                    assertFalse(commentCol.isSortable(), "Comment column should not be sortable");
                }
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @Test
    void testEmptySearchResults() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                when(starService.findStarsWithName(anyString(), anyString())).thenReturn(new ArrayList<>());

                dialog = new LookupStarDialog("NonExistent", "TestDataset", databaseManagementService, starService);

                TableView<StarObject> tableView = findTableView();
                assertNotNull(tableView, "TableView should exist");
                assertEquals(0, tableView.getItems().size(), "Should have no stars for empty search");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
    }

    @SuppressWarnings("unchecked")
    private TableView<StarObject> findTableView() {
        if (dialog == null || dialog.getDialogPane() == null) {
            return null;
        }
        return (TableView<StarObject>) dialog.getDialogPane().getContent()
                .lookup(".table-view");
    }

    private List<StarObject> createTestStars() {
        List<StarObject> stars = new ArrayList<>();

        StarObject star1 = new StarObject();
        star1.setId(UUID.randomUUID().toString());
        star1.setDisplayName("Alpha Centauri");
        star1.setDistance(4.37);
        star1.setSpectralClass("G2V");
        star1.setRadius(1.1);
        star1.setMass(1.1);
        star1.setRa(219.9);
        star1.setDeclination(-60.8);
        star1.setParallax(747.1);
        star1.setX(1.0);
        star1.setY(2.0);
        star1.setZ(-1.0);
        star1.setRealStar(true);
        stars.add(star1);

        StarObject star2 = new StarObject();
        star2.setId(UUID.randomUUID().toString());
        star2.setDisplayName("Barnard's Star");
        star2.setDistance(5.96);
        star2.setSpectralClass("M4V");
        star2.setRadius(0.2);
        star2.setMass(0.14);
        star2.setRa(269.5);
        star2.setDeclination(4.7);
        star2.setParallax(548.3);
        star2.setX(-2.0);
        star2.setY(1.0);
        star2.setZ(3.0);
        star2.setRealStar(true);
        stars.add(star2);

        StarObject star3 = new StarObject();
        star3.setId(UUID.randomUUID().toString());
        star3.setDisplayName("Sirius");
        star3.setDistance(8.6);
        star3.setSpectralClass("A1V");
        star3.setRadius(1.7);
        star3.setMass(2.0);
        star3.setRa(101.3);
        star3.setDeclination(-16.7);
        star3.setParallax(379.2);
        star3.setX(3.0);
        star3.setY(-2.0);
        star3.setZ(1.0);
        star3.setRealStar(true);
        stars.add(star3);

        return stars;
    }
}
