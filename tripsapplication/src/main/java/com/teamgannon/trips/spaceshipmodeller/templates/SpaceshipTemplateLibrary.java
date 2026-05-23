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
                galaxyClassExplorer(),
                battlestarCarrier(),
                daedalusArk(),
                honorverseCruiser(),
                orionBattleship(),
                heavyChemicalLander(),
                vasimrDeepSpaceTug(),
                starshotProbe(),
                bussardRamjetExplorer(),
                antimatterFrigate());
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
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(3000).engineTons(1000).propellantTons(4000)
                .payloadTons(2000).crewTons(600).radiatorTons(1500)
                .crew(2500).lengthMeters(1400)
                .carry("Viper", ShipClass.FIGHTER, 40, 12, "space superiority")
                .carry("Raptor", ShipClass.DROPSHIP, 8, 20, "recon and assault")
                .description("Military carrier built around its embarked Viper wing and Raptor dropships; "
                        + "powerful fusion-torch main drive for high-energy and fast-transit maneuvers.")
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
}
