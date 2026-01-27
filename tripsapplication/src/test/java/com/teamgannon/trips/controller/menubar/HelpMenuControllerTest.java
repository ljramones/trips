package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.measure.OshiMeasure;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HelpMenuController}.
 * Note: Some methods require JavaFX runtime for full testing.
 */
class HelpMenuControllerTest {

    @Mock
    private FxWeaver fxWeaver;

    @Mock
    private Localization localization;

    @Mock
    private HostServices hostServices;

    @Mock
    private OshiMeasure oshiMeasure;

    @Mock
    private ActionEvent actionEvent;

    private HelpMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // FxWeaver.getBean returns HostServices mock
        when(fxWeaver.getBean(HostServices.class)).thenReturn(hostServices);
        controller = new HelpMenuController(fxWeaver, localization, oshiMeasure);
    }

    @Test
    @DisplayName("Should create controller with all dependencies")
    void shouldCreateControllerWithAllDependencies() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("howToSupport should open documentation URL")
    void howToSupport_shouldOpenDocumentationUrl() {
        // When
        controller.howToSupport(actionEvent);

        // Then
        verify(hostServices).showDocument("https://github.com/ljramones/trips/wiki");
    }

    // Note: Other methods require JavaFX runtime for full testing.
}
