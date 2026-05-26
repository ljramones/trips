package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Seed catalog entries for non-ship assets and ship-adjacent assets that need the shared asset view.
 */
public final class Catalog {

    private static final Instant CREATED_AT = Instant.now();

    public static final StationDesign TROY = new StationDesign(
            UUID.randomUUID().toString(),
            "Troy",
            "BS-1",
            StationType.GATE_FORT,
            "Troy Rising",
            "Hollowed nickel-iron asteroid gate-fort. CANON dry mass is two trillion tons; CANON interior "
                    + "cavity is about 7 km; CANON armor is approximate, described as walls kilometres thick. "
                    + "CANON AI is named Paris. INFERRED values: 9000 m overall span, 150000 crew capacity, "
                    + "120000 crew complement, 1.5e11 m3 pressurized volume, 5.0e7 m3 hangar volume.",
            9_000,
            7_000,
            2.0e12,
            2_000,
            150_000,
            120_000,
            1.5e11,
            Mobility.MANEUVERABLE,
            DriveType.ORION,
            List.of(),
            List.of(
                    new Armament(
                            "SAPL feed apertures",
                            WeaponType.SOLAR_PUMPED_LASER,
                            0,
                            0,
                            0,
                            "Primary",
                            "Fed by external SAPL; aperture count, power, and range unknown"),
                    new Armament(
                            "Heavy Laser Emitters",
                            WeaponType.LASER,
                            500,
                            0,
                            0,
                            "Anti-ship",
                            "Quantity INFERRED; power and range unknown"),
                    new Armament(
                            "Missile Ports",
                            WeaponType.MISSILE,
                            1_000,
                            0,
                            0,
                            "Anti-ship",
                            "Quantity INFERRED; missile type and range unknown")),
            5.0e7,
            true,
            TechLevel.ADVANCED,
            "gate fortification",
            CREATED_AT,
            CREATED_AT);

    public static final WeaponInstallation SAPL = new WeaponInstallation(
            UUID.randomUUID().toString(),
            "SAPL",
            "Solar Array Pumped Laser",
            InstallationType.BEAM_ARRAY,
            Emplacement.SOLAR_ORBIT,
            "Troy Rising",
            "Distributed mirror array focusing sunlight into an industrial/military beam; CANON concept bores "
                    + "asteroids, cracks warships, and scales by adding mirrors. INFERRED values: 1.0e6 ton "
                    + "dry mass, 1.0e8 m footprint span, 5000 crew complement.",
            1.0e6,
            1.0e8,
            false,
            5_000,
            List.of(new Armament(
                    "SAPL primary beam",
                    WeaponType.SOLAR_PUMPED_LASER,
                    1,
                    1.0e9,
                    0,
                    "Primary",
                    "Petawatt-class effective output, scalable - INFERRED magnitude; range unknown")),
            TechLevel.ADVANCED,
            "solar beam array",
            CREATED_AT,
            CREATED_AT);

    public static final WeaponInstallation SHEVA_GUN = new WeaponInstallation(
            UUID.randomUUID().toString(),
            "SheVa Gun",
            "SheVa-9",
            InstallationType.SUPER_CANNON,
            Emplacement.GROUND_MOBILE,
            "Aldenata",
            "Continent-mobile anti-lander super-cannon; CANON concept fires sub-caliber DU and later "
                    + "nuclear-tipped rounds at Posleen command ships. INFERRED values: 14000 ton dry mass, "
                    + "90 m footprint span, 20 crew complement.",
            14_000,
            90,
            true,
            20,
            List.of(new Armament(
                    "SheVa main gun",
                    WeaponType.NUCLEAR_PULSE_GUN,
                    1,
                    0,
                    300,
                    "Primary",
                    "Anti-C-Dec, nuclear-tipped rounds; effective range 300 km INFERRED; yield unknown")),
            TechLevel.NEAR_FUTURE,
            "ground mobile super-cannon",
            CREATED_AT,
            CREATED_AT);

    public static final SpaceshipDesign POSLEEN_COMMAND_DODECAHEDRON = new SpaceshipDesign(
            UUID.randomUUID().toString(),
            "Posleen Command Dodecahedron",
            "C-Dec",
            ShipClass.COMMAND_SHIP,
            DriveType.POSLEEN_NORMAL_SPACE,
            new MassBudget(3.7e7, 2.0e6, 1.0e6, 1.0e7, 0, 1.0e6),
            40_000,
            1_500,
            List.of(new CarriedCraft(
                    "Lamprey",
                    ShipClass.LANDER,
                    40,
                    50_000,
                    "Assault landing; quantity and unit mass INFERRED")),
            List.of(new Armament(
                    "Plasma cannon",
                    WeaponType.PLASMA,
                    50,
                    0,
                    0,
                    "Anti-ship",
                    "Quantity INFERRED; power and range unknown")),
            "",
            "God-King flagship, faceted/spherical hull carrying landers and normals; CANON concept says "
                    + "individually powerful but tactically rigid due to Posleen psychology. ALL dimensions "
                    + "and mass values are INFERRED: 1500 m length, 1500 m diameter, 5.0e7 ton dry mass, "
                    + "1.0e6 ton propellant, 1.0e7 ton payload, 2.0e6 ton engine mass, 1.0e6 ton radiator mass, "
                    + "50000 crew capacity, 40000 crew complement. MassBudget uses structure as a residual "
                    + "INFERRED value to preserve the stated dry mass.",
            SourceType.SCIENCE_FICTION,
            "Aldenata",
            "",
            "",
            CREATED_AT);

    public static final SpaceshipDesign POSLEEN_BATTLE_DODECAHEDRON = new SpaceshipDesign(
            UUID.randomUUID().toString(),
            "Posleen Battle Dodecahedron",
            "B-Dec",
            ShipClass.DREADNOUGHT,
            DriveType.POSLEEN_NORMAL_SPACE,
            new MassBudget(2.59e7, 1.4e6, 7.0e5, 7.0e6, 0, 7.0e5),
            28_000,
            1_050,
            List.of(),
            List.of(new Armament(
                    "Plasma cannon",
                    WeaponType.PLASMA,
                    35,
                    0,
                    0,
                    "Anti-ship",
                    "Quantity scaled from C-Dec at about 70 percent - INFERRED; power and range unknown")),
            "",
            "Line battleship in the Posleen dodecahedron family rather than a command ship. Everything numeric "
                    + "is INFERRED: about 70 percent of C-Dec scale, 1050 m length/diameter, 3.5e7 ton dry mass, "
                    + "7.0e5 ton propellant, 7.0e6 ton payload, 1.4e6 ton engine mass, 7.0e5 ton radiator mass, "
                    + "28000 crew complement. Carried craft capacity unknown.",
            SourceType.SCIENCE_FICTION,
            "Aldenata",
            "",
            "",
            CREATED_AT);

    private static final List<SpaceAsset> ALL = List.of(
            TROY,
            SAPL,
            SHEVA_GUN,
            POSLEEN_COMMAND_DODECAHEDRON,
            POSLEEN_BATTLE_DODECAHEDRON);

    private Catalog() {
    }

    public static List<SpaceAsset> all() {
        return ALL;
    }
}
