package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.model.StarDescriptionPreference;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.StarDisplayPreferencesChangeEvent;
import com.teamgannon.trips.stellarmodelling.StellarType;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TestFX tests for {@link StarsPane}.
 */
@ExtendWith(ApplicationExtension.class)
@ExtendWith(MockitoExtension.class)
class StarsPaneTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StarDisplayPreferences starDisplayPreferences;
    private StarsPane starsPane;
    private Stage testStage;

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() {
        starDisplayPreferences = new StarDisplayPreferences();
        starDisplayPreferences.setDefaults();
    }

    @Start
    void start(Stage stage) {
        testStage = stage;
    }

    @Nested
    @DisplayName("Pane Creation Tests")
    class PaneCreationTests {

        @Test
        @DisplayName("StarsPane should be created successfully")
        void starsPaneShouldBeCreated() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    assertNotNull(starsPane, "StarsPane should be created");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("StarsPane should contain children")
        void starsPaneShouldContainChildren() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    assertFalse(starsPane.getChildren().isEmpty(),
                            "StarsPane should have children");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("StarsPane first child should be VBox")
        void starsPaneFirstChildShouldBeVBox() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    Node firstChild = starsPane.getChildren().get(0);
                    assertTrue(firstChild instanceof VBox,
                            "First child should be a VBox");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }
    }

    @Nested
    @DisplayName("Star Display Preferences Tests")
    class StarDisplayPreferencesTests {

        @Test
        @DisplayName("Should use provided star display preferences")
        void shouldUseProvidedPreferences() throws Exception {
            // Customize preferences
            StarDescriptionPreference oStar = starDisplayPreferences.get(StellarType.O);
            oStar.setColor(Color.PURPLE);
            oStar.setSize(8.0f);

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);

                    // The pane should be created successfully with custom preferences
                    assertNotNull(starsPane, "StarsPane should be created with custom preferences");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("Should handle all stellar types")
        void shouldHandleAllStellarTypes() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    // Verify all stellar types are in preferences
                    StellarType[] expectedTypes = {
                            StellarType.O, StellarType.B, StellarType.A, StellarType.F,
                            StellarType.G, StellarType.K, StellarType.M, StellarType.L,
                            StellarType.T, StellarType.Y
                    };

                    for (StellarType type : expectedTypes) {
                        StarDescriptionPreference pref = starDisplayPreferences.get(type);
                        assertNotNull(pref, "Should have preference for " + type);
                    }

                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    assertNotNull(starsPane, "StarsPane should be created with all stellar types");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }
    }

    @Nested
    @DisplayName("Reset Functionality Tests")
    class ResetFunctionalityTests {

        @Test
        @DisplayName("Reset should publish StarDisplayPreferencesChangeEvent")
        void resetShouldPublishEvent() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);

                    // Call reset
                    starsPane.reset();

                    // Verify event was published
                    verify(eventPublisher).publishEvent(any(StarDisplayPreferencesChangeEvent.class));
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("Reset should restore O star to LIGHTBLUE")
        void resetShouldRestoreOStarColor() throws Exception {
            // Modify O star color before creating pane
            StarDescriptionPreference oStar = starDisplayPreferences.get(StellarType.O);
            oStar.setColor(Color.PURPLE);

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);

                    // Call reset
                    starsPane.reset();

                    // Verify event was published with reset colors
                    ArgumentCaptor<StarDisplayPreferencesChangeEvent> captor =
                            ArgumentCaptor.forClass(StarDisplayPreferencesChangeEvent.class);
                    verify(eventPublisher).publishEvent(captor.capture());

                    StarDisplayPreferences prefs = captor.getValue().getStarDisplayPreferences();
                    StarDescriptionPreference resetOStar = prefs.get(StellarType.O);

                    assertEquals(Color.LIGHTBLUE, resetOStar.getColor(),
                            "O star color should be reset to LIGHTBLUE");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("Reset should restore M star to RED")
        void resetShouldRestoreMStarColor() throws Exception {
            // Modify M star color before creating pane
            StarDescriptionPreference mStar = starDisplayPreferences.get(StellarType.M);
            mStar.setColor(Color.GREEN);

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);

                    // Call reset
                    starsPane.reset();

                    // Verify event was published with reset colors
                    ArgumentCaptor<StarDisplayPreferencesChangeEvent> captor =
                            ArgumentCaptor.forClass(StarDisplayPreferencesChangeEvent.class);
                    verify(eventPublisher).publishEvent(captor.capture());

                    StarDisplayPreferences prefs = captor.getValue().getStarDisplayPreferences();
                    StarDescriptionPreference resetMStar = prefs.get(StellarType.M);

                    assertEquals(Color.RED, resetMStar.getColor(),
                            "M star color should be reset to RED");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("Reset should restore default sizes")
        void resetShouldRestoreDefaultSizes() throws Exception {
            // Modify star sizes before creating pane
            StarDescriptionPreference oStar = starDisplayPreferences.get(StellarType.O);
            oStar.setSize(100.0f);

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);

                    // Call reset
                    starsPane.reset();

                    // Verify event was published with reset sizes
                    ArgumentCaptor<StarDisplayPreferencesChangeEvent> captor =
                            ArgumentCaptor.forClass(StarDisplayPreferencesChangeEvent.class);
                    verify(eventPublisher).publishEvent(captor.capture());

                    StarDisplayPreferences prefs = captor.getValue().getStarDisplayPreferences();
                    StarDescriptionPreference resetOStar = prefs.get(StellarType.O);

                    assertEquals(4.0f, resetOStar.getSize(), 0.001,
                            "O star size should be reset to 4.0");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }
    }

    @Nested
    @DisplayName("Number of Labels Tests")
    class NumberOfLabelsTests {

        @Test
        @DisplayName("Default number of labels should be 30")
        void defaultNumberOfLabelsShouldBe30() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    assertEquals(30, starDisplayPreferences.getNumberOfVisibleLabels(),
                            "Default number of visible labels should be 30");

                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    assertNotNull(starsPane, "StarsPane should be created");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("Custom number of labels should be preserved")
        void customNumberOfLabelsShouldBePreserved() throws Exception {
            starDisplayPreferences.setNumberOfVisibleLabels(75);

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    assertEquals(75, starDisplayPreferences.getNumberOfVisibleLabels(),
                            "Custom number of visible labels should be preserved");

                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    assertNotNull(starsPane, "StarsPane should be created with custom labels");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }
    }

    @Nested
    @DisplayName("Event Publisher Tests")
    class EventPublisherTests {

        @Test
        @DisplayName("Should store event publisher reference")
        void shouldStoreEventPublisherReference() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);

                    // Call reset to verify event publisher is used
                    starsPane.reset();

                    verify(eventPublisher, atLeastOnce())
                            .publishEvent(any(StarDisplayPreferencesChangeEvent.class));
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }

        @Test
        @DisplayName("Event should contain correct source")
        void eventShouldContainCorrectSource() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    starsPane = new StarsPane(starDisplayPreferences, eventPublisher);
                    starsPane.reset();

                    ArgumentCaptor<StarDisplayPreferencesChangeEvent> captor =
                            ArgumentCaptor.forClass(StarDisplayPreferencesChangeEvent.class);
                    verify(eventPublisher).publishEvent(captor.capture());

                    StarDisplayPreferencesChangeEvent event = captor.getValue();
                    assertEquals(starsPane, event.getSource(),
                            "Event source should be the StarsPane");
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within timeout");
        }
    }

    @Nested
    @DisplayName("Default Color Tests")
    class DefaultColorTests {

        @Test
        @DisplayName("O class should default to LIGHTBLUE")
        void oClassShouldDefaultToLightBlue() {
            StarDescriptionPreference oStar = starDisplayPreferences.get(StellarType.O);
            assertEquals(Color.LIGHTBLUE, oStar.getColor(), "O class should be LIGHTBLUE");
        }

        @Test
        @DisplayName("G class should default to YELLOW")
        void gClassShouldDefaultToYellow() {
            StarDescriptionPreference gStar = starDisplayPreferences.get(StellarType.G);
            assertEquals(Color.YELLOW, gStar.getColor(), "G class should be YELLOW");
        }

        @Test
        @DisplayName("K class should default to ORANGE")
        void kClassShouldDefaultToOrange() {
            StarDescriptionPreference kStar = starDisplayPreferences.get(StellarType.K);
            assertEquals(Color.ORANGE, kStar.getColor(), "K class should be ORANGE");
        }

        @Test
        @DisplayName("M class should default to RED")
        void mClassShouldDefaultToRed() {
            StarDescriptionPreference mStar = starDisplayPreferences.get(StellarType.M);
            assertEquals(Color.RED, mStar.getColor(), "M class should be RED");
        }
    }

    @Nested
    @DisplayName("Default Size Tests")
    class DefaultSizeTests {

        @Test
        @DisplayName("O class should default to size 4.0")
        void oClassShouldDefaultToSize4() {
            StarDescriptionPreference oStar = starDisplayPreferences.get(StellarType.O);
            assertEquals(4.0f, oStar.getSize(), 0.001, "O class size should be 4.0");
        }

        @Test
        @DisplayName("M class should default to size 1.5")
        void mClassShouldDefaultToSize1_5() {
            StarDescriptionPreference mStar = starDisplayPreferences.get(StellarType.M);
            assertEquals(1.5f, mStar.getSize(), 0.001, "M class size should be 1.5");
        }

        @Test
        @DisplayName("Y class should default to size 1.0")
        void yClassShouldDefaultToSize1() {
            StarDescriptionPreference yStar = starDisplayPreferences.get(StellarType.Y);
            assertEquals(1.0f, yStar.getSize(), 0.001, "Y class size should be 1.0");
        }
    }
}
