package com.teamgannon.trips.noisegen;

import com.teamgannon.trips.noisegen.NoiseTypes.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoiseConfig configuration holder class.
 */
class NoiseConfigTest {

    private NoiseConfig config;

    @BeforeEach
    void setUp() {
        config = new NoiseConfig();
    }

    @Nested
    @DisplayName("Default value tests")
    class DefaultValueTests {

        @Test
        @DisplayName("default seed should be 1337")
        void defaultSeedShouldBe1337() {
            assertEquals(1337, config.getSeed());
        }

        @Test
        @DisplayName("default frequency should be 0.01")
        void defaultFrequencyShouldBe001() {
            assertEquals(0.01f, config.getFrequency(), 0.0001f);
        }

        @Test
        @DisplayName("default noise type should be OpenSimplex2")
        void defaultNoiseTypeShouldBeOpenSimplex2() {
            assertEquals(NoiseType.OpenSimplex2, config.getNoiseType());
        }

        @Test
        @DisplayName("default rotation type should be None")
        void defaultRotationTypeShouldBeNone() {
            assertEquals(RotationType3D.None, config.getRotationType3D());
        }

        @Test
        @DisplayName("default fractal type should be None")
        void defaultFractalTypeShouldBeNone() {
            assertEquals(FractalType.None, config.getFractalType());
        }

        @Test
        @DisplayName("default octaves should be 3")
        void defaultOctavesShouldBe3() {
            assertEquals(3, config.getOctaves());
        }

        @Test
        @DisplayName("default lacunarity should be 2.0")
        void defaultLacunarityShouldBe2() {
            assertEquals(2.0f, config.getLacunarity(), 0.0001f);
        }

        @Test
        @DisplayName("default gain should be 0.5")
        void defaultGainShouldBe05() {
            assertEquals(0.5f, config.getGain(), 0.0001f);
        }

        @Test
        @DisplayName("default weighted strength should be 0.0")
        void defaultWeightedStrengthShouldBe0() {
            assertEquals(0.0f, config.getWeightedStrength(), 0.0001f);
        }

        @Test
        @DisplayName("default ping pong strength should be 2.0")
        void defaultPingPongStrengthShouldBe2() {
            assertEquals(2.0f, config.getPingPongStrength(), 0.0001f);
        }

        @Test
        @DisplayName("default cellular distance function should be EuclideanSq")
        void defaultCellularDistanceFunctionShouldBeEuclideanSq() {
            assertEquals(CellularDistanceFunction.EuclideanSq, config.getCellularDistanceFunction());
        }

        @Test
        @DisplayName("default cellular return type should be Distance")
        void defaultCellularReturnTypeShouldBeDistance() {
            assertEquals(CellularReturnType.Distance, config.getCellularReturnType());
        }

        @Test
        @DisplayName("default cellular jitter modifier should be 1.0")
        void defaultCellularJitterModifierShouldBe1() {
            assertEquals(1.0f, config.getCellularJitterModifier(), 0.0001f);
        }

        @Test
        @DisplayName("default domain warp type should be OpenSimplex2")
        void defaultDomainWarpTypeShouldBeOpenSimplex2() {
            assertEquals(DomainWarpType.OpenSimplex2, config.getDomainWarpType());
        }

        @Test
        @DisplayName("default domain warp amp should be 1.0")
        void defaultDomainWarpAmpShouldBe1() {
            assertEquals(1.0f, config.getDomainWarpAmp(), 0.0001f);
        }
    }

    @Nested
    @DisplayName("Setter/Getter tests")
    class SetterGetterTests {

        @Test
        @DisplayName("setSeed should update seed value")
        void setSeedShouldUpdateSeedValue() {
            config.setSeed(42);
            assertEquals(42, config.getSeed());
        }

        @Test
        @DisplayName("setFrequency should update frequency value")
        void setFrequencyShouldUpdateFrequencyValue() {
            config.setFrequency(0.05f);
            assertEquals(0.05f, config.getFrequency(), 0.0001f);
        }

        @Test
        @DisplayName("setNoiseType should update noise type")
        void setNoiseTypeShouldUpdateNoiseType() {
            config.setNoiseType(NoiseType.Perlin);
            assertEquals(NoiseType.Perlin, config.getNoiseType());
        }

        @Test
        @DisplayName("setRotationType3D should update rotation type")
        void setRotationType3DShouldUpdateRotationType() {
            config.setRotationType3D(RotationType3D.ImproveXYPlanes);
            assertEquals(RotationType3D.ImproveXYPlanes, config.getRotationType3D());
        }

        @Test
        @DisplayName("setFractalType should update fractal type")
        void setFractalTypeShouldUpdateFractalType() {
            config.setFractalType(FractalType.FBm);
            assertEquals(FractalType.FBm, config.getFractalType());
        }

        @Test
        @DisplayName("setOctaves should update octaves value")
        void setOctavesShouldUpdateOctavesValue() {
            config.setOctaves(5);
            assertEquals(5, config.getOctaves());
        }

        @Test
        @DisplayName("setLacunarity should update lacunarity value")
        void setLacunarityShouldUpdateLacunarityValue() {
            config.setLacunarity(3.0f);
            assertEquals(3.0f, config.getLacunarity(), 0.0001f);
        }

        @Test
        @DisplayName("setGain should update gain value")
        void setGainShouldUpdateGainValue() {
            config.setGain(0.6f);
            assertEquals(0.6f, config.getGain(), 0.0001f);
        }

        @Test
        @DisplayName("setWeightedStrength should update weighted strength value")
        void setWeightedStrengthShouldUpdateWeightedStrengthValue() {
            config.setWeightedStrength(0.5f);
            assertEquals(0.5f, config.getWeightedStrength(), 0.0001f);
        }

        @Test
        @DisplayName("setPingPongStrength should update ping pong strength value")
        void setPingPongStrengthShouldUpdatePingPongStrengthValue() {
            config.setPingPongStrength(3.0f);
            assertEquals(3.0f, config.getPingPongStrength(), 0.0001f);
        }

        @Test
        @DisplayName("setCellularDistanceFunction should update cellular distance function")
        void setCellularDistanceFunctionShouldUpdateCellularDistanceFunction() {
            config.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
            assertEquals(CellularDistanceFunction.Manhattan, config.getCellularDistanceFunction());
        }

        @Test
        @DisplayName("setCellularReturnType should update cellular return type")
        void setCellularReturnTypeShouldUpdateCellularReturnType() {
            config.setCellularReturnType(CellularReturnType.CellValue);
            assertEquals(CellularReturnType.CellValue, config.getCellularReturnType());
        }

        @Test
        @DisplayName("setCellularJitterModifier should update cellular jitter modifier")
        void setCellularJitterModifierShouldUpdateCellularJitterModifier() {
            config.setCellularJitterModifier(0.5f);
            assertEquals(0.5f, config.getCellularJitterModifier(), 0.0001f);
        }

        @Test
        @DisplayName("setDomainWarpType should update domain warp type")
        void setDomainWarpTypeShouldUpdateDomainWarpType() {
            config.setDomainWarpType(DomainWarpType.BasicGrid);
            assertEquals(DomainWarpType.BasicGrid, config.getDomainWarpType());
        }

        @Test
        @DisplayName("setDomainWarpAmp should update domain warp amp")
        void setDomainWarpAmpShouldUpdateDomainWarpAmp() {
            config.setDomainWarpAmp(50.0f);
            assertEquals(50.0f, config.getDomainWarpAmp(), 0.0001f);
        }
    }

    @Nested
    @DisplayName("Transform type derivation tests")
    class TransformTypeDerivationTests {

        @Test
        @DisplayName("OpenSimplex2 with None rotation should use DefaultOpenSimplex2 transform")
        void openSimplex2WithNoneRotationShouldUseDefaultTransform() {
            config.setNoiseType(NoiseType.OpenSimplex2);
            config.setRotationType3D(RotationType3D.None);
            assertEquals(TransformType3D.DefaultOpenSimplex2, config.getTransformType3D());
        }

        @Test
        @DisplayName("OpenSimplex2 with ImproveXYPlanes should use ImproveXYPlanes transform")
        void openSimplex2WithImproveXYPlanesShouldUseImproveXYPlanesTransform() {
            config.setNoiseType(NoiseType.OpenSimplex2);
            config.setRotationType3D(RotationType3D.ImproveXYPlanes);
            assertEquals(TransformType3D.ImproveXYPlanes, config.getTransformType3D());
        }

        @Test
        @DisplayName("OpenSimplex2 with ImproveXZPlanes should use ImproveXZPlanes transform")
        void openSimplex2WithImproveXZPlanesShouldUseImproveXZPlanesTransform() {
            config.setNoiseType(NoiseType.OpenSimplex2);
            config.setRotationType3D(RotationType3D.ImproveXZPlanes);
            assertEquals(TransformType3D.ImproveXZPlanes, config.getTransformType3D());
        }

        @Test
        @DisplayName("OpenSimplex2S with None rotation should use DefaultOpenSimplex2 transform")
        void openSimplex2SWithNoneRotationShouldUseDefaultTransform() {
            config.setNoiseType(NoiseType.OpenSimplex2S);
            config.setRotationType3D(RotationType3D.None);
            assertEquals(TransformType3D.DefaultOpenSimplex2, config.getTransformType3D());
        }

        @Test
        @DisplayName("Perlin noise should use None transform by default")
        void perlinNoiseShouldUseNoneTransform() {
            config.setNoiseType(NoiseType.Perlin);
            config.setRotationType3D(RotationType3D.None);
            assertEquals(TransformType3D.None, config.getTransformType3D());
        }

        @Test
        @DisplayName("Value noise should use None transform by default")
        void valueNoiseShouldUseNoneTransform() {
            config.setNoiseType(NoiseType.Value);
            config.setRotationType3D(RotationType3D.None);
            assertEquals(TransformType3D.None, config.getTransformType3D());
        }
    }

    @Nested
    @DisplayName("Fractal bounding calculation tests")
    class FractalBoundingCalculationTests {

        @Test
        @DisplayName("fractal bounding should be calculated from gain and octaves")
        void fractalBoundingShouldBeCalculatedFromGainAndOctaves() {
            config.setGain(0.5f);
            config.setOctaves(3);

            // Fractal bounding should be positive
            float bounding = config.getFractalBounding();
            assertTrue(bounding > 0, "Fractal bounding should be positive");
            assertTrue(bounding <= 1.0f, "Fractal bounding should be <= 1.0 for gain <= 1.0");
        }

        @Test
        @DisplayName("increasing octaves should decrease fractal bounding")
        void increasingOctavesShouldDecreaseFractalBounding() {
            config.setGain(0.5f);

            config.setOctaves(2);
            float bounding2 = config.getFractalBounding();

            config.setOctaves(5);
            float bounding5 = config.getFractalBounding();

            assertTrue(bounding5 < bounding2,
                "More octaves should result in smaller fractal bounding");
        }

        @Test
        @DisplayName("higher gain should result in smaller fractal bounding")
        void higherGainShouldResultInSmallerFractalBounding() {
            config.setOctaves(4);

            config.setGain(0.3f);
            float boundingLow = config.getFractalBounding();

            config.setGain(0.7f);
            float boundingHigh = config.getFractalBounding();

            assertTrue(boundingHigh < boundingLow,
                "Higher gain should result in smaller fractal bounding");
        }
    }
}
