package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.test.TestFXBase;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterisation test for {@link SolarSystemRenderer}. Asserts the shape of the
 * scene graph that {@code render()} produces against a known fixture, so any
 * Phase 4.1 decomposition that changes behaviour (rather than just structure)
 * will fail loudly.
 * <p>
 * Headless TestFX boots the JavaFX toolkit; the renderer is invoked on the FX
 * thread via {@code interact()}. Each test inspects {@link Group} child counts
 * across the public sub-groups exposed via the renderer's getters.
 */
class SolarSystemRendererCharacterizationTest extends TestFXBase {

    @BeforeAll
    static void enableHeadlessIfMissing() {
        // The surefire config in pom.xml already sets testfx.headless=true, but
        // be defensive: if this test is run from an IDE without those props,
        // turn on headless mode so the JavaFX toolkit doesn't try to open a
        // real window.
        if (System.getProperty("testfx.headless") == null) {
            System.setProperty("testfx.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }

    @Override
    public void start(Stage stage) {
        // We don't need a real UI — only the toolkit must be up so the renderer
        // can construct Sphere/Label/etc. nodes.
        stage.setScene(new Scene(new Group(), 1, 1));
        stage.show();
    }

    @Test
    @DisplayName("render(): empty system produces an empty sub-tree")
    void rendersEmptySystem() {
        SolarSystemDescription d = new SolarSystemDescription();
        d.setStarDisplayRecord(sol());

        Group result = renderOnFx(d);

        assertNotNull(result);
        // System group should at least contain the standard sub-groups (scale grid,
        // habitable zone, ecliptic plane, asteroid belt, kuiper belt, features,
        // orbits, orbit nodes, apsides, rings, planets, labels).
        assertEquals(12, result.getChildren().size(),
                "systemGroup top-level child count should be the stable 12 sub-groups");
    }

    @Test
    @DisplayName("render(): planets produce matching orbits, spheres, and descriptions")
    void rendersThreePlanets() {
        SolarSystemDescription d = new SolarSystemDescription();
        d.setStarDisplayRecord(sol());
        d.setHabitableZoneInnerAU(0.95);
        d.setHabitableZoneOuterAU(1.37);
        d.setPlanetDescriptionList(List.of(
                planet("Mercury", 0.39, 0.21, 7.0),
                planet("Venus",   0.72, 0.01, 3.4),
                planet("Earth",   1.00, 0.02, 0.0)));

        SolarSystemRenderer renderer = new SolarSystemRenderer();
        Group result = renderOnFx(renderer, d);
        assertNotNull(result);

        // Each planet must register a sphere keyed by name.
        assertEquals(3, renderer.getPlanetNodes().size(),
                "planetNodes should hold one Sphere per planet");
        assertTrue(renderer.getPlanetNodes().containsKey("Mercury"));
        assertTrue(renderer.getPlanetNodes().containsKey("Venus"));
        assertTrue(renderer.getPlanetNodes().containsKey("Earth"));

        // Each planet must contribute an orbit group keyed by name.
        assertEquals(3, renderer.getOrbitGroups().size(),
                "orbitGroups should hold one Group per planet");

        // planetDescriptions is populated alongside planetNodes.
        assertEquals(3, renderer.getPlanetDescriptions().size(),
                "planetDescriptions should hold one entry per planet");

        // Habitable zone should have produced at least one shape.
        assertFalse(renderer.getHabitableZoneGroup().getChildren().isEmpty(),
                "habitable zone group should be populated");

        // shapeToLabel is populated by callers (SolarSystemLabelManager), not the
        // renderer itself — confirm that contract.
        assertTrue(renderer.getShapeToLabel().isEmpty(),
                "shapeToLabel is populated externally (by SolarSystemLabelManager.registerLabel)");
    }

    @Test
    @DisplayName("render(): planets populate the orbit + planet groups")
    void rendersPlanetsIntoGroups() {
        SolarSystemDescription d = new SolarSystemDescription();
        d.setStarDisplayRecord(sol());
        d.setPlanetDescriptionList(List.of(
                planet("Earth", 1.0, 0.02, 0.0),
                planet("Mars",  1.52, 0.09, 1.85)));

        SolarSystemRenderer renderer = new SolarSystemRenderer();
        renderOnFx(renderer, d);

        assertFalse(renderer.getOrbitsGroup().getChildren().isEmpty(),
                "orbits group should hold rendered orbit ellipses");
        assertFalse(renderer.getPlanetsGroup().getChildren().isEmpty(),
                "planets group should hold rendered planet spheres");
        // labelsGroup is populated externally by SolarSystemLabelManager
        // (see registerLabel/createLabel API on the renderer) — render() itself
        // does not add label nodes.
    }

    @Test
    @DisplayName("clear(): purges all rendered state without keeping orphans")
    void clearWipesState() {
        SolarSystemDescription d = new SolarSystemDescription();
        d.setStarDisplayRecord(sol());
        d.setPlanetDescriptionList(List.of(planet("Earth", 1.0, 0.02, 0.0)));

        SolarSystemRenderer renderer = new SolarSystemRenderer();
        renderOnFx(renderer, d);
        assertFalse(renderer.getPlanetNodes().isEmpty());

        // clear() runs on the FX thread (mutates the scene graph).
        interact(renderer::clear);

        assertTrue(renderer.getPlanetNodes().isEmpty(), "planetNodes should be empty after clear");
        assertTrue(renderer.getOrbitGroups().isEmpty(), "orbitGroups should be empty after clear");
        assertTrue(renderer.getPlanetsGroup().getChildren().isEmpty(),
                "planetsGroup children should be cleared");
        assertTrue(renderer.getOrbitsGroup().getChildren().isEmpty(),
                "orbitsGroup children should be cleared");
    }

    // ----- fixture helpers -----

    private static StarDisplayRecord sol() {
        StarDisplayRecord s = new StarDisplayRecord();
        s.setStarName("Sol");
        s.setSpectralClass("G2V");
        s.setRadius(1.0);
        s.setMass(1.0);
        s.setTemperature(5778);
        return s;
    }

    private static PlanetDescription planet(String name, double smaAU, double e, double iDeg) {
        PlanetDescription p = new PlanetDescription();
        p.setId(name);
        p.setName(name);
        p.setSemiMajorAxis(smaAU);
        p.setEccentricity(e);
        p.setInclination(iDeg);
        p.setMass(1.0);
        p.setRadius(1.0);
        return p;
    }

    /** Convenience: build a fresh renderer + render against {@code d} on the FX thread. */
    private Group renderOnFx(SolarSystemDescription d) {
        return renderOnFx(new SolarSystemRenderer(), d);
    }

    private Group renderOnFx(SolarSystemRenderer renderer, SolarSystemDescription d) {
        AtomicReference<Group> out = new AtomicReference<>();
        interact(() -> out.set(renderer.render(d)));
        return out.get();
    }
}
