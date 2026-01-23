package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.nightsky.model.VisibleStarResult;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarDataConverter.
 */
class StarDataConverterTest {

    private StarDataConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StarDataConverter();
    }

    @Nested
    @DisplayName("resolveName tests")
    class ResolveNameTests {

        @Test
        @DisplayName("should return common name when available")
        void shouldReturnCommonName() {
            StarObject star = new StarObject();
            star.setCommonName("Sirius");
            star.setDisplayName("Alpha CMa");
            star.setSystemName("HIP 32349");
            star.setId("123");

            String result = converter.resolveName(star);

            assertEquals("Sirius", result);
        }

        @Test
        @DisplayName("should return display name when common name is null")
        void shouldReturnDisplayNameWhenCommonNameNull() {
            StarObject star = new StarObject();
            star.setCommonName(null);
            star.setDisplayName("Alpha Centauri");
            star.setSystemName("HIP 71683");
            star.setId("456");

            String result = converter.resolveName(star);

            assertEquals("Alpha Centauri", result);
        }

        @Test
        @DisplayName("should return display name when common name is empty")
        void shouldReturnDisplayNameWhenCommonNameEmpty() {
            StarObject star = new StarObject();
            star.setCommonName("  ");
            star.setDisplayName("Proxima");
            star.setSystemName("HIP 70890");
            star.setId("789");

            String result = converter.resolveName(star);

            assertEquals("Proxima", result);
        }

        @Test
        @DisplayName("should return system name when common and display names are empty")
        void shouldReturnSystemName() {
            StarObject star = new StarObject();
            star.setCommonName("");
            star.setDisplayName("  ");
            star.setSystemName("HD 217987");
            star.setId("101");

            String result = converter.resolveName(star);

            assertEquals("HD 217987", result);
        }

        @Test
        @DisplayName("should return ID when all names are empty")
        void shouldReturnId() {
            StarObject star = new StarObject();
            star.setCommonName(null);
            star.setDisplayName("");
            star.setSystemName("   ");
            star.setId("STAR-12345");

            String result = converter.resolveName(star);

            assertEquals("STAR-12345", result);
        }

        @Test
        @DisplayName("should return Unknown when all fields are empty")
        void shouldReturnUnknown() {
            StarObject star = new StarObject();
            star.setCommonName(null);
            star.setDisplayName(null);
            star.setSystemName(null);
            star.setId(null);

            String result = converter.resolveName(star);

            assertEquals("Unknown", result);
        }

        @Test
        @DisplayName("should trim whitespace from names")
        void shouldTrimWhitespace() {
            StarObject star = new StarObject();
            star.setCommonName("  Vega  ");

            String result = converter.resolveName(star);

            assertEquals("Vega", result);
        }
    }

    @Nested
    @DisplayName("toStarDisplayRecords tests")
    class ToStarDisplayRecordsTests {

        @Test
        @DisplayName("should convert empty list")
        void shouldConvertEmptyList() {
            List<VisibleStarResult> results = new ArrayList<>();

            List<StarDisplayRecord> records = converter.toStarDisplayRecords(results);

            assertTrue(records.isEmpty());
        }

        @Test
        @DisplayName("should skip null star objects")
        void shouldSkipNullStarObjects() {
            List<VisibleStarResult> results = new ArrayList<>();
            // Constructor: star, altitudeDeg, azimuthDeg, magnitude, distanceLy
            results.add(new VisibleStarResult(null, 45.0, 90.0, 5.0, 10.0));

            List<StarDisplayRecord> records = converter.toStarDisplayRecords(results);

            assertTrue(records.isEmpty());
        }

        @Test
        @DisplayName("should convert valid star results")
        void shouldConvertValidStarResults() {
            StarObject star = new StarObject();
            star.setId("test-id");
            star.setCommonName("Test Star");
            star.setDistance(10.5);
            star.setSpectralClass("G2V");
            star.setX(1.0);
            star.setY(2.0);
            star.setZ(3.0);

            // Constructor: star, altitudeDeg, azimuthDeg, magnitude, distanceLy
            VisibleStarResult result = new VisibleStarResult(star, 45.0, 90.0, 4.5, 10.5);
            List<VisibleStarResult> results = List.of(result);

            List<StarDisplayRecord> records = converter.toStarDisplayRecords(results);

            assertEquals(1, records.size());
            StarDisplayRecord record = records.get(0);
            assertEquals("test-id", record.getRecordId());
            assertEquals("Test Star", record.getStarName());
            assertEquals(4.5, record.getMagnitude(), 0.001);
            assertEquals(10.5, record.getDistance(), 0.001);
            assertEquals("G2V", record.getSpectralClass());
            assertEquals(1.0, record.getX(), 0.001);
            assertEquals(2.0, record.getY(), 0.001);
            assertEquals(3.0, record.getZ(), 0.001);
        }

        @Test
        @DisplayName("should convert multiple star results")
        void shouldConvertMultipleResults() {
            StarObject star1 = new StarObject();
            star1.setId("id1");
            star1.setCommonName("Star 1");

            StarObject star2 = new StarObject();
            star2.setId("id2");
            star2.setCommonName("Star 2");

            // Constructor: star, altitudeDeg, azimuthDeg, magnitude, distanceLy
            List<VisibleStarResult> results = List.of(
                    new VisibleStarResult(star1, 0.0, 0.0, 1.0, 5.0),
                    new VisibleStarResult(star2, 45.0, 90.0, 2.0, 8.0)
            );

            List<StarDisplayRecord> records = converter.toStarDisplayRecords(results);

            assertEquals(2, records.size());
            assertEquals("Star 1", records.get(0).getStarName());
            assertEquals("Star 2", records.get(1).getStarName());
        }
    }

    @Nested
    @DisplayName("toBrightestEntries tests")
    class ToBrightestEntriesTests {

        @Test
        @DisplayName("should convert empty list")
        void shouldConvertEmptyList() {
            List<VisibleStarResult> results = new ArrayList<>();

            List<PlanetarySkyRenderer.BrightStarEntry> entries = converter.toBrightestEntries(results);

            assertTrue(entries.isEmpty());
        }

        @Test
        @DisplayName("should skip null star objects")
        void shouldSkipNullStarObjects() {
            List<VisibleStarResult> results = new ArrayList<>();
            // Constructor: star, altitudeDeg, azimuthDeg, magnitude, distanceLy
            results.add(new VisibleStarResult(null, 30.0, 45.0, 5.0, 10.0));

            List<PlanetarySkyRenderer.BrightStarEntry> entries = converter.toBrightestEntries(results);

            assertTrue(entries.isEmpty());
        }

        @Test
        @DisplayName("should convert valid star results to bright star entries")
        void shouldConvertToBrightStarEntries() {
            StarObject star = new StarObject();
            star.setId("bright-star-id");
            star.setCommonName("Bright Star");
            star.setDistance(5.0);
            star.setSpectralClass("A0V");
            star.setX(10.0);
            star.setY(20.0);
            star.setZ(30.0);

            // Constructor: star, altitudeDeg, azimuthDeg, magnitude, distanceLy
            VisibleStarResult result = new VisibleStarResult(star, 60.0, 180.0, -1.5, 5.0);
            List<VisibleStarResult> results = List.of(result);

            List<PlanetarySkyRenderer.BrightStarEntry> entries = converter.toBrightestEntries(results);

            assertEquals(1, entries.size());
            PlanetarySkyRenderer.BrightStarEntry entry = entries.get(0);
            assertEquals("Bright Star", entry.getName());
            assertEquals(5.0, entry.getDistanceFromPlanet(), 0.001);
            assertEquals(-1.5, entry.getApparentMagnitude(), 0.001);
            assertEquals(180.0, entry.getAzimuth(), 0.001);
            assertEquals(60.0, entry.getAltitude(), 0.001);
            assertNotNull(entry.getStarRecord());
            assertEquals("Bright Star", entry.getStarRecord().getStarName());
        }

        @Test
        @DisplayName("should preserve azimuth and altitude from result")
        void shouldPreserveAzimuthAndAltitude() {
            StarObject star = new StarObject();
            star.setId("test");
            star.setCommonName("Test");

            // Constructor: star, altitudeDeg, azimuthDeg, magnitude, distanceLy
            VisibleStarResult result = new VisibleStarResult(star, 15.0, 270.0, 3.0, 15.0);
            List<VisibleStarResult> results = List.of(result);

            List<PlanetarySkyRenderer.BrightStarEntry> entries = converter.toBrightestEntries(results);

            assertEquals(1, entries.size());
            assertEquals(270.0, entries.get(0).getAzimuth(), 0.001);
            assertEquals(15.0, entries.get(0).getAltitude(), 0.001);
        }
    }
}
