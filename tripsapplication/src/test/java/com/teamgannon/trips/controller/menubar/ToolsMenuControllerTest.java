package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.MainSplitPaneManager;
import com.teamgannon.trips.controller.TransitFilterPane;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.automation.RouteFinderInView;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.graphsearch.LargeGraphSearchService;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.TransitCalculationService;
import javafx.event.ActionEvent;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ToolsMenuController}.
 * Note: Most methods require JavaFX runtime for full testing.
 */
class ToolsMenuControllerTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private InterstellarSpacePane interstellarSpacePane;

    @Mock
    private RouteFinderInView routeFinderInView;

    @Mock
    private LargeGraphSearchService largeGraphSearchService;

    @Mock
    private DatabaseManagementService databaseManagementService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private StarService starService;

    @Mock
    private StarMeasurementService starMeasurementService;

    @Mock
    private TransitCalculationService transitCalculationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RoutingPanel routingPanel;

    @Mock
    private MainSplitPaneManager mainSplitPaneManager;

    @Mock
    private FxWeaver fxWeaver;

    @Mock
    private SearchContext searchContext;

    @Mock
    private DataSetDescriptor dataSetDescriptor;

    @Mock
    private TransitFilterPane transitFilterPane;

    @Mock
    private ActionEvent actionEvent;

    @Mock
    private StarDisplayRecord starDisplayRecord1;

    @Mock
    private StarDisplayRecord starDisplayRecord2;

    @Mock
    private StarDisplayRecord starDisplayRecord3;

    private ToolsMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ToolsMenuController(
                tripsContext,
                interstellarSpacePane,
                routeFinderInView,
                largeGraphSearchService,
                databaseManagementService,
                datasetService,
                starService,
                starMeasurementService,
                transitCalculationService,
                eventPublisher,
                routingPanel,
                mainSplitPaneManager,
                fxWeaver
        );
    }

    @Test
    @DisplayName("Should create controller with all dependencies")
    void shouldCreateControllerWithAllDependencies() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("routeFinderInView should start route finder with enough stars")
    void routeFinderInView_shouldStartRouteFinderWithEnoughStars() {
        // Given - 3 stars in view (enough to enable routing)
        when(interstellarSpacePane.getCurrentStarsInView()).thenReturn(
                Arrays.asList(starDisplayRecord1, starDisplayRecord2, starDisplayRecord3));
        when(tripsContext.getSearchContext()).thenReturn(searchContext);
        when(searchContext.getDataSetDescriptor()).thenReturn(dataSetDescriptor);

        // When
        controller.routeFinderInView(actionEvent);

        // Then
        verify(routeFinderInView).startRouteLocation(dataSetDescriptor);
    }

    @Test
    @DisplayName("clearRoutes should delegate to services")
    void clearRoutes_shouldDelegateToServices() {
        // Given
        when(tripsContext.getSearchContext()).thenReturn(searchContext);
        when(searchContext.getDataSetDescriptor()).thenReturn(dataSetDescriptor);

        // When
        controller.clearRoutes(actionEvent);

        // Then
        verify(interstellarSpacePane).clearRoutes();
        verify(datasetService).clearRoutesFromCurrent(dataSetDescriptor);
        verify(routingPanel).clearData();
    }

    @Test
    @DisplayName("clearTransits should delegate to pane and publish events")
    void clearTransits_shouldDelegateToPaneAndPublishEvents() {
        // Given
        when(mainSplitPaneManager.getTransitFilterPane()).thenReturn(transitFilterPane);

        // When
        controller.clearTransits(actionEvent);

        // Then
        verify(interstellarSpacePane).clearTransits();
        verify(eventPublisher, times(2)).publishEvent(any());
        verify(transitFilterPane).clear();
    }

    // Note: Other methods require JavaFX runtime for full testing.
}
