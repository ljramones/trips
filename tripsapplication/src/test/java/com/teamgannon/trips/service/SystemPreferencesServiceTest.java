package com.teamgannon.trips.service;

import com.teamgannon.trips.config.application.PreferencesConstants;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.CivilizationDisplayPreferencesChangeEvent;
import com.teamgannon.trips.events.ColorPaletteChangeEvent;
import com.teamgannon.trips.events.GraphEnablesPersistEvent;
import com.teamgannon.trips.events.StarDisplayPreferencesChangeEvent;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.jpa.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SystemPreferencesService}.
 */
@ExtendWith(MockitoExtension.class)
class SystemPreferencesServiceTest {

    @Mock
    private GraphColorsRepository graphColorsRepository;

    @Mock
    private GraphEnablesRepository graphEnablesRepository;

    @Mock
    private StarDetailsPersistRepository starDetailsPersistRepository;

    @Mock
    private CivilizationDisplayPreferencesRepository civilizationDisplayPreferencesRepository;

    @Mock
    private TripsPrefsRepository tripsPrefsRepository;

    @InjectMocks
    private SystemPreferencesService service;

    @Nested
    @DisplayName("TripsPrefs Tests")
    class TripsPrefsTests {

        @Test
        @DisplayName("getTripsPrefs should return existing prefs")
        void getTripsPrefsReturnsExisting() {
            TripsPrefs existing = new TripsPrefs();
            existing.setId(PreferencesConstants.MAIN_PREFS_ID);
            existing.setSkipStartupDialog(true);

            when(tripsPrefsRepository.findById(PreferencesConstants.MAIN_PREFS_ID))
                    .thenReturn(Optional.of(existing));

            TripsPrefs result = service.getTripsPrefs();

            assertNotNull(result);
            assertEquals(PreferencesConstants.MAIN_PREFS_ID, result.getId());
            assertTrue(result.isSkipStartupDialog());
            verify(tripsPrefsRepository).findById(PreferencesConstants.MAIN_PREFS_ID);
        }

        @Test
        @DisplayName("getTripsPrefs should create default when none exists")
        void getTripsPrefsCreatesDefault() {
            when(tripsPrefsRepository.findById(PreferencesConstants.MAIN_PREFS_ID))
                    .thenReturn(Optional.empty());
            when(tripsPrefsRepository.save(any(TripsPrefs.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TripsPrefs result = service.getTripsPrefs();

            assertNotNull(result);
            assertEquals(PreferencesConstants.MAIN_PREFS_ID, result.getId());
            assertFalse(result.isSkipStartupDialog(), "Default should not skip startup dialog");
            verify(tripsPrefsRepository).save(any(TripsPrefs.class));
        }

        @Test
        @DisplayName("saveTripsPrefs should save preferences")
        void saveTripsPrefs() {
            TripsPrefs prefs = new TripsPrefs();
            prefs.setId("test-id");

            service.saveTripsPrefs(prefs);

            verify(tripsPrefsRepository).save(prefs);
        }

        @Test
        @DisplayName("updateDataSet should update dataset name")
        void updateDataSet() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("TestDataset");

            TripsPrefs existing = new TripsPrefs();
            existing.setId(PreferencesConstants.MAIN_PREFS_ID);

            when(tripsPrefsRepository.findById(PreferencesConstants.MAIN_PREFS_ID))
                    .thenReturn(Optional.of(existing));

            service.updateDataSet(descriptor);

            ArgumentCaptor<TripsPrefs> captor = ArgumentCaptor.forClass(TripsPrefs.class);
            verify(tripsPrefsRepository).save(captor.capture());

            assertEquals("TestDataset", captor.getValue().getDatasetName());
        }
    }

    @Nested
    @DisplayName("Star Details Tests")
    class StarDetailsTests {

        @Test
        @DisplayName("getStarDetails should return existing details")
        void getStarDetailsReturnsExisting() {
            List<StarDetailsPersist> existingDetails = createTestStarDetails();

            when(starDetailsPersistRepository.findAll()).thenReturn(existingDetails);

            List<StarDetailsPersist> result = service.getStarDetails();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(starDetailsPersistRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("getStarDetails should create defaults when empty")
        void getStarDetailsCreatesDefaults() {
            when(starDetailsPersistRepository.findAll()).thenReturn(Collections.emptyList());
            when(starDetailsPersistRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            List<StarDetailsPersist> result = service.getStarDetails();

            assertNotNull(result);
            assertFalse(result.isEmpty(), "Should create default star details");
            verify(starDetailsPersistRepository).saveAll(any());
        }

        @Test
        @DisplayName("updateStarPreferences should save all star details")
        void updateStarPreferences() {
            StarDisplayPreferences prefs = new StarDisplayPreferences();
            prefs.setDefaults();

            service.updateStarPreferences(prefs);

            verify(starDetailsPersistRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Graph Enables Tests")
    class GraphEnablesTests {

        @Test
        @DisplayName("getGraphEnablesFromDB should return existing")
        void getGraphEnablesFromDBReturnsExisting() {
            GraphEnablesPersist existing = new GraphEnablesPersist();
            existing.setId("test-id");

            when(graphEnablesRepository.findAll()).thenReturn(Collections.singletonList(existing));

            GraphEnablesPersist result = service.getGraphEnablesFromDB();

            assertNotNull(result);
            assertEquals("test-id", result.getId());
        }

        @Test
        @DisplayName("getGraphEnablesFromDB should create default when empty")
        void getGraphEnablesFromDBCreatesDefault() {
            when(graphEnablesRepository.findAll()).thenReturn(Collections.emptyList());
            when(graphEnablesRepository.save(any(GraphEnablesPersist.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            GraphEnablesPersist result = service.getGraphEnablesFromDB();

            assertNotNull(result);
            assertNotNull(result.getId());
            verify(graphEnablesRepository).save(any(GraphEnablesPersist.class));
        }

        @Test
        @DisplayName("updateGraphEnables should save enables")
        void updateGraphEnables() {
            GraphEnablesPersist enables = new GraphEnablesPersist();
            enables.setId("test-id");

            service.updateGraphEnables(enables);

            verify(graphEnablesRepository).save(enables);
        }
    }

    @Nested
    @DisplayName("Civilization Display Preferences Tests")
    class CivilizationPreferencesTests {

        @Test
        @DisplayName("getCivilizationDisplayPreferences should return existing")
        void getCivilizationDisplayPreferencesReturnsExisting() {
            CivilizationDisplayPreferences existing = new CivilizationDisplayPreferences();
            existing.setId("test-id");
            existing.setStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG);

            when(civilizationDisplayPreferencesRepository.findByStorageTag(
                    PreferencesConstants.CIVILIZATION_STORAGE_TAG))
                    .thenReturn(Optional.of(existing));

            CivilizationDisplayPreferences result = service.getCivilizationDisplayPreferences();

            assertNotNull(result);
            assertEquals("test-id", result.getId());
        }

        @Test
        @DisplayName("getCivilizationDisplayPreferences should create default when none exists")
        void getCivilizationDisplayPreferencesCreatesDefault() {
            when(civilizationDisplayPreferencesRepository.findByStorageTag(
                    PreferencesConstants.CIVILIZATION_STORAGE_TAG))
                    .thenReturn(Optional.empty());
            when(civilizationDisplayPreferencesRepository.save(any(CivilizationDisplayPreferences.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CivilizationDisplayPreferences result = service.getCivilizationDisplayPreferences();

            assertNotNull(result);
            assertEquals(PreferencesConstants.CIVILIZATION_STORAGE_TAG, result.getStorageTag());
            verify(civilizationDisplayPreferencesRepository).save(any(CivilizationDisplayPreferences.class));
        }

        @Test
        @DisplayName("updateCivilizationDisplayPreferences should update existing")
        void updateCivilizationDisplayPreferences() {
            CivilizationDisplayPreferences existing = new CivilizationDisplayPreferences();
            existing.setId("existing-id");
            existing.setStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG);

            CivilizationDisplayPreferences updated = new CivilizationDisplayPreferences();
            updated.setId("new-id");
            updated.setStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG);
            updated.setHumanPolityColor("BLUE");

            when(civilizationDisplayPreferencesRepository.findByStorageTag(
                    PreferencesConstants.CIVILIZATION_STORAGE_TAG))
                    .thenReturn(Optional.of(existing));

            service.updateCivilizationDisplayPreferences(updated);

            ArgumentCaptor<CivilizationDisplayPreferences> captor =
                    ArgumentCaptor.forClass(CivilizationDisplayPreferences.class);
            verify(civilizationDisplayPreferencesRepository).save(captor.capture());

            // Should update existing, not create new
            assertEquals("existing-id", captor.getValue().getId());
            assertEquals("BLUE", captor.getValue().getHumanPolityColor());
        }
    }

    @Nested
    @DisplayName("Graph Colors Tests")
    class GraphColorsTests {

        @Test
        @DisplayName("getGraphColorsFromDB should return color palette")
        void getGraphColorsFromDBReturnsColorPalette() {
            GraphColorsPersist existing = new GraphColorsPersist();
            existing.init();

            when(graphColorsRepository.findAll()).thenReturn(Collections.singletonList(existing));

            ColorPalette result = service.getGraphColorsFromDB();

            assertNotNull(result);
        }

        @Test
        @DisplayName("getGraphColorsFromDB should create default when empty")
        void getGraphColorsFromDBCreatesDefault() {
            when(graphColorsRepository.findAll()).thenReturn(Collections.emptyList());
            when(graphColorsRepository.save(any(GraphColorsPersist.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ColorPalette result = service.getGraphColorsFromDB();

            assertNotNull(result);
            verify(graphColorsRepository).save(any(GraphColorsPersist.class));
        }

        @Test
        @DisplayName("updateColors should update existing palette")
        void updateColors() {
            ColorPalette palette = new ColorPalette();
            palette.setId("test-id");

            GraphColorsPersist existing = new GraphColorsPersist();
            existing.setId("test-id");

            when(graphColorsRepository.findById("test-id")).thenReturn(Optional.of(existing));

            service.updateColors(palette);

            verify(graphColorsRepository).save(existing);
        }

        @Test
        @DisplayName("updateColors should throw when palette not found")
        void updateColorsThrowsWhenNotFound() {
            ColorPalette palette = new ColorPalette();
            palette.setId("non-existent");

            when(graphColorsRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.updateColors(palette));
        }
    }

    @Nested
    @DisplayName("Event Listener Tests")
    class EventListenerTests {

        @Test
        @DisplayName("onColorPaletteChangeEvent should update colors")
        void onColorPaletteChangeEvent() {
            ColorPalette palette = new ColorPalette();
            palette.setId("test-id");

            GraphColorsPersist existing = new GraphColorsPersist();
            existing.setId("test-id");

            when(graphColorsRepository.findById("test-id")).thenReturn(Optional.of(existing));

            ColorPaletteChangeEvent event = new ColorPaletteChangeEvent(this, palette);
            service.onColorPaletteChangeEvent(event);

            verify(graphColorsRepository).save(existing);
        }

        @Test
        @DisplayName("onColorPaletteChangeEvent should handle errors gracefully")
        void onColorPaletteChangeEventHandlesErrors() {
            ColorPalette palette = new ColorPalette();
            palette.setId("test-id");

            when(graphColorsRepository.findById("test-id"))
                    .thenThrow(new RuntimeException("Database error"));

            ColorPaletteChangeEvent event = new ColorPaletteChangeEvent(this, palette);

            // Should not throw - error is logged internally
            assertDoesNotThrow(() -> service.onColorPaletteChangeEvent(event));
        }

        @Test
        @DisplayName("onGraphEnablesPersistEvent should update enables")
        void onGraphEnablesPersistEvent() {
            GraphEnablesPersist enables = new GraphEnablesPersist();
            enables.setId("test-id");

            GraphEnablesPersistEvent event = new GraphEnablesPersistEvent(this, enables);
            service.onGraphEnablesPersistEvent(event);

            verify(graphEnablesRepository).save(enables);
        }

        @Test
        @DisplayName("onGraphEnablesPersistEvent should handle errors gracefully")
        void onGraphEnablesPersistEventHandlesErrors() {
            GraphEnablesPersist enables = new GraphEnablesPersist();
            enables.setId("test-id");

            doThrow(new RuntimeException("Database error"))
                    .when(graphEnablesRepository).save(any());

            GraphEnablesPersistEvent event = new GraphEnablesPersistEvent(this, enables);

            // Should not throw - error is logged internally
            assertDoesNotThrow(() -> service.onGraphEnablesPersistEvent(event));
        }

        @Test
        @DisplayName("onStarDisplayPreferencesChangeEvent should update star preferences")
        void onStarDisplayPreferencesChangeEvent() {
            StarDisplayPreferences prefs = new StarDisplayPreferences();
            prefs.setDefaults();

            StarDisplayPreferencesChangeEvent event =
                    new StarDisplayPreferencesChangeEvent(this, prefs);
            service.onStarDisplayPreferencesChangeEvent(event);

            verify(starDetailsPersistRepository).saveAll(any());
        }

        @Test
        @DisplayName("onStarDisplayPreferencesChangeEvent should handle errors gracefully")
        void onStarDisplayPreferencesChangeEventHandlesErrors() {
            StarDisplayPreferences prefs = new StarDisplayPreferences();
            prefs.setDefaults();

            doThrow(new RuntimeException("Database error"))
                    .when(starDetailsPersistRepository).saveAll(any());

            StarDisplayPreferencesChangeEvent event =
                    new StarDisplayPreferencesChangeEvent(this, prefs);

            // Should not throw - error is logged internally
            assertDoesNotThrow(() -> service.onStarDisplayPreferencesChangeEvent(event));
        }

        @Test
        @DisplayName("onCivilizationDisplayPreferencesChangeEvent should update preferences")
        void onCivilizationDisplayPreferencesChangeEvent() {
            CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
            prefs.setId("test-id");
            prefs.setStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG);

            when(civilizationDisplayPreferencesRepository.findByStorageTag(
                    PreferencesConstants.CIVILIZATION_STORAGE_TAG))
                    .thenReturn(Optional.of(prefs));

            CivilizationDisplayPreferencesChangeEvent event =
                    new CivilizationDisplayPreferencesChangeEvent(this, prefs);
            service.onCivilizationDisplayPreferencesChangeEvent(event);

            verify(civilizationDisplayPreferencesRepository).save(any());
        }

        @Test
        @DisplayName("onCivilizationDisplayPreferencesChangeEvent should handle errors gracefully")
        void onCivilizationDisplayPreferencesChangeEventHandlesErrors() {
            CivilizationDisplayPreferences prefs = new CivilizationDisplayPreferences();
            prefs.setId("test-id");
            prefs.setStorageTag(PreferencesConstants.CIVILIZATION_STORAGE_TAG);

            when(civilizationDisplayPreferencesRepository.findByStorageTag(any()))
                    .thenThrow(new RuntimeException("Database error"));

            CivilizationDisplayPreferencesChangeEvent event =
                    new CivilizationDisplayPreferencesChangeEvent(this, prefs);

            // Should not throw - error is logged internally
            assertDoesNotThrow(() -> service.onCivilizationDisplayPreferencesChangeEvent(event));
        }
    }

    private List<StarDetailsPersist> createTestStarDetails() {
        List<StarDetailsPersist> details = new ArrayList<>();

        StarDetailsPersist oStar = new StarDetailsPersist();
        oStar.setId(UUID.randomUUID().toString());
        oStar.setStellarClass("O");
        oStar.setStarColor("LIGHTBLUE");
        oStar.setRadius(4.0f);
        details.add(oStar);

        StarDetailsPersist gStar = new StarDetailsPersist();
        gStar.setId(UUID.randomUUID().toString());
        gStar.setStellarClass("G");
        gStar.setStarColor("YELLOW");
        gStar.setRadius(2.5f);
        details.add(gStar);

        return details;
    }
}
