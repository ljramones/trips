package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogTest {

    @Test
    void assetSubtypesExposeSharedCatalogContract() {
        Armament laser = new Armament("Spinal laser", WeaponType.LASER, 1, 5000, 250_000, "primary", "");
        Instant now = Instant.now();

        SpaceAsset ship = new SpaceshipDesign(
                "ship-1",
                "Test Frigate",
                "TF-1",
                ShipClass.FRIGATE,
                DriveType.FUSION_TORCH,
                new MassBudget(100, 40, 150, 20, 10, 30),
                12,
                80,
                List.of(),
                List.of(laser),
                "",
                "Test ship",
                SourceType.UNKNOWN,
                "",
                "",
                "",
                now);

        SpaceAsset station = new StationDesign(
                "station-1",
                "Gate Fort",
                "GF-1",
                StationType.GATE_FORT,
                "Test source",
                "Hollowed-asteroid fortification",
                10_000,
                8_000,
                50_000_000,
                250,
                10_000,
                2_000,
                2_000_000,
                Mobility.FIXED,
                null,
                List.of(),
                List.of(laser),
                500_000,
                true,
                TechLevel.ADVANCED,
                "fortification",
                now,
                now);

        SpaceAsset weapon = new WeaponInstallation(
                "weapon-1",
                "Solar Beam Array",
                "SBA-1",
                InstallationType.BEAM_ARRAY,
                Emplacement.SOLAR_ORBIT,
                "Test source",
                "Distributed solar-pumped beam array",
                5_000_000,
                500_000,
                false,
                120,
                List.of(laser),
                TechLevel.EXOTIC,
                "strategic weapon",
                now,
                now);

        assertEquals(AssetKind.SHIP, ship.kind());
        assertEquals(AssetKind.STATION, station.kind());
        assertEquals(AssetKind.WEAPON_INSTALLATION, weapon.kind());
        assertEquals("Unknown", station.faction());
        assertEquals(station.faction(), ((StationDesign) station).allegiance());
        assertFalse(ship.concealed());
        assertFalse(station.concealed());
        assertFalse(weapon.concealed());
        assertEquals(OperationalState.OPERATIONAL, ship.operationalState());
        assertEquals(OperationalState.OPERATIONAL, station.operationalState());
        assertEquals(OperationalState.OPERATIONAL, weapon.operationalState());
        assertNotNull(ship.armaments());
        assertNotNull(station.armaments());
        assertNotNull(weapon.armaments());
        assertEquals("ship", exhaustiveLabel(ship));
        assertEquals("station", exhaustiveLabel(station));
        assertEquals("weapon", exhaustiveLabel(weapon));
    }

    @Test
    void catalogContainsSeedAssetsAndCanBePrinted() {
        List<SpaceAsset> assets = Catalog.all();

        // Original seed: TROY + SAPL + SHEVA_GUN + 2 Posleen dodecahedra = 5.
        // Phase D.5 (2026-05-28) added 8 real Earth space stations under
        // source = "Real / Proposed", taking the floor to 13.
        assertTrue(assets.size() >= 13, "Catalog should carry at least the original 5 + 8 real stations");
        assertFalse(assets.isEmpty());
        for (SpaceAsset asset : assets) {
            assertNotNull(asset.armaments());
            System.out.printf(
                    "%s | %s | %s | dryMass=%.3e tons | weaponCount=%d%n",
                    asset.name(),
                    asset.source(),
                    asset.kind(),
                    asset.dryMassTons(),
                    asset.armaments().size());
        }
    }

    private static String exhaustiveLabel(SpaceAsset asset) {
        return switch (asset) {
            case SpaceshipDesign ignored -> "ship";
            case StationDesign ignored -> "station";
            case WeaponInstallation ignored -> "weapon";
            case Megastructure ignored -> "megastructure";
        };
    }
}
