package com.teamgannon.trips.dataset.factories;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.ChViewRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.StarObject;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AstrographicObjectFactory Tests")
class AstrographicObjectFactoryTest {

    private Dataset dataset;
    private ChViewRecord chViewRecord;

    @BeforeEach
    void setUp() {
        dataset = new Dataset();
        dataset.setName("TestDataset");

        chViewRecord = new ChViewRecord();
        chViewRecord.setStarName("Alpha Centauri");
        chViewRecord.setDistanceToEarth("4.37");
        chViewRecord.setOrdinates(1.0, 2.0, 3.0);
        chViewRecord.setCollapsedMass(1.1);
        chViewRecord.setRadius(1.2);
        chViewRecord.setSpectra("G2V");
        chViewRecord.setConstellation("Centaurus");
        chViewRecord.setComment("Nearest star system");
        chViewRecord.setGroupNumber(8); // Terran
    }

    @Nested
    @DisplayName("Basic Star Object Creation Tests")
    class BasicStarObjectCreationTests {

        @Test
        @DisplayName("should create star object with non-null ID")
        void shouldCreateStarObjectWithNonNullId() {
            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertNotNull(result.getId());
            assertFalse(result.getId().isEmpty());
        }

        @Test
        @DisplayName("should set dataset name from dataset")
        void shouldSetDatasetNameFromDataset() {
            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("TestDataset", result.getDataSetName());
        }

        @Test
        @DisplayName("should set realStar to true")
        void shouldSetRealStarToTrue() {
            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertTrue(result.isRealStar());
        }

        @Test
        @DisplayName("should set display name from star name")
        void shouldSetDisplayNameFromStarName() {
            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("Alpha Centauri", result.getDisplayName());
        }

        @Test
        @DisplayName("should set source to CHView")
        void shouldSetSourceToCHView() {
            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("CHView", result.getSource());
        }
    }

    @Nested
    @DisplayName("Constellation Tests")
    class ConstellationTests {

        @Test
        @DisplayName("should set constellation name from record")
        void shouldSetConstellationNameFromRecord() {
            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("Centaurus", result.getConstellationName());
        }

        @Test
        @DisplayName("should set default constellation when null")
        void shouldSetDefaultConstellationWhenNull() {
            chViewRecord.setConstellation(null);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("none specified", result.getConstellationName());
        }
    }

    @Nested
    @DisplayName("Physical Properties Tests")
    class PhysicalPropertiesTests {

        @Test
        @DisplayName("should set mass from collapsed mass")
        void shouldSetMassFromCollapsedMass() {
            chViewRecord.setCollapsedMass(1.5);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(1.5, result.getMass());
        }

        @Test
        @DisplayName("should set radius from record")
        void shouldSetRadiusFromRecord() {
            chViewRecord.setRadius(2.5);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(2.5, result.getRadius());
        }

        @Test
        @DisplayName("should set distance from record")
        void shouldSetDistanceFromRecord() {
            chViewRecord.setDistanceToEarth("10.5");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(10.5, result.getDistance());
        }
    }

    @Nested
    @DisplayName("Coordinate Tests")
    class CoordinateTests {

        @Test
        @DisplayName("should set coordinates from ordinates")
        void shouldSetCoordinatesFromOrdinates() {
            chViewRecord.setOrdinates(5.0, 10.0, 15.0);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            double[] coords = result.getCoordinates();
            assertEquals(5.0, coords[0]);
            assertEquals(10.0, coords[1]);
            assertEquals(15.0, coords[2]);
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            chViewRecord.setOrdinates(-5.0, -10.0, -15.0);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            double[] coords = result.getCoordinates();
            assertEquals(-5.0, coords[0]);
            assertEquals(-10.0, coords[1]);
            assertEquals(-15.0, coords[2]);
        }

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            chViewRecord.setOrdinates(0.0, 0.0, 0.0);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            double[] coords = result.getCoordinates();
            assertEquals(0.0, coords[0]);
            assertEquals(0.0, coords[1]);
            assertEquals(0.0, coords[2]);
        }
    }

    @Nested
    @DisplayName("Notes Tests")
    class NotesTests {

        @Test
        @DisplayName("should set notes from comment")
        void shouldSetNotesFromComment() {
            chViewRecord.setComment("Important star system");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("Important star system", result.getNotes());
        }

        @Test
        @DisplayName("should set default notes when comment is null")
        void shouldSetDefaultNotesWhenCommentIsNull() {
            chViewRecord.setComment(null);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("none", result.getNotes());
        }
    }

    @Nested
    @DisplayName("Spectral Class Tests")
    class SpectralClassTests {

        @Test
        @DisplayName("should set spectral class from spectra")
        void shouldSetSpectralClassFromSpectra() {
            chViewRecord.setSpectra("G2V");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("G2V", result.getSpectralClass());
        }

        @Test
        @DisplayName("should set ortho spectral class as first character")
        void shouldSetOrthoSpectralClassAsFirstCharacter() {
            chViewRecord.setSpectra("K5III");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("K", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should set X for null spectra")
        void shouldSetXForNullSpectra() {
            chViewRecord.setSpectra(null);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("X", result.getSpectralClass());
            assertEquals("X", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should set X for empty spectra")
        void shouldSetXForEmptySpectra() {
            chViewRecord.setSpectra("");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("X", result.getSpectralClass());
            assertEquals("X", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should handle O class star")
        void shouldHandleOClassStar() {
            chViewRecord.setSpectra("O5V");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("O5V", result.getSpectralClass());
            assertEquals("O", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should handle B class star")
        void shouldHandleBClassStar() {
            chViewRecord.setSpectra("B3IV");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("B3IV", result.getSpectralClass());
            assertEquals("B", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should handle A class star")
        void shouldHandleAClassStar() {
            chViewRecord.setSpectra("A0V");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("A0V", result.getSpectralClass());
            assertEquals("A", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should handle F class star")
        void shouldHandleFClassStar() {
            chViewRecord.setSpectra("F5V");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("F5V", result.getSpectralClass());
            assertEquals("F", result.getOrthoSpectralClass());
        }

        @Test
        @DisplayName("should handle M class star")
        void shouldHandleMClassStar() {
            chViewRecord.setSpectra("M5V");

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals("M5V", result.getSpectralClass());
            assertEquals("M", result.getOrthoSpectralClass());
        }
    }

    @Nested
    @DisplayName("Polity Assignment Tests")
    class PolityAssignmentTests {

        @Test
        @DisplayName("should assign Arakur for group 1")
        void shouldAssignArakurForGroup1() {
            chViewRecord.setGroupNumber(1);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(CivilizationDisplayPreferences.ARAKUR, result.getPolity());
        }

        @Test
        @DisplayName("should assign HkhRkh for group 2")
        void shouldAssignHkhRkhForGroup2() {
            chViewRecord.setGroupNumber(2);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(CivilizationDisplayPreferences.HKHRKH, result.getPolity());
        }

        @Test
        @DisplayName("should assign Ktor for group 4")
        void shouldAssignKtorForGroup4() {
            chViewRecord.setGroupNumber(4);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(CivilizationDisplayPreferences.KTOR, result.getPolity());
        }

        @Test
        @DisplayName("should assign Terran for group 8")
        void shouldAssignTerranForGroup8() {
            chViewRecord.setGroupNumber(8);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            assertEquals(CivilizationDisplayPreferences.TERRAN, result.getPolity());
        }

        @Test
        @DisplayName("should keep default polity for unknown group")
        void shouldKeepDefaultPolityForUnknownGroup() {
            chViewRecord.setGroupNumber(16);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            // Default polity is "NA" for unknown groups
            assertEquals("NA", result.getPolity());
        }

        @Test
        @DisplayName("should keep default polity for group 0")
        void shouldKeepDefaultPolityForGroup0() {
            chViewRecord.setGroupNumber(0);

            StarObject result = AstrographicObjectFactory.create(dataset, chViewRecord);

            // Default polity is "NA" for group 0
            assertEquals("NA", result.getPolity());
        }
    }

    @Nested
    @DisplayName("setColor Utility Method Tests")
    class SetColorUtilityMethodTests {

        @Test
        @DisplayName("should convert red color to array")
        void shouldConvertRedColorToArray() {
            double[] result = AstrographicObjectFactory.setColor(Color.RED);

            assertEquals(3, result.length);
            assertEquals(1.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert green color to array")
        void shouldConvertGreenColorToArray() {
            double[] result = AstrographicObjectFactory.setColor(Color.GREEN);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(Color.GREEN.getGreen(), result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert blue color to array")
        void shouldConvertBlueColorToArray() {
            double[] result = AstrographicObjectFactory.setColor(Color.BLUE);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(1.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert white color to array")
        void shouldConvertWhiteColorToArray() {
            double[] result = AstrographicObjectFactory.setColor(Color.WHITE);

            assertEquals(3, result.length);
            assertEquals(1.0, result[0], 0.001);
            assertEquals(1.0, result[1], 0.001);
            assertEquals(1.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert black color to array")
        void shouldConvertBlackColorToArray() {
            double[] result = AstrographicObjectFactory.setColor(Color.BLACK);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert custom color to array")
        void shouldConvertCustomColorToArray() {
            Color customColor = Color.color(0.25, 0.50, 0.75);

            double[] result = AstrographicObjectFactory.setColor(customColor);

            assertEquals(3, result.length);
            assertEquals(0.25, result[0], 0.001);
            assertEquals(0.50, result[1], 0.001);
            assertEquals(0.75, result[2], 0.001);
        }
    }

    @Nested
    @DisplayName("Complete Star Object Creation Tests")
    class CompleteStarObjectCreationTests {

        @Test
        @DisplayName("should create complete star object with all fields")
        void shouldCreateCompleteStarObjectWithAllFields() {
            Dataset ds = new Dataset();
            ds.setName("Caine Universe");

            ChViewRecord record = new ChViewRecord();
            record.setStarName("Sol");
            record.setDistanceToEarth("0.0");
            record.setOrdinates(0.0, 0.0, 0.0);
            record.setCollapsedMass(1.0);
            record.setRadius(1.0);
            record.setSpectra("G2V");
            record.setConstellation("N/A");
            record.setComment("Home star");
            record.setGroupNumber(8);

            StarObject result = AstrographicObjectFactory.create(ds, record);

            assertNotNull(result.getId());
            assertEquals("Caine Universe", result.getDataSetName());
            assertTrue(result.isRealStar());
            assertEquals("Sol", result.getDisplayName());
            assertEquals("N/A", result.getConstellationName());
            assertEquals(1.0, result.getMass());
            assertEquals("Home star", result.getNotes());
            assertEquals(0.0, result.getDistance());
            assertEquals(1.0, result.getRadius());
            assertEquals("G2V", result.getSpectralClass());
            assertEquals("G", result.getOrthoSpectralClass());
            assertEquals(CivilizationDisplayPreferences.TERRAN, result.getPolity());
            assertEquals("CHView", result.getSource());
        }

        @Test
        @DisplayName("should create unique IDs for different stars")
        void shouldCreateUniqueIdsForDifferentStars() {
            StarObject star1 = AstrographicObjectFactory.create(dataset, chViewRecord);

            ChViewRecord anotherRecord = new ChViewRecord();
            anotherRecord.setStarName("Proxima Centauri");
            anotherRecord.setDistanceToEarth("4.24");
            anotherRecord.setOrdinates(1.1, 2.1, 3.1);
            anotherRecord.setSpectra("M5V");

            StarObject star2 = AstrographicObjectFactory.create(dataset, anotherRecord);

            assertNotEquals(star1.getId(), star2.getId());
        }
    }
}
