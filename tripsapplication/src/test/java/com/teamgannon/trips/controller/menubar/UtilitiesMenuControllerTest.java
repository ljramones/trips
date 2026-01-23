package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UtilitiesMenuController}.
 * Note: Methods create JavaFX dialogs and cannot be tested without a JavaFX runtime.
 */
class UtilitiesMenuControllerTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private DatabaseManagementService databaseManagementService;

    @Mock
    private StarService starService;

    private UtilitiesMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UtilitiesMenuController(
                tripsContext,
                databaseManagementService,
                starService
        );
    }

    @Test
    @DisplayName("Should create controller with all dependencies")
    void shouldCreateControllerWithAllDependencies() {
        assertNotNull(controller);
    }

    // Note: Methods require JavaFX runtime to test.
    // Full integration tests with TestFX would be needed for dialog interactions.
}
