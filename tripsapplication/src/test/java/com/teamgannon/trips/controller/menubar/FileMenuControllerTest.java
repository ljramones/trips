package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FileMenuController}.
 * Note: Most methods create JavaFX dialogs and cannot be tested without a JavaFX runtime.
 * These tests verify controller instantiation and dependency injection.
 */
class FileMenuControllerTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private ApplicationContext appContext;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DatabaseManagementService databaseManagementService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DataImportService dataImportService;

    @Mock
    private DataExportService dataExportService;

    @Mock
    private Localization localization;

    private FileMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new FileMenuController(
                tripsContext,
                appContext,
                eventPublisher,
                databaseManagementService,
                datasetService,
                dataImportService,
                dataExportService,
                localization
        );
    }

    @Test
    @DisplayName("Should create controller with all dependencies")
    void shouldCreateControllerWithAllDependencies() {
        assertNotNull(controller);
    }

    // Note: Other methods require JavaFX runtime to test.
    // Full integration tests with TestFX would be needed for dialog interactions.
}
