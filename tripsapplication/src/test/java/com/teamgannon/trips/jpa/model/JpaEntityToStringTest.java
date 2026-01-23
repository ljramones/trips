package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for @ToString exclusions to prevent large field output.
 */
@DisplayName("JPA Entity ToString Exclusions")
class JpaEntityToStringTest {

    @Nested
    @DisplayName("ExoPlanet toString()")
    class ExoPlanetToStringTests {

        @Test
        @DisplayName("should exclude proceduralPreview from toString")
        void shouldExcludeProceduralPreview() {
            ExoPlanet planet = new ExoPlanet("Test Planet");
            planet.setProceduralPreview(new byte[1000]);

            String result = planet.toString();

            assertFalse(result.contains("proceduralPreview"));
        }

        @Test
        @DisplayName("should exclude proceduralAccreteSnapshot from toString")
        void shouldExcludeProceduralAccreteSnapshot() {
            ExoPlanet planet = new ExoPlanet("Test Planet");
            planet.setProceduralAccreteSnapshot("{\"large\": \"json object\"}");

            String result = planet.toString();

            assertFalse(result.contains("proceduralAccreteSnapshot"));
        }

        @Test
        @DisplayName("should exclude proceduralOverrides from toString")
        void shouldExcludeProceduralOverrides() {
            ExoPlanet planet = new ExoPlanet("Test Planet");
            planet.setProceduralOverrides("{\"overrides\": \"data\"}");

            String result = planet.toString();

            assertFalse(result.contains("proceduralOverrides"));
        }

        @Test
        @DisplayName("should exclude notes from toString")
        void shouldExcludeNotes() {
            ExoPlanet planet = new ExoPlanet("Test Planet");
            planet.setNotes("These are some very long notes about the planet...");

            String result = planet.toString();

            assertFalse(result.contains("notes="));
        }

        @Test
        @DisplayName("should exclude atmosphereComposition from toString")
        void shouldExcludeAtmosphereComposition() {
            ExoPlanet planet = new ExoPlanet("Test Planet");
            planet.setAtmosphereComposition("N2:780;O2:210;Ar:9;CO2:0.4");

            String result = planet.toString();

            assertFalse(result.contains("atmosphereComposition"));
        }

        @Test
        @DisplayName("should include basic fields in toString")
        void shouldIncludeBasicFields() {
            ExoPlanet planet = new ExoPlanet("Kepler-442b");
            planet.setStarName("Kepler-442");
            planet.setPlanetStatus("Confirmed");

            String result = planet.toString();

            assertTrue(result.contains("name=Kepler-442b"));
            assertTrue(result.contains("starName=Kepler-442"));
            assertTrue(result.contains("planetStatus=Confirmed"));
        }
    }

    @Nested
    @DisplayName("SolarSystem toString()")
    class SolarSystemToStringTests {

        @Test
        @DisplayName("should exclude notes from toString")
        void shouldExcludeNotes() {
            SolarSystem system = new SolarSystem("Test System");
            system.setNotes("Some detailed notes about the system...");

            String result = system.toString();

            assertFalse(result.contains("notes="));
        }

        @Test
        @DisplayName("should include basic fields in toString")
        void shouldIncludeBasicFields() {
            SolarSystem system = new SolarSystem("Alpha Centauri");
            system.setStarCount(3);
            system.setPlanetCount(2);

            String result = system.toString();

            assertTrue(result.contains("systemName=Alpha Centauri"));
            assertTrue(result.contains("starCount=3"));
            assertTrue(result.contains("planetCount=2"));
        }
    }

    @Nested
    @DisplayName("StarObject toString()")
    class StarObjectToStringTests {

        @Test
        @DisplayName("should exclude notes from toString")
        void shouldExcludeNotes() {
            StarObject star = new StarObject();
            star.setNotes("Detailed observation notes...");

            String result = star.toString();

            assertFalse(result.contains("notes="));
        }

        @Test
        @DisplayName("should exclude source from toString")
        void shouldExcludeSource() {
            StarObject star = new StarObject();
            star.setSource("Gaia DR3 catalog with extended metadata...");

            String result = star.toString();

            assertFalse(result.contains("source="));
        }

        @Test
        @DisplayName("should exclude aliasList from toString")
        void shouldExcludeAliasList() {
            StarObject star = new StarObject();
            star.getAliasList().add("Alias 1");
            star.getAliasList().add("Alias 2");

            String result = star.toString();

            assertFalse(result.contains("aliasList"));
        }

        @Test
        @DisplayName("should include basic fields in toString")
        void shouldIncludeBasicFields() {
            StarObject star = new StarObject();
            star.setDisplayName("Proxima Centauri");
            star.setSpectralClass("M5.5V");

            String result = star.toString();

            assertTrue(result.contains("displayName=Proxima Centauri"));
            assertTrue(result.contains("spectralClass=M5.5V"));
        }
    }

    @Nested
    @DisplayName("DataSetDescriptor toString()")
    class DataSetDescriptorToStringTests {

        @Test
        @DisplayName("should exclude themeStr from toString")
        void shouldExcludeThemeStr() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setThemeStr("{\"theme\": \"data\"}");

            String result = desc.toString();

            assertFalse(result.contains("themeStr"));
        }

        @Test
        @DisplayName("should exclude astrographicDataList from toString")
        void shouldExcludeAstrographicDataList() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setAstrographicDataList("uuid1,uuid2,uuid3,uuid4,uuid5");

            String result = desc.toString();

            assertFalse(result.contains("astrographicDataList"));
        }

        @Test
        @DisplayName("should exclude routesStr from toString")
        void shouldExcludeRoutesStr() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setRoutesStr("[{\"route\": \"data\"}]");

            String result = desc.toString();

            assertFalse(result.contains("routesStr"));
        }

        @Test
        @DisplayName("should exclude customDataDefsStr from toString")
        void shouldExcludeCustomDataDefsStr() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setCustomDataDefsStr("[{\"definition\": \"data\"}]");

            String result = desc.toString();

            assertFalse(result.contains("customDataDefsStr"));
        }

        @Test
        @DisplayName("should exclude customDataValuesStr from toString")
        void shouldExcludeCustomDataValuesStr() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setCustomDataValuesStr("[{\"value\": \"data\"}]");

            String result = desc.toString();

            assertFalse(result.contains("customDataValuesStr"));
        }

        @Test
        @DisplayName("should exclude transitPreferencesStr from toString")
        void shouldExcludeTransitPreferencesStr() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setTransitPreferencesStr("{\"transit\": \"prefs\"}");

            String result = desc.toString();

            assertFalse(result.contains("transitPreferencesStr"));
        }

        @Test
        @DisplayName("should include basic fields in toString")
        void shouldIncludeBasicFields() {
            DataSetDescriptor desc = new DataSetDescriptor();
            desc.setDataSetName("Test Dataset");
            desc.setFileCreator("Test Creator");
            desc.setNumberStars(1000L);

            String result = desc.toString();

            assertTrue(result.contains("dataSetName=Test Dataset"));
            assertTrue(result.contains("fileCreator=Test Creator"));
            assertTrue(result.contains("numberStars=1000"));
        }
    }
}
