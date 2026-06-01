package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Fixed or semi-fixed fortification / catalog asset without ship-only propellant or mass-ratio fields.
 *
 * <p>v2 Phase D.6 added three orthogonal-axis fields:
 * <ul>
 *   <li>{@code primaryFunction} ({@link StationFunction}) — what the station is <em>for</em>;
 *       distinct from {@link StationType} which captures what it is <em>structurally</em>.</li>
 *   <li>{@code secondaryFunctions} (Set&lt;StationFunction&gt;) — supporting roles, typically
 *       0–3 values. Must not contain the primary function.</li>
 *   <li>{@code provenance} ({@link CatalogProvenance}) — where the station comes from and what
 *       its real-world or in-universe documentary status is. Subsumes the former free-text
 *       {@code source} field: the source string now lives at {@code provenance.sourceUniverse()}.</li>
 * </ul>
 *
 * <p>The free-text {@code source} field is dropped from the canonical record at v2 Phase D.6.
 * Callers retrieve the universe label via {@link #source()} which now reads from the provenance
 * composite. Existing compatibility constructors that accepted a {@code source} string continue to
 * work — they wrap the value into a default-shape {@link CatalogProvenance}; the editor dialog
 * and Catalog seeds construct provenance explicitly.
 *
 * <p>Canonical constructor is 29-arg; compatibility shims at 23 / 26 / 27 args preserve every
 * existing call site. Chain: 23 → 26 → 27-compat → canonical-29.
 *
 * @param faction    the builder/owner faction at time of design
 * @param allegiance the current controller; may differ from {@code faction} for captured or
 *                   transferred installations
 */
public record StationDesign(
        String id,
        String name,
        String designation,
        StationType stationType,
        String faction,
        boolean concealed,
        String allegiance,
        String description,
        double overallSpanMeters,
        double interiorSpanMeters,
        double dryMassTons,
        double armourThicknessMeters,
        int crewCapacity,
        int crewComplement,
        double pressurizedVolumeM3,
        Mobility mobility,
        DriveType auxiliaryDrive,
        List<CarriedCraft> carriedCraft,
        List<Armament> armaments,
        double hangarVolumeM3,
        boolean carrierCapable,
        TechLevel techLevel,
        String category,
        OperationalState operationalState,
        Instant createdAt,
        Instant modifiedAt,
        StationFunction primaryFunction,
        Set<StationFunction> secondaryFunctions,
        CatalogProvenance provenance,
        String universeId
) implements SpaceAsset {

    // ------------------------------------------------------------------ chain

    /**
     * Backwards-compatible 29-arg constructor preserving the pre-F.1 canonical signature.
     * Delegates to the 30-arg canonical with {@code universeId = null} (canonical/real-data
     * scope). v2 Phase F.1 §4.4 added the field.
     */
    public StationDesign(
            String id,
            String name,
            String designation,
            StationType stationType,
            String faction,
            boolean concealed,
            String allegiance,
            String description,
            double overallSpanMeters,
            double interiorSpanMeters,
            double dryMassTons,
            double armourThicknessMeters,
            int crewCapacity,
            int crewComplement,
            double pressurizedVolumeM3,
            Mobility mobility,
            DriveType auxiliaryDrive,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            double hangarVolumeM3,
            boolean carrierCapable,
            TechLevel techLevel,
            String category,
            OperationalState operationalState,
            Instant createdAt,
            Instant modifiedAt,
            StationFunction primaryFunction,
            Set<StationFunction> secondaryFunctions,
            CatalogProvenance provenance
    ) {
        this(id, name, designation, stationType, faction, concealed, allegiance, description,
                overallSpanMeters, interiorSpanMeters, dryMassTons, armourThicknessMeters,
                crewCapacity, crewComplement, pressurizedVolumeM3, mobility, auxiliaryDrive,
                carriedCraft, armaments, hangarVolumeM3, carrierCapable, techLevel, category,
                operationalState, createdAt, modifiedAt,
                primaryFunction, secondaryFunctions, provenance, null);
    }

    /**
     * Backwards-compatible constructor matching the pre-v2-Phase-D.6 23-arg signature. Delegates
     * to the 26-arg shim with {@code faction="Unknown"}, {@code concealed=false},
     * {@code allegiance=null}.
     */
    public StationDesign(
            String id,
            String name,
            String designation,
            StationType stationType,
            String source,
            String description,
            double overallSpanMeters,
            double interiorSpanMeters,
            double dryMassTons,
            double armourThicknessMeters,
            int crewCapacity,
            int crewComplement,
            double pressurizedVolumeM3,
            Mobility mobility,
            DriveType auxiliaryDrive,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            double hangarVolumeM3,
            boolean carrierCapable,
            TechLevel techLevel,
            String category,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, stationType, source, "Unknown", false, null, description,
                overallSpanMeters, interiorSpanMeters, dryMassTons, armourThicknessMeters,
                crewCapacity, crewComplement, pressurizedVolumeM3, mobility, auxiliaryDrive,
                carriedCraft, armaments, hangarVolumeM3, carrierCapable, techLevel, category,
                createdAt, modifiedAt);
    }

    /**
     * Backwards-compatible constructor matching the pre-v2-Phase-D.6 26-arg signature. Delegates
     * to the 27-arg compatibility shim with {@code operationalState=OPERATIONAL}.
     */
    public StationDesign(
            String id,
            String name,
            String designation,
            StationType stationType,
            String source,
            String faction,
            boolean concealed,
            String allegiance,
            String description,
            double overallSpanMeters,
            double interiorSpanMeters,
            double dryMassTons,
            double armourThicknessMeters,
            int crewCapacity,
            int crewComplement,
            double pressurizedVolumeM3,
            Mobility mobility,
            DriveType auxiliaryDrive,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            double hangarVolumeM3,
            boolean carrierCapable,
            TechLevel techLevel,
            String category,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, stationType, source, faction, concealed, allegiance, description,
                overallSpanMeters, interiorSpanMeters, dryMassTons, armourThicknessMeters,
                crewCapacity, crewComplement, pressurizedVolumeM3, mobility, auxiliaryDrive,
                carriedCraft, armaments, hangarVolumeM3, carrierCapable, techLevel, category,
                OperationalState.OPERATIONAL, createdAt, modifiedAt);
    }

    /**
     * Backwards-compatible constructor matching the pre-v2-Phase-D.6 canonical 27-arg signature
     * (including the now-dropped {@code source} string). The {@code source} value is wrapped into
     * a default-shape {@link CatalogProvenance} ({@code (UNKNOWN, source, null, UNKNOWN)}); the
     * three new fields default to {@code UNKNOWN} / empty set / unknown provenance.
     *
     * <p>This shim is the bridge that lets every pre-Phase-D.6 caller continue to compile
     * unchanged. Step 6 of Phase D.6 will replace catalog-seed calls with the canonical 29-arg
     * form for explicit provenance population.
     */
    public StationDesign(
            String id,
            String name,
            String designation,
            StationType stationType,
            String source,
            String faction,
            boolean concealed,
            String allegiance,
            String description,
            double overallSpanMeters,
            double interiorSpanMeters,
            double dryMassTons,
            double armourThicknessMeters,
            int crewCapacity,
            int crewComplement,
            double pressurizedVolumeM3,
            Mobility mobility,
            DriveType auxiliaryDrive,
            List<CarriedCraft> carriedCraft,
            List<Armament> armaments,
            double hangarVolumeM3,
            boolean carrierCapable,
            TechLevel techLevel,
            String category,
            OperationalState operationalState,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, stationType, faction, concealed, allegiance, description,
                overallSpanMeters, interiorSpanMeters, dryMassTons, armourThicknessMeters,
                crewCapacity, crewComplement, pressurizedVolumeM3, mobility, auxiliaryDrive,
                carriedCraft, armaments, hangarVolumeM3, carrierCapable, techLevel, category,
                operationalState, createdAt, modifiedAt,
                StationFunction.UNKNOWN, Set.of(),
                new CatalogProvenance(SourceType.UNKNOWN, source, null, CatalogOperationalStatus.UNKNOWN));
    }

    // ------------------------------------------------------------ compact ctor

    public StationDesign {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("StationDesign name must be provided");
        }
        if (stationType == null) {
            throw new IllegalArgumentException("StationDesign stationType must be provided");
        }
        mobility = mobility == null ? Mobility.FIXED : mobility;
        if (mobility == Mobility.FIXED && auxiliaryDrive != null) {
            throw new IllegalArgumentException("auxiliaryDrive is only valid for non-fixed stations");
        }
        if (dryMassTons < 0 || overallSpanMeters < 0 || interiorSpanMeters < 0 || armourThicknessMeters < 0
                || crewCapacity < 0 || crewComplement < 0 || pressurizedVolumeM3 < 0 || hangarVolumeM3 < 0) {
            throw new IllegalArgumentException("Station numeric fields must not be negative");
        }
        designation = designation == null ? "" : designation;
        faction = faction == null || faction.isBlank() ? "Unknown" : faction;
        allegiance = allegiance == null || allegiance.isBlank() ? faction : allegiance;
        description = description == null ? "" : description;
        carriedCraft = carriedCraft == null ? List.of() : List.copyOf(carriedCraft);
        armaments = armaments == null ? List.of() : List.copyOf(armaments);
        techLevel = techLevel == null ? TechLevel.UNKNOWN : techLevel;
        category = category == null ? stationType.name() : category;
        operationalState = operationalState == null ? OperationalState.OPERATIONAL : operationalState;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
        // ---- v2 Phase D.6 invariants ----
        primaryFunction = primaryFunction == null ? StationFunction.UNKNOWN : primaryFunction;
        secondaryFunctions = secondaryFunctions == null ? Set.of() : Set.copyOf(secondaryFunctions);
        if (secondaryFunctions.contains(primaryFunction)) {
            throw new IllegalArgumentException(
                    "secondaryFunctions must not contain the primary function (primary="
                            + primaryFunction + "); choose distinct values or omit it from the set");
        }
        provenance = provenance == null ? CatalogProvenance.unknown() : provenance;
    }

    // ------------------------------------------------------------ accessors

    @Override
    public AssetKind kind() {
        return AssetKind.STATION;
    }

    /**
     * Cataloged-interface source accessor. v2 Phase D.6 dropped the dedicated {@code source} field
     * from this record; the universe label now lives at {@code provenance.sourceUniverse()} and
     * this override surfaces it under the existing {@link Cataloged#source()} contract so the
     * Installations Designer's universe tab strip (Phase D.5) and every other source-string
     * reader keep working unchanged.
     *
     * @return {@code provenance.sourceUniverse()} — never null, may be empty string
     */
    @Override
    public String source() {
        return provenance.sourceUniverse();
    }
}
