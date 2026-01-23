package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EditMenuController}.
 * Note: Most methods create JavaFX dialogs and cannot be tested without a JavaFX runtime.
 * These tests verify controller instantiation and dependency injection.
 */
class EditMenuControllerTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private InterstellarSpacePane interstellarSpacePane;

    @Mock
    private DatabaseManagementService databaseManagementService;

    @Mock
    private StarService starService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EditMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EditMenuController(
                tripsContext,
                interstellarSpacePane,
                databaseManagementService,
                starService,
                eventPublisher
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
