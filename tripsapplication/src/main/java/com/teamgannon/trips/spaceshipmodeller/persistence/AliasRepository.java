package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.AliasTargetKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AliasEntity}.
 *
 * <p>Phase F.2 §4.5 — finders support the three core query patterns:
 * <ul>
 *   <li>"All aliases in this universe" — the AliasesDialog table view filtered by universe</li>
 *   <li>"All aliases for this target" — the renderer tooltip + Fictional Info tab (filtered
 *       further by active universes via {@code AliasDesignerService.findActiveAliasesForTarget})</li>
 *   <li>"Does this (universe, target) pair already have an alias?" — the uniqueness check
 *       on Create Alias (enforced by V18's unique constraint but pre-checked here for a
 *       friendlier error message)</li>
 * </ul>
 *
 * <p>All three finders are indexed via V18 — the {@code (universe_id, target_kind, target_id)}
 * composite unique index covers all three lookup patterns efficiently.
 */
public interface AliasRepository extends JpaRepository<AliasEntity, String> {

    List<AliasEntity> findByUniverseId(String universeId);

    List<AliasEntity> findByTargetKindAndTargetId(AliasTargetKind targetKind, String targetId);

    Optional<AliasEntity> findByUniverseIdAndTargetKindAndTargetId(
            String universeId, AliasTargetKind targetKind, String targetId);
}
