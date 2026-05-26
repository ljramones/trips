package com.teamgannon.trips.spaceshipmodeller.templates;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A curated set of ready-made spaceship designs spanning the drive catalogue, from a chemical lander to a
 * Bussard ramjet.
 * <p>
 * Each call to {@link #getAllTemplates()} builds fresh instances (with new ids and timestamps) via
 * {@link SpaceshipBuilder}, so a template can be seeded into the library, opened as the starting point for a
 * new design, or duplicated without aliasing. The mass budgets are chosen so every template passes the
 * {@code ValidationEngine} (no errors): radiator-hungry drives carry radiators, landing classes use a
 * landing-capable drive, and motherships keep their carried craft within their payload allowance.
 */
@Component
public class SpaceshipTemplateLibrary {

    /**
     * @return freshly built copies of every template, in display order
     */
    public List<SpaceshipDesign> getAllTemplates() {
        return List.of(
                // --- Original hard-SF / franchise sampler ---
                galaxyClassExplorer(),
                battlestarCarrier(),
                daedalusArk(),
                honorverseCruiser(),
                orionBattleship(),
                heavyChemicalLander(),
                vasimrDeepSpaceTug(),
                starshotProbe(),
                bussardRamjetExplorer(),
                antimatterFrigate(),

                // --- Real-world / current spacecraft ---
                spaceShuttleOrbiter(),
                starship(),
                crewDragon(),
                apolloCsm(),
                orionMpcv(),
                voyagerProbe(),
                dawnSpacecraft(),

                // --- The Expanse ---
                rocinante(),
                donnager(),
                thomasPrince(),
                freeNavyPella(),
                tychoConstructionShip(),
                behemoth(),
                purnKleenIceHauler(),
                canterbury(),

                // --- Foundation (Asimov) ---
                foundationTrader(),
                imperialNavyCruiser(),
                mulesFlagship(),
                terminusWarship(),
                cleonDreadnought(),

                // --- Project Hail Mary ---
                hailMary(),

                // --- Other notable sci-fi ---
                millenniumFalcon(),
                serenity(),
                discoveryOne(),
                hermes(),
                normandy(),
                heartOfGold(),
                executor(),

                // --- Terran Republic universe: United Earth / Terran Republic (Caine Riordan) ---
                ekraniCruiser(),
                arduinosDestroyer(),
                indrajitSupercarrier(),
                ospreyAssaultTransport(),
                caineRiordanCorvette(),
                ueBattleship(),
                ueBattlecruiser(),
                ueNavyPatrolCorvette(),
                diplomaticCourier(),
                terranColonyTransport(),
                uegFirstContactSurveyShip(),

                // --- Terran Republic universe: Ktoran Dominion (very advanced) ---
                ktoranDreadnought(),
                ktoranCruiser(),
                ktoranFastAttackShip(),

                // --- Terran Republic universe: Hkh'Rkh (warrior race) ---
                hkhRkhWarcruiser(),
                hkhRkhAssaultShip(),

                // --- Terran Republic universe: SpinDog / RockHound (Murphy's Lawless) ---
                spinDogQShip(),
                spinDogHabitatMonitor(),
                rockHoundRaider(),

                // --- Terran Republic universe: other notable powers ---
                aratKurWarship(),
                slaasriithiShiftCarrier(),
                dornaaniPatrolCruiser(),
                roachHiveShip(),
                roachSwarmRaider(),
                gokWarship(),
                lostSoldiersTransport(),

                // --- Terran Republic universe: additional lore vessels ---
                caineRiordanSurveyCutter(),
                ueDreadnought(),
                lostSoldiersArmedFreighter(),
                hkhRkhStrikeCraft(),
                aratKurDreadnought(),
                dornaaniCustodianCarrier());
    }

    private SpaceshipDesign galaxyClassExplorer() {
        return SpaceshipBuilder.create("Galaxy-class Explorer")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Star Trek").faction("Starfleet").era("24th century")
                .designation("NCC-EXP")
                .shipClass(ShipClass.MOTHERSHIP)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(2000).engineTons(800).propellantTons(3000)
                .payloadTons(1500).crewTons(400).radiatorTons(900)
                .crew(1000).lengthMeters(640)
                .carry("Runabout", ShipClass.SHUTTLE, 4, 60, "exploration/transport")
                .carry("Shuttlepod", ShipClass.SHUTTLE, 8, 10, "short-range transfer")
                .description("Long-duration exploration mothership with a large hangar of runabouts and "
                        + "shuttlepods; fusion torch for fast interplanetary cruise.")
                .build();
    }

    private SpaceshipDesign battlestarCarrier() {
        return SpaceshipBuilder.create("Battlestar-type Carrier")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Battlestar Galactica").faction("Colonial Fleet").era("Second Cylon War")
                .designation("BS-75")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(3000).engineTons(1000).propellantTons(24000)
                .payloadTons(2000).crewTons(600).radiatorTons(0)
                .crew(2500).lengthMeters(1400)
                .carry("Viper", ShipClass.FIGHTER, 40, 12, "space superiority")
                .carry("Raptor", ShipClass.DROPSHIP, 8, 20, "recon and assault")
                .description("Military carrier with a high-thrust nuclear-thermal main drive and large "
                        + "propellant tanks (~14 km/s of Δv — enough for inner-system transfers such as "
                        + "Mercury-Venus); built around its Viper wing and Raptor dropships.")
                .build();
    }

    private SpaceshipDesign daedalusArk() {
        return SpaceshipBuilder.create("Daedalus Interstellar Ark")
                .sourceType(SourceType.PROPOSED).faction("Project Daedalus (BIS)").era("1970s study")
                .designation("ARK-1")
                .shipClass(ShipClass.COLONY_SHIP)
                .driveType(DriveType.FUSION_PULSE)
                .structureTons(5000).engineTons(2000).propellantTons(8000)
                .payloadTons(3000).crewTons(2000).radiatorTons(1500)
                .crew(5000).lengthMeters(1900)
                .carry("Colony Lander", ShipClass.LANDER, 6, 200, "surface colonisation")
                .description("Inertial-confinement fusion-pulse generation ship carrying colony landers for "
                        + "an interstellar precursor mission.")
                .build();
    }

    private SpaceshipDesign honorverseCruiser() {
        return SpaceshipBuilder.create("Honorverse Heavy Cruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Honor Harrington").faction("Royal Manticoran Navy").era("~1900 PD")
                .designation("CA-HH")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(800).engineTons(400).propellantTons(1200)
                .payloadTons(300).crewTons(150).radiatorTons(400)
                .crew(400).lengthMeters(480)
                .carry("Pinnace", ShipClass.SHUTTLE, 2, 40, "courier / boarding")
                .carry("Assault Shuttle", ShipClass.DROPSHIP, 4, 25, "marine boarding")
                .description("Heavy line cruiser with internal bays for pinnaces and assault shuttles; "
                        + "fusion-torch acceleration for the wall of battle.")
                .build();
    }

    private SpaceshipDesign orionBattleship() {
        return SpaceshipBuilder.create("Orion Pulse Battleship")
                .sourceType(SourceType.PROPOSED).faction("Project Orion").era("1958–1965 study")
                .designation("BB-ORION")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.ORION_PULSE)
                .structureTons(4000).engineTons(1500).propellantTons(3000)
                .payloadTons(1000).crewTons(300).radiatorTons(0)
                .crew(500).lengthMeters(300)
                .description("Brute-force nuclear-pulse capital ship; enormous thrust shoves a heavily "
                        + "armoured hull, at the cost of a radioactive wake.")
                .build();
    }

    private SpaceshipDesign heavyChemicalLander() {
        return SpaceshipBuilder.create("Heavy Chemical Lander")
                .sourceType(SourceType.PROPOSED).faction("Generic concept").era("Near-future")
                .designation("HCL-9")
                .shipClass(ShipClass.LANDER)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(30).engineTons(15).propellantTons(80)
                .payloadTons(20).crewTons(5).radiatorTons(0)
                .crew(4).lengthMeters(28)
                .description("High-thrust bipropellant lander rated for crewed surface descent and ascent in "
                        + "an atmosphere.")
                .build();
    }

    private SpaceshipDesign vasimrDeepSpaceTug() {
        return SpaceshipBuilder.create("VASIMR Deep Space Tug")
                .sourceType(SourceType.PROPOSED).faction("Ad Astra Rocket (VASIMR)").era("Near-future concept")
                .designation("TUG-V")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.VASIMR)
                .structureTons(200).engineTons(100).propellantTons(150)
                .payloadTons(400).crewTons(20).radiatorTons(80)
                .crew(6).lengthMeters(90)
                .description("Electric deep-space tug for slow, efficient cargo and module repositioning; "
                        + "large radiators dump reactor waste heat.")
                .build();
    }

    private SpaceshipDesign starshotProbe() {
        return SpaceshipBuilder.create("Breakthrough Starshot Probe")
                .sourceType(SourceType.PROPOSED).faction("Breakthrough Initiatives").era("2016 proposal")
                .designation("SHOT-1")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.LASER_SAIL)
                .structureTons(0.002).engineTons(0).propellantTons(0)
                .payloadTons(0.001).crewTons(0).radiatorTons(0)
                .crew(0).lengthMeters(4)
                .description("Gram-scale laser-sail nanocraft accelerated by a ground-based beam; carries no "
                        + "reaction mass.")
                .build();
    }

    private SpaceshipDesign bussardRamjetExplorer() {
        return SpaceshipBuilder.create("Bussard Ramjet Explorer")
                .sourceType(SourceType.PROPOSED).faction("Bussard ramjet concept").era("1960 concept")
                .designation("RAM-1")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.BUSSARD_RAMJET)
                .structureTons(400).engineTons(200).propellantTons(100)
                .payloadTons(100).crewTons(50).radiatorTons(250)
                .crew(20).lengthMeters(300)
                .description("Interstellar explorer that scoops hydrogen with a magnetic ram field; carries "
                        + "only enough fuel to reach scoop speed.")
                .build();
    }

    private SpaceshipDesign antimatterFrigate() {
        return SpaceshipBuilder.create("Antimatter Beam-Core Frigate")
                .sourceType(SourceType.PROPOSED).faction("Antimatter concept").era("Theoretical")
                .designation("FF-AM")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.ANTIMATTER_BEAM_CORE)
                .structureTons(300).engineTons(200).propellantTons(600)
                .payloadTons(100).crewTons(40).radiatorTons(400)
                .crew(12).lengthMeters(180)
                .description("Hard-SF fast frigate driven by proton-antiproton annihilation; massive "
                        + "radiators and heavy gamma shielding dominate the dry mass.")
                .build();
    }

    // ------------------------------------------------------------------
    // Real-world / current spacecraft
    // ------------------------------------------------------------------

    private SpaceshipDesign spaceShuttleOrbiter() {
        return SpaceshipBuilder.create("Space Shuttle Orbiter")
                .sourceType(SourceType.REAL).faction("NASA").era("1981–2011")
                .designation("OV-105")
                .shipClass(ShipClass.SHUTTLE)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(78).engineTons(10).propellantTons(15)
                .payloadTons(24).crewTons(5).radiatorTons(0)
                .crew(7).lengthMeters(37)
                .description("Reusable crewed spaceplane; chemical OMS for orbital manoeuvring, with a "
                        + "gliding re-entry and runway landing. Modest on-orbit Δv.")
                .build();
    }

    private SpaceshipDesign starship() {
        return SpaceshipBuilder.create("Starship (SpaceX)")
                .sourceType(SourceType.REAL).faction("SpaceX").era("2020s")
                .designation("SN")
                .shipClass(ShipClass.LANDER)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(120).engineTons(30).propellantTons(1200)
                .payloadTons(100).crewTons(0).radiatorTons(0)
                .crew(0).lengthMeters(50)
                .description("Fully-reusable methalox super-heavy upper stage and lander; propulsive landing "
                        + "on Earth, the Moon and Mars with on-orbit refilling.")
                .build();
    }

    private SpaceshipDesign crewDragon() {
        return SpaceshipBuilder.create("SpaceX Crew Dragon")
                .sourceType(SourceType.REAL).faction("SpaceX").era("2020")
                .designation("Falcon Heavy / Dragon")
                .shipClass(ShipClass.SHUTTLE)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(8).engineTons(1).propellantTons(2)
                .payloadTons(3).crewTons(0.5).radiatorTons(0)
                .crew(4).lengthMeters(8)
                .description("Crew capsule launched atop Falcon Heavy/9; Draco manoeuvring thrusters, "
                        + "SuperDraco abort, and a parachute splashdown.")
                .build();
    }

    private SpaceshipDesign apolloCsm() {
        return SpaceshipBuilder.create("Apollo Command/Service Module")
                .sourceType(SourceType.REAL).faction("NASA").era("1968–1975")
                .designation("CSM")
                .shipClass(ShipClass.SHUTTLE)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(12).engineTons(1).propellantTons(18)
                .payloadTons(1).crewTons(1).radiatorTons(0)
                .crew(3).lengthMeters(11)
                .description("Lunar-program crew ship; the SPS engine performed lunar-orbit insertion and "
                        + "trans-Earth injection, with the command module returning by parachute.")
                .build();
    }

    private SpaceshipDesign orionMpcv() {
        return SpaceshipBuilder.create("Orion MPCV")
                .sourceType(SourceType.REAL).faction("NASA / ESA").era("2020s")
                .designation("Artemis")
                .shipClass(ShipClass.SHUTTLE)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(10).engineTons(1).propellantTons(9)
                .payloadTons(2).crewTons(1).radiatorTons(0)
                .crew(4).lengthMeters(5)
                .description("Beyond-LEO crew vehicle for the Artemis program, paired with a European "
                        + "service module for propulsion and power.")
                .build();
    }

    private SpaceshipDesign voyagerProbe() {
        return SpaceshipBuilder.create("Voyager Probe")
                .sourceType(SourceType.REAL).faction("NASA / JPL").era("1977")
                .designation("V'Ger")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(0.7).engineTons(0.05).propellantTons(0.1)
                .payloadTons(0.15).crewTons(0).radiatorTons(0)
                .crew(0).lengthMeters(4)
                .description("RTG-powered interstellar probe; tiny hydrazine thrusters and a chain of "
                        + "gravity assists carried it past the heliopause.")
                .build();
    }

    private SpaceshipDesign dawnSpacecraft() {
        return SpaceshipBuilder.create("Dawn Ion Spacecraft")
                .sourceType(SourceType.REAL).faction("NASA / JPL").era("2007–2018")
                .designation("Dawn")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.ION_GRIDDED)
                .structureTons(0.6).engineTons(0.1).propellantTons(0.45)
                .payloadTons(0.15).crewTons(0).radiatorTons(0)
                .crew(0).lengthMeters(2)
                .description("NASA ion-propulsion probe that entered orbit around both Vesta and Ceres on a "
                        + "few hundred kilograms of xenon — high Δv from a trickle of thrust.")
                .build();
    }

    // ------------------------------------------------------------------
    // The Expanse
    // ------------------------------------------------------------------

    private SpaceshipDesign rocinante() {
        return SpaceshipBuilder.create("Rocinante")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("MCRN / independent").era("~2350")
                .designation("MCRN Tachi / ECF-270")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.EPSTEIN_DRIVE)
                .structureTons(200).engineTons(150).propellantTons(600)
                .payloadTons(50).crewTons(20).radiatorTons(250)
                .crew(6).lengthMeters(46)
                .description("Corvette-class Martian frigate built around an Epstein drive for sustained "
                        + "high-g burns; PDCs, torpedoes and a keel-mounted rail gun.")
                .build();
    }

    private SpaceshipDesign donnager() {
        return SpaceshipBuilder.create("MCRN Donnager")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("MCRN").era("~2350")
                .designation("Donnager-class")
                .shipClass(ShipClass.MOTHERSHIP)
                .driveType(DriveType.EPSTEIN_DRIVE)
                .structureTons(5000).engineTons(2000).propellantTons(8000)
                .payloadTons(3000).crewTons(800).radiatorTons(2000)
                .crew(1000).lengthMeters(500)
                .carry("Marine Dropship", ShipClass.DROPSHIP, 8, 25, "boarding / assault")
                .carry("Shuttle", ShipClass.SHUTTLE, 4, 30, "transfer")
                .description("Martian Navy flagship battleship; an Epstein-drive capital ship carrying "
                        + "dropships and shuttles with overwhelming firepower.")
                .build();
    }

    private SpaceshipDesign thomasPrince() {
        return SpaceshipBuilder.create("UNN Thomas Prince")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("UN Navy").era("~2350")
                .designation("Truman-class")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.EPSTEIN_DRIVE)
                .structureTons(4000).engineTons(1800).propellantTons(7000)
                .payloadTons(2500).crewTons(600).radiatorTons(1800)
                .crew(800).lengthMeters(480)
                .carry("Marine Dropship", ShipClass.DROPSHIP, 10, 25, "boarding")
                .carry("Shuttle", ShipClass.SHUTTLE, 6, 30, "transfer")
                .description("UN Navy battleship/carrier; flagship of the combined fleet sent to the Ring.")
                .build();
    }

    private SpaceshipDesign freeNavyPella() {
        return SpaceshipBuilder.create("Free Navy Pella")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("Free Navy").era("~2350")
                .designation("Pella")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.EPSTEIN_DRIVE)
                .structureTons(1500).engineTons(700).propellantTons(2500)
                .payloadTons(600).crewTons(150).radiatorTons(700)
                .crew(200).lengthMeters(300)
                .carry("Raider Dropship", ShipClass.DROPSHIP, 4, 25, "raids")
                .description("Marco Inaros's flagship: a captured Martian battleship leading the Free Navy.")
                .build();
    }

    private SpaceshipDesign tychoConstructionShip() {
        return SpaceshipBuilder.create("Tycho Construction Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("Tycho / OPA").era("~2350")
                .designation("Tycho")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(3000).engineTons(1000).propellantTons(2000)
                .payloadTons(5000).crewTons(300).radiatorTons(800)
                .crew(500).lengthMeters(600)
                .description("Mobile orbital shipyard and construction platform; a vast payload of fabrication "
                        + "gear and habitat modules rather than weapons.")
                .build();
    }

    private SpaceshipDesign behemoth() {
        return SpaceshipBuilder.create("Behemoth / Medina Station")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("OPA").era("~2350")
                .designation("LDSS Nauvoo")
                .shipClass(ShipClass.COLONY_SHIP)
                .driveType(DriveType.EPSTEIN_DRIVE)
                .structureTons(8000).engineTons(3000).propellantTons(6000)
                .payloadTons(5000).crewTons(3000).radiatorTons(2500)
                .crew(5000).lengthMeters(2000)
                .carry("Colony Lander", ShipClass.LANDER, 6, 200, "surface colonisation")
                .description("Mormon generation ship turned OPA warship turned Ring-space station; a spun-up "
                        + "cylinder habitat driven by Epstein drives.")
                .build();
    }

    private SpaceshipDesign purnKleenIceHauler() {
        return SpaceshipBuilder.create("Pur'n'Kleen Ice Hauler")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("Pur'n'Kleen Water Co.").era("Pre-Epstein era")
                .designation("early-era")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(500).engineTons(50).propellantTons(1500)
                .payloadTons(2000).crewTons(10).radiatorTons(0)
                .crew(8).lengthMeters(300)
                .description("Early-era chemical ice freighter working short Belt hops; cavernous tanks but "
                        + "minimal Δv, suited only to nearby transfers.")
                .build();
    }

    private SpaceshipDesign canterbury() {
        return SpaceshipBuilder.create("Canterbury")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("Pur'n'Kleen Water Co.").era("~2350")
                .designation("Cant")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(600).engineTons(100).propellantTons(1500)
                .payloadTons(2500).crewTons(40).radiatorTons(0)
                .crew(50).lengthMeters(500)
                .description("Converted colony transport hauling ice from Saturn's rings to the Belt; the "
                        + "original Pur'n'Kleen workhorse, with a nuclear-thermal upgrade.")
                .build();
    }

    // ------------------------------------------------------------------
    // Foundation (Asimov)
    // ------------------------------------------------------------------

    private SpaceshipDesign foundationTrader() {
        return SpaceshipBuilder.create("Foundation Trader Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Foundation").faction("Foundation Traders").era("Early Foundation era")
                .designation("early-era")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(80).engineTons(30).propellantTons(150)
                .payloadTons(100).crewTons(5).radiatorTons(0)
                .crew(4).lengthMeters(80)
                .description("Independent trader spreading Foundation nucleics to the Periphery; a small "
                        + "atomic-powered hull crammed with trade goods.")
                .build();
    }

    private SpaceshipDesign imperialNavyCruiser() {
        return SpaceshipBuilder.create("Imperial Navy Cruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Foundation").faction("Galactic Empire").era("Imperial era")
                .designation("Trantor")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(600).engineTons(300).propellantTons(800)
                .payloadTons(200).crewTons(80).radiatorTons(300)
                .crew(100).lengthMeters(400)
                .description("Galactic Empire line warship projecting Trantor's authority across the spiral "
                        + "arm (hyperdrive not modelled; fusion-torch sublight performance).")
                .build();
    }

    private SpaceshipDesign mulesFlagship() {
        return SpaceshipBuilder.create("The Mule's Flagship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Foundation").faction("The Mule").era("Interregnum")
                .designation("Mutant")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.ANTIMATTER_BEAM_CORE)
                .structureTons(150).engineTons(100).propellantTons(300)
                .payloadTons(50).crewTons(10).radiatorTons(200)
                .crew(20).lengthMeters(180)
                .description("The mentalic conqueror's advanced warship, outclassing the decaying Imperial "
                        + "fleets it faced.")
                .build();
    }

    private SpaceshipDesign terminusWarship() {
        return SpaceshipBuilder.create("Terminus Foundation Warship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Foundation").faction("Foundation").era("Late Seldon era")
                .designation("Star Bridge")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(300).engineTons(150).propellantTons(400)
                .payloadTons(100).crewTons(30).radiatorTons(150)
                .crew(40).lengthMeters(250)
                .description("Foundation-built warship of the late Seldon era, defending the Periphery with "
                        + "technology the Empire had forgotten.")
                .build();
    }

    private SpaceshipDesign cleonDreadnought() {
        return SpaceshipBuilder.create("Cleon-era Imperial Dreadnought")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Foundation").faction("Galactic Empire").era("Cleon dynasty")
                .designation("Genetic Dynasty")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(6000).engineTons(2500).propellantTons(7000)
                .payloadTons(4000).crewTons(1500).radiatorTons(2500)
                .crew(3000).lengthMeters(1200)
                .carry("Interceptor", ShipClass.FIGHTER, 60, 12, "space superiority")
                .carry("Assault Lander", ShipClass.DROPSHIP, 10, 25, "planetary assault")
                .description("Capital ship of the Genetic Dynasty's fleet; carries interceptors and assault "
                        + "landers to overawe rebellious worlds.")
                .build();
    }

    // ------------------------------------------------------------------
    // Project Hail Mary
    // ------------------------------------------------------------------

    private SpaceshipDesign hailMary() {
        return SpaceshipBuilder.create("Hail Mary Spacecraft")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Project Hail Mary").faction("Earth").era("Near future")
                .designation("Project Hail Mary")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.SPIN_DRIVE)
                .structureTons(100).engineTons(50).propellantTons(200)
                .payloadTons(30).crewTons(20).radiatorTons(100)
                .crew(3).lengthMeters(47)
                .description("Astrophage-fuelled interstellar ship sent to Tau Ceti; the spin drive sustains "
                        + "roughly 1.5 g of cruise and carries the Beetle return probes.")
                .build();
    }

    // ------------------------------------------------------------------
    // Other notable sci-fi
    // ------------------------------------------------------------------

    private SpaceshipDesign millenniumFalcon() {
        return SpaceshipBuilder.create("Millennium Falcon")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Star Wars").faction("Rebel Alliance").era("Galactic Civil War")
                .designation("YT-1300")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(30).engineTons(15).propellantTons(40)
                .payloadTons(20).crewTons(3).radiatorTons(15)
                .crew(4).lengthMeters(35)
                .description("Heavily-modified light freighter; sublight ion/fusion drive (hyperdrive not "
                        + "modelled), with quad laser cannons and a smuggler's holds.")
                .build();
    }

    private SpaceshipDesign serenity() {
        return SpaceshipBuilder.create("Serenity (Firefly)")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Firefly").faction("Independent").era("2517")
                .designation("Firefly-class")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(40).engineTons(20).propellantTons(50)
                .payloadTons(60).crewTons(8).radiatorTons(20)
                .crew(9).lengthMeters(58)
                .description("Mid-bulk transport with a gravity-rotor fusion drive; no weapons, lots of "
                        + "cargo space and a found family for a crew.")
                .build();
    }

    private SpaceshipDesign discoveryOne() {
        return SpaceshipBuilder.create("Discovery One")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("2001: A Space Odyssey").faction("United States").era("2001")
                .designation("XD-1")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.GAS_CORE_NUCLEAR)
                .structureTons(400).engineTons(150).propellantTons(300)
                .payloadTons(100).crewTons(40).radiatorTons(150)
                .crew(5).lengthMeters(140)
                .description("Jupiter-mission ship from 2001; a gas-core nuclear drive, a centrifuge habitat "
                        + "and the HAL 9000 computer.")
                .build();
    }

    private SpaceshipDesign hermes() {
        return SpaceshipBuilder.create("Hermes (The Martian)")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Martian").faction("NASA").era("2035")
                .designation("Ares")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.NUCLEAR_ELECTRIC)
                .structureTons(300).engineTons(100).propellantTons(150)
                .payloadTons(200).crewTons(30).radiatorTons(120)
                .crew(6).lengthMeters(110)
                .description("Reusable Earth-Mars cycler; a nuclear reactor feeds ion engines for steady, "
                        + "fuel-efficient low-thrust cruise.")
                .build();
    }

    private SpaceshipDesign normandy() {
        return SpaceshipBuilder.create("Normandy SR-2")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Mass Effect").faction("Cerberus / Systems Alliance").era("2185")
                .designation("SR-2")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(100).engineTons(50).propellantTons(120)
                .payloadTons(40).crewTons(20).radiatorTons(50)
                .crew(30).lengthMeters(216)
                .description("Stealth frigate with a Tantalus drive core (FTL not modelled); fusion sublight "
                        + "thrusters and an internal heat sink for silent running.")
                .build();
    }

    private SpaceshipDesign heartOfGold() {
        return SpaceshipBuilder.create("Heart of Gold")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Hitchhiker's Guide to the Galaxy").faction("Galactic Government").era("—")
                .designation("Improbable")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.ANTIMATTER_BEAM_CORE)
                .structureTons(5).engineTons(5).propellantTons(10)
                .payloadTons(3).crewTons(2).radiatorTons(8)
                .crew(2).lengthMeters(45)
                .description("Prototype ship with an Infinite Improbability Drive (modelled here as an exotic "
                        + "antimatter torch). Don't Panic.")
                .build();
    }

    private SpaceshipDesign executor() {
        return SpaceshipBuilder.create("Executor-class Star Dreadnought")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("Star Wars").faction("Galactic Empire").era("Galactic Civil War")
                .designation("Super Star Destroyer")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(8000).engineTons(3000).propellantTons(9000)
                .payloadTons(6000).crewTons(2000).radiatorTons(3000)
                .crew(5000).lengthMeters(19000)
                .carry("TIE Fighter", ShipClass.FIGHTER, 100, 12, "space superiority")
                .carry("Assault Lander", ShipClass.DROPSHIP, 20, 25, "ground assault")
                .description("Command Super Star Destroyer carrying a vast complement of fighters and "
                        + "landers (sublight drive modelled; hyperdrive omitted).")
                .build();
    }

    // ==================================================================
    // Terran Republic universe (Charles E. Gannon: Caine Riordan / Terran
    // Republic, and the Murphy's Lawless / Lost Soldiers spinoff)
    //
    // Sublight thrust drives only; interstellar travel in these books is by
    // "shift", which is instantaneous between fixed points and so is not a
    // delta-V budget. The Terran Republic is deliberately capable-but-not-
    // dominant; the Ktoran Dominion runs visibly ahead on the KTORAN_ADVANCED
    // drive (higher Isp and thrust, lighter structures).
    // ==================================================================

    private static final String CR = "Caine Riordan";

    // ---------------------------- United Earth / Terran Republic ----------

    private SpaceshipDesign ekraniCruiser() {
        return SpaceshipBuilder.create("Ekrani-class Cruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Terran Republic").era("Post-Contact")
                .designation("CA-EKR")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(800).engineTons(350).propellantTons(900)
                .payloadTons(250).crewTons(120).radiatorTons(300)
                .crew(250).lengthMeters(300)
                .carry("Pinnace", ShipClass.SHUTTLE, 2, 40, "boarding / courier")
                .carry("Assault Shuttle", ShipClass.DROPSHIP, 2, 25, "marine boarding")
                .description("Mainstay Terran Republic line cruiser: balanced armour, rail and missile "
                        + "armament and a pair of boarding craft. Capable, but no match one-on-one for a "
                        + "Ktoran cruiser.")
                .build();
    }

    private SpaceshipDesign arduinosDestroyer() {
        return SpaceshipBuilder.create("Arduinos-class Destroyer")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Terran Republic").era("Post-Contact")
                .designation("DD-ARD")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(300).engineTons(150).propellantTons(400)
                .payloadTons(80).crewTons(50).radiatorTons(130)
                .crew(90).lengthMeters(180)
                .description("Fast Terran Republic destroyer/frigate for escort, picket and anti-shipping "
                        + "work; relies on speed and missiles rather than weight of armour.")
                .build();
    }

    private SpaceshipDesign indrajitSupercarrier() {
        return SpaceshipBuilder.create("Indrajit-class Supercarrier")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Terran Republic").era("Post-Contact")
                .designation("CV-IND")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(4000).engineTons(1500).propellantTons(5000)
                .payloadTons(3000).crewTons(800).radiatorTons(1500)
                .crew(2000).lengthMeters(700)
                .carry("Strike Fighter", ShipClass.FIGHTER, 48, 12, "space superiority / strike")
                .carry("Assault Dropship", ShipClass.DROPSHIP, 12, 25, "planetary assault")
                .carry("Shuttle", ShipClass.SHUTTLE, 6, 30, "logistics")
                .description("Terran Republic fleet supercarrier and command flagship; its air wing of strike "
                        + "fighters and dropships is the Republic's main force-projection tool.")
                .build();
    }

    private SpaceshipDesign ospreyAssaultTransport() {
        return SpaceshipBuilder.create("Osprey-class Assault Transport")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Terran Republic").era("Post-Contact")
                .designation("LPA-OSP")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(600).engineTons(250).propellantTons(700)
                .payloadTons(800).crewTons(100).radiatorTons(250)
                .crew(300).lengthMeters(280)
                .carry("Assault Dropship", ShipClass.DROPSHIP, 8, 25, "ship-to-surface assault")
                .carry("Heavy Lander", ShipClass.LANDER, 4, 60, "armour / heavy lift")
                .description("Terran Republic assault transport: carries a reinforced ground element and lands "
                        + "it via dropships and heavy landers under contested conditions.")
                .build();
    }

    private SpaceshipDesign caineRiordanCorvette() {
        return SpaceshipBuilder.create("Caine Riordan's Corvette")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("IRIS / Terran Republic").era("Post-Contact")
                .designation("CRF-1")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(25).engineTons(12).propellantTons(35)
                .payloadTons(15).crewTons(5).radiatorTons(12)
                .crew(6).lengthMeters(40)
                .description("Representative of the fast, lightly-armed vessels Caine Riordan crews across the "
                        + "series for survey, contact and covert insertion work — long legs, small footprint.")
                .build();
    }

    private SpaceshipDesign ueBattleship() {
        return SpaceshipBuilder.create("UE Battleship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("United Earth").era("First Contact era")
                .designation("BB-UE")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(1500).engineTons(600).propellantTons(1500)
                .payloadTons(400).crewTons(250).radiatorTons(500)
                .crew(600).lengthMeters(360)
                .description("United Earth heavy line-of-battle ship: maximum armour and primary armament for "
                        + "the wall of battle, accepting lower acceleration than a battlecruiser.")
                .build();
    }

    private SpaceshipDesign ueBattlecruiser() {
        return SpaceshipBuilder.create("UE Battlecruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("United Earth").era("First Contact era")
                .designation("BC-UE")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(1000).engineTons(500).propellantTons(1400)
                .payloadTons(300).crewTons(180).radiatorTons(400)
                .crew(450).lengthMeters(340)
                .description("United Earth battlecruiser: battleship-grade guns on a faster, lighter hull, "
                        + "trading protection for the acceleration to dictate the engagement range.")
                .build();
    }

    private SpaceshipDesign ueNavyPatrolCorvette() {
        return SpaceshipBuilder.create("UE Navy Patrol Corvette")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("United Earth").era("First Contact era")
                .designation("PC-UE")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(40).engineTons(20).propellantTons(50)
                .payloadTons(20).crewTons(8).radiatorTons(15)
                .crew(12).lengthMeters(60)
                .description("United Earth patrol and customs corvette: system policing, interdiction and "
                        + "showing the flag at the Republic's expanding frontier.")
                .build();
    }

    private SpaceshipDesign diplomaticCourier() {
        return SpaceshipBuilder.create("Diplomatic Courier Vessel")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Terran Republic").era("Post-Contact")
                .designation("DC-1")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(30).engineTons(18).propellantTons(45)
                .payloadTons(15).crewTons(8).radiatorTons(12)
                .crew(10).lengthMeters(55)
                .description("Fast, lightly-armed courier carrying Terran Republic delegations to Accord "
                        + "gatherings and first-contact meetings; speed and prestige over firepower.")
                .build();
    }

    private SpaceshipDesign terranColonyTransport() {
        return SpaceshipBuilder.create("Terran Colony Transport")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Terran Republic").era("Post-Contact")
                .designation("CT-TR")
                .shipClass(ShipClass.COLONY_SHIP)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(3000).engineTons(1200).propellantTons(3500)
                .payloadTons(4000).crewTons(1500).radiatorTons(1000)
                .crew(4000).lengthMeters(900)
                .carry("Colony Lander", ShipClass.LANDER, 6, 200, "surface colonisation")
                .description("Terran Republic colony transport moving settlers and infrastructure to new "
                        + "worlds opened by shift routes; carries heavy landers to seed a colony.")
                .build();
    }

    private SpaceshipDesign uegFirstContactSurveyShip() {
        return SpaceshipBuilder.create("UEG First Contact Survey Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("United Earth (UEG)").era("First Contact (2105)")
                .designation("SV-UEG")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(200).engineTons(100).propellantTons(350)
                .payloadTons(120).crewTons(60).radiatorTons(120)
                .crew(40).lengthMeters(160)
                .description("United Earth survey ship of the first-contact era (the Fire with Fire period): "
                        + "sensors and labs over weapons, sent down newly-opened shift lines to make contact.")
                .build();
    }

    // ---------------------------- Ktoran Dominion (very advanced) ----------

    private SpaceshipDesign ktoranDreadnought() {
        return SpaceshipBuilder.create("Ktoran Dreadnought")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Ktoran Dominion").era("Post-Contact")
                .designation("KDN")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.KTORAN_ADVANCED)
                .structureTons(5000).engineTons(2500).propellantTons(9000)
                .payloadTons(3500).crewTons(700).radiatorTons(1500)
                .crew(1500).lengthMeters(800)
                .carry("Ktoran Interceptor", ShipClass.FIGHTER, 60, 12, "space superiority")
                .carry("Boarding Craft", ShipClass.DROPSHIP, 12, 25, "boarding / suppression")
                .description("Capital ship of the Ktoran Dominion: superior drive, weapons and automation make "
                        + "it the benchmark every other power measures itself against — and falls short of.")
                .build();
    }

    private SpaceshipDesign ktoranCruiser() {
        return SpaceshipBuilder.create("Ktoran Cruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Ktoran Dominion").era("Post-Contact")
                .designation("KCA")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.KTORAN_ADVANCED)
                .structureTons(700).engineTons(400).propellantTons(1600)
                .payloadTons(250).crewTons(100).radiatorTons(350)
                .crew(200).lengthMeters(320)
                .description("Dominion line cruiser: out-accelerates and outranges its Terran equivalent, "
                        + "reflecting the Ktor's arrogant, generations-long technological lead.")
                .build();
    }

    private SpaceshipDesign ktoranFastAttackShip() {
        return SpaceshipBuilder.create("Ktoran Fast Attack Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Ktoran Dominion").era("Post-Contact")
                .designation("KFA")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.KTORAN_ADVANCED)
                .structureTons(60).engineTons(50).propellantTons(200)
                .payloadTons(25).crewTons(10).radiatorTons(40)
                .crew(15).lengthMeters(90)
                .description("Ktoran raider/interceptor built around a vast propellant fraction and an advanced "
                        + "drive — blistering acceleration and reach for hit-and-run strikes.")
                .build();
    }

    // ---------------------------- Hkh'Rkh (warrior race) ----------

    private SpaceshipDesign hkhRkhWarcruiser() {
        return SpaceshipBuilder.create("Hkh'Rkh Warcruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Hkh'Rkh").era("Post-Contact")
                .designation("HRW")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.HKHRKH_THRUST)
                .structureTons(1200).engineTons(700).propellantTons(1200)
                .payloadTons(300).crewTons(200).radiatorTons(400)
                .crew(500).lengthMeters(350)
                .description("Hkh'Rkh warcruiser: heavily overbuilt and hard-accelerating, crewed by a proud "
                        + "warrior species that closes to decisive range and accepts losses to win honour.")
                .build();
    }

    private SpaceshipDesign hkhRkhAssaultShip() {
        return SpaceshipBuilder.create("Hkh'Rkh Assault Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Hkh'Rkh").era("Post-Contact")
                .designation("HRA")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.HKHRKH_THRUST)
                .structureTons(2000).engineTons(1000).propellantTons(1800)
                .payloadTons(1500).crewTons(400).radiatorTons(600)
                .crew(800).lengthMeters(450)
                .carry("War Dropship", ShipClass.DROPSHIP, 12, 25, "boarding / planetary assault")
                .carry("Heavy Lander", ShipClass.LANDER, 4, 60, "armour landing")
                .description("Hkh'Rkh assault ship carrying boarding parties and ground forces; built to deliver "
                        + "warriors into contact rather than to fence at long range.")
                .build();
    }

    // ---------------------------- SpinDog / RockHound (Murphy's Lawless) ----

    private SpaceshipDesign spinDogQShip() {
        return SpaceshipBuilder.create("SpinDog Q-Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("SpinDog").era("Lost Soldiers Era")
                .designation("SD-Q")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(300).engineTons(150).propellantTons(400)
                .payloadTons(600).crewTons(40).radiatorTons(150)
                .crew(60).lengthMeters(250)
                .description("SpinDog armed freighter: a habitat-built hauler with concealed weapons and "
                        + "armour, looking like an ordinary trader until it bares its teeth.")
                .build();
    }

    private SpaceshipDesign spinDogHabitatMonitor() {
        return SpaceshipBuilder.create("SpinDog Habitat Monitor")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("SpinDog").era("Lost Soldiers Era")
                .designation("SD-MON")
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(250).engineTons(120).propellantTons(300)
                .payloadTons(100).crewTons(40).radiatorTons(130)
                .crew(80).lengthMeters(200)
                .description("SpinDog defensive monitor guarding the spin habitats: short-legged but heavily "
                        + "armed, optimised to fight near home rather than to range across the system.")
                .build();
    }

    private SpaceshipDesign rockHoundRaider() {
        return SpaceshipBuilder.create("RockHound Raider")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("RockHound").era("Lost Soldiers Era")
                .designation("RH-R")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(50).engineTons(25).propellantTons(90)
                .payloadTons(30).crewTons(8).radiatorTons(0)
                .crew(12).lengthMeters(70)
                .description("RockHound raider converted from an asteroid-mining boat: rugged, cheap and crewed "
                        + "by hardened belters who fight dirty and disappear into the rocks.")
                .build();
    }

    // ---------------------------- Other notable powers ----------

    private SpaceshipDesign aratKurWarship() {
        return SpaceshipBuilder.create("Arat Kur Warship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Arat Kur Wholenest").era("First Contact era")
                .designation("AK-W")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(700).engineTons(350).propellantTons(900)
                .payloadTons(200).crewTons(80).radiatorTons(350)
                .crew(150).lengthMeters(280)
                .description("Arat Kur warship: the cautious, subterranean species that besieged Earth in Trial "
                        + "by Fire — technically strong and methodical, but brittle when its plans unravel.")
                .build();
    }

    private SpaceshipDesign slaasriithiShiftCarrier() {
        return SpaceshipBuilder.create("Slaasriithi Shift-Carrier")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Slaasriithi").era("Post-Contact")
                .designation("SL-SC")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(3000).engineTons(1200).propellantTons(3000)
                .payloadTons(2500).crewTons(500).radiatorTons(1000)
                .crew(1000).lengthMeters(650)
                .carry("Biotech Shuttle", ShipClass.SHUTTLE, 8, 30, "transfer / contact")
                .carry("Survey Lander", ShipClass.LANDER, 4, 60, "exploration")
                .description("Slaasriithi shift-carrier: the grown-not-built craft of a gentle, communal, "
                        + "biotechnological species who become wary allies of humanity.")
                .build();
    }

    private SpaceshipDesign dornaaniPatrolCruiser() {
        return SpaceshipBuilder.create("Dornaani Patrol Cruiser")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Dornaani Collective").era("Post-Contact")
                .designation("DOR-PC")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.KTORAN_ADVANCED)
                .structureTons(500).engineTons(300).propellantTons(1400)
                .payloadTons(200).crewTons(60).radiatorTons(300)
                .crew(40).lengthMeters(260)
                .description("Dornaani Custodian patrol cruiser: small, lightly crewed and heavily automated, "
                        + "fielding technology at or beyond the Ktoran level on behalf of the Accord.")
                .build();
    }

    private SpaceshipDesign roachHiveShip() {
        return SpaceshipBuilder.create("Roach Hive Ship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Roaches").era("Post-Contact")
                .designation("RCH-H")
                .shipClass(ShipClass.MOTHERSHIP)
                .driveType(DriveType.HKHRKH_THRUST)
                .structureTons(4000).engineTons(1500).propellantTons(4000)
                .payloadTons(3500).crewTons(600).radiatorTons(1500)
                .crew(5000).lengthMeters(900)
                .carry("Swarm Attack Craft", ShipClass.FIGHTER, 80, 12, "swarm assault")
                .carry("Spore Lander", ShipClass.DROPSHIP, 8, 25, "infestation")
                .description("Roach hive ship: an insectoid mothership that fights by drowning opponents in "
                        + "expendable swarm craft rather than by individual quality.")
                .build();
    }

    private SpaceshipDesign roachSwarmRaider() {
        return SpaceshipBuilder.create("Roach Swarm Raider")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Roaches").era("Post-Contact")
                .designation("RCH-R")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.HKHRKH_THRUST)
                .structureTons(30).engineTons(15).propellantTons(40)
                .payloadTons(15).crewTons(5).radiatorTons(15)
                .crew(20).lengthMeters(45)
                .description("Roach swarm raider: a cheap, numerous attack craft meant to be spent freely, "
                        + "dangerous in numbers and all but disposable individually.")
                .build();
    }

    private SpaceshipDesign gokWarship() {
        return SpaceshipBuilder.create("Gok Warship")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Gok").era("Post-Contact")
                .designation("GOK-W")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(900).engineTons(300).propellantTons(1400)
                .payloadTons(200).crewTons(100).radiatorTons(0)
                .crew(300).lengthMeters(320)
                .description("Gok warship: the large, aggressive client species fields crude, heavily-built "
                        + "vessels — strong and stubborn, but technologically a tier below their patrons.")
                .build();
    }

    private SpaceshipDesign lostSoldiersTransport() {
        return SpaceshipBuilder.create("Lost Soldiers Transport")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Lost Soldiers").era("Lost Soldiers Era")
                .designation("LS-T")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(800).engineTons(300).propellantTons(700)
                .payloadTons(1500).crewTons(100).radiatorTons(300)
                .crew(200).lengthMeters(400)
                .description("Transport of the Lost Soldiers / Murphy's Lawless thread: a hold full of displaced "
                        + "human troops carried far from home to fight someone else's war.")
                .build();
    }

    // ---------------------------- additional lore vessels ----------

    private SpaceshipDesign caineRiordanSurveyCutter() {
        return SpaceshipBuilder.create("Caine Riordan's Survey Cutter")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("United Earth (UEG)").era("First Contact (2105)")
                .designation("CRF-0")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(20).engineTons(10).propellantTons(30)
                .payloadTons(12).crewTons(4).radiatorTons(10)
                .crew(5).lengthMeters(35)
                .description("The lightly-armed survey cutter of Caine Riordan's first-contact years: sensors and "
                        + "long endurance over weapons, the kind of hull that drops him into trouble far from home.")
                .build();
    }

    private SpaceshipDesign ueDreadnought() {
        return SpaceshipBuilder.create("UE Dreadnought")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("United Earth").era("First Contact era")
                .designation("DN-UE")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(2000).engineTons(700).propellantTons(1800)
                .payloadTons(500).crewTons(350).radiatorTons(650)
                .crew(800).lengthMeters(420)
                .description("The heaviest United Earth line-of-battle ship: maximum armour and main armament to "
                        + "anchor the wall of battle. Formidable against peers, yet still outclassed ship-for-ship "
                        + "by the Ktoran Dominion.")
                .build();
    }

    private SpaceshipDesign lostSoldiersArmedFreighter() {
        return SpaceshipBuilder.create("Lost Soldiers Armed Freighter")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Lost Soldiers").era("Lost Soldiers Era")
                .designation("LS-AF")
                .shipClass(ShipClass.FREIGHTER)
                .driveType(DriveType.TERRAN_FUSION_DRIVE)
                .structureTons(280).engineTons(140).propellantTons(380)
                .payloadTons(550).crewTons(35).radiatorTons(140)
                .crew(50).lengthMeters(240)
                .description("A displaced-soldiers crew's working freighter, quietly up-gunned for the Lost Soldiers / "
                        + "Murphy's Lawless campaigns: a hauler that can fight when cornered.")
                .build();
    }

    private SpaceshipDesign hkhRkhStrikeCraft() {
        return SpaceshipBuilder.create("Hkh'Rkh Strike Craft")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Hkh'Rkh").era("Post-Contact")
                .designation("HRS")
                .shipClass(ShipClass.CORVETTE)
                .driveType(DriveType.HKHRKH_THRUST)
                .structureTons(45).engineTons(35).propellantTons(90)
                .payloadTons(20).crewTons(10).radiatorTons(25)
                .crew(8).lengthMeters(60)
                .description("Fast Hkh'Rkh attack craft: all thrust and aggression, flown by warriors who measure "
                        + "worth by closing the range and striking first.")
                .build();
    }

    private SpaceshipDesign aratKurDreadnought() {
        return SpaceshipBuilder.create("Arat Kur Dreadnought")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Arat Kur Wholenest").era("First Contact era")
                .designation("AK-DN")
                .shipClass(ShipClass.CRUISER)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(1200).engineTons(600).propellantTons(1500)
                .payloadTons(350).crewTons(150).radiatorTons(600)
                .crew(250).lengthMeters(380)
                .description("Heavy capital ship of the Arat Kur Wholenest, of the kind that besieged Earth: "
                        + "methodical, technically strong and dangerous — until its careful plans are upset.")
                .build();
    }

    private SpaceshipDesign dornaaniCustodianCarrier() {
        return SpaceshipBuilder.create("Dornaani Custodian Shift-Carrier")
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse(CR).faction("Dornaani Collective").era("Post-Contact")
                .designation("DOR-CC")
                .shipClass(ShipClass.CARRIER)
                .driveType(DriveType.KTORAN_ADVANCED)
                .structureTons(3500).engineTons(1800).propellantTons(7000)
                .payloadTons(2500).crewTons(300).radiatorTons(1200)
                .crew(60).lengthMeters(700)
                .carry("Custodian Drone", ShipClass.FIGHTER, 24, 12, "automated enforcement")
                .carry("Shuttle", ShipClass.SHUTTLE, 8, 30, "contact / transfer")
                .description("Enforcement carrier of the Dornaani Custodians: pinnacle Accord technology, heavily "
                        + "automated and barely crewed, policing the Slaasriithi-Dornaani sphere with overwhelming reach.")
                .build();
    }
}
