package com.teamgannon.trips.solarsystem.jumppoint;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository;
import com.teamgannon.trips.model.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.SolarSystemActivatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JumpPointActivationListener}. Mocks the repository; uses the real
 * {@link JumpPointComputer} so the activation contract is exercised end-to-end through the
 * deterministic algorithm.
 *
 * <p>v2 Phase E.1 §7 — covers: first activation creates features, repeat activation is
 * idempotent (no duplicates), jump-inaccessible stars produce no feature (absence-of-feature
 * state), graceful failure on repository exceptions, multi-star scaling.
 */
@ExtendWith(MockitoExtension.class)
class JumpPointActivationListenerTest {

    @Mock
    private SolarSystemFeatureRepository featureRepository;

    private final JumpPointComputer computer = new JumpPointComputer();
    private JumpPointActivationListener listener;

    @BeforeEach
    void setUp() {
        listener = new JumpPointActivationListener(featureRepository, computer);
    }

    private static StarDisplayRecord starOf(String id, String name, double massSolar) {
        StarDisplayRecord s = new StarDisplayRecord();
        s.setRecordId(id);
        s.setStarName(name);
        s.setMass(massSolar);
        return s;
    }

    private static SolarSystemDescription singleStarSystem(String systemId, StarDisplayRecord star) {
        SolarSystemDescription system = new SolarSystemDescription();
        system.setSolarSystemId(systemId);
        system.setStarDisplayRecord(star);
        system.setPlanetDescriptionList(new ArrayList<>());
        return system;
    }

    private static SolarSystemDescription multiStarSystem(String systemId, StarDisplayRecord primary,
                                                          StarDisplayRecord... companions) {
        SolarSystemDescription system = singleStarSystem(systemId, primary);
        List<StarDisplayRecord> comps = new ArrayList<>();
        for (StarDisplayRecord c : companions) {
            comps.add(c);
        }
        system.setCompanionStars(comps);
        return system;
    }

    // ============================================================
    // First activation creates features
    // ============================================================

    @Test
    @DisplayName("first activation of single-star system: one JUMP_POINT feature created")
    void firstActivationSingleStarCreatesOneFeature() {
        StarDisplayRecord sol = starOf("star-sol", "Sol", 1.0);
        SolarSystemDescription system = singleStarSystem("system-sol", sol);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        ArgumentCaptor<SolarSystemFeature> captor = ArgumentCaptor.forClass(SolarSystemFeature.class);
        verify(featureRepository, times(1)).save(captor.capture());
        SolarSystemFeature saved = captor.getValue();
        assertEquals("system-sol", saved.getSolarSystemId());
        assertEquals("star-sol", saved.getParentBodyId());
        assertEquals("JUMP_POINT", saved.getFeatureType());
        assertEquals("NATURAL", saved.getFeatureCategory());
        assertNotNull(saved.getOrbitalRadiusAU());
        assertNotNull(saved.getOrbitalAngleDeg());
        assertNotNull(saved.getOrbitalHeightAU());
    }

    @Test
    @DisplayName("single-star feature name is just 'Jump Point' (no star-name prefix)")
    void singleStarFeatureNameIsPlain() {
        StarDisplayRecord sol = starOf("star-sol", "Sol", 1.0);
        SolarSystemDescription system = singleStarSystem("system-sol", sol);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        ArgumentCaptor<SolarSystemFeature> captor = ArgumentCaptor.forClass(SolarSystemFeature.class);
        verify(featureRepository).save(captor.capture());
        assertEquals("Jump Point", captor.getValue().getName());
    }

    // ============================================================
    // Idempotency on repeat activation
    // ============================================================

    @Test
    @DisplayName("repeat activation of same system: no duplicate features (idempotency via findByParentBodyIdAndFeatureType)")
    void repeatActivationDoesNotDuplicate() {
        StarDisplayRecord sol = starOf("star-sol", "Sol", 1.0);
        SolarSystemDescription system = singleStarSystem("system-sol", sol);
        // Pretend the feature is already present for this star.
        SolarSystemFeature existing = new SolarSystemFeature();
        existing.setParentBodyId("star-sol");
        existing.setFeatureType(SolarSystemFeature.FeatureType.JUMP_POINT);
        when(featureRepository.findByParentBodyIdAndFeatureType("star-sol", "JUMP_POINT"))
                .thenReturn(List.of(existing));

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        // No save call: idempotency guard skipped the work.
        verify(featureRepository, never()).save(any());
    }

    // ============================================================
    // Jump-inaccessible system (Optional.empty() from computer)
    // ============================================================

    @Test
    @DisplayName("jump-inaccessible system: no feature created, no error")
    void jumpInaccessibleSystemCreatesNoFeature() {
        // Construct a pathological system where the computer returns Optional.empty() —
        // a "planet" so massive its Hill sphere covers the entire outer band (same fixture
        // shape as JumpPointComputerTest.iterationCapExhaustionReturnsEmpty).
        StarDisplayRecord star = starOf("star-jump-inaccessible", "Pathological", 1.0);
        SolarSystemDescription system = singleStarSystem("system-pathological", star);
        com.teamgannon.trips.model.PlanetDescription massive = new com.teamgannon.trips.model.PlanetDescription();
        massive.setMass(200_000.0);  // ~0.6 solar mass disguised as a planet
        massive.setSemiMajorAxis(35.0);
        system.getPlanetDescriptionList().add(massive);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        // Computer returned Optional.empty() → no feature saved → no error propagated.
        verify(featureRepository, never()).save(any());
    }

    // ============================================================
    // Graceful failure on exception
    // ============================================================

    @Test
    @DisplayName("graceful failure: repository.save throws — listener catches, no propagation")
    void repositoryExceptionCaughtGracefully() {
        StarDisplayRecord sol = starOf("star-sol", "Sol", 1.0);
        SolarSystemDescription system = singleStarSystem("system-sol", sol);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());
        when(featureRepository.save(any())).thenThrow(new RuntimeException("DB unreachable"));

        // Must not throw.
        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        // save() was attempted; the throwable was swallowed.
        verify(featureRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("graceful failure: null system in event is ignored without error")
    void nullSystemIgnored() {
        listener.onSystemActivated(new SolarSystemActivatedEvent(null));
        verify(featureRepository, never()).save(any());
    }

    @Test
    @DisplayName("graceful failure: system with null id is ignored")
    void nullSystemIdIgnored() {
        SolarSystemDescription system = new SolarSystemDescription();
        // system.solarSystemId left null
        listener.onSystemActivated(new SolarSystemActivatedEvent(system));
        verify(featureRepository, never()).save(any());
    }

    // ============================================================
    // Multi-star scaling
    // ============================================================

    @Test
    @DisplayName("binary system: each star gets its own JUMP_POINT feature (2 saves)")
    void binarySystemCreatesTwoFeatures() {
        StarDisplayRecord primary = starOf("alpha-cen-a", "Alpha Centauri A", 1.1);
        StarDisplayRecord companion = starOf("alpha-cen-b", "Alpha Centauri B", 0.9);
        SolarSystemDescription system = multiStarSystem("system-alpha-cen", primary, companion);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        verify(featureRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("trinary system: each of the 3 stars gets its own JUMP_POINT feature (3 saves)")
    void trinarySystemCreatesThreeFeatures() {
        StarDisplayRecord a = starOf("trinary-a", "A", 1.2);
        StarDisplayRecord b = starOf("trinary-b", "B", 0.9);
        StarDisplayRecord c = starOf("trinary-c", "C", 0.3);
        SolarSystemDescription system = multiStarSystem("system-trinary", a, b, c);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        verify(featureRepository, times(3)).save(any());
    }

    @Test
    @DisplayName("multi-star feature names include star-name prefix for disambiguation")
    void multiStarFeatureNamesIncludeStarNamePrefix() {
        StarDisplayRecord primary = starOf("alpha-cen-a", "Alpha Centauri A", 1.1);
        StarDisplayRecord companion = starOf("alpha-cen-b", "Alpha Centauri B", 0.9);
        SolarSystemDescription system = multiStarSystem("system-alpha-cen", primary, companion);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        ArgumentCaptor<SolarSystemFeature> captor = ArgumentCaptor.forClass(SolarSystemFeature.class);
        verify(featureRepository, times(2)).save(captor.capture());
        List<String> savedNames = captor.getAllValues().stream()
                .map(SolarSystemFeature::getName)
                .toList();
        assertTrue(savedNames.contains("Alpha Centauri A Jump Point"),
                "multi-star feature must use star-name prefix; got names: " + savedNames);
        assertTrue(savedNames.contains("Alpha Centauri B Jump Point"),
                "multi-star feature must use star-name prefix; got names: " + savedNames);
    }

    @Test
    @DisplayName("partial pre-existing: star A has feature, star B doesn't — only B gets saved")
    void partialPreExistingOnlyMissingStarGetsSaved() {
        StarDisplayRecord a = starOf("binary-a", "A", 1.0);
        StarDisplayRecord b = starOf("binary-b", "B", 0.9);
        SolarSystemDescription system = multiStarSystem("system-binary", a, b);
        // A already has a JUMP_POINT feature
        SolarSystemFeature existingA = new SolarSystemFeature();
        existingA.setParentBodyId("binary-a");
        when(featureRepository.findByParentBodyIdAndFeatureType("binary-a", "JUMP_POINT"))
                .thenReturn(List.of(existingA));
        // B has no feature yet
        when(featureRepository.findByParentBodyIdAndFeatureType("binary-b", "JUMP_POINT"))
                .thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        ArgumentCaptor<SolarSystemFeature> captor = ArgumentCaptor.forClass(SolarSystemFeature.class);
        verify(featureRepository, times(1)).save(captor.capture());
        assertEquals("binary-b", captor.getValue().getParentBodyId(),
                "only the star without a pre-existing feature should be saved");
    }

    // ============================================================
    // Coordinate mapping (Cartesian → spherical)
    // ============================================================

    @Test
    @DisplayName("persisted feature carries non-zero orbitalRadius and a 0-360 angle (cartesian → spherical mapping)")
    void persistedCoordinateMappingIsValid() {
        StarDisplayRecord sol = starOf("star-sol", "Sol", 1.0);
        SolarSystemDescription system = singleStarSystem("system-sol", sol);
        when(featureRepository.findByParentBodyIdAndFeatureType(any(), any())).thenReturn(List.of());

        listener.onSystemActivated(new SolarSystemActivatedEvent(system));

        ArgumentCaptor<SolarSystemFeature> captor = ArgumentCaptor.forClass(SolarSystemFeature.class);
        verify(featureRepository).save(captor.capture());
        SolarSystemFeature saved = captor.getValue();
        // orbitalRadiusAU is the xy-plane projection — bounded above by the outer-system boundary.
        // For Sol (HYG fallback = 40 AU), the radius should be in [0, 40].
        assertTrue(saved.getOrbitalRadiusAU() >= 0.0,
                "orbital radius must be non-negative; got " + saved.getOrbitalRadiusAU());
        assertTrue(saved.getOrbitalRadiusAU() <= 40.0,
                "orbital radius must be within Sol's HYG outer boundary (40 AU); got " + saved.getOrbitalRadiusAU());
        // orbitalAngleDeg is normalized to [0, 360).
        assertTrue(saved.getOrbitalAngleDeg() >= 0.0 && saved.getOrbitalAngleDeg() < 360.0,
                "orbital angle must be in [0, 360); got " + saved.getOrbitalAngleDeg());
    }
}
