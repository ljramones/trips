package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;
import com.terranrepublic.assets.AliasTargetKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase F.2 §6.1 — covers {@link StarRenderer#buildStarTooltipText} integration with
 * {@link AliasDesignerService#findActiveAliasesForTooltip}: alias service is consulted with
 * the star's recordId + STAR kind, then the formatter renders the multi-line text.
 *
 * <p>The actual tooltip-install + hover-glow behavior is tested via the existing
 * {@code StarRendererTest}; this file pins F.2's contract on the text content + service
 * routing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StarRendererTooltipTest {

    @Mock private StarLODManager lodManager;
    @Mock private StarLabelManager labelManager;
    @Mock private InterstellarScaleManager scaleManager;
    @Mock private SpecialStarMeshManager meshManager;
    @Mock private PolityObjectFactory polityObjectFactory;
    @Mock private StarClickHandler clickHandler;
    @Mock private AliasDesignerService aliasService;

    private StarRenderer rendererWithAliases;
    private StarRenderer rendererWithoutAliases;

    @BeforeEach
    void setUp() {
        rendererWithAliases = new StarRenderer(lodManager, labelManager, scaleManager,
                meshManager, polityObjectFactory, clickHandler, aliasService);
        rendererWithoutAliases = new StarRenderer(lodManager, labelManager, scaleManager,
                meshManager, polityObjectFactory, clickHandler, null);
    }

    private static StarDisplayRecord starRecord(String id, String name, String polity) {
        StarDisplayRecord r = new StarDisplayRecord();
        r.setRecordId(id);
        r.setStarName(name);
        r.setPolity(polity);
        return r;
    }

    @Test
    @DisplayName("null aliasService: tooltip text is name + polity only (no service interaction)")
    void nullAliasServiceProducesBaselineText() {
        String text = rendererWithoutAliases.buildStarTooltipText(
                starRecord("star-1", "Sol", "Terran Republic"));
        assertEquals("Sol\nPolity: Terran Republic", text);
    }

    @Test
    @DisplayName("zero active aliases: tooltip text is name + polity only")
    void zeroActiveAliasesProducesBaselineText() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-1"))
                .thenReturn(List.of());
        String text = rendererWithAliases.buildStarTooltipText(
                starRecord("star-1", "Sol", "Terran Republic"));
        assertEquals("Sol\nPolity: Terran Republic", text);
        verify(aliasService).findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-1");
    }

    @Test
    @DisplayName("one active alias: appends ' (universeName)' line")
    void oneActiveAliasAppendsLine() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-40-eri-a"))
                .thenReturn(List.of(new AliasDisplay("Vulcan", "Star Trek")));
        String text = rendererWithAliases.buildStarTooltipText(
                starRecord("star-40-eri-a", "40 Eridani A", "Non-Aligned"));
        assertEquals("40 Eridani A\nPolity: Non-Aligned\nVulcan (Star Trek)", text);
    }

    @Test
    @DisplayName("multiple active aliases: append in service-returned order")
    void multipleAliasesAppendInOrder() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "s"))
                .thenReturn(List.of(
                        new AliasDisplay("Vulcan", "Star Trek"),
                        new AliasDisplay("Forty Eri Prime", "Children of the Pattern")));
        String text = rendererWithAliases.buildStarTooltipText(
                starRecord("s", "40 Eridani A", "Non-Aligned"));
        assertTrue(text.endsWith("\nVulcan (Star Trek)\nForty Eri Prime (Children of the Pattern)"),
                "alias lines must follow service order: " + text);
    }

    @Test
    @DisplayName("service routed with STAR kind + recordId — not name")
    void serviceRoutedWithStarKindAndRecordId() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-id-not-name"))
                .thenReturn(List.of());
        rendererWithAliases.buildStarTooltipText(
                starRecord("star-id-not-name", "DisplayName", "Polity"));
        verify(aliasService).findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-id-not-name");
        // Never called with the display name as the target id
        verify(aliasService, never()).findActiveAliasesForTooltip(AliasTargetKind.STAR, "DisplayName");
        // Never called with EXOPLANET kind from the star renderer
        verify(aliasService, never()).findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "star-id-not-name");
    }

    @Test
    @DisplayName("polity NA normalises to Non-Aligned in tooltip")
    void polityNANormalised() {
        when(aliasService.findActiveAliasesForTooltip(AliasTargetKind.STAR, "s"))
                .thenReturn(List.of());
        String text = rendererWithAliases.buildStarTooltipText(starRecord("s", "Wolf 359", "NA"));
        assertEquals("Wolf 359\nPolity: Non-Aligned", text);
    }
}
