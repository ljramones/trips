package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.model.FeatureDescription;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase E.1 Step 7 — pins the {@link SolarSystemRenderer} dispatch arms for the new
 * {@code JUMP_POINT} feature type.
 *
 * <p>Tests target the package-private static helpers
 * {@link SolarSystemRenderer#featureSizeMultiplier(String)} and
 * {@link SolarSystemRenderer#defaultFeatureColor(String)} — pure-function extracts of the
 * renderer's per-featureType dispatch. Tests also verify {@link FeatureDescription#isPointType()}
 * recognises JUMP_POINT so the renderer's point/belt routing picks it up.
 *
 * <p>The full {@code renderPointFeature} + glow special-case path is verified at the integration
 * level by the existing {@code FeatureDescription} + {@code FeatureType} constants tests + this
 * test class's dispatch coverage. Direct unit testing of the full method body would require
 * constructing a {@code SolarSystemRenderer} instance (with ScaleManager + many dependencies)
 * and is deliberately out of scope; the size/color/glow split into pure helpers is the
 * test-seam refactor.
 */
class SolarSystemRendererJumpPointTest {

    // ============================================================
    // Size dispatch
    // ============================================================

    @Test
    @DisplayName("JUMP_POINT size multiplier is 1.5× baseSize (smaller than JUMP_GATE, larger than RESEARCH_STATION)")
    void jumpPointSizeMultiplier() {
        assertEquals(1.5, SolarSystemRenderer.featureSizeMultiplier("JUMP_POINT"), 1e-9);
    }

    @Test
    @DisplayName("JUMP_POINT is distinct in size from JUMP_GATE (1.5× vs 2.0×)")
    void jumpPointSizeDistinctFromJumpGate() {
        assertNotEquals(
                SolarSystemRenderer.featureSizeMultiplier("JUMP_GATE"),
                SolarSystemRenderer.featureSizeMultiplier("JUMP_POINT"),
                "constructed gates and natural jump points should be visually distinguishable by size");
    }

    @Test
    @DisplayName("size dispatch covers all 7 sized feature types + default")
    void sizeDispatchCoversAllKnownTypes() {
        // Each of these should map to a multiplier != 1.0 (non-default).
        String[] sized = {"JUMP_GATE", "JUMP_POINT", "ORBITAL_HABITAT", "SHIPYARD",
                "MINING_OPERATION", "TROJAN_CLUSTER"};
        for (String featureType : sized) {
            double multiplier = SolarSystemRenderer.featureSizeMultiplier(featureType);
            assertTrue(multiplier > 0, featureType + " should have positive size multiplier");
            assertNotEquals(1.0, multiplier, featureType + " should have non-default size");
        }
        // RESEARCH_STATION is explicitly 1.0× (baseline size) — distinct case.
        assertEquals(1.0, SolarSystemRenderer.featureSizeMultiplier("RESEARCH_STATION"), 1e-9);
        // Unknown types fall through to the default multiplier of 1.0.
        assertEquals(1.0, SolarSystemRenderer.featureSizeMultiplier("UNKNOWN_TYPE"), 1e-9);
    }

    // ============================================================
    // Color dispatch
    // ============================================================

    @Test
    @DisplayName("JUMP_POINT default color is MEDIUMPURPLE (distinctive vs all other feature colors)")
    void jumpPointColorIsMediumPurple() {
        assertEquals(Color.MEDIUMPURPLE, SolarSystemRenderer.defaultFeatureColor("JUMP_POINT"));
    }

    @Test
    @DisplayName("JUMP_POINT color is distinct from JUMP_GATE (MEDIUMPURPLE vs CYAN)")
    void jumpPointColorDistinctFromJumpGate() {
        assertNotEquals(
                SolarSystemRenderer.defaultFeatureColor("JUMP_GATE"),
                SolarSystemRenderer.defaultFeatureColor("JUMP_POINT"),
                "jump points should be a visually distinct color from constructed gates");
    }

    @Test
    @DisplayName("color dispatch covers all 8 typed colors + WHITE default")
    void colorDispatchCoversAllKnownTypes() {
        // Each known feature type should produce a non-white color.
        String[] typed = {"JUMP_GATE", "JUMP_POINT", "ORBITAL_HABITAT", "SHIPYARD",
                "RESEARCH_STATION", "MINING_OPERATION", "TROJAN_CLUSTER", "DEFENSE_PERIMETER"};
        for (String featureType : typed) {
            Color color = SolarSystemRenderer.defaultFeatureColor(featureType);
            assertNotEquals(Color.WHITE, color,
                    featureType + " should have a non-default (non-white) color");
        }
        // Unknown types fall through to WHITE.
        assertEquals(Color.WHITE, SolarSystemRenderer.defaultFeatureColor("UNKNOWN_TYPE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"JUMP_GATE", "ORBITAL_HABITAT", "SHIPYARD", "RESEARCH_STATION",
            "MINING_OPERATION", "TROJAN_CLUSTER", "DEFENSE_PERIMETER"})
    @DisplayName("JUMP_POINT's color is distinct from every other typed feature color")
    void jumpPointColorIsUniqueAmongTypedFeatures(String otherType) {
        Color jumpPoint = SolarSystemRenderer.defaultFeatureColor("JUMP_POINT");
        Color other = SolarSystemRenderer.defaultFeatureColor(otherType);
        assertNotEquals(other, jumpPoint,
                "JUMP_POINT color must be visually distinct from " + otherType);
    }

    // ============================================================
    // Point-type routing
    // ============================================================

    @Test
    @DisplayName("FeatureDescription.isPointType() recognises JUMP_POINT (renderer's point/belt routing picks it up)")
    void featureDescriptionRecognisesJumpPointAsPointType() {
        FeatureDescription jp = new FeatureDescription();
        jp.setFeatureType("JUMP_POINT");
        assertTrue(jp.isPointType(),
                "renderFeatures() dispatches via isPointType() — JUMP_POINT must return true so "
                        + "renderPointFeature() gets called");
    }

    @Test
    @DisplayName("FeatureDescription.isBeltType() returns false for JUMP_POINT (not a belt)")
    void featureDescriptionDoesNotMisclassifyJumpPointAsBelt() {
        FeatureDescription jp = new FeatureDescription();
        jp.setFeatureType("JUMP_POINT");
        assertFalse(jp.isBeltType(),
                "JUMP_POINT must not classify as a belt-type feature");
    }
}
