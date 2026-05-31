package com.terranrepublic.assets;

/**
 * Discriminator for {@link com.teamgannon.trips.jpa.model.SolarSystemFeature#getCatalogReferenceId
 * SolarSystemFeature.catalogReferenceId} — identifies which catalog table a feature's
 * {@code catalogReferenceId} points to.
 *
 * <p>v2 Phase E.1 §6.2 + Divergence B resolution — distinct from {@link AssetKind}, which stays
 * focused on the four {@link SpaceAsset} sealed-hierarchy members (SHIP, STATION,
 * WEAPON_INSTALLATION, MEGASTRUCTURE). {@code CatalogedKind} extends that coverage with
 * {@code TRANSPORT_NODE} (which is a {@code SpaceInfrastructure} member, not a SpaceAsset) so a
 * single discriminator covers every persisted catalog kind a {@code SolarSystemFeature} can
 * reference.
 *
 * <p>The four values that parallel {@code AssetKind} keep their ordinal positions: SHIP=0,
 * STATION=1, WEAPON_INSTALLATION=2, MEGASTRUCTURE=3. {@code TRANSPORT_NODE} is appended at
 * ordinal 4. This parallel-ordinal-stability lets future code that holds an
 * {@code AssetKind} value translate cleanly into a {@code CatalogedKind} for the
 * SpaceAsset-derived cases by name (or ordinal, where applicable).
 *
 * <p>{@code GateNetwork} is NOT in this enum — gate networks are not referenced via
 * {@code catalogReferenceId} (they're referenced via {@code networkId} on JUMP_GATE features,
 * a separate field with its own dispatch path).
 */
public enum CatalogedKind {
    SHIP,
    STATION,
    WEAPON_INSTALLATION,
    MEGASTRUCTURE,
    TRANSPORT_NODE
}
