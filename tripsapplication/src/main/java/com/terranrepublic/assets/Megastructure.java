package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Scale-class, self-contained-setting catalog asset — the fourth sealed subtype of
 * {@link SpaceAsset}, added in v2 Phase D.7.
 *
 * <p>Megastructures are objects too large or too self-contained to honestly model
 * as {@link StationDesign}s. The taxonomy spans hollowed asteroids (Troy), purpose-built
 * war machines (Death Star), disguised moons (Dahak), found enigmas (Rama), and
 * engineered worlds (Ringworld). The {@link MegastructureArchetype} axis captures
 * the primary structural category; the {@link MegastructureOriginType} axis captures
 * where the object came from; the {@link InteriorGravityType} axis captures how
 * gravity is provided inside an interior setting. The {@link StationFunction} axis
 * (shared with {@link StationDesign} via D.6) covers what the object is <em>for</em>.
 *
 * <p>The {@link Mobility} axis was extended in D.7 Step 1.6 to cover megastructure
 * scale: Troy sits at {@link Mobility#MOBILE_LIMITED} with an ORION auxiliary drive,
 * the Death Star at {@link Mobility#MOBILE}, Dahak at {@link Mobility#MOBILE_AUTONOMOUS}.
 *
 * <p>Mass is expressed in megatons (1 MT = 10⁶ tons) per design §3.1 — megastructures
 * routinely span 10⁶ to 10¹⁵ tons, which is unwieldy at the ton scale. The
 * {@code SpaceAsset#dryMassTons()} accessor is overridden to derive tons from
 * megatons.
 *
 * <p>The canonical constructor is 30-arg. Three fields are SpaceAsset-interface
 * bookkeeping ({@code designation}, {@code createdAt}, {@code modifiedAt}); the
 * other 27 are megastructure-specific per design §3.1 plus the {@code auxiliaryDrive}
 * resolution from Divergence D.
 *
 * @param faction      builder/owner faction at time of design or construction
 * @param allegiance   current controller; may differ from {@code faction} for
 *                     captured, transferred, or rediscovered megastructures
 */
public record Megastructure(
        String id,
        String name,
        String designation,
        String description,
        String category,
        String notes,
        MegastructureArchetype archetype,
        double dimensionsKm,
        double dryMassMegatons,
        double internalVolumeKm3,
        Mobility mobility,
        DriveType auxiliaryDrive,
        MegastructureOriginType originType,
        String builderPolity,
        Integer discoveryYear,
        Integer constructionYear,
        StationFunction primaryFunction,
        Set<StationFunction> secondaryFunctions,
        boolean hasInteriorSetting,
        long interiorPopulation,
        InteriorGravityType interiorGravity,
        OperationalState operationalState,
        boolean concealed,
        List<Armament> armaments,
        CatalogProvenance provenance,
        String faction,
        String allegiance,
        TechLevel techLevel,
        Instant createdAt,
        Instant modifiedAt
) implements SpaceAsset {

    // ------------------------------------------------------------ compact ctor

    public Megastructure {
        // ---- design §3.2 invariants (10 throwing-or-defaulting checks) ----
        archetype = archetype == null ? MegastructureArchetype.UNKNOWN : archetype;
        mobility = mobility == null ? Mobility.STATIONKEEPING : mobility;
        originType = originType == null ? MegastructureOriginType.UNKNOWN : originType;
        primaryFunction = primaryFunction == null ? StationFunction.UNKNOWN : primaryFunction;
        secondaryFunctions = secondaryFunctions == null ? Set.of() : Set.copyOf(secondaryFunctions);
        if (secondaryFunctions.contains(primaryFunction)) {
            throw new IllegalArgumentException(
                    "secondaryFunctions must not contain the primary function (primary="
                            + primaryFunction + "); choose distinct values or omit it from the set");
        }
        interiorGravity = interiorGravity == null ? InteriorGravityType.UNKNOWN : interiorGravity;
        operationalState = operationalState == null ? OperationalState.OPERATIONAL : operationalState;
        armaments = armaments == null ? List.of() : List.copyOf(armaments);
        provenance = provenance == null ? CatalogProvenance.unknown() : provenance;

        // ---- SpaceAsset-interface bookkeeping defaults (pure, non-throwing) ----
        designation = designation == null ? "" : designation;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    // ------------------------------------------------------------ accessors

    @Override
    public AssetKind kind() {
        return AssetKind.MEGASTRUCTURE;
    }

    /**
     * Cataloged-interface source accessor. Mirrors the v2 Phase D.6 pattern from
     * {@link StationDesign#source()}: the source label lives at
     * {@code provenance.sourceUniverse()} and this override surfaces it under the
     * existing {@link Cataloged#source()} contract.
     *
     * @return {@code provenance.sourceUniverse()} — never null, may be empty string
     */
    @Override
    public String source() {
        return provenance.sourceUniverse();
    }

    /**
     * SpaceAsset-interface mass accessor. Megastructure mass is canonically stored
     * in megatons via {@link #dryMassMegatons()} for scale honesty; this override
     * derives the ton-scale value required by the shared {@link SpaceAsset}
     * contract.
     *
     * @return mass in tons (= dryMassMegatons × 10⁶)
     */
    @Override
    public double dryMassTons() {
        return dryMassMegatons * 1_000_000.0;
    }
}
