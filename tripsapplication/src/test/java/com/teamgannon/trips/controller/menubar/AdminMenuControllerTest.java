package com.teamgannon.trips.controller.menubar;

import javafx.event.ActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AdminMenuController}.
 * Note: Tests that create JavaFX dialogs are limited as they require a JavaFX runtime.
 */
class AdminMenuControllerTest {

    @Mock
    private ActionEvent actionEvent;

    private AdminMenuController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AdminMenuController();
    }

    @Test
    @DisplayName("Should create controller")
    void shouldCreateController() {
        assertNotNull(controller);
    }

    // === findInSesame Tests ===
    // Note: findInSesame creates a dialog that requires JavaFX runtime
    // Dialog creation tests are skipped as they would fail without JavaFX
}
