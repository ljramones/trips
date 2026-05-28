package com.teamgannon.trips.construct;

import com.terranrepublic.assets.AssetKind;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.infrastructure.InfrastructureKind;
import com.terranrepublic.infrastructure.SpaceInfrastructure;

import java.util.List;

/**
 * Read-only aggregator over the two parallel catalog hierarchies that the Constructs UI
 * needs to present as a single browsable list.
 *
 * <h2>Why this exists</h2>
 * The reconciliation pre-work ({@code docs/design/constructs-existing-hierarchies.md},
 * {@code docs/design/constructs-feature-plan-v2.md}) decided to keep
 * {@link SpaceAsset} (units: ships, stations, weapon installations) and
 * {@link SpaceInfrastructure} (network: transport nodes, conduits) as two parallel
 * sealed hierarchies. "Construct" is the UI umbrella term, not a new domain type.
 *
 * <p>That decision left a gap: code that wants to ask "give me every catalogued thing,
 * regardless of which hierarchy it lives in" had no single point to call. This
 * interface fills the gap without collapsing the type system.
 *
 * <h2>Phase A0 scope</h2>
 * Introduced as a skeleton in Phase A0 of the Constructs feature so that v2 Phase A's
 * new station / weapon-installation / transport-node entities have a stable seam to
 * register against when they land. The {@link DefaultConstructRegistry} implementation
 * shipped alongside this interface returns ships only and stubs the station and weapon
 * installation buckets with empty lists. v2 Phase A flips those buckets to real data.
 *
 * <p>The {@code SpaceshipDesignerPanel} is <strong>not</strong> rewired through this
 * registry in Phase A0. That panel keeps reading directly from
 * {@code SpaceshipRepository}; the registry is here so that the broader Constructs UI
 * can be written against a stable contract from day one of Phase A.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>All returned lists are immutable (or treated as such by callers).</li>
 *   <li>All returned lists are non-null. Empty lists are fine; null is not.</li>
 *   <li>Implementations are free to cache or to query on every call. Callers must not
 *       assume one or the other.</li>
 * </ul>
 *
 * @see DefaultConstructRegistry
 * @see SpaceAsset
 * @see SpaceInfrastructure
 */
public interface ConstructRegistry {

    /**
     * Every catalogued construct currently known to the application, across both
     * hierarchies. Useful for "all constructs" tables and global search.
     *
     * @return non-null, possibly empty list of {@link Cataloged} entries
     */
    List<Cataloged> allById();

    /**
     * Catalogued {@link SpaceAsset}s filtered by their {@link AssetKind}. Used by the
     * Constructs UI to populate per-tab views (Ships tab, Stations tab, Weapon
     * Installations tab).
     *
     * @param kind the asset kind to filter by, non-null
     * @return non-null, possibly empty list of assets of the requested kind
     */
    List<SpaceAsset> assetsByKind(AssetKind kind);

    /**
     * Catalogued {@link SpaceInfrastructure} entries filtered by their
     * {@link InfrastructureKind}. Used by the Constructs UI to populate the
     * Transport Nodes tab (and, later, the Conduits tab).
     *
     * @param kind the infrastructure kind to filter by, non-null
     * @return non-null, possibly empty list of infrastructure of the requested kind
     */
    List<SpaceInfrastructure> infrastructureByKind(InfrastructureKind kind);
}
