package com.terranrepublic.assets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.List;

/**
 * Shared catalog contract for ships, fortifications, and standalone war machines.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SpaceshipDesign.class, name = "SHIP"),
        @JsonSubTypes.Type(value = StationDesign.class, name = "STATION"),
        @JsonSubTypes.Type(value = WeaponInstallation.class, name = "WEAPON_INSTALLATION")
})
public sealed interface SpaceAsset extends Cataloged permits SpaceshipDesign, StationDesign, WeaponInstallation {

    String designation();

    TechLevel techLevel();

    String category();

    OperationalState operationalState();

    Instant createdAt();

    Instant modifiedAt();

    List<Armament> armaments();

    double dryMassTons();

    AssetKind kind();
}
