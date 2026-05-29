package com.terranrepublic.assets;

/**
 * Functional axis describing what a station is <em>for</em>, orthogonal to {@link StationType}
 * which describes what it <em>is structurally</em>. Captures role and purpose so that Babylon 5
 * and the ISS can share the structural class {@code ORBITAL_CITADEL} while differing on their
 * functional class ({@code DIPLOMATIC} vs {@code RESEARCH}).
 *
 * <p>30 values across six functional groups:
 * <ul>
 *   <li><strong>Military</strong> (7): {@link #MILITARY_COMMAND}, {@link #WEAPONS_PLATFORM},
 *       {@link #DEFENSIVE}, {@link #SURVEILLANCE}, {@link #BORDER_CONTROL},
 *       {@link #FLEET_ANCHORAGE}, {@link #FLEET_REPAIR}</li>
 *   <li><strong>Governance and civilian core</strong> (7): {@link #GOVERNMENT_ADMINISTRATION},
 *       {@link #DIPLOMATIC}, {@link #RESEARCH}, {@link #RESIDENTIAL}, {@link #COMMERCIAL},
 *       {@link #TOURISM}, {@link #MEDICAL_QUARANTINE}</li>
 *   <li><strong>Industrial</strong> (6): {@link #INDUSTRIAL}, {@link #SHIPBUILDING},
 *       {@link #MINING_REFINING}, {@link #LOGISTICS_DEPOT}, {@link #AGRICULTURAL_BIOSPHERE},
 *       {@link #ENERGY_COLLECTION}</li>
 *   <li><strong>Transit and infrastructure</strong> (3): {@link #TRANSPORTATION_HUB},
 *       {@link #COMMUNICATION_RELAY}, {@link #NAVIGATION_BEACON}</li>
 *   <li><strong>Specialized</strong> (5): {@link #COLONIZATION}, {@link #PENAL},
 *       {@link #CULTURAL_EDUCATIONAL}, {@link #TERRAFORMING_CONTROL}, {@link #CONTAINMENT}</li>
 *   <li><strong>Catch-alls</strong> (2): {@link #MULTI_ROLE}, {@link #UNKNOWN}</li>
 * </ul>
 *
 * <p>The grouping is editorial; the enum itself is flat. The order below preserves the
 * grouping for readability.
 *
 * <p>Stations carry one {@code primaryFunction} and an optional set of secondary functions
 * via {@link StationDesign}; secondaries cannot duplicate the primary. {@code MULTI_ROLE} is
 * reserved for genuinely indeterminate cases and any seed using it must carry a description
 * with rationale ({@link Catalog} audit tests enforce this).
 *
 * <p>Note: {@code DERELICT} is intentionally NOT a function value — derelict-ness is a
 * <em>status</em> covered by {@link OperationalState#DERELICT} / {@link OperationalState#WRECK}
 * / {@link OperationalState#SALVAGED}. A Forerunner installation whose purpose is unknown is
 * {@code primaryFunction = UNKNOWN} + {@code operationalState = DERELICT}; the combination
 * handles status-vs-function cleanly without conflating the axes.
 */
public enum StationFunction {

    // ---------------------------------------------------------------- Military

    /** HQ, command authority, flag officer's seat. Example: Starbase 1 (Trek), Earth Spacedock. */
    MILITARY_COMMAND,

    /** Station's primary purpose is firing things. Example: Death Star, Starkiller Base. */
    WEAPONS_PLATFORM,

    /** Planetary/system defense, primarily reactive. Example: Honor Harrington system-defense forts. */
    DEFENSIVE,

    /** Listening, early warning, intelligence gathering. Example: border listening posts, deep-space ELINT. */
    SURVEILLANCE,

    /** Customs, immigration, inspection, transit checkpoints. Example: Expanse inspection stations. */
    BORDER_CONTROL,

    /** Where the fleet sits between operations. Example: Honor Harrington's repair-and-resupply anchorages. */
    FLEET_ANCHORAGE,

    /** Major fleet maintenance and refit. Example: Utopia Planitia (refit aspect), Starbase 1 yard work. */
    FLEET_REPAIR,

    // ----------------------------------------- Governance and civilian core

    /** Seat of civil authority, sector governance, bureaucratic hub. Example: the Citadel (administrative aspect). */
    GOVERNMENT_ADMINISTRATION,

    /** Neutral ground, embassies, treaty venues. Example: Babylon 5, Deep Space 9 (diplomatic aspect). */
    DIPLOMATIC,

    /** Science, observation, laboratories. Example: ISS, Project Lazarus, hard-SF deep-space telescopes. */
    RESEARCH,

    /** Primarily where people live. Example: Stanford Torus, Bernal sphere habitats, Expanse belter stations. */
    RESIDENTIAL,

    /** Trade hub, markets, business. Example: Tycho's commercial face, Nar Shaddaa, freeport stations. */
    COMMERCIAL,

    /** Resort stations, cruise terminals, leisure facilities. Example: Risa (Trek), Mass Effect leisure stations. */
    TOURISM,

    /** Hospitals, plague isolation, biohazard containment, recovery. Example: orbital hospital stations, biohazard isolation. */
    MEDICAL_QUARANTINE,

    // -------------------------------------------------------------- Industrial

    /** Manufacturing, fabrication, finished goods. Example: Tycho's industrial face, Roche Habitat. */
    INDUSTRIAL,

    /** Constructing or repairing ships specifically. Example: Kuat Drive Yards, Utopia Planitia (construction aspect). */
    SHIPBUILDING,

    /** Resource extraction and processing. Example: Belter colonies, Expanse refineries, Bespin's gas mining. */
    MINING_REFINING,

    /** Storage, resupply, refueling. Example: military supply nodes, fuel scoops at gas giants, ice depots. */
    LOGISTICS_DEPOT,

    /** Orbital farms, closed-ecology food production. Example: Robinson-style closed loops, generation-ship greenhouses. */
    AGRICULTURAL_BIOSPHERE,

    /** Solar power, Dyson swarm elements, stellar-energy harvest. Example: solar power satellites, Dyson swarm collectors. */
    ENERGY_COLLECTION,

    // -------------------------------------------- Transit and infrastructure

    /** Passenger and cargo transit at scale. Example: DS9 (post-wormhole), busy Expanse stations. */
    TRANSPORTATION_HUB,

    /** Comms infrastructure, signal repeating. Example: Mass Effect comm buoys at scale, Foundation hyperspace relays. */
    COMMUNICATION_RELAY,

    /** Navigation aid, jump-point markers, hazard warning. Example: hard-SF nav buoys, choke-point markers. */
    NAVIGATION_BEACON,

    // ------------------------------------------------------------- Specialized

    /** Generation ships, ark function, in-transit colonies. Example: Nauvoo/Behemoth, BSG colony ships. */
    COLONIZATION,

    /** Prison, detention, exile. Example: penal asteroids, prison stations. */
    PENAL,

    /** Monasteries, universities, archives, cultural institutions, schools. Example: Streeling University. */
    CULTURAL_EDUCATIONAL,

    /** Planetary engineering coordination, orbital mirrors, atmospheric processing. Example: Mars trilogy control stations. */
    TERRAFORMING_CONTROL,

    /** Holding the dangerous — alien artifacts, sealed anomalies, rogue AI. Example: precursor containment, anomaly platforms. */
    CONTAINMENT,

    // --------------------------------------------------------------- Catch-alls

    /**
     * Genuinely multi-purpose, no clear primary. Last-resort value: any seed using {@code MULTI_ROLE}
     * must carry a description explaining the rationale ({@code CatalogAuditTest} enforces ≥20 chars).
     */
    MULTI_ROLE,

    /**
     * Function not yet determined or genuinely mysterious. Default for entries pending categorization;
     * legitimate for unexplored precursor sites (Ringworld on first encounter, Forerunner installations).
     */
    UNKNOWN
}
