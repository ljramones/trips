package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.service.DatasetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReportsMenuController}.
 * Note: Most methods require JavaFX runtime for full testing.
 */
class ReportsMenuControllerTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private InterstellarSpacePane interstellarSpacePane;

    @Mock
    private DatasetService datasetService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ReportsMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ReportsMenuController(
                tripsContext,
                interstellarSpacePane,
                datasetService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should create controller with all dependencies")
    void shouldCreateControllerWithAllDependencies() {
        assertNotNull(controller);
    }

    // Note: Other methods require JavaFX runtime to test.
    // Full integration tests with TestFX would be needed for dialog/report interactions.
}
