package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.model.PlanetDescription;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;
import com.terranrepublic.assets.AliasTargetKind;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase F.2 §6.1 — covers {@link BodyRenderer}'s F.2-aware tooltip text builders:
 * {@code buildStarTooltipText(StarDisplayRecord)} and
 * {@code buildPlanetTooltipText(PlanetDescription)}.
 *
 * <p>BodyRenderer construction is heavy (many collaborators), so this test wires a minimal
 * set: collaborators are mocked, scene groups + maps are real empty instances. The targets
 * under test are the two text-building methods, not the full {@code renderPlanet} pipeline.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BodyRendererTooltipTest {

    @Mock private ScaleManager scaleManager;
    @Mock private OrbitVisualizer orbitVisualizer;
    @Mock private com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider orbitSamplingProvider;
    @Mock private OrbitMarkerRenderer orbitMarkerRenderer;
    @Mock private SelectionStyleManager selectionStyleManager;
    @Mock private PlanetaryRingManager rings;
    @Mock private AliasDesignerService aliasService;

    private BodyRenderer rendererWithAliases;
    private BodyRenderer rendererWithoutAliases;

    @BeforeEach
    void setUp() {
        rendererWithAliases = newRenderer(aliasService);
        rendererWithoutAliases = newRenderer(null);
    }

    private BodyRenderer newRenderer(AliasDesignerService svc) {
        BodyRenderer r = new BodyRenderer(
                scaleManager, orbitVisualizer, orbitSamplingProvider,
                orbitMarkerRenderer, selectionStyleManager, rings,
                new Group(), new Group(), new Group(), new Group(),
                new HashMap<String, Sphere>(),
                new HashMap<String, Node>(),
                new HashMap<String, PlanetDescription>(),
                new HashMap<String, Group>(),
                new HashMap<String, Color>(),
                new HashMap<String, List<Group>>());
        r.setAliasService(svc);
        return r;
    }

    private static StarDisplayRecord starRecord(String id, String name, String spectral, double distLy) {
        StarDisplayRecord r = new StarDisplayRecord();
        r.setRecordId(id);
        r.setStarName(name);
        r.setSpectralClass(spectral);
        r.setDistance(distLy);
        return r;
    }

    private static PlanetDescription planetRecord(String id, String name,
                                                  double sma, double period, double radius) {
        PlanetDescription p = new PlanetDescription();
        p.setId(id);
        p.setName(name);
        p.setSemiMajorAxis(sma);
        p.setOrbitalPeriod(period);
        p.setRadius(radius);
        return p;
    }

    // ===== star tooltip =====

    @Test
    @DisplayName("star tooltip: null aliasService → legacy 'name + spectral + distance' format")
    void starNoAliasServiceLegacyFormat() {
        String text = rendererWithoutAliases.buildStarTooltipText(
                starRecord("star-40", "40 Eri A", "K1V", 16.45));
        assertEquals("40 Eri A\nSpectral: K1V\nDistance: 16.45 ly", text);
    }

    @Test
    @DisplayName("star tooltip: zero active aliases → legacy format, but service was queried")
    void starZeroAliasesQueriesServiceReturnsLegacy() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-40"))
                .thenReturn(List.of());
        String text = rendererWithAliases.buildStarTooltipText(
                starRecord("star-40", "40 Eri A", "K1V", 16.45));
        assertEquals("40 Eri A\nSpectral: K1V\nDistance: 16.45 ly", text);
        verify(aliasService).findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-40");
    }

    @Test
    @DisplayName("star tooltip: one alias appends ' (universeName)' line")
    void starOneAliasAppendsLine() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-40"))
                .thenReturn(List.of(new AliasDisplay("Vulcan", "Star Trek")));
        String text = rendererWithAliases.buildStarTooltipText(
                starRecord("star-40", "40 Eri A", "K1V", 16.45));
        assertEquals("40 Eri A\nSpectral: K1V\nDistance: 16.45 ly\nVulcan (Star Trek)", text);
    }

    @Test
    @DisplayName("star tooltip: null recordId on star → no service query (defensive)")
    void starNullRecordIdSkipsService() {
        StarDisplayRecord r = starRecord(null, "Unknown", "G2V", 0.0);
        String text = rendererWithAliases.buildStarTooltipText(r);
        // Falls back to legacy format
        assertEquals("Unknown\nSpectral: G2V\nDistance: 0.00 ly", text);
        verify(aliasService, never()).findActiveAliasesForTooltip(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    // ===== planet tooltip =====

    @Test
    @DisplayName("planet tooltip: null aliasService → legacy orbital-metadata format")
    void planetNoAliasServiceLegacyFormat() {
        String text = rendererWithoutAliases.buildPlanetTooltipText(
                planetRecord("ep-1", "Mars", 1.524, 686.97, 0.532));
        assertEquals("Mars\nSemi-major axis: 1.524 AU\nPeriod: 687.0 days\nRadius: 0.53 Earth", text);
    }

    @Test
    @DisplayName("planet tooltip: zero active aliases → legacy format, but service was queried")
    void planetZeroAliasesQueriesServiceReturnsLegacy() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "ep-1"))
                .thenReturn(List.of());
        String text = rendererWithAliases.buildPlanetTooltipText(
                planetRecord("ep-1", "Mars", 1.524, 686.97, 0.532));
        assertEquals("Mars\nSemi-major axis: 1.524 AU\nPeriod: 687.0 days\nRadius: 0.53 Earth", text);
        verify(aliasService).findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "ep-1");
    }

    @Test
    @DisplayName("planet tooltip: one alias appends ' (universeName)' line")
    void planetOneAliasAppendsLine() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "ep-1"))
                .thenReturn(List.of(new AliasDisplay("Barsoom", "John Carter of Mars")));
        String text = rendererWithAliases.buildPlanetTooltipText(
                planetRecord("ep-1", "Mars", 1.524, 686.97, 0.532));
        assertEquals(
                "Mars\nSemi-major axis: 1.524 AU\nPeriod: 687.0 days\nRadius: 0.53 Earth\nBarsoom (John Carter of Mars)",
                text);
    }

    @Test
    @DisplayName("planet tooltip: routes EXOPLANET kind, not STAR (kind-discriminator pin)")
    void planetRoutesExoplanetKind() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "ep-1"))
                .thenReturn(List.of());
        rendererWithAliases.buildPlanetTooltipText(planetRecord("ep-1", "Mars", 1.0, 365.0, 1.0));
        verify(aliasService).findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "ep-1");
        // Never STAR kind from the planet renderer path
        verify(aliasService, never()).findActiveAliasesForTooltip(AliasTargetKind.STAR, "ep-1");
    }

    @Test
    @DisplayName("planet tooltip: null planet.id → no service query (procedurally-generated bodies)")
    void planetNullIdSkipsService() {
        PlanetDescription p = planetRecord(null, "Procedural-3", 5.0, 1500.0, 2.0);
        String text = rendererWithAliases.buildPlanetTooltipText(p);
        // Legacy format intact
        assertTrue(text.startsWith("Procedural-3\nSemi-major axis: 5.000 AU"),
                "format intact: " + text);
        verify(aliasService, never()).findActiveAliasesForTooltip(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
