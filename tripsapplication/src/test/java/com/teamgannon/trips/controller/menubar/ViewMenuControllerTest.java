package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.MainSplitPaneManager;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.nebula.dialogs.catalog.NebulaCatalogService;
import com.teamgannon.trips.nebula.service.NebulaService;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ViewMenuController}.
 * Note: Most methods require JavaFX runtime for full testing.
 */
class ViewMenuControllerTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private SharedUIFunctions sharedUIFunctions;

    @Mock
    private SharedUIState sharedUIState;

    @Mock
    private InterstellarSpacePane interstellarSpacePane;

    @Mock
    private MainSplitPaneManager mainSplitPaneManager;

    @Mock
    private NebulaService nebulaService;

    @Mock
    private NebulaCatalogService nebulaCatalogService;

    @Mock
    private ActionEvent actionEvent;

    private ViewMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ViewMenuController(
                tripsContext,
                sharedUIFunctions,
                sharedUIState,
                interstellarSpacePane,
                mainSplitPaneManager,
                nebulaService,
                nebulaCatalogService
        );
    }

    @Test
    @DisplayName("Should create controller with all dependencies")
    void shouldCreateControllerWithAllDependencies() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("plotStars should delegate to SharedUIFunctions")
    void plotStars_shouldDelegateToSharedUIFunctions() {
        // When
        controller.plotStars(actionEvent);

        // Then
        verify(sharedUIFunctions).plotStars();
    }

    // Note: Other methods require JavaFX runtime for full testing.
}
