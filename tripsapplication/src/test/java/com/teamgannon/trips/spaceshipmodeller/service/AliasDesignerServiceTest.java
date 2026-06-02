package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.AliasEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.AliasMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.AliasRepository;
import com.teamgannon.trips.worldbuilding.UniverseFilteringService;
import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase F.2 §4.5 — coverage for {@link AliasDesignerService}'s F.2-specific behavior:
 *
 * <ul>
 *   <li>{@link AliasDesignerService#findActiveAliasesForTarget} consults
 *       {@link UniverseFilteringService#getActiveUniverseIds()} and filters in-memory.</li>
 *   <li>Save with uniqueness pre-check — throws a friendly {@link IllegalStateException}
 *       when a (universe, target) collision exists.</li>
 *   <li>Save bypasses the uniqueness check when updating an existing id.</li>
 *   <li>Find/delete delegate cleanly to the repository.</li>
 * </ul>
 *
 * <p>Mocked repository + UniverseFilteringService; real mapper. Same shape as the F.1
 * UniverseDesignerServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class AliasDesignerServiceTest {

    @Mock
    private AliasRepository repository;

    @Mock
    private UniverseFilteringService filteringService;

    private final AliasMapper mapper = new AliasMapper();
    private AliasDesignerService service;

    @BeforeEach
    void setUp() {
        service = new AliasDesignerService(repository, mapper, filteringService);
    }

    private static AliasEntity entityFor(String id, String universeId, AliasTargetKind kind,
                                         String targetId, String aliasText) {
        AliasEntity e = new AliasEntity();
        e.setId(id);
        e.setUniverseId(universeId);
        e.setTargetKind(kind);
        e.setTargetId(targetId);
        e.setAliasText(aliasText);
        e.setDescription("");
        Instant now = Instant.parse("2026-06-01T00:00:00Z");
        e.setCreatedAt(now);
        e.setModifiedAt(now);
        return e;
    }

    // ============================================================
    // findActiveAliasesForTarget (the F.2-specific lookup)
    // ============================================================

    @Test
    @DisplayName("findActiveAliasesForTarget returns empty list when no universes are active")
    void findActiveReturnsEmptyWhenNoneActive() {
        when(filteringService.getActiveUniverseIds()).thenReturn(Set.of());
        // Repository should not be queried — short-circuit when no active universes
        List<Alias> result = service.findActiveAliasesForTarget(AliasTargetKind.STAR, "star-1");
        assertTrue(result.isEmpty());
        verify(repository, never()).findByTargetKindAndTargetId(any(), any());
    }

    @Test
    @DisplayName("findActiveAliasesForTarget filters in-memory by active universe ids")
    void findActiveFiltersByActiveUniverseIds() {
        AliasEntity trekAlias = entityFor("a1", "u-trek", AliasTargetKind.STAR, "star-1", "Vulcan");
        AliasEntity cotpAlias = entityFor("a2", "u-cotp", AliasTargetKind.STAR, "star-1", "Akane's");
        AliasEntity inactiveAlias = entityFor("a3", "u-foundation", AliasTargetKind.STAR, "star-1", "Terminus-adjacent");

        when(filteringService.getActiveUniverseIds()).thenReturn(Set.of("u-trek", "u-cotp"));
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "star-1"))
                .thenReturn(List.of(trekAlias, cotpAlias, inactiveAlias));

        List<Alias> result = service.findActiveAliasesForTarget(AliasTargetKind.STAR, "star-1");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> "Vulcan".equals(a.aliasText())));
        assertTrue(result.stream().anyMatch(a -> "Akane's".equals(a.aliasText())));
        assertTrue(result.stream().noneMatch(a -> "Terminus-adjacent".equals(a.aliasText())),
                "inactive-universe alias must NOT appear");
    }

    @Test
    @DisplayName("findActiveAliasesForTarget returns empty list when no aliases match the target")
    void findActiveReturnsEmptyWhenNoMatches() {
        when(filteringService.getActiveUniverseIds()).thenReturn(Set.of("u-trek"));
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "no-such-star"))
                .thenReturn(List.of());
        List<Alias> result = service.findActiveAliasesForTarget(AliasTargetKind.STAR, "no-such-star");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findActiveAliasesForTarget routes exoplanet kind to the exoplanet path")
    void findActiveRoutesExoplanetKind() {
        AliasEntity epAlias = entityFor("a1", "u-trek", AliasTargetKind.EXOPLANET, "ep-1", "Vulcan-IV");
        when(filteringService.getActiveUniverseIds()).thenReturn(Set.of("u-trek"));
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.EXOPLANET, "ep-1"))
                .thenReturn(List.of(epAlias));

        List<Alias> result = service.findActiveAliasesForTarget(AliasTargetKind.EXOPLANET, "ep-1");

        assertEquals(1, result.size());
        assertEquals(AliasTargetKind.EXOPLANET, result.get(0).targetKind());
    }

    // ============================================================
    // findActiveAliasesForTooltip (F.2 §6.1 — renderer-tooltip-shaped projection)
    // ============================================================

    @Test
    @DisplayName("findActiveAliasesForTooltip returns empty list when no universes are active")
    void tooltipReturnsEmptyWhenNoneActive() {
        when(filteringService.getActiveUniverseNamesById()).thenReturn(Map.of());
        List<AliasDisplay> result = service.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-1");
        assertTrue(result.isEmpty());
        // Short-circuit — repository must not be queried per F.2 §6.1 per-hover efficiency
        verify(repository, never()).findByTargetKindAndTargetId(any(), any());
    }

    @Test
    @DisplayName("findActiveAliasesForTooltip pairs each active-universe alias with its universe display name")
    void tooltipPairsAliasWithUniverseName() {
        AliasEntity trekAlias = entityFor("a1", "u-trek", AliasTargetKind.STAR, "star-1", "Vulcan");
        AliasEntity cotpAlias = entityFor("a2", "u-cotp", AliasTargetKind.STAR, "star-1", "Akane's");
        AliasEntity inactiveAlias = entityFor("a3", "u-foundation", AliasTargetKind.STAR, "star-1", "Terminus-adjacent");

        when(filteringService.getActiveUniverseNamesById()).thenReturn(Map.of(
                "u-trek", "Star Trek",
                "u-cotp", "Children of the Pattern"));
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "star-1"))
                .thenReturn(List.of(trekAlias, cotpAlias, inactiveAlias));

        List<AliasDisplay> result = service.findActiveAliasesForTooltip(AliasTargetKind.STAR, "star-1");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d ->
                "Vulcan".equals(d.aliasText()) && "Star Trek".equals(d.universeDisplayName())));
        assertTrue(result.stream().anyMatch(d ->
                "Akane's".equals(d.aliasText()) && "Children of the Pattern".equals(d.universeDisplayName())));
        assertTrue(result.stream().noneMatch(d -> "Terminus-adjacent".equals(d.aliasText())),
                "inactive-universe alias must NOT appear in tooltip output");
    }

    @Test
    @DisplayName("findActiveAliasesForTooltip routes exoplanet kind to the exoplanet path")
    void tooltipRoutesExoplanetKind() {
        when(filteringService.getActiveUniverseNamesById()).thenReturn(Map.of("u-trek", "Star Trek"));
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.EXOPLANET, "ep-1"))
                .thenReturn(List.of(entityFor("a1", "u-trek", AliasTargetKind.EXOPLANET, "ep-1", "Vulcan-IV")));

        List<AliasDisplay> result = service.findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "ep-1");

        assertEquals(1, result.size());
        assertEquals("Vulcan-IV", result.get(0).aliasText());
        assertEquals("Star Trek", result.get(0).universeDisplayName());
    }

    @Test
    @DisplayName("findActiveAliasesForTooltip returns empty when target has no aliases at all")
    void tooltipEmptyWhenNoAliasesForTarget() {
        when(filteringService.getActiveUniverseNamesById()).thenReturn(Map.of("u-trek", "Star Trek"));
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "no-such-star"))
                .thenReturn(List.of());
        List<AliasDisplay> result = service.findActiveAliasesForTooltip(AliasTargetKind.STAR, "no-such-star");
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // save — uniqueness pre-check + update bypass
    // ============================================================

    @Test
    @DisplayName("save with no existing alias persists cleanly")
    void saveNewAliasPersists() {
        Alias fresh = new Alias("u", AliasTargetKind.STAR, "star-1", "Vulcan", "");
        when(repository.existsById(fresh.id())).thenReturn(false);
        when(repository.findByUniverseIdAndTargetKindAndTargetId("u", AliasTargetKind.STAR, "star-1"))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Alias saved = service.save(fresh);

        assertEquals("Vulcan", saved.aliasText());
        verify(repository).save(any(AliasEntity.class));
    }

    @Test
    @DisplayName("save with duplicate (universe, target) throws IllegalStateException with friendly message")
    void saveDuplicateThrowsFriendly() {
        Alias newAlias = new Alias("u-trek", AliasTargetKind.STAR, "star-1", "Vulcan", "");
        AliasEntity existing = entityFor("a-existing", "u-trek", AliasTargetKind.STAR, "star-1", "T'Khut");

        when(repository.existsById(newAlias.id())).thenReturn(false);
        when(repository.findByUniverseIdAndTargetKindAndTargetId("u-trek", AliasTargetKind.STAR, "star-1"))
                .thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.save(newAlias));
        assertTrue(ex.getMessage().contains("T'Khut"),
                "error message must include the existing alias text: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("a-existing"),
                "error message must include the existing alias id: " + ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("save bypasses uniqueness check when updating an existing id")
    void saveUpdateBypassesUniquenessCheck() {
        Alias existing = new Alias("catalog-alias-existing", "u-trek", AliasTargetKind.STAR,
                "star-1", "Vulcan (updated)", "", null, null);
        when(repository.existsById("catalog-alias-existing")).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.save(existing);

        // findByUniverseIdAndTargetKindAndTargetId must NOT be invoked on update path —
        // updating an existing id is allowed to keep the same (universe, target) pair.
        verify(repository, never()).findByUniverseIdAndTargetKindAndTargetId(any(), any(), any());
        verify(repository).save(any(AliasEntity.class));
    }

    // ============================================================
    // find/delete delegation
    // ============================================================

    @Test
    @DisplayName("findAll maps entities back to domain objects")
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(
                entityFor("a1", "u", AliasTargetKind.STAR, "s1", "Name1"),
                entityFor("a2", "u", AliasTargetKind.EXOPLANET, "ep1", "Name2")));
        List<Alias> all = service.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("count delegates to the repository")
    void countDelegates() {
        when(repository.count()).thenReturn(7L);
        assertEquals(7L, service.count());
    }

    @Test
    @DisplayName("findById round-trips through the mapper")
    void findByIdRoundTrips() {
        when(repository.findById("a1")).thenReturn(Optional.of(
                entityFor("a1", "u-trek", AliasTargetKind.STAR, "s1", "Vulcan")));
        Optional<Alias> result = service.findById("a1");
        assertTrue(result.isPresent());
        assertEquals("Vulcan", result.get().aliasText());
    }

    @Test
    @DisplayName("findByUniverseId delegates")
    void findByUniverseIdDelegates() {
        when(repository.findByUniverseId("u-trek")).thenReturn(List.of(
                entityFor("a1", "u-trek", AliasTargetKind.STAR, "s1", "Vulcan")));
        List<Alias> result = service.findByUniverseId("u-trek");
        assertEquals(1, result.size());
        assertEquals("u-trek", result.get(0).universeId());
    }

    @Test
    @DisplayName("findByTargetKindAndTargetId delegates (unfiltered by activation)")
    void findByTargetDelegates() {
        when(repository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "s1"))
                .thenReturn(List.of(
                        entityFor("a1", "u-trek", AliasTargetKind.STAR, "s1", "Vulcan"),
                        entityFor("a2", "u-cotp", AliasTargetKind.STAR, "s1", "Akane's")));
        List<Alias> result = service.findByTargetKindAndTargetId(AliasTargetKind.STAR, "s1");
        assertEquals(2, result.size());
        // No filtering by activation — both returned regardless of UniverseFilteringService state
        verify(filteringService, never()).getActiveUniverseIds();
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        AliasEntity entity = entityFor("a1", "u", AliasTargetKind.STAR, "s1", "Vulcan");
        when(repository.findById("a1")).thenReturn(Optional.of(entity));
        service.deleteById("a1");
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("deleteById on a missing id is a no-op")
    void deleteMissingIsNoop() {
        when(repository.findById("absent")).thenReturn(Optional.empty());
        service.deleteById("absent");
        verify(repository, never()).delete(any());
    }
}
