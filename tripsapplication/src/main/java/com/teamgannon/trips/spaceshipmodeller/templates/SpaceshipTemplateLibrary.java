package com.teamgannon.trips.spaceshipmodeller.templates;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
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
                executor());
    }

    private SpaceshipDesign galaxyClassExplorer() {
        return SpaceshipBuilder.create("Galaxy-class Explorer")
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
}
