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
 *
 * <h2>Dual role</h2>
 * As of Phase A0 of the Constructs feature, this class has a documented dual role and must
 * <em>not</em> be deleted, relocated, or have its constants migrated wholesale into JPA seed
 * scripts:
 * <ol>
 *   <li><strong>Canonical in-memory seed data</strong> for the {@code SpaceAsset} sealed
 *       hierarchy. v2 Phase A introduces JPA entities for {@code StationDesign} and
 *       {@code WeaponInstallation}; the planned seed-on-empty startup step for those tables
 *       reads from {@link #all()} so that {@link #TROY}, {@link #SAPL}, {@link #SHEVA_GUN},
 *       and the Posleen dodecahedra remain the single source of truth for "what the catalog
 *       ships with".</li>
 *   <li><strong>Shared test fixture</strong> for the asset / economy / sim layers. Tests
 *       across {@code com.terranrepublic.assets}, {@code .economy}, and {@code .sim}
 *       reference these constants directly to avoid bootstrapping a database fixture per
 *       test. Deleting them would force a wave of fixture-builder rewrites with no
 *       functional gain.</li>
 * </ol>
 *
 * <h2>Do not</h2>
 * Do not delete or relocate these constants without also updating (a) the seed-on-empty
 * startup step that v2 Phase A's {@code StationDesignerService} introduces and (b) every
 * test that references {@link #TROY}, {@link #SAPL}, {@link #SHEVA_GUN},
 * {@link #POSLEEN_COMMAND_DODECAHEDRON}, or {@link #POSLEEN_BATTLE_DODECAHEDRON} by name.
 * The Constructs reconciliation explicitly chose data-duplication-via-known-source-of-truth
 * over data-drift-via-two-independent-seeds; preserve that choice.
 *
 * <p>See {@code docs/design/constructs-feature-plan-v2.md} §"Decisions pinned in Phase A0"
 * for the full rationale.
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

    // =====================================================================
    // Real / Proposed Earth space stations (Phase D.5)
    //
    // All entries share source = "Real / Proposed" so the Installations
    // Designer's universe tab strip groups them under one tab. Numbers were
    // web-verified at population time; any value that's a reasonable
    // contemporary inference (not a primary-source number) is flagged
    // INFERRED in the description, mirroring TROY's convention. Sources are
    // cited in the comment block above each entry.
    // =====================================================================

    private static final String SOURCE_REAL = "Real / Proposed";
    private static final String CATEGORY_CREWED = "Crewed orbital station";

    /**
     * International Space Station. Operational. Crewed continuously since 2000.
     * Sources: NASA ISS overview (nasa.gov); Wikipedia "International Space Station".
     */
    public static final StationDesign ISS = new StationDesign(
            "real-station-iss",
            "International Space Station",
            "ISS",
            StationType.ORBITAL_CITADEL,
            SOURCE_REAL,
            "International (NASA / Roscosmos / ESA / JAXA / CSA)",
            false,
            "International (NASA / Roscosmos / ESA / JAXA / CSA)",
            "Crewed multi-module station in low Earth orbit (~408 km). Assembly began 1998; "
                    + "continuously crewed since November 2000. NASA / Roscosmos / ESA / JAXA / CSA "
                    + "partnership. Nominal crew complement 7. Truss length 109 m; pressurized "
                    + "volume approximately 1000 m^3; dry mass approximately 420 t. "
                    + "interiorSpanMeters INFERRED as habitable span (73 m); armourThicknessMeters "
                    + "INFERRED as zero (no armour) since the station is unarmoured civilian "
                    + "infrastructure.",
            109,
            73,
            420,
            0,
            7,
            7,
            1000,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.OPERATIONAL,
            CREATED_AT,
            CREATED_AT);

    /**
     * Tiangong (Chinese: "Heavenly Palace"). Operational since 2021.
     * Sources: CNSA; Wikipedia "Tiangong space station".
     */
    public static final StationDesign TIANGONG = new StationDesign(
            "real-station-tiangong",
            "Tiangong",
            "TSS",
            StationType.ORBITAL_CITADEL,
            SOURCE_REAL,
            "China (CNSA / CMSA)",
            false,
            "China (CNSA / CMSA)",
            "Crewed Chinese space station in low Earth orbit (~390 km). Operational since 2021; "
                    + "modular T-shape with Tianhe core + Wentian + Mengtian. Nominal crew "
                    + "complement 3; up to 6 during handover. Mass approximately 100 t; length "
                    + "approximately 55 m across docked configuration; pressurized volume "
                    + "approximately 340 m^3 (about a third of ISS). interiorSpanMeters INFERRED "
                    + "from core-module diameter and module count; armourThicknessMeters INFERRED "
                    + "as zero.",
            55,
            17,
            100,
            0,
            6,
            3,
            340,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.OPERATIONAL,
            CREATED_AT,
            CREATED_AT);

    /**
     * Mir. First modular long-duration station; operated 1986-2001.
     * Sources: ESA Mir FAQ; NASA NSSDC; Wikipedia "Mir".
     * OperationalState SALVAGED reflects the controlled de-orbit in March 2001 over the
     * Pacific (planned end-of-life, distinct from a {@code WRECK} after-failure outcome).
     */
    public static final StationDesign MIR = new StationDesign(
            "real-station-mir",
            "Mir",
            "Mir",
            StationType.ORBITAL_CITADEL,
            SOURCE_REAL,
            "Soviet Union / Russia",
            false,
            "Soviet Union / Russia",
            "First long-duration modular space station; Soviet then Russian operation 1986-2001. "
                    + "Final 1996 configuration: core module plus Kvant-1, Kvant-2, Kristall, "
                    + "Spektr, Priroda + docking module. Nominal crew 3. Length about 33 m, mass "
                    + "approximately 130 t, pressurized volume approximately 350 m^3. Controlled "
                    + "de-orbit over the Pacific on 23 March 2001 (SALVAGED rather than WRECK; the "
                    + "end-of-life burn was intentional). interiorSpanMeters and "
                    + "armourThicknessMeters INFERRED.",
            33,
            13,
            130,
            0,
            3,
            3,
            350,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.SALVAGED,
            CREATED_AT,
            CREATED_AT);

    /**
     * Skylab. First US space station; three crewed visits 1973-1974.
     * Sources: NASA Skylab history; Wikipedia "Skylab".
     * OperationalState SALVAGED per the v2 Phase D.5 prompt (atmospheric re-entry 1979 was
     * uncontrolled but pre-planned end-of-life).
     */
    public static final StationDesign SKYLAB = new StationDesign(
            "real-station-skylab",
            "Skylab",
            "Skylab",
            StationType.HABITAT,
            SOURCE_REAL,
            "United States (NASA)",
            false,
            "United States (NASA)",
            "First US space station, built from a modified Saturn V third-stage hull. Three "
                    + "crewed visits 1973-1974. Mass approximately 77 t, length 36 m, pressurized "
                    + "volume approximately 361 m^3, maximum diameter 6.6 m. Atmospheric re-entry "
                    + "on 11 July 1979 over Western Australia and the Indian Ocean. "
                    + "interiorSpanMeters INFERRED as roughly the habitable workshop length; "
                    + "armourThicknessMeters INFERRED as zero.",
            36,
            14,
            77,
            0,
            3,
            3,
            361,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.SALVAGED,
            CREATED_AT,
            CREATED_AT);

    /**
     * Salyut 1 (DOS-1). World's first space station. Operated April-October 1971.
     * Sources: NASA NSSDC Salyut 1; Wikipedia "Salyut 1".
     */
    public static final StationDesign SALYUT_1 = new StationDesign(
            "real-station-salyut-1",
            "Salyut 1",
            "DOS-1",
            StationType.HABITAT,
            SOURCE_REAL,
            "Soviet Union",
            false,
            "Soviet Union",
            "First space station ever launched (19 April 1971). Visited by Soyuz 10 (failed "
                    + "hard-dock) and Soyuz 11; the Soyuz 11 crew (Dobrovolsky, Volkov, Patsayev) "
                    + "died on return from a 23-day stay due to a valve failure. Mass 18.4 t, "
                    + "length 20 m, interior volume 99 m^3, nominal crew 3. Controlled de-orbit "
                    + "on 11 October 1971 over the Pacific. interiorSpanMeters INFERRED as "
                    + "habitable section length; armourThicknessMeters INFERRED as zero.",
            20,
            15,
            18.4,
            0,
            3,
            3,
            99,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.SALVAGED,
            CREATED_AT,
            CREATED_AT);

    /**
     * Salyut 7 (DOS-6). Final Salyut; operated 1982-1991.
     * Sources: NASA NSSDC Salyut 7; Wikipedia "Salyut 7".
     * OperationalState SALVAGED per the v2 Phase D.5 prompt, though the actual 7 February 1991
     * re-entry over Capitán Bermúdez, Argentina was uncontrolled — see description.
     */
    public static final StationDesign SALYUT_7 = new StationDesign(
            "real-station-salyut-7",
            "Salyut 7",
            "DOS-6",
            StationType.HABITAT,
            SOURCE_REAL,
            "Soviet Union",
            false,
            "Soviet Union",
            "Last of the Salyut series; precursor patterns to Mir's modular design. Operated "
                    + "1982-1986 crewed (six resident crews); record stay Kizim/Solovyov/Atkov "
                    + "237 days in 1984. Mass 19.8 t, length 16 m, habitable volume 90 m^3, "
                    + "nominal crew 3. Uncontrolled re-entry on 7 February 1991 over Capitán "
                    + "Bermúdez, Argentina (the intended Pacific reentry corridor was overshot). "
                    + "interiorSpanMeters INFERRED; armourThicknessMeters INFERRED as zero.",
            16,
            12,
            19.8,
            0,
            3,
            3,
            90,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.SALVAGED,
            CREATED_AT,
            CREATED_AT);

    /**
     * Lunar Gateway. Planned crewed station in lunar Near-Rectilinear Halo Orbit.
     * Sources: NASA Gateway overview; Wikipedia "Lunar Gateway"; SpaceNews / SpacePolicyOnline
     * coverage of GAO mass risk + March 2026 program-suspension announcement.
     *
     * Status note: NASA announced an indefinite suspension of the Gateway program in March 2026,
     * but HALO hardware physically exists (arrived in Arizona for final outfitting at Northrop
     * Grumman in April 2026) and Falcon Heavy comanifested launch of PPE+HALO was previously
     * scheduled for 2027. UNDER_CONSTRUCTION reflects the hardware reality; the description
     * carries the program-status caveat.
     */
    public static final StationDesign LUNAR_GATEWAY = new StationDesign(
            "real-station-lunar-gateway",
            "Lunar Gateway",
            "Gateway",
            StationType.OUTPOST,
            SOURCE_REAL,
            "International (NASA / ESA / JAXA / CSA)",
            false,
            "International (NASA / ESA / JAXA / CSA)",
            "Planned periodically-crewed Artemis support station in lunar Near-Rectilinear Halo "
                    + "Orbit (NRHO). Initial configuration PPE (Power and Propulsion Element, "
                    + "approximately 5 t) plus HALO (Habitation and Logistics Outpost); previously "
                    + "scheduled co-manifested Falcon Heavy launch in 2027. NASA announced "
                    + "indefinite program suspension in March 2026 while HALO is in final "
                    + "outfitting at Northrop Grumman, Arizona. Projected full-station mass "
                    + "approximately 63 t per NASA Gateway program documentation. dryMassTons "
                    + "INFERRED as projected full-station figure; overallSpanMeters and "
                    + "interiorSpanMeters INFERRED from NASA's full-assembly 43 m / 20 m "
                    + "envelope. pressurizedVolumeM3 and crewCapacity INFERRED; no primary-source "
                    + "number for the comanifested-vehicle phase.",
            43,
            20,
            63,
            0,
            4,
            4,
            125,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.UNDER_CONSTRUCTION,
            CREATED_AT,
            CREATED_AT);

    /**
     * Axiom Station (early modules). First commercial space station.
     * Sources: Axiom Space; SpaceNews "Axiom Space revises space station assembly plans" (Dec 2024);
     * NASA commercial-space LEO economy updates.
     *
     * Status note: as of late 2024 / 2025, Axiom restructured its assembly sequence so the
     * Payload Power Thermal Module (PPTM) launches first (NET 2027) and berths to ISS; the
     * habitat module Hab-1 launches NET 2028. When Hab-1 reaches orbit PPTM undocks from ISS
     * and meets it, forming the initial free-flying two-module Axiom Station. All numeric
     * fields below are INFERRED — Axiom has not published mass / volume / crew figures for the
     * comanifested early-module configuration.
     */
    public static final StationDesign AXIOM_STATION = new StationDesign(
            "real-station-axiom",
            "Axiom Station",
            "Axiom",
            StationType.HABITAT,
            SOURCE_REAL,
            "Axiom Space (United States, commercial)",
            false,
            "Axiom Space (United States, commercial)",
            "First commercial space station. Revised 2024 assembly plan: Payload Power Thermal "
                    + "Module (PPTM) launches NET 2027 and berths to ISS; Hab-1 launches NET 2028, "
                    + "PPTM undocks from ISS and meets it to form the initial two-module "
                    + "free-flying Axiom Station. All numeric fields here are INFERRED; Axiom has "
                    + "not published canonical mass / span / pressurized-volume figures for the "
                    + "early-module configuration. Use only as a placeholder until primary-source "
                    + "numbers are available.",
            15,
            12,
            20,
            0,
            4,
            4,
            60,
            Mobility.STATIONKEEPING,
            null,
            List.of(),
            List.of(),
            0,
            false,
            TechLevel.CONTEMPORARY,
            CATEGORY_CREWED,
            OperationalState.UNDER_CONSTRUCTION,
            CREATED_AT,
            CREATED_AT);

    private static final List<SpaceAsset> ALL = List.of(
            TROY,
            SAPL,
            SHEVA_GUN,
            POSLEEN_COMMAND_DODECAHEDRON,
            POSLEEN_BATTLE_DODECAHEDRON,
            // Phase D.5 real space stations
            ISS,
            TIANGONG,
            MIR,
            SKYLAB,
            SALYUT_1,
            SALYUT_7,
            LUNAR_GATEWAY,
            AXIOM_STATION);

    private Catalog() {
    }

    public static List<SpaceAsset> all() {
        return ALL;
    }
}
