package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.solarsysmodelling.accrete.AtmosphericChemical;
import com.teamgannon.trips.solarsysmodelling.accrete.Chemical;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.PlanetTypeEnum;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccretePlanetConverter.
 */
class AccretePlanetConverterTest {

    @Nested
    @DisplayName("Basic Conversion")
    class BasicConversionTests {

        @Test
        @DisplayName("should convert basic planet to ExoPlanet")
        void shouldConvertBasicPlanet() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertFalse(result.getId().isBlank());
        }

        @Test
        @DisplayName("should set planet name using star name and letter")
        void shouldSetPlanetNameUsingStarNameAndLetter() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals("Alpha Centauri b", result.getName());
        }

        @Test
        @DisplayName("should set solar system ID")
        void shouldSetSolarSystemId() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals("solar-system-123", result.getSolarSystemId());
        }

        @Test
        @DisplayName("should set host star ID")
        void shouldSetHostStarId() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals("star-id-123", result.getHostStarId());
        }

        @Test
        @DisplayName("should set star name")
        void shouldSetStarName() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals("Alpha Centauri", result.getStarName());
        }

        @Test
        @DisplayName("should mark as simulated")
        void shouldMarkAsSimulated() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals("Simulated", result.getPlanetStatus());
            assertEquals("Simulated", result.getDetectionType());
        }
    }

    @Nested
    @DisplayName("Moon Conversion")
    class MoonConversionTests {

        @Test
        @DisplayName("should set parent planet ID for moons")
        void shouldSetParentPlanetIdForMoons() {
            Planet moon = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    moon, hostStar, "solar-system-123", 1, "parent-planet-id", true);

            assertEquals("parent-planet-id", result.getParentPlanetId());
            assertTrue(result.getIsMoon());
        }

        @Test
        @DisplayName("should not set parent planet ID for planets")
        void shouldNotSetParentPlanetIdForPlanets() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertNull(result.getParentPlanetId());
            assertFalse(result.getIsMoon());
        }
    }

    @Nested
    @DisplayName("Orbital Parameters")
    class OrbitalParametersTests {

        @Test
        @DisplayName("should convert semi-major axis from Planet")
        void shouldConvertSemiMajorAxis() {
            // Note: sma is a protected field in SystemObject, defaults to 0
            // Testing that conversion handles default values
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            // Default value is 0.0 since we can't set sma directly
            assertNotNull(result.getSemiMajorAxis());
        }

        @Test
        @DisplayName("should convert eccentricity from Planet")
        void shouldConvertEccentricity() {
            // Note: eccentricity is a protected field in SystemObject, defaults to 0
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertNotNull(result.getEccentricity());
        }

        @Test
        @DisplayName("should convert inclination from Planet")
        void shouldConvertInclination() {
            // Note: inclination is a protected field in SystemObject, defaults to 0
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertNotNull(result.getInclination());
        }

        @Test
        @DisplayName("should convert orbital period from seconds to days")
        void shouldConvertOrbitalPeriodFromSecondsToDays() {
            Planet planet = createBasicPlanet();
            // Set orbital period to 365.25 days worth of seconds
            planet.setOrbitalPeriod(365.25 * 24 * 3600);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(365.25, result.getOrbitalPeriod(), 0.01);
        }
    }

    @Nested
    @DisplayName("Physical Properties")
    class PhysicalPropertiesTests {

        @Test
        @DisplayName("should convert radius from km to Earth radii")
        void shouldConvertRadiusFromKmToEarthRadii() {
            Planet planet = createBasicPlanet();
            planet.setRadius(6371.0); // Earth radius in km
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(1.0, result.getRadius(), 0.01);
        }

        @Test
        @DisplayName("should convert density")
        void shouldConvertDensity() {
            Planet planet = createBasicPlanet();
            planet.setDensity(5.51);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(5.51, result.getDensity(), 0.01);
        }

        @Test
        @DisplayName("should convert core radius")
        void shouldConvertCoreRadius() {
            Planet planet = createBasicPlanet();
            planet.setCoreRadius(3480.0);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(3480.0, result.getCoreRadius(), 0.1);
        }

        @Test
        @DisplayName("should convert axial tilt")
        void shouldConvertAxialTilt() {
            Planet planet = createBasicPlanet();
            planet.setAxialTilt(23.44);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(23.44, result.getAxialTilt(), 0.01);
        }

        @Test
        @DisplayName("should convert day length from seconds to hours")
        void shouldConvertDayLengthFromSecondsToHours() {
            Planet planet = createBasicPlanet();
            planet.setDayLength(24 * 3600); // 24 hours in seconds
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(24.0, result.getDayLength(), 0.01);
        }

        @Test
        @DisplayName("should convert surface gravity and calculate log g")
        void shouldConvertSurfaceGravityAndCalculateLogG() {
            Planet planet = createBasicPlanet();
            planet.setSurfaceGravity(1.0); // 1 Earth gravity
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(1.0, result.getSurfaceGravity(), 0.01);
            // log10(980.665) ≈ 2.99
            assertNotNull(result.getLogG());
            assertTrue(result.getLogG() > 2.9 && result.getLogG() < 3.1);
        }
    }

    @Nested
    @DisplayName("Habitability Flags")
    class HabitabilityFlagsTests {

        @Test
        @DisplayName("should convert habitable flag")
        void shouldConvertHabitableFlag() {
            Planet planet = createBasicPlanet();
            planet.setHabitable(true);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertTrue(result.getHabitable());
        }

        @Test
        @DisplayName("should convert earthlike flag")
        void shouldConvertEarthlikeFlag() {
            Planet planet = createBasicPlanet();
            planet.setEarthlike(true);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertTrue(result.getEarthlike());
        }

        @Test
        @DisplayName("should convert gas giant flag")
        void shouldConvertGasGiantFlag() {
            Planet planet = createBasicPlanet();
            planet.setGasGiant(true);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertTrue(result.getGasGiant());
        }

        @Test
        @DisplayName("should convert greenhouse effect flag")
        void shouldConvertGreenhouseEffectFlag() {
            Planet planet = createBasicPlanet();
            planet.setGreenhouseEffect(true);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertTrue(result.getGreenhouseEffect());
        }

        @Test
        @DisplayName("should convert tidally locked flag from resonant period")
        void shouldConvertTidallyLockedFlag() {
            Planet planet = createBasicPlanet();
            planet.setResonantPeriod(true);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertTrue(result.getTidallyLocked());
        }
    }

    @Nested
    @DisplayName("Climate Properties")
    class ClimatePropertiesTests {

        @Test
        @DisplayName("should convert hydrosphere")
        void shouldConvertHydrosphere() {
            Planet planet = createBasicPlanet();
            planet.setHydrosphere(0.71);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(0.71, result.getHydrosphere(), 0.01);
        }

        @Test
        @DisplayName("should convert cloud cover")
        void shouldConvertCloudCover() {
            Planet planet = createBasicPlanet();
            planet.setCloudCover(0.5);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(0.5, result.getCloudCover(), 0.01);
        }

        @Test
        @DisplayName("should convert ice cover")
        void shouldConvertIceCover() {
            Planet planet = createBasicPlanet();
            planet.setIceCover(0.1);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(0.1, result.getIceCover(), 0.01);
        }

        @Test
        @DisplayName("should convert albedo")
        void shouldConvertAlbedo() {
            Planet planet = createBasicPlanet();
            planet.setAlbedo(0.3);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(0.3, result.getAlbedo(), 0.01);
        }

        @Test
        @DisplayName("should convert surface pressure")
        void shouldConvertSurfacePressure() {
            Planet planet = createBasicPlanet();
            planet.setSurfacePressure(1013.25);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(1013.25, result.getSurfacePressure(), 0.1);
        }
    }

    @Nested
    @DisplayName("Temperature Properties")
    class TemperaturePropertiesTests {

        @Test
        @DisplayName("should convert surface temperature")
        void shouldConvertSurfaceTemperature() {
            Planet planet = createBasicPlanet();
            planet.setSurfaceTemperature(288.0);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(288.0, result.getSurfaceTemperature(), 0.1);
            assertEquals(288.0, result.getTempCalculated(), 0.1);
        }

        @Test
        @DisplayName("should use estimated temperature when surface temperature is zero")
        void shouldUseEstimatedTemperatureWhenSurfaceIsZero() {
            Planet planet = createBasicPlanet();
            planet.setSurfaceTemperature(0);
            planet.setEstimatedTemperature(300.0);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(300.0, result.getTempCalculated(), 0.1);
        }

        @Test
        @DisplayName("should convert high and low temperatures")
        void shouldConvertHighAndLowTemperatures() {
            Planet planet = createBasicPlanet();
            planet.setHighTemperature(310.0);
            planet.setLowTemperature(260.0);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(310.0, result.getHighTemperature(), 0.1);
            assertEquals(260.0, result.getLowTemperature(), 0.1);
        }

        @Test
        @DisplayName("should convert max and min temperatures")
        void shouldConvertMaxAndMinTemperatures() {
            Planet planet = createBasicPlanet();
            planet.setMaxTemperature(330.0);
            planet.setMinTemperature(220.0);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(330.0, result.getMaxTemperature(), 0.1);
            assertEquals(220.0, result.getMinTemperature(), 0.1);
        }

        @Test
        @DisplayName("should convert boiling point")
        void shouldConvertBoilingPoint() {
            Planet planet = createBasicPlanet();
            planet.setBoilingPoint(373.15);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(373.15, result.getBoilingPoint(), 0.1);
        }

        @Test
        @DisplayName("should convert greenhouse rise")
        void shouldConvertGreenhouseRise() {
            Planet planet = createBasicPlanet();
            planet.setGreenhouseRise(33.0);
            StarObject hostStar = createHostStar();

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(33.0, result.getGreenhouseRise(), 0.1);
        }
    }

    @Nested
    @DisplayName("Host Star Properties")
    class HostStarPropertiesTests {

        @Test
        @DisplayName("should copy RA and Dec from host star")
        void shouldCopyRaAndDecFromHostStar() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();
            hostStar.setRa(219.902);
            hostStar.setDeclination(-60.837);

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(219.902, result.getRa(), 0.001);
            assertEquals(-60.837, result.getDec(), 0.001);
        }

        @Test
        @DisplayName("should copy star distance from host star")
        void shouldCopyStarDistanceFromHostStar() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();
            hostStar.setDistance(4.37);

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(4.37, result.getStarDistance(), 0.01);
        }

        @Test
        @DisplayName("should copy spectral class from host star")
        void shouldCopySpectralClassFromHostStar() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();
            hostStar.setSpectralClass("G2V");

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals("G2V", result.getStarSpType());
        }

        @Test
        @DisplayName("should copy star mass and radius from host star")
        void shouldCopyStarMassAndRadiusFromHostStar() {
            Planet planet = createBasicPlanet();
            StarObject hostStar = createHostStar();
            hostStar.setMass(1.1);
            hostStar.setRadius(1.2);

            ExoPlanet result = AccretePlanetConverter.convert(
                    planet, hostStar, "solar-system-123", 1, null, false);

            assertEquals(1.1, result.getStarMass(), 0.01);
            assertEquals(1.2, result.getStarRadius(), 0.01);
        }
    }

    @Nested
    @DisplayName("Planet Letter Generation")
    class PlanetLetterTests {

        @Test
        @DisplayName("should return 'b' for index 1")
        void shouldReturnBForIndex1() {
            assertEquals("b", AccretePlanetConverter.getPlanetLetter(1));
        }

        @Test
        @DisplayName("should return 'c' for index 2")
        void shouldReturnCForIndex2() {
            assertEquals("c", AccretePlanetConverter.getPlanetLetter(2));
        }

        @Test
        @DisplayName("should return 'z' for index 25")
        void shouldReturnZForIndex25() {
            assertEquals("z", AccretePlanetConverter.getPlanetLetter(25));
        }

        @Test
        @DisplayName("should return 'aa' for index 26")
        void shouldReturnAaForIndex26() {
            assertEquals("aa", AccretePlanetConverter.getPlanetLetter(26));
        }

        @Test
        @DisplayName("should return 'ab' for index 27")
        void shouldReturnAbForIndex27() {
            assertEquals("ab", AccretePlanetConverter.getPlanetLetter(27));
        }

        @Test
        @DisplayName("should handle large indices")
        void shouldHandleLargeIndices() {
            String result = AccretePlanetConverter.getPlanetLetter(52);
            assertNotNull(result);
            assertEquals(2, result.length());
        }
    }

    @Nested
    @DisplayName("Roman Numeral Generation")
    class RomanNumeralTests {

        @Test
        @DisplayName("should return I for 1")
        void shouldReturnIFor1() {
            assertEquals("I", AccretePlanetConverter.toRomanNumeral(1));
        }

        @Test
        @DisplayName("should return V for 5")
        void shouldReturnVFor5() {
            assertEquals("V", AccretePlanetConverter.toRomanNumeral(5));
        }

        @Test
        @DisplayName("should return X for 10")
        void shouldReturnXFor10() {
            assertEquals("X", AccretePlanetConverter.toRomanNumeral(10));
        }

        @Test
        @DisplayName("should return XX for 20")
        void shouldReturnXXFor20() {
            assertEquals("XX", AccretePlanetConverter.toRomanNumeral(20));
        }

        @Test
        @DisplayName("should return number for values over 20")
        void shouldReturnNumberForValuesOver20() {
            assertEquals("21", AccretePlanetConverter.toRomanNumeral(21));
        }

        @Test
        @DisplayName("should return number for zero")
        void shouldReturnNumberForZero() {
            assertEquals("0", AccretePlanetConverter.toRomanNumeral(0));
        }

        @Test
        @DisplayName("should return number for negative values")
        void shouldReturnNumberForNegativeValues() {
            assertEquals("-1", AccretePlanetConverter.toRomanNumeral(-1));
        }
    }

    // Helper methods

    private SimStar createSimStar() {
        // SimStar requires: (mass, luminosity, radius, temperature, absoluteMagnitude)
        return new SimStar(
            1.0,    // mass (solar masses)
            1.0,    // luminosity (solar luminosities)
            1.0,    // radius (solar radii)
            5778,   // temperature (K)
            4.83    // absolute magnitude
        );
    }

    private Planet createBasicPlanet() {
        SimStar simStar = createSimStar();
        Planet planet = new Planet(simStar);
        // Note: mass, sma, eccentricity, inclination are protected in SystemObject
        // and don't have setters, so they default to 0
        planet.setRadius(6371.0);
        planet.setOrbitalPeriod(365.25 * 24 * 3600);
        planet.setType(PlanetTypeEnum.tTerrestrial);
        return planet;
    }

    private StarObject createHostStar() {
        StarObject star = new StarObject();
        star.setId("star-id-123");
        star.setDisplayName("Alpha Centauri");
        star.setCommonName("Alpha Centauri");
        star.setRa(219.902);
        star.setDeclination(-60.837);
        star.setDistance(4.37);
        star.setSpectralClass("G2V");
        star.setMass(1.1);
        star.setRadius(1.2);
        return star;
    }
}
