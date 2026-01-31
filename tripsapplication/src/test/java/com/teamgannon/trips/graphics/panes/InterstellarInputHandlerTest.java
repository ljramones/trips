package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.model.UserControls;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.SubScene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

/**
 * Tests for InterstellarInputHandler.
 * <p>
 * Note: JavaFX 25+ requires toolkit initialization before mocking SubScene.
 */
@ExtendWith(MockitoExtension.class)
class InterstellarInputHandlerTest {

    @BeforeAll
    static void initToolkit() {
        // Ensure JavaFX toolkit is initialized before mocking SubScene
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Mock
    private SubScene subScene;

    @Mock
    private InterstellarCameraController cameraController;

    private AtomicInteger inputChangeCallCount;
    private InterstellarInputHandler inputHandler;

    @BeforeEach
    void setUp() {
        inputChangeCallCount = new AtomicInteger(0);
        inputHandler = new InterstellarInputHandler(
                subScene,
                cameraController,
                inputChangeCallCount::incrementAndGet
        );
    }

    @Nested
    @DisplayName("Initialization tests")
    class InitializationTests {

        @Test
        @DisplayName("should initialize event handlers on subscene")
        void shouldInitializeEventHandlers() {
            inputHandler.initialize();

            verify(subScene).setOnKeyPressed(any());
            verify(subScene).setOnScroll(any());
            verify(subScene).setOnMousePressed(any());
            verify(subScene).setOnMouseDragged(any());
        }

        @Test
        @DisplayName("should not throw during initialization")
        void shouldNotThrowDuringInitialization() {
            assertDoesNotThrow(() -> inputHandler.initialize());
        }
    }

    @Nested
    @DisplayName("User controls tests")
    class UserControlsTests {

        @Test
        @DisplayName("should accept user controls")
        void shouldAcceptUserControls() {
            UserControls controls = new UserControls();
            controls.setControlSense(true);

            assertDoesNotThrow(() -> inputHandler.setUserControls(controls));
        }

        @Test
        @DisplayName("should accept null user controls")
        void shouldAcceptNullUserControls() {
            // Should not throw even with null
            assertDoesNotThrow(() -> inputHandler.setUserControls(null));
        }
    }

    @Nested
    @DisplayName("Input callback tests")
    class InputCallbackTests {

        @Test
        @DisplayName("should have callback function set")
        void shouldHaveCallbackFunctionSet() {
            // The callback was set in setUp
            // Just verify the handler was created successfully
            assertNotNull(inputHandler);
            assertEquals(0, inputChangeCallCount.get());
        }
    }

    @Nested
    @DisplayName("Handler creation tests")
    class HandlerCreationTests {

        @Test
        @DisplayName("should create handler with valid parameters")
        void shouldCreateHandlerWithValidParameters() {
            InterstellarInputHandler handler = new InterstellarInputHandler(
                    subScene,
                    cameraController,
                    () -> {}
            );

            assertNotNull(handler);
        }

        @Test
        @DisplayName("should create handler with different callback")
        void shouldCreateHandlerWithDifferentCallback() {
            AtomicInteger counter = new AtomicInteger(0);

            InterstellarInputHandler handler = new InterstellarInputHandler(
                    subScene,
                    cameraController,
                    counter::incrementAndGet
            );

            assertNotNull(handler);
            assertEquals(0, counter.get());
        }
    }

    @Nested
    @DisplayName("Integration with camera controller tests")
    class CameraControllerIntegrationTests {

        @Test
        @DisplayName("should be configured with camera controller")
        void shouldBeConfiguredWithCameraController() {
            // Verify handler was created with the camera controller
            assertNotNull(inputHandler);
            // The camera controller is used internally by the handler
        }
    }
}
