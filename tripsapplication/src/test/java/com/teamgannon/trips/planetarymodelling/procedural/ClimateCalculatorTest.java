package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.ClimateCalculator.ClimateZone;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;

class ClimateCalculatorTest {

    private List<Polygon> polygons;
    private ClimateCalculator calculator;

    @BeforeEach
    void setUp() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();
        var mesh = new IcosahedralMesh(config);
        polygons = mesh.generate();
        calculator = new ClimateCalculator(polygons);
    }

    @Test
    @DisplayName("calculate() returns array matching polygon count")
    void calculateReturnsCorrectSize() {
        ClimateZone[] zones = calculator.calculate();

        assertThat(zones).hasSize(polygons.size());
    }

    @Test
    @DisplayName("All zones are assigned (no nulls)")
    void allZonesAssigned() {
        ClimateZone[] zones = calculator.calculate();

        for (ClimateZone zone : zones) {
            assertThat(zone).isNotNull();
        }
    }

    @Test
    @DisplayName("Only valid climate zones")
    void validClimateZones() {
        ClimateZone[] zones = calculator.calculate();

        for (ClimateZone zone : zones) {
            assertThat(zone).isIn(
                ClimateZone.TROPICAL,
                ClimateZone.TEMPERATE,
                ClimateZone.POLAR
            );
        }
    }

    @Test
    @DisplayName("All three climate zones present on standard-sized planet")
    void allZonesPresent() {
        ClimateZone[] zones = calculator.calculate();

        boolean hasTropical = false;
        boolean hasTemperate = false;
        boolean hasPolar = false;

        for (ClimateZone zone : zones) {
            if (zone == ClimateZone.TROPICAL) hasTropical = true;
            if (zone == ClimateZone.TEMPERATE) hasTemperate = true;
            if (zone == ClimateZone.POLAR) hasPolar = true;
        }

        assertThat(hasTropical).as("Should have tropical zones").isTrue();
        assertThat(hasTemperate).as("Should have temperate zones").isTrue();
        assertThat(hasPolar).as("Should have polar zones").isTrue();
    }

    @Test
    @DisplayName("Equatorial polygons are tropical")
    void equatorialIsTropical() {
        ClimateZone[] zones = calculator.calculate();

        for (int i = 0; i < polygons.size(); i++) {
            double latitude = Math.abs(ClimateCalculator.getLatitudeDegrees(polygons.get(i)));

            if (latitude < 25) { // Well within tropical zone
                assertThat(zones[i])
                    .as("Polygon at latitude %.1f should be tropical", latitude)
                    .isEqualTo(ClimateZone.TROPICAL);
            }
        }
    }

    @Test
    @DisplayName("Polar polygons are polar")
    void polarIsPolar() {
        ClimateZone[] zones = calculator.calculate();

        for (int i = 0; i < polygons.size(); i++) {
            double latitude = Math.abs(ClimateCalculator.getLatitudeDegrees(polygons.get(i)));

            if (latitude > 65) { // Well within polar zone
                assertThat(zones[i])
                    .as("Polygon at latitude %.1f should be polar", latitude)
                    .isEqualTo(ClimateZone.POLAR);
            }
        }
    }

    @Test
    @DisplayName("Polar center is handled without error")
    void polarCenterIsHandled() {
        Vector3D northPole = new Vector3D(0, 1, 0);
        Polygon polarPoly = new Polygon(northPole, createDummyVertices(northPole));

        ClimateCalculator calc = new ClimateCalculator(List.of(polarPoly));
        ClimateZone[] zones = calc.calculate();

        assertThat(zones[0]).isEqualTo(ClimateZone.POLAR);
        assertThat(ClimateCalculator.getLatitudeDegrees(polarPoly))
            .as("Polar latitude should be near 90 degrees")
            .isCloseTo(90.0, offset(0.1));
    }

    @Test
    @DisplayName("Mid-latitude polygons are temperate")
    void midLatitudeIsTemperate() {
        ClimateZone[] zones = calculator.calculate();

        for (int i = 0; i < polygons.size(); i++) {
            double latitude = Math.abs(ClimateCalculator.getLatitudeDegrees(polygons.get(i)));

            if (latitude > 35 && latitude < 55) { // Well within temperate zone
                assertThat(zones[i])
                    .as("Polygon at latitude %.1f should be temperate", latitude)
                    .isEqualTo(ClimateZone.TEMPERATE);
            }
        }
    }

    @Test
    @DisplayName("getLatitudeDegrees returns value in valid range")
    void latitudeInValidRange() {
        for (Polygon p : polygons) {
            double latitude = ClimateCalculator.getLatitudeDegrees(p);
            assertThat(latitude)
                .as("Latitude should be between -90 and 90")
                .isBetween(-90.0, 90.0);
        }
    }

    @Test
    @DisplayName("getLatitudeDegrees is positive for northern hemisphere")
    void northernHemispherePositive() {
        // Create a polygon with center in northern hemisphere (positive Y)
        Vector3D northCenter = new Vector3D(0, 0.5, 0.5).normalize();
        Polygon northPoly = new Polygon(northCenter, List.of(
            new Vector3D(0.1, 0.5, 0.5).normalize(),
            new Vector3D(-0.1, 0.5, 0.5).normalize(),
            new Vector3D(0, 0.6, 0.4).normalize()
        ));

        double latitude = ClimateCalculator.getLatitudeDegrees(northPoly);
        assertThat(latitude)
            .as("Northern hemisphere should have positive latitude")
            .isGreaterThan(0);
    }

    @Test
    @DisplayName("getLatitudeDegrees is negative for southern hemisphere")
    void southernHemisphereNegative() {
        // Create a polygon with center in southern hemisphere (negative Y)
        Vector3D southCenter = new Vector3D(0, -0.5, 0.5).normalize();
        Polygon southPoly = new Polygon(southCenter, List.of(
            new Vector3D(0.1, -0.5, 0.5).normalize(),
            new Vector3D(-0.1, -0.5, 0.5).normalize(),
            new Vector3D(0, -0.6, 0.4).normalize()
        ));

        double latitude = ClimateCalculator.getLatitudeDegrees(southPoly);
        assertThat(latitude)
            .as("Southern hemisphere should have negative latitude")
            .isLessThan(0);
    }

    @Test
    @DisplayName("Tropical boundary is at 30 degrees")
    void tropicalBoundary() {
        // Polygon just inside tropical zone
        Vector3D center25 = new Vector3D(
            Math.cos(Math.toRadians(25)),
            Math.sin(Math.toRadians(25)),
            0
        ).normalize();
        Polygon tropical = new Polygon(center25, createDummyVertices(center25));

        // Polygon just outside tropical zone
        Vector3D center35 = new Vector3D(
            Math.cos(Math.toRadians(35)),
            Math.sin(Math.toRadians(35)),
            0
        ).normalize();
        Polygon temperate = new Polygon(center35, createDummyVertices(center35));

        ClimateCalculator calc = new ClimateCalculator(List.of(tropical, temperate));
        ClimateZone[] zones = calc.calculate();

        assertThat(zones[0]).isEqualTo(ClimateZone.TROPICAL);
        assertThat(zones[1]).isEqualTo(ClimateZone.TEMPERATE);
    }

    @Test
    @DisplayName("Temperate boundary is at 60 degrees")
    void temperateBoundary() {
        // Polygon just inside temperate zone
        Vector3D center55 = new Vector3D(
            Math.cos(Math.toRadians(55)),
            Math.sin(Math.toRadians(55)),
            0
        ).normalize();
        Polygon temperate = new Polygon(center55, createDummyVertices(center55));

        // Polygon just outside temperate zone
        Vector3D center65 = new Vector3D(
            Math.cos(Math.toRadians(65)),
            Math.sin(Math.toRadians(65)),
            0
        ).normalize();
        Polygon polar = new Polygon(center65, createDummyVertices(center65));

        ClimateCalculator calc = new ClimateCalculator(List.of(temperate, polar));
        ClimateZone[] zones = calc.calculate();

        assertThat(zones[0]).isEqualTo(ClimateZone.TEMPERATE);
        assertThat(zones[1]).isEqualTo(ClimateZone.POLAR);
    }

    @Test
    @DisplayName("Climate distribution is symmetric between hemispheres")
    void symmetricDistribution() {
        ClimateZone[] zones = calculator.calculate();

        int northTropical = 0, southTropical = 0;
        int northTemperate = 0, southTemperate = 0;
        int northPolar = 0, southPolar = 0;

        for (int i = 0; i < polygons.size(); i++) {
            double lat = ClimateCalculator.getLatitudeDegrees(polygons.get(i));
            ClimateZone zone = zones[i];

            if (lat >= 0) {
                if (zone == ClimateZone.TROPICAL) northTropical++;
                else if (zone == ClimateZone.TEMPERATE) northTemperate++;
                else northPolar++;
            } else {
                if (zone == ClimateZone.TROPICAL) southTropical++;
                else if (zone == ClimateZone.TEMPERATE) southTemperate++;
                else southPolar++;
            }
        }

        // Allow 20% variance for asymmetry due to discrete polygons
        assertThat(northTropical).isCloseTo(southTropical, offset((int)(northTropical * 0.3 + 5)));
        assertThat(northTemperate).isCloseTo(southTemperate, offset((int)(northTemperate * 0.3 + 5)));
        assertThat(northPolar).isCloseTo(southPolar, offset((int)(northPolar * 0.3 + 5)));
    }

    private List<Vector3D> createDummyVertices(Vector3D center) {
        // Create simple triangle vertices around center
        Vector3D perp1 = center.crossProduct(Vector3D.PLUS_K);
        if (perp1.getNorm() < 0.01) {
            perp1 = center.crossProduct(Vector3D.PLUS_I);
        }
        perp1 = perp1.normalize().scalarMultiply(0.1);
        Vector3D perp2 = center.crossProduct(perp1).normalize().scalarMultiply(0.1);

        return List.of(
            center.add(perp1).normalize(),
            center.add(perp2).normalize(),
            center.subtract(perp1).normalize()
        );
    }

    // =============================================
    // Climate Model Tests
    // =============================================

    @Test
    @DisplayName("ICE_WORLD model has no tropical zones")
    void iceWorldNoTropical() {
        ClimateCalculator iceCalc = new ClimateCalculator(polygons,
            ClimateCalculator.ClimateModel.ICE_WORLD);
        ClimateZone[] zones = iceCalc.calculate();

        for (ClimateZone zone : zones) {
            assertThat(zone)
                .as("Ice world should have no tropical zones")
                .isNotEqualTo(ClimateZone.TROPICAL);
        }
    }

    @Test
    @DisplayName("ICE_WORLD model has mostly polar zones")
    void iceWorldMostlyPolar() {
        ClimateCalculator iceCalc = new ClimateCalculator(polygons,
            ClimateCalculator.ClimateModel.ICE_WORLD);
        ClimateZone[] zones = iceCalc.calculate();

        int polarCount = 0;
        for (ClimateZone zone : zones) {
            if (zone == ClimateZone.POLAR) polarCount++;
        }

        // Ice worlds should be at least 50% polar
        assertThat((double) polarCount / zones.length)
            .as("Ice world should be mostly polar")
            .isGreaterThan(0.5);
    }

    @Test
    @DisplayName("TROPICAL_WORLD model has extended tropical zones")
    void tropicalWorldExtendedTropical() {
        ClimateCalculator tropicalCalc = new ClimateCalculator(polygons,
            ClimateCalculator.ClimateModel.TROPICAL_WORLD);
        ClimateZone[] zones = tropicalCalc.calculate();

        // Check polygon at 40 degrees (would be temperate in simple model)
        Vector3D center40 = new Vector3D(
            Math.cos(Math.toRadians(40)),
            Math.sin(Math.toRadians(40)),
            0
        ).normalize();
        Polygon poly40 = new Polygon(center40, createDummyVertices(center40));

        ClimateCalculator calc = new ClimateCalculator(List.of(poly40),
            ClimateCalculator.ClimateModel.TROPICAL_WORLD);
        ClimateZone[] result = calc.calculate();

        assertThat(result[0])
            .as("40° latitude should be tropical in tropical world model")
            .isEqualTo(ClimateZone.TROPICAL);
    }

    @Test
    @DisplayName("TROPICAL_WORLD model has smaller polar caps")
    void tropicalWorldSmallPolarCaps() {
        ClimateCalculator tropicalCalc = new ClimateCalculator(polygons,
            ClimateCalculator.ClimateModel.TROPICAL_WORLD);
        ClimateCalculator simpleCalc = new ClimateCalculator(polygons,
            ClimateCalculator.ClimateModel.SIMPLE_LATITUDE);

        ClimateZone[] tropicalZones = tropicalCalc.calculate();
        ClimateZone[] simpleZones = simpleCalc.calculate();

        int tropicalPolar = 0, simplePolar = 0;
        for (ClimateZone zone : tropicalZones) {
            if (zone == ClimateZone.POLAR) tropicalPolar++;
        }
        for (ClimateZone zone : simpleZones) {
            if (zone == ClimateZone.POLAR) simplePolar++;
        }

        assertThat(tropicalPolar)
            .as("Tropical world should have fewer polar zones than simple latitude model")
            .isLessThan(simplePolar);
    }

    @Test
    @DisplayName("TIDALLY_LOCKED model zones based on longitude not latitude")
    void tidallyLockedLongitudeBased() {
        // Create polygons at same latitude but different longitudes
        // Day side (positive X)
        Vector3D dayCenter = new Vector3D(1, 0, 0);
        Polygon dayPoly = new Polygon(dayCenter, createDummyVertices(dayCenter));

        // Night side (negative X)
        Vector3D nightCenter = new Vector3D(-1, 0, 0);
        Polygon nightPoly = new Polygon(nightCenter, createDummyVertices(nightCenter));

        ClimateCalculator tidalCalc = new ClimateCalculator(List.of(dayPoly, nightPoly),
            ClimateCalculator.ClimateModel.TIDALLY_LOCKED);
        ClimateZone[] zones = tidalCalc.calculate();

        assertThat(zones[0])
            .as("Day side should be tropical (hot)")
            .isEqualTo(ClimateZone.TROPICAL);
        assertThat(zones[1])
            .as("Night side should be polar (frozen)")
            .isEqualTo(ClimateZone.POLAR);
    }

    @Test
    @DisplayName("HADLEY_CELLS model includes subtropical transition")
    void hadleyCellsSubtropical() {
        ClimateCalculator hadleyCalc = new ClimateCalculator(polygons,
            ClimateCalculator.ClimateModel.HADLEY_CELLS);
        ClimateZone[] zones = hadleyCalc.calculate();

        // Should still have all three zone types
        boolean hasTropical = false, hasTemperate = false, hasPolar = false;
        for (ClimateZone zone : zones) {
            if (zone == ClimateZone.TROPICAL) hasTropical = true;
            if (zone == ClimateZone.TEMPERATE) hasTemperate = true;
            if (zone == ClimateZone.POLAR) hasPolar = true;
        }

        assertThat(hasTropical).as("Hadley model should have tropical zones").isTrue();
        assertThat(hasTemperate).as("Hadley model should have temperate zones").isTrue();
        assertThat(hasPolar).as("Hadley model should have polar zones").isTrue();
    }

    @Test
    @DisplayName("Seasonal tilt shifts high-latitude climate zones")
    void seasonalTiltShiftsBelts() {
        Vector3D center80 = new Vector3D(
            Math.cos(Math.toRadians(80)),
            Math.sin(Math.toRadians(80)),
            0
        ).normalize();
        Polygon highLat = new Polygon(center80, createDummyVertices(center80));

        ClimateCalculator noTilt = new ClimateCalculator(List.of(highLat),
            ClimateCalculator.ClimateModel.SEASONAL, 0.0, 0.0, 24);
        ClimateCalculator highTilt = new ClimateCalculator(List.of(highLat),
            ClimateCalculator.ClimateModel.SEASONAL, 60.0, 0.0, 24);

        assertThat(noTilt.calculate()[0])
            .as("High latitude should be polar without tilt")
            .isEqualTo(ClimateZone.POLAR);
        assertThat(highTilt.calculate()[0])
            .as("High tilt should warm high latitudes into temperate range")
            .isEqualTo(ClimateZone.TEMPERATE);
    }

    @Test
    @DisplayName("Climate model can be specified in PlanetConfig")
    void climateModelInConfig() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .climateModel(ClimateCalculator.ClimateModel.ICE_WORLD)
            .build();

        assertThat(config.climateModel())
            .isEqualTo(ClimateCalculator.ClimateModel.ICE_WORLD);
    }

    @Test
    @DisplayName("Default climate model is SIMPLE_LATITUDE")
    void defaultClimateModel() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .build();

        assertThat(config.climateModel())
            .isEqualTo(ClimateCalculator.ClimateModel.SIMPLE_LATITUDE);
    }

    @Test
    @DisplayName("Null model defaults to SIMPLE_LATITUDE")
    void nullModelDefault() {
        ClimateCalculator calc = new ClimateCalculator(polygons, null);
        ClimateZone[] zones = calc.calculate();

        // Should behave like simple latitude model
        for (int i = 0; i < polygons.size(); i++) {
            double latitude = Math.abs(ClimateCalculator.getLatitudeDegrees(polygons.get(i)));
            if (latitude < 25) {
                assertThat(zones[i]).isEqualTo(ClimateZone.TROPICAL);
            }
        }
    }
}
