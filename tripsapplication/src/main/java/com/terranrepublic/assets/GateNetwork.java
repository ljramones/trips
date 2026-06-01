package com.terranrepublic.assets;

import java.time.Instant;

/**
 * A grouping of {@code TransportNode} gate instances forming a connected transit network.
 *
 * <p>v2 Phase E.1 §5 — the first persisted catalog entity that does NOT extend
 * {@link SpaceAsset} or {@link SpaceInfrastructure}. {@code GateNetwork} is a top-level
 * worldbuilding concept (a grouping, not a concrete physical thing); placing it in either sealed
 * hierarchy would force a semantic distortion. It implements {@link Cataloged} for catalog
 * uniformity (so future cross-bucket browsers can see all catalogued entries through one
 * interface) but stops there.
 *
 * <p>Ships gain transponder access to a network at the design level via
 * {@code SpaceshipDesign.defaultAccessibleNetworkIds} (added in E.1 Step 4). Gates belonging to
 * a network are identified by {@code SolarSystemFeature.networkId} referencing the network's
 * stable catalog id (also added in E.1 Step 4).
 *
 * <p>The {@code lifecycle} field's {@link GateNetworkLifecycle#ACTIVE} /
 * {@link GateNetworkLifecycle#REACTIVATED} distinction is preserved permanently: a recovered
 * network carries different worldbuilding flavor than an always-live one.
 *
 * @param id                 stable catalog id, by convention {@code "catalog-network-<slug>"}
 * @param name               the canonical name (e.g. "Aldenata Civilian Network")
 * @param builderPolity      who built the network (e.g. "Aldenata", "Solar Confederation"); also
 *                           surfaces through {@link #faction()} per the {@link Cataloged}
 *                           contract
 * @param lifecycle          {@code ACTIVE} / {@code DERELICT} / {@code REACTIVATED}
 * @param transponderName    canonical transponder identifier for accessing the network
 * @param description        free-form prose
 * @param notes              free-form curator notes (INFERRED flags, source citations, etc.)
 * @param category           free-form designer category label
 * @param provenance         where the network comes from + its documentary status; the source
 *                           label surfaces through {@link #source()} per D.6 Concern A
 * @param createdAt          Instant of catalog entry creation
 * @param modifiedAt         Instant of last modification
 */
public record GateNetwork(
        String id,
        String name,
        String builderPolity,
        GateNetworkLifecycle lifecycle,
        String transponderName,
        String description,
        String notes,
        String category,
        CatalogProvenance provenance,
        Instant createdAt,
        Instant modifiedAt,
        String universeId
) implements Cataloged {

    /**
     * Backwards-compatible 11-arg constructor preserving every pre-F.1 call site. Delegates to
     * the 12-arg canonical with {@code universeId = null} (canonical/real-data scope). v2 Phase
     * F.1 §4.4 added the field.
     */
    public GateNetwork(
            String id,
            String name,
            String builderPolity,
            GateNetworkLifecycle lifecycle,
            String transponderName,
            String description,
            String notes,
            String category,
            CatalogProvenance provenance,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, builderPolity, lifecycle, transponderName, description, notes, category,
                provenance, createdAt, modifiedAt, null);
    }

    public GateNetwork {
        lifecycle = lifecycle == null ? GateNetworkLifecycle.ACTIVE : lifecycle;
        provenance = provenance == null ? CatalogProvenance.unknown() : provenance;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    /**
     * Cataloged-interface source accessor — surfaces the universe label from {@code provenance}
     * per the D.6 Concern A pattern.
     */
    @Override
    public String source() {
        return provenance.sourceUniverse();
    }

    /**
     * Cataloged-interface faction accessor — returns {@link #builderPolity} per the E.1 §G
     * gap-resolution decision. {@code GateNetwork} doesn't carry a separate faction field; the
     * builder polity IS the faction for catalog-display purposes.
     */
    @Override
    public String faction() {
        return builderPolity;
    }

    /**
     * Cataloged-interface concealed accessor — always {@code false}. {@code GateNetwork} doesn't
     * model concealment (a network is either known/usable, derelict, or undiscovered, but
     * "concealment" isn't a network-level property). Per the E.1 §G gap-resolution decision.
     */
    @Override
    public boolean concealed() {
        return false;
    }
}
