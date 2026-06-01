package com.terranrepublic.assets;

import java.time.Instant;
import java.util.List;

/**
 * Standalone war machine catalog asset without ship mass-budget or drive fields.
 */
public record WeaponInstallation(
        String id,
        String name,
        String designation,
        InstallationType installationType,
        Emplacement emplacement,
        String source,
        String faction,
        boolean concealed,
        String description,
        double dryMassTons,
        double footprintSpanMeters,
        boolean mobile,
        int crewComplement,
        List<Armament> armaments,
        TechLevel techLevel,
        String category,
        OperationalState operationalState,
        Instant createdAt,
        Instant modifiedAt,
        String universeId
) implements SpaceAsset {

    /**
     * Backwards-compatible 19-arg constructor preserving the pre-F.1 canonical signature.
     * Delegates to the 20-arg canonical with {@code universeId = null} (canonical/real-data
     * scope). v2 Phase F.1 §4.4 added the field.
     */
    public WeaponInstallation(
            String id,
            String name,
            String designation,
            InstallationType installationType,
            Emplacement emplacement,
            String source,
            String faction,
            boolean concealed,
            String description,
            double dryMassTons,
            double footprintSpanMeters,
            boolean mobile,
            int crewComplement,
            List<Armament> armaments,
            TechLevel techLevel,
            String category,
            OperationalState operationalState,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, installationType, emplacement, source, faction, concealed, description,
                dryMassTons, footprintSpanMeters, mobile, crewComplement, armaments, techLevel, category,
                operationalState, createdAt, modifiedAt, null);
    }

    /**
     * Backwards-compatible constructor for existing weapon-installation call sites with faction/concealment.
     */
    public WeaponInstallation(
            String id,
            String name,
            String designation,
            InstallationType installationType,
            Emplacement emplacement,
            String source,
            String faction,
            boolean concealed,
            String description,
            double dryMassTons,
            double footprintSpanMeters,
            boolean mobile,
            int crewComplement,
            List<Armament> armaments,
            TechLevel techLevel,
            String category,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, installationType, emplacement, source, faction, concealed, description,
                dryMassTons, footprintSpanMeters, mobile, crewComplement, armaments, techLevel, category,
                OperationalState.OPERATIONAL, createdAt, modifiedAt);
    }

    /**
     * Backwards-compatible constructor for existing weapon-installation catalog call sites.
     */
    public WeaponInstallation(
            String id,
            String name,
            String designation,
            InstallationType installationType,
            Emplacement emplacement,
            String source,
            String description,
            double dryMassTons,
            double footprintSpanMeters,
            boolean mobile,
            int crewComplement,
            List<Armament> armaments,
            TechLevel techLevel,
            String category,
            Instant createdAt,
            Instant modifiedAt
    ) {
        this(id, name, designation, installationType, emplacement, source, "Unknown", false, description,
                dryMassTons, footprintSpanMeters, mobile, crewComplement, armaments, techLevel, category,
                OperationalState.OPERATIONAL, createdAt, modifiedAt);
    }

    public WeaponInstallation {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("WeaponInstallation name must be provided");
        }
        if (installationType == null) {
            throw new IllegalArgumentException("WeaponInstallation installationType must be provided");
        }
        if (emplacement == null) {
            throw new IllegalArgumentException("WeaponInstallation emplacement must be provided");
        }
        if (dryMassTons < 0 || footprintSpanMeters < 0 || crewComplement < 0) {
            throw new IllegalArgumentException("WeaponInstallation numeric fields must not be negative");
        }
        designation = designation == null ? "" : designation;
        source = source == null ? "" : source;
        faction = faction == null || faction.isBlank() ? "Unknown" : faction;
        description = description == null ? "" : description;
        armaments = armaments == null ? List.of() : List.copyOf(armaments);
        techLevel = techLevel == null ? TechLevel.UNKNOWN : techLevel;
        category = category == null ? installationType.name() : category;
        operationalState = operationalState == null ? OperationalState.OPERATIONAL : operationalState;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    @Override
    public AssetKind kind() {
        return AssetKind.WEAPON_INSTALLATION;
    }
}
