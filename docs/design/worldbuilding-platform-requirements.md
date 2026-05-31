# TRIPS Worldbuilding Platform — Requirements

**Status**: requirements, pre-architecture
**Date**: 2026-05-31
**Audience**: technical readers new to TRIPS who want to understand why the system is being shaped the way it is

---

## §1 — Purpose and scope

TRIPS (Terran Republic Interstellar Plotting System) is a JavaFX desktop application for visualizing and reasoning about interstellar space. It uses the AT-HYG database (~2.5 million real stars) as its base data and renders them as a 3D stellar map. Users can enter individual star systems to see procedurally-generated planetary geometry, place catalog entries (stations, weapons, megastructures, transport infrastructure) at specific locations, and visualize routing between stars.

This document specifies the requirements for evolving TRIPS into a worldbuilding platform that serves two distinct roles:

**Role 1**: An accurate modeling tool for the real universe. Users see real star data, real planet data where known, real space stations, real planned infrastructure. The system's representation of reality matches canonical scientific sources within the limits of what's known.

**Role 2**: A worldbuilding tool for science fiction. Users can either consume existing fictional universes (with canonical content from an SF series populated into the appropriate places) or create their own universe definitions from scratch (defining fictional places, ships, factions, networks, and overlays).

The two roles coexist in a single TRIPS installation. A user can be in Role 1 mode (seeing only real data) or Role 2 mode (seeing fictional content from one or more active universes). The mode is determined by which universes the user has activated, not by a global flag. Multiple universes can be active simultaneously.

The current TRIPS installation conflates these roles by including fiction-specific entries (Troy from Troy Rising, Posleen Dodecahedrons from Legacy of the Aldenata, HKHRKH_THRUST from Caine Riordan) in the default catalog. This is the architectural debt the requirements in this document are designed to resolve.

**What TRIPS is, in scope.**

A visualization tool. A worldbuilding data platform. A reference system for stellar geography. A canvas for creative invention constrained by real-data accuracy.

**What TRIPS is not, out of scope.**

A game. A simulation of stellar evolution, civilizational dynamics, or trade economics. A procedural universe generator. A multi-user collaboration platform (though universe definitions are shareable artifacts). A timeline simulator that advances state forward in time. A tactical combat resolver. A spacecraft physics simulator.

---

## §2 — Glossary

This section defines terms used throughout this document. Some terms have different meanings in SF worldbuilding vs. real astronomy; this glossary disambiguates.

**Active Universe**: A worldbuilding universe currently enabled in the user's view. Multiple universes can be simultaneously active. Real data is always active (it has no universe tag); fictional content appears only when the relevant universe is active.

**Alias**: A fictional name for a real astronomical place. "40 Eridani A = Vulcan" is an alias defined by the Star Trek universe. Aliases don't modify the real place; they overlay an additional label that appears when the relevant universe is active.

**Catalog**: The application's collection of all known entities — ships, stations, weapons, megastructures, transport nodes, gate networks. Catalog entries are persisted in JPA tables and managed through editors in the Worldbuilding menu. The catalog includes both real entries (ISS, Lunar Gateway) and fictional entries (Troy when the Legacy of the Aldenata universe is active).

**Canon Source**: The fictional source material a fiction-specific catalog entry comes from. "Troy" has a canon source of "Troy Rising" (the John Ringo novel series). Canon sources are tracked via `CatalogProvenance` (added in Phase D.6).

**Era**: A time period within a universe. Universes may span multiple eras (the Foundation universe has Empire, Foundation, Mule, Second Foundation eras). Era determines which entries are visible (under-construction vs operational vs destroyed). Real-data has eras too (Skylab existed only in the 1970s, ISS exists from 1998 onward, Lunar Gateway is planned for the late 2020s).

**Faction**: A polity, civilization, corporation, or group within a universe that controls or builds entities. Aldenata is a faction. Solar Confederation is a faction. Posleen is a faction. Real data has factions too (NASA, ESA, Roscosmos, CNSA).

**Mode**: An informal term for "what's active." Real-only mode means no fictional universes are active. Mixed mode means at least one universe is active alongside real data. Pure-fiction mode means real data is hidden (rare; usually the user wants both).

**Place**: An astronomical location — a star, planet, moon, asteroid belt, Lagrange point. Real places come from HYG and accrete. Fictional places (a colony world, an artificial station, a deep-space megastructure location) are added by universes.

**Polity**: An organized political entity within a universe. A polity is a kind of faction; the terms are often interchangeable. "Solar Confederation" is a polity; "Aldenata" might be a polity or a species-level grouping depending on universe.

**Provenance**: Metadata about where a catalog entry comes from. Real entries have provenance "REAL" with HISTORIC/ACTIVE/PLANNED/CANCELLED status. Fiction entries have provenance with the source universe and source work (book, series, etc.) named.

**Real Data**: Astronomical data from canonical scientific sources — the HYG database, exoplanet catalogs, NASA mission data, ESA station data. Real data has no universe tag and is always visible regardless of which universes are active.

**Universe (worldbuilding sense)**: A bounded collection of fiction-specific content. "Legacy of the Aldenata" is a universe. "Star Trek Federation" is a universe. "Larry's Children of the Pattern" is a universe. Each universe contains a set of catalog entries, aliases, factions, networks, and other content scoped to that fictional setting.

**Universe (cosmological sense)**: The actual universe — the 3D space of stars and planets that TRIPS visualizes. This document uses "universe" almost exclusively in the worldbuilding sense; cosmological-universe is referred to as "real space" or "the real universe."

---

## §3 — Role 1: Accurate Universe Modeling

### Personas

**The amateur astronomer.** Has used Stellarium, Celestia, or similar tools. Wants to explore stellar geography interactively — fly around the local solar neighborhood, see where Alpha Centauri is relative to Sol, understand which stars have known exoplanets. Cares deeply about accuracy: if TRIPS shows Sirius at the wrong distance or with the wrong magnitude, that's a defect. Doesn't care about fictional content; would prefer not to see it by default.

**The educator.** Teaches undergraduate astronomy or planetary science. Uses TRIPS to show students what the actual neighborhood looks like, to demonstrate stellar distances and planetary system structure, to make orbital mechanics concrete. Needs the visualization to match what textbooks and authoritative sources say. Wants to easily turn off any fiction content for classroom use.

**The hard-SF author needing reference data.** Writing fiction that takes place in the real stellar neighborhood (e.g., a story set on a planet around Tau Ceti). Uses TRIPS to ensure their fictional setting is geographically plausible — that Tau Ceti is where they say it is, that travel times between stars are correct, that their imagined planet is in the habitable zone of a real-world star. The real data is reference material; their creative invention happens outside TRIPS but uses TRIPS as a constraint.

### Requirements

R1.1 The system shall provide visualization of real stars from canonical astronomical databases (currently AT-HYG ~2.5M stars).

R1.2 Real stellar properties (position, magnitude, spectral type, distance) shall match the source database within standard astronomical precision.

R1.3 The system shall provide planetary data for stars with known exoplanets, drawing from published exoplanet catalogs.

R1.4 For stars without published planetary data, the system may generate procedural planetary systems (per the existing accrete model). Such generated systems shall be labeled clearly as procedural, not factual.

R1.5 The system shall include real space stations (ISS, Tiangong, Mir, Skylab, Salyut 1, Salyut 7, Lunar Gateway, Axiom Station) with accurate orbital placements and operational history.

R1.6 The system shall include real proposed/under-construction infrastructure (Lunar Gateway as planned, Axiom commercial stations, etc.) with PROPOSED or PLANNED status clearly distinguished from operational.

R1.7 Real data shall include provenance pointing to canonical sources (NASA, ESA, JAXA, peer-reviewed publications). A user clicking on the ISS shall be able to see what source the data came from.

R1.8 Real data shall be visible by default. A fresh TRIPS installation shows only real data — no fictional content appears unless the user explicitly activates a universe.

R1.9 Real data shall be available regardless of which (if any) universes are active. The user shall not need to deactivate all universes to see real data; real data is always present alongside whatever universe content is active.

R1.10 Real data shall not be modifiable by universe definitions. A universe can add aliases (Tau Ceti → "Foundation system") that overlay real data, but a universe cannot change Tau Ceti's actual stellar properties. Real data is canonical and immutable from the worldbuilding system's perspective.

R1.11 Real data shall be edit-protected. Users can add their own catalog entries; they cannot delete or modify the canonical real entries. (Future requirement: a "user customization layer" might allow personal annotations, but the canonical layer remains untouched.)

R1.12 The system shall accurately represent the time-dimension of real data. Skylab is historic (operational 1973-1979, deorbited). ISS is currently operational. Lunar Gateway is planned. Future telescope deployments have specified launch windows. The era/status of each real entry shall be visible to the user.

R1.13 Real data updates (new stations launched, new exoplanets discovered) shall be possible through code updates to the application without disturbing user-created universe content.

### What "accurate" means in this role

Accuracy in Role 1 means:

- Stellar positions match HYG/Gaia data within published uncertainties.
- Planetary parameters (mass, orbit, semi-major axis) match exoplanet catalogs.
- Space stations are placed in their actual orbits (or expected orbits for planned stations).
- Real entries cite their canonical source so users can verify the data.
- No creative invention happens in Role 1 data. If something isn't established by canonical sources, it doesn't exist in real data.

Things explicitly allowed even though they involve modeling choices:

- Procedurally-generated planetary systems for stars without known planets (labeled as procedural).
- Visual choices in rendering (colors, sizes, label placement) are aesthetic decisions, not factual claims.
- Approximate orbits for newly-discovered or poorly-characterized objects (with uncertainty noted).

---

## §4 — Role 2: Worldbuilding

### Personas

**The published SF author.** Has a series of novels or short stories set in a coherent fictional universe. Wants TRIPS to make that universe visible — to render the canonical Aldenata wormhole network, to show where Solomon Asteroid Fortress sits in the Gundam universe, to map the Foundation universe's galactic geography. Cares about consistency with their published works and the ability to share the universe definition with readers and collaborators.

**The tabletop RPG gamemaster.** Running a Traveller-style or Stars Without Number campaign. Has a partially-developed setting; wants to define stars, factions, gates, ships at the pace the campaign needs. Doesn't have all the data up front — builds it incrementally as the campaign reveals more. Wants per-session edits to be lightweight (just a station added, a faction renamed) without architectural ceremony.

**The hobbyist worldbuilder.** Building a fictional setting for personal enjoyment, not publication. Mixes inspiration from multiple sources: maybe a sublight-only setting like *The Forever War*, or a setting inspired by both *Diaspora* and *Schild's Ladder*. Plays with what-ifs and experiments. Cares about visualization (does the universe look right?) and rapid iteration (let me try moving this colony elsewhere and see how it feels).

**The collaborative worldbuilding group.** A group of friends or collaborators working together on a shared fictional setting. Want to export universe definitions, share them, merge contributions. Maybe one person owns the canonical version; others propose changes. Want versioning ideally, but at minimum want lossless import/export so universe definitions can live in shared file repositories.

### Requirements

R2.1 The system shall allow users to create and manage fictional universes.

R2.2 Each universe shall be a named, persistent, identifiable container for fictional content.

R2.3 Universes shall be independently activatable. Activating a universe makes its content visible; deactivating hides it.

R2.4 Multiple universes shall be activatable simultaneously. A user can have both "Legacy of the Aldenata" and "Children of the Pattern" active; both their respective fictional entries become visible.

R2.5 Universe content shall include:
- Catalog entries (ships, stations, weapons, megastructures, transport nodes specific to the universe)
- Gate networks specific to the universe
- Aliases mapping real places to fictional names within the universe
- Factions/polities operating within the universe
- Drive types specific to the universe (if any)
- Era definitions within the universe
- Visual presentation rules (colors, icon styles, label priorities) for the universe
- Population/activation rules (which entries get placed where on activation)

R2.6 The system shall ship with at least one example universe demonstrating the platform. This example universe shall be opt-in — present in the installation but not active by default. The example universe shall contain the existing fiction-specific entries currently in the default catalog (Troy, Posleen ships, fiction-specific drive types from Legacy of the Aldenata canon).

R2.7 Users shall be able to create new universes from scratch through the Worldbuilding menu. The creation flow shall require minimum a name; everything else (factions, entries, aliases) can be added incrementally.

R2.8 Universe content shall be persistable to a portable file format (JSON or similar) for sharing.

R2.9 Universes shall be importable from the same portable format. Importing a universe adds it to the installation without disturbing other universes.

R2.10 Universe content is owned by the universe — when a universe is deleted, its content disappears. A user accidentally deleting the "Legacy of the Aldenata" universe loses Troy and the Posleen Dodecahedrons. Deletion shall be confirmed via dialog to prevent accidents.

R2.11 Conflicting universe content shall be resolved by the user. If two active universes both define "Tau Ceti" as having a colony, the user shall see both aliases displayed (perhaps stacked) and can choose to deactivate one.

R2.12 Editing catalog entries within a universe shall not require the user to know which universe they're editing — the editor shall remember the user's active universe context and tag new entries appropriately.

R2.13 Catalog entries belonging to a universe shall be visually distinguished from real entries (e.g., a small badge or icon showing the universe affiliation).

R2.14 Universe definitions shall be lossless when exported and re-imported. A round-trip through JSON export → JSON import shall produce a functionally identical universe.

R2.15 Universe imports from third-party sources shall be safe (no code execution; no installation modification beyond the imported universe data). Universes are pure data.

R2.16 The system shall support universe versioning at the metadata level (each universe has a version field). Sophisticated version control is out of scope; tracking which version is loaded is in scope.

### What "worldbuilding" means in this role

Worldbuilding in Role 2 means:

- Creative invention is welcome. Fictional ships, fictional stations, fictional gate networks, fictional aliases for real stars. Anything that doesn't contradict real data within the universe definition.
- Internal consistency is the user's responsibility, not the system's. TRIPS will not validate that "all Aldenata ships use the Aldenata gate network"; it will display whatever the user defines.
- Universes are bounded. A universe defines what's in it, not what's outside it. The Foundation universe is not required to define the Aldenata universe; they coexist as independent collections.
- Real data is the substrate. Universes overlay onto real stellar geography; they don't replace it. Aliases overlay real star names; placements drop fictional content into real systems.

---

## §5 — Mode separation and crossover

### Mode definitions

The system has three implicit modes determined by which universes are active:

**Real-only mode**: No fictional universes are active. The user sees only real stars, real planets, real stations, real proposed infrastructure. This is the default state of a fresh TRIPS installation.

**Single-universe mode**: Exactly one fictional universe is active. The user sees real data plus that universe's content. Most common worldbuilding scenario.

**Multi-universe mode**: Two or more fictional universes are simultaneously active. The user sees real data plus content from all active universes. Useful for "comparing" universes or for users who genuinely want to see what's in their installation comprehensively.

### Mode behavior requirements

R5.1 Mode shall be determined dynamically by which universes the user has activated. There is no separate mode toggle distinct from universe activation.

R5.2 Universe activation state shall persist across application restarts.

R5.3 Universe activation/deactivation shall take effect immediately. The user shall not need to restart the application after toggling a universe.

R5.4 Visual indication of mode shall be present in the application chrome. The user shall always know which universes are currently active. A status bar or menu indicator showing "Real + Legacy of the Aldenata" or "Real only" satisfies this requirement.

R5.5 Universes shall not "leak" content into each other. A catalog entry tagged as belonging to "Legacy of the Aldenata" shall not appear when only "Children of the Pattern" is active.

R5.6 Real data shall not be lost when a universe is active. The 8 D.5 real stations remain visible regardless of which universes are active.

R5.7 Universe-specific aliases shall be applied only when their universe is active. With Star Trek universe active, 40 Eridani A may display "Vulcan." With Star Trek universe deactivated, it displays "40 Eridani A" again.

### Crossover scenarios

The system shall handle these crossover scenarios correctly:

C5.a A user activating two universes that both define different fictional content in the same real system. The user shall see all the fictional content from both universes layered together, with visual cues distinguishing which universe contributed each entry.

C5.b A user activating two universes that both define different aliases for the same real star. The user shall see both aliases displayed (e.g., "Tau Ceti / Colony Sigma / Foundation system") and shall be able to identify which alias comes from which universe.

C5.c A user deactivating a universe while the catalog editor is open for an entry from that universe. The editor shall warn the user and either close gracefully or allow the user to continue editing (the entry remains accessible to the editor even though it's hidden from the main view).

C5.d A user attempting to assign a real-data placement to a fictional ship that doesn't belong to any active universe. The system shall prevent the assignment or warn the user.

C5.e A user importing a universe that references catalog entries (e.g., factions, drive types) that don't exist in the importing installation. The import shall either fail with a clear error, or succeed with imported content noting the missing dependencies.

---

## §6 — User scenarios

These concrete scenarios illustrate how the system is intended to work in practice. They surface requirements that the bullet points above may not make obvious.

### Scenario A — Fresh install, real-only

Larry installs TRIPS for the first time. He opens the application and sees the 3D stellar map centered on Sol. He sees real stars, real distances, real planets where known. He enters Sol's system view and sees the 8 real space stations: ISS in Earth orbit, Lunar Gateway in lunar orbit, Mir's historic location marked with a "destroyed" badge. He sees no Troy. He sees no Posleen ships. He sees no fictional drive types in the editor. The "Worldbuilding" menu lists "Universes" with "(0 active)" next to it. Everything reads as plausible-by-default.

### Scenario B — Activating the example universe

Larry clicks Worldbuilding → Universes. He sees a panel listing "Legacy of the Aldenata" (description: "John Ringo's Posleen War setting"; status: Available). He clicks Activate. The dialog closes and the status bar updates to "Real + Legacy of the Aldenata." He returns to Sol's system view. Troy is now visible in the outer system. SAPL elements are visible distributed across the inner-system defense perimeter. SheVa Gun is visible at its Earth surface location. The editor's New Construct dropdown now includes Posleen drive types alongside the real ones.

### Scenario C — Creating a personal universe

Larry decides to model his own SF series, "Children of the Pattern." He clicks Worldbuilding → Create Universe. The dialog asks for: Name ("Children of the Pattern"), Description (optional), Source/Author ("Larry Mitchell"), Era (optional first era: "Pre-Collapse"). He clicks Create. The universe is now created (empty — no content yet), and is automatically activated. He opens the Worldbuilding → Star Aliases dialog and adds aliases: "Sol → Old Earth," "Tau Ceti → Akane's homeworld." These aliases now appear in the stellar map when his universe is active. He opens the Stations editor and creates a new station: "Pattern Station." The editor knows his active universe context and tags the station to "Children of the Pattern." He saves. Pattern Station now appears in his universe's content but not in the example universe.

### Scenario D — Sharing a universe with a friend

Larry finishes a session of worldbuilding on "Children of the Pattern." He clicks Worldbuilding → Universes → Export. The dialog asks where to save. He picks `/Users/larry/Documents/cotp-v1.json`. The file is written. He emails it to a friend who also has TRIPS. The friend clicks Worldbuilding → Universes → Import. The friend navigates to the file. The friend's TRIPS imports the universe: "Children of the Pattern" appears in their universe list. They activate it. They see the same aliases and stations Larry created.

### Scenario E — Mid-session universe toggling

Larry is showing his SF series to a friend who's familiar with it. He activates "Children of the Pattern." They explore the universe. Then he wants to compare to another series he's been reading. He activates "Legacy of the Aldenata" while keeping his own universe active. Now both universes show. The friend can see Sol with both Larry's Pattern Station and the Posleen War's Troy. The status bar shows "Real + Legacy of the Aldenata + Children of the Pattern." Larry can toggle each universe independently; the visualization updates immediately.

### Scenario F — A conflict between universes

Larry's "Children of the Pattern" universe defines Tau Ceti as "Akane's homeworld." The Star Trek universe (which Larry has imported as a hypothetical example) defines Tau Ceti as something else (say, "Tau Ceti Prime"). With both universes active, Tau Ceti's label shows both aliases stacked: "Tau Ceti / Akane's homeworld / Tau Ceti Prime" with visual cues showing which universe contributed each. Larry decides this is too cluttered. He deactivates Star Trek. The label becomes just "Tau Ceti / Akane's homeworld." Real-data identity is preserved; the layered aliases were a temporary view.

---

## §7 — Content categories

This section specifies the requirements for each of the 14 content categories. Each category has: definition, role-specific examples, mode interaction (per-universe vs shared vs mode-controlled), and concrete requirements.

### §7.1 — Places

**Definition**: Astronomical locations — stars, planets, moons, asteroid belts, Lagrange points, deep-space regions.

**Role 1 examples**: Sol, Alpha Centauri, Tau Ceti, Earth orbit, Jupiter Trojan region, the asteroid belt.

**Role 2 examples**: A colony location in a fictional star system. A named region within a fictional empire. A deep-space construct location.

**Mode interaction**: Real places are shared (always visible). Fictional places are universe-scoped (visible when their universe is active).

**Requirements**:

R7.1.1 Real places shall come from canonical astronomical databases (HYG, accrete for procedural planetary systems).

R7.1.2 Fictional places shall be definable per-universe. A universe can add a fictional star (not in HYG), a fictional planet in a real system, a fictional asteroid belt at specific coordinates.

R7.1.3 Fictional places shall be visually distinguishable from real places (badge, icon, or color cue).

R7.1.4 Fictional places cannot replace real places. A universe cannot redefine Tau Ceti's spectral type; it can alias the name but the real star remains real.

R7.1.5 Fictional places shall persist when their universe is deactivated, but be invisible until reactivated.

### §7.2 — System Overlays / Aliases

**Definition**: Fictional names, annotations, or interpretations layered onto real astronomical places.

**Role 1 examples**: None. Aliases are inherently a worldbuilding concept.

**Role 2 examples**: "40 Eridani A = Vulcan" (Star Trek). "Tau Ceti = Foundation capital system" (Foundation). "Sol = Terran Republic capital" (Caine Riordan).

**Mode interaction**: Universe-scoped (an alias appears when its universe is active and disappears when deactivated).

**Requirements**:

R7.2.1 Universes shall be able to define aliases for real places.

R7.2.2 Aliases shall be displayed alongside or in place of the real place name when the universe is active.

R7.2.3 Multiple aliases for the same real place (from different universes) shall coexist; visual cues shall distinguish their universe origin.

R7.2.4 Aliases shall include a label, optional description, and optional political/historical context.

R7.2.5 Aliases shall not delete or hide real names. Even with an alias active, the user shall be able to access the real name (e.g., via hover, tooltip, or context menu).

R7.2.6 Real places that haven't been aliased shall display their real names unchanged regardless of which universes are active.

### §7.3 — Space Assets

**Definition**: Artificial objects that exist in space. Ships, stations, habitats, megastructures, weapons platforms, probes, relics.

**Role 1 examples**: ISS, Tiangong, Mir, Skylab, Lunar Gateway (planned), Axiom Station, JWST (in solar orbit). Real probes (Voyager, Pioneer, New Horizons) if/when added.

**Role 2 examples**: Troy (Troy Rising), the Aldenata wormhole gates, Babylon 5, Death Star, Posleen Battle Dodecahedrons.

**Mode interaction**: Real assets are shared (always visible). Fictional assets are universe-scoped.

**Requirements**:

R7.3.1 Real space assets shall be present by default with canonical provenance.

R7.3.2 Fictional space assets shall be tagged to their universe and visible only when the universe is active.

R7.3.3 The existing sealed-hierarchy (SpaceshipDesign, StationDesign, WeaponInstallation, Megastructure) shall apply to both real and fictional assets — the same data model serves both roles.

R7.3.4 Space assets shall be placeable at specific positions in specific star systems via `SolarSystemFeature` (the architecture from Phase E.1).

R7.3.5 Catalog editors shall support creating universe-tagged assets. The editor remembers the active universe context and tags new assets accordingly.

R7.3.6 Real assets shall not be deletable through the editor UI. Fictional assets shall be deletable by the universe owner (i.e., the user who created the universe).

R7.3.7 Asset visibility in the system view shall depend on universe activation. Troy appears in Sol's outer system only when "Legacy of the Aldenata" is active.

### §7.4 — Travel Infrastructure

**Definition**: Things that enable or constrain movement through space. Drive types, jump points, gates, wormholes, beanstalks, launch systems.

**Role 1 examples**: Real launch systems (Falcon 9, SLS), real propulsion technologies (chemical, ion, nuclear thermal where deployed). Real beanstalk concepts (proposed). Real Lagrange-point stations.

**Role 2 examples**: Aldenata wormhole network, Grtul Gates (Caine Riordan), the Posleen interstellar drive, the Aldenata-built jump infrastructure.

**Mode interaction**: Real propulsion technologies are shared. Fictional travel infrastructure is universe-scoped. Drive types are particularly mixed — some are real (chemical, fusion), some are fictional (POSLEEN_INTERSTELLAR).

**Requirements**:

R7.4.1 Real propulsion technologies shall be available in the DriveType enum without universe tagging.

R7.4.2 Fictional drive types shall be tagged to a universe. They appear in editors only when that universe is active.

R7.4.3 The TransitMode taxonomy (SUBLIGHT, JUMP_POINT, WORMHOLE, JUMP_GATE, WARP from Phase E.1) shall apply to both real and fictional drives.

R7.4.4 The DriveType enum's current state (25 values, locked mapping from Phase E.1) shall be reclassified: real drives get no universe tag, fictional drives get tagged to their respective universes (Posleen War or Caine Riordan).

R7.4.5 Jump points shall continue to be derived per the Phase E.1 algorithm. They are inherently universe-agnostic — they exist for any star regardless of whether a fictional universe is active. (The interpretation of jump points may be universe-specific: "Aldenata jump points" might be a description in one universe, "natural gravitational anomalies" in another. But the points themselves are computed identically.)

R7.4.6 Wormholes and gate networks shall be defined per universe. The Aldenata wormhole network exists only when "Legacy of the Aldenata" is active.

R7.4.7 Beanstalks (when modeled as canonical or fictional) shall be tagged appropriately.

### §7.5 — Networks and Connections

**Definition**: The graph layer connecting places. Gate networks, trade routes, military corridors, communication links, exploration paths.

**Role 1 examples**: Communication links between real ground stations and orbital infrastructure. Operational space probe trajectories.

**Role 2 examples**: The Aldenata wormhole network's connectivity graph. Trade routes within a fictional empire. Military corridors used by Posleen invasion forces.

**Mode interaction**: Real networks (limited; mostly informational) are shared. Fictional networks are universe-scoped.

**Requirements**:

R7.5.1 Networks shall be definable per universe via the `GateNetwork` entity (from Phase E.1) and similar future entities for non-gate networks (trade-route entities, communication-link entities).

R7.5.2 Network membership of individual transit nodes (jump gates, wormhole mouths) shall be modeled per Phase E.1's design (`networkId` foreign key on `SolarSystemFeature`).

R7.5.3 Multiple networks within a universe shall be supported (the Aldenata Civilian network and Aldenata Military network as separate entities, for example).

R7.5.4 Cross-universe networks shall not exist. A network is defined within exactly one universe.

R7.5.5 Per-ship network access shall be modeled per Phase E.1's design (`defaultAccessibleNetworkIds` on `SpaceshipDesign`). Ships in a universe can be configured to have access to that universe's networks.

R7.5.6 Network rendering shall be visible when the universe is active. Network edges between gates shall be drawable on the stellar map.

### §7.6 — Factions and Ownership

**Definition**: The polities, civilizations, species, corporations, alliances within a universe that control or use places and assets.

**Role 1 examples**: Real space agencies (NASA, ESA, Roscosmos, CNSA, ISRO, JAXA). Real commercial entities (SpaceX, Boeing, Northrop Grumman where they operate real stations).

**Role 2 examples**: Solar Confederation (Caine Riordan), Aldenata (Posleen War), Federation (Star Trek), Foundation (Foundation series), Hkh'Rkh.

**Mode interaction**: Real factions are shared. Fictional factions are universe-scoped. (Some factions overlap: NASA operates the ISS and might be referenced from multiple universes that involve near-future humanity.)

**Requirements**:

R7.6.1 Factions shall be definable per universe.

R7.6.2 The existing `faction` and `allegiance` String fields on catalog entries shall be enhanced to reference Faction entities rather than free-form strings (a significant refactor).

R7.6.3 Factions shall have: name, description, parent polity (if any), homeworld (reference to a real or fictional place), era of activity, visual color/icon.

R7.6.4 Factions shall be hierarchical. A faction can be a subsidiary of another. (Hkh'Rkh Imperial Navy is a subsidiary of Hkh'Rkh civilization.)

R7.6.5 Faction-controlled places shall be visually distinguishable. Activating the Solar Confederation faction view shall color or highlight all places associated with that faction.

R7.6.6 Faction relationships (allied, hostile, neutral, vassal) shall be definable for the future political-rendering feature.

R7.6.7 Real factions need not have all faction data filled in. A "NASA" faction can exist as a placeholder for ISS attribution without requiring extensive metadata.

### §7.7 — Status and Era

**Definition**: When things exist. Operational status (active, destroyed, abandoned, under construction). Era (which time period). Proposed vs canon vs hypothetical.

**Role 1 examples**: Skylab = HISTORIC/DESTROYED. ISS = ACTIVE/OPERATIONAL. Lunar Gateway = PLANNED. Future telescope mission = PROPOSED.

**Role 2 examples**: A fictional era ("Pre-Collapse," "Post-Collapse," "Late Empire"). Within Foundation universe, distinct eras for First Empire, Foundation, Mule, Second Foundation. Within Posleen War, eras like "Before Invasion," "During Invasion," "After Invasion."

**Mode interaction**: Real statuses are real-data attributes. Eras are universe-scoped. The current viewing era is a user choice that filters visibility.

**Requirements**:

R7.7.1 The existing `OperationalState` enum (OPERATIONAL/DAMAGED/DERELICT/WRECK/UNDER_CONSTRUCTION/SALVAGED) shall apply to both real and fictional assets.

R7.7.2 Real status shall include time-anchoring (start/end dates). Skylab's status is HISTORIC (1973-1979). ISS is OPERATIONAL (1998-present).

R7.7.3 Universes shall be able to define eras within their setting (Pre-Collapse era runs 2400-2790; Post-Collapse era runs 2790-3500).

R7.7.4 Each universe-scoped catalog entry shall be taggable with the era(s) it belongs to.

R7.7.5 The user shall be able to filter visibility by era. Selecting "Post-Collapse" shall hide entries that are tagged only as Pre-Collapse.

R7.7.6 Era filtering shall be in addition to universe activation. The user activates a universe and then optionally narrows the view by era.

R7.7.7 Real entries shall be visible based on the user's "current time" view — a default of "current date" makes real entries with HISTORIC status appear with a "destroyed" badge but still visible; ACTIVE entries appear normally; PLANNED entries appear with a "planned" badge.

### §7.8 — Visual and Presentation Rules

**Definition**: How TRIPS should render content. Icons, colors, label priority, visibility layers, map vs system view rules, scale behavior, selection behavior.

**Role 1 examples**: Real planets get standard colors based on temperature. Real stars get colors based on spectral type. Real stations get a standard icon. Visual rules for real data are application-default.

**Role 2 examples**: A universe might want all its content rendered in a particular color (e.g., red for the Hkh'Rkh empire). A universe might want gates to be visible on the stellar map view in addition to the system view (Aldenata gates are major navigational landmarks).

**Mode interaction**: Visual rules are universe-scoped. Each active universe contributes its own visual styling for its content.

**Requirements**:

R7.8.1 Each universe shall be able to specify a default color/palette for its content.

R7.8.2 Specific entries within a universe shall be able to override the universe's default visual styling.

R7.8.3 Visual rules shall include: per-entry color, icon, size hint, label priority, visibility-at-zoom-level.

R7.8.4 The "visible at zoom level" property shall distinguish between content visible on the stellar map (large-scale view) vs. content visible only when entering a specific system.

R7.8.5 Visual rules shall be applied consistently across the renderer for system view, stellar view, and the catalog browser.

R7.8.6 The user shall be able to override universe visual rules for their personal view. A universe authored by someone else may use colors the user dislikes; the user shall be able to change those colors locally without modifying the universe definition itself.

R7.8.7 Real-data visual rules shall be application-default and not customizable per-user in the first version. Future enhancement may allow customization; not in scope here.

### §7.9 — Provenance and Metadata

**Definition**: Where the data comes from. Source universe, author, canon level, source work, notes.

**Role 1 examples**: ISS has provenance pointing to NASA documentation. Lunar Gateway has provenance pointing to NASA/Artemis program documentation. Stellar data points to HYG database with version/date.

**Role 2 examples**: Troy has provenance "John Ringo / Troy Rising series." Hkh'Rkh has provenance "Charles Gannon / Caine Riordan series." Larry's Pattern Station has provenance "Larry Mitchell / Children of the Pattern."

**Mode interaction**: Provenance is per-entry; respects the entry's universe affiliation.

**Requirements**:

R7.9.1 The existing `CatalogProvenance` record (Phase D.6) shall be the basis for provenance tracking.

R7.9.2 Provenance shall include: source type (REAL, SCIENCE_FICTION, USER_CUSTOM), source universe (the worldbuilding universe), source work (specific book/series), source citation (URL or canonical reference).

R7.9.3 Provenance shall be visible in the entry's editor and properties dialog.

R7.9.4 Provenance shall be exportable with universe definitions (a shared universe includes provenance info so receivers know the source).

R7.9.5 The CatalogAuditTest discipline (Phase D.6) shall extend to verify provenance integrity for all entries — real entries have REAL provenance with HISTORIC/ACTIVE/PLANNED/CANCELLED status; fictional entries have non-REAL provenance with non-FICTIONAL status only if the universe is itself an exception (none currently planned).

### §7.10 — Population and Activation Rules

**Definition**: Rules for what gets placed where when a universe is activated.

**Role 1 examples**: Real data is always populated; no activation rules needed.

**Role 2 examples**: When "Legacy of the Aldenata" is activated, place Troy in Sol's outer system, place SAPL elements in Sol's defense perimeter, place SheVa Gun at Earth surface, define Aldenata gate network with gates in specific systems.

**Mode interaction**: Per-universe; populated on activation.

**Requirements**:

R7.10.1 Each universe shall be able to specify what content goes where on activation.

R7.10.2 Population rules shall be implemented as data, not code. A universe definition shall include a list of catalog-entry-to-place mappings.

R7.10.3 Activation shall be idempotent. Activating "Legacy of the Aldenata" twice shall not duplicate Troy.

R7.10.4 Deactivation shall hide the populated entries but not delete them from the database. Re-activation immediately re-shows them.

R7.10.5 Population shall not require the user to manually place entries — the universe definition specifies where things go, and activation just makes them visible.

R7.10.6 Population may be partial. A universe might specify only the major entries and leave secondary entries unpopulated by default; users can manually place those if they choose.

### §7.11 — Technology and Capabilities

**Definition**: Tech levels, allowed drives, prohibited tech, breakthrough inventions.

**Role 1 examples**: Real-world tech level (limited propulsion, no FTL, limited automation). Specific real technologies (Nuclear Thermal Propulsion, ion drives, fusion concepts).

**Role 2 examples**: A universe might define tech levels (TL 11 advanced fusion, TL 14 alien advanced, TL 16 hypothetical). A universe might prohibit certain drives (no antimatter in the *Foundation* universe). A universe might allow breakthrough technologies (FTL via gates only, with no continuous warp).

**Mode interaction**: Per-universe; affects what's available to entries within that universe.

**Requirements**:

R7.11.1 Universes shall be able to define tech-level scales (e.g., 1-15) and assign tech levels to drive types and other technology-bearing entries.

R7.11.2 Universes shall be able to designate certain technologies as canon-allowed or canon-prohibited within the universe.

R7.11.3 Editors creating new entries shall respect the universe's tech constraints. Creating a ship in a universe that prohibits antimatter shall not offer antimatter drives.

R7.11.4 Tech level may have implications for validation (a TL 5 universe shouldn't have starships); this is a soft enforcement, primarily a worldbuilding aid rather than a hard rule.

### §7.12 — Events and Timeline

**Definition**: Major historical events within a universe — wars, first contacts, disasters, discoveries.

**Role 1 examples**: Real historical milestones (Sputnik launch, Apollo 11, ISS first crew, Hubble launch, JWST launch). The dating of these events is canonical.

**Role 2 examples**: First contact with Aldenata (Posleen War). Foundation's establishment. The Triune-Collapse Event. The Harada Event in Larry's "Children of the Pattern."

**Mode interaction**: Universe-scoped. Real events are real-data attributes.

**Requirements**:

R7.12.1 Each universe shall be able to define a list of major events with: date/era, name, description, affected places, affected factions.

R7.12.2 Events shall be displayable as a timeline view (out of scope for first implementation; just a viewport into the data).

R7.12.3 The user shall be able to filter content visibility by event — "show only what existed after the First Contact" filters entries that were created post-First-Contact.

R7.12.4 Events shall not be required. A universe with no defined events is valid; events are an optional enrichment.

### §7.13 — Economics and Resources

**Definition**: Trade goods, strategic resources, mining rights, currency, economic ties.

**Role 1 examples**: Real-world space economy at a high level (Earth's launch capabilities, specific commercial entities). Largely out of scope for visualization.

**Role 2 examples**: Within an SF universe, defined resources (deuterium, helium-3, antimatter-feedstock, exotic minerals). Trade routes between resource-producing and resource-consuming systems. Currency systems.

**Mode interaction**: Universe-scoped. Real economy data is highly optional and largely irrelevant for visualization.

**Requirements**:

R7.13.1 Universes shall be able to define resource types.

R7.13.2 Places (stars, systems, planets) shall be tagged with available resources.

R7.13.3 The connection between resources, trade routes, and faction economic interests shall be definable but not required for a universe to function.

R7.13.4 Economics is the most aspirational of the 14 categories. Initial implementation may be minimal — just resource tagging on places — with deeper economic modeling deferred to future phases.

### §7.14 — Threats and Anomalies

**Definition**: Known dangers within a universe — pirate zones, unknown phenomena, precursor warnings, lethal regions.

**Role 1 examples**: Real space hazards (radiation belts, debris clouds in LEO, dangerous orbital regions). Very limited; mostly informational.

**Role 2 examples**: Posleen-infested systems. The Veil (a real-or-fictional anomaly between sectors). Pirate-controlled regions. Forbidden zones in alien universes.

**Mode interaction**: Universe-scoped (mostly). Real space hazards are real-data attributes.

**Requirements**:

R7.14.1 Universes shall be able to define threat regions associated with specific places or volumes of space.

R7.14.2 Threats shall have: type (pirate, anomaly, precursor, hazard), description, affected places, era of activity.

R7.14.3 Rendering of threats shall be possible (a colored zone overlay, a warning icon on a place).

R7.14.4 Threats are an optional enrichment; universes without defined threats are valid.

---

## §8 — User interaction requirements

### Universe management UI

R8.1 The Worldbuilding menu shall include a "Universes" submenu with:
- List of installed universes
- Active/Inactive toggle per universe
- "Create Universe..." option
- "Import Universe..." option
- "Export Universe..." option (per-universe)

R8.2 The Universes list shall show, per universe: name, version, status (active/inactive), description, source/author, era list.

R8.3 The user shall be able to view universe details (catalog entry count, places defined, factions defined, etc.) before activating.

R8.4 Universe deactivation shall not require confirmation; activation shall not require confirmation. (The cost of toggling is low; encourage exploration.)

R8.5 Universe deletion shall require confirmation, with the warning that user data tagged to that universe will be lost.

### Universe creation flow

R8.6 The "Create Universe" dialog shall ask for: Name (required), Description (optional), Source/Author (optional), Version (defaults to "1.0").

R8.7 On creation, the new universe shall be: persisted, made available in the universe list, and (default) automatically activated.

R8.8 The user shall be able to optionally populate the new universe with starting eras, factions, etc., during creation, or skip and add later.

### Universe import/export

R8.9 Import dialog shall accept a JSON file path. On import, the universe is loaded into the local database. The user is shown the universe's details and asked whether to activate it.

R8.10 Export dialog shall accept a destination file path. On export, the universe (all its content — entries, places, networks, factions, aliases, events) is serialized to JSON.

R8.11 Import shall fail gracefully if the JSON is malformed or refers to incompatible content. Error messages shall be specific.

R8.12 Export shall be deterministic — exporting the same universe twice produces semantically equivalent JSON (perhaps with the only differences being timestamps or order-independence).

### Catalog editor integration

R8.13 All existing catalog editors (StationEditorDialog, MegastructureEditorDialog, etc.) shall include a "Universe" field. For real entries, this field shall display "Real" and not be editable. For new entries, this field shall default to the user's active universe (or "Real" if no universe is active and the user has appropriate permission).

R8.14 The "active universe" concept shall be visible in the application chrome — a small indicator in the status bar or toolbar showing "Editing: Real" or "Editing: Children of the Pattern."

R8.15 Catalog browsers (the installation designer panel, the ship designer panel) shall provide filters for universe affiliation — the user can filter to see only real, only specific universes, or all.

### Filter UI

R8.16 The main view (stellar map and system view) shall always reflect the active universes. When universes change, the view updates without explicit action.

R8.17 The status bar shall show active universe count and a brief description ("Real + 2 universes active" or "Real only").

R8.18 Hovering over a fictional entry shall show the universe affiliation. Clicking shall navigate to the universe's metadata view.

---

## §9 — Data persistence requirements

R9.1 Universes shall be stored in JPA tables.

R9.2 Each catalog entry table (station_design, weapon_installation, megastructure, spaceship_design, transport_node, gate_network) shall include a nullable `universe_id` foreign key referencing the universe table. Null = Real (canonical).

R9.3 The universe table shall contain: id, name, description, source/author, version, lifecycle (similar to GateNetwork), created/modified timestamps, plus universe-level configuration (default colors, era list serialized as JSON if compact, separate era table if more structure needed).

R9.4 Places that are fictional (added by a universe) shall be in their own table or in an extended schema; real places remain in HYG database and aren't duplicated.

R9.5 Aliases shall be in their own table linking universe + real place + alias.

R9.6 Factions shall be in their own table with: id, universe_id (null for real), name, description, parent_faction_id (nullable for hierarchy), homeworld_id.

R9.7 Events shall be in their own table per universe.

R9.8 Resources and threats may share patterns with factions or events; specifics TBD when those phases land.

R9.9 The migration from the current state (existing fiction-canon entries in the default catalog) shall be one-time and explicit. A new Flyway migration shall:
- Create the universe table
- Add `universe_id` column to all catalog tables
- Create the example universe row (e.g., "Legacy of the Aldenata")
- Tag the existing fiction-canon entries with that universe id

R9.10 User-created universes shall be persistable to JSON in a stable format. The format shall be documented (so users can edit by hand, write tools, etc.).

R9.11 JSON format shall be machine-readable, human-editable, and forward-compatible (older versions of TRIPS can ignore unknown fields without crashing).

R9.12 The existing test discipline (D.6 catalog audit tests, the FlywayBaselineSmokeTest discipline) shall extend to universe-related schema and data.

---

## §10 — Out of scope

This section explicitly names what this document does NOT require, to prevent scope creep.

**Not in scope: A complete political-economy simulator.** The Factions category supports definition, hierarchy, and color-coding. It does not support faction-vs-faction conflict resolution, war simulation, or economic flow modeling. Those are gameplay features.

**Not in scope: Stellar evolution simulation.** Stars don't change in TRIPS' time-window. Real stars are as they are; fictional stars are described by their definition.

**Not in scope: Real-time multi-user collaboration.** Universe definitions are shareable via export/import (JSON files), but TRIPS itself is a single-user application. Concurrent editing of a universe is not supported. (Future enhancement could add this; not in scope here.)

**Not in scope: Procedural universe generation.** Universes are user-defined or canonical (real). There's no "generate a random Star Wars-style universe" feature.

**Not in scope: Combat tactics, ship-to-ship dynamics, or military simulation.** Ships have stats; ships don't fight in TRIPS.

**Not in scope: Spacecraft physics simulation beyond visualization.** Orbits are visualized; orbits aren't dynamically evolved.

**Not in scope: A history-editor tool that allows modifying real historical data.** Real data is canonical and immutable through the UI.

**Not in scope: A timeline-replay tool that advances simulated time.** The system shows "what does this look like at time T" but doesn't simulate evolution from T1 to T2.

**Not in scope: A fan-fiction site, a publishing platform, or a community-sharing network.** TRIPS is a desktop app for individuals.

**Not in scope: Cosmology or general relativity in any depth.** Distances are visualized; spacetime curvature effects (gravitational lensing, time dilation) are not.

**Not in scope: Programming language plugins, scripting, or extensibility beyond JSON universe imports.** The system is what it is; users don't write code to extend it.

---

## §11 — Success criteria

The worldbuilding platform vision is successful when:

**SC1.** A new TRIPS user installs the application and sees only real data by default. No accidental fictional content appears.

**SC2.** A user can activate at least one example universe with a single click and see its content appear in the appropriate places (Sol gets Troy in the outer system, etc.).

**SC3.** A user can deactivate the example universe and return to real-only mode without losing real data.

**SC4.** A user can create their own universe through a dialog, with their universe appearing alongside the example universe in the list.

**SC5.** A user can add catalog entries (ships, stations, etc.) to their own universe and see those entries appear when their universe is active.

**SC6.** A user can export their universe to a JSON file, send it to another user, who can import and use it.

**SC7.** A user can simultaneously activate multiple universes (their own + an example) and see content from both without conflict.

**SC8.** Real data remains canonical regardless of how many universes are active. ISS doesn't get renamed by a fictional universe.

**SC9.** The example universe's content (Troy, Posleen ships, fictional drives) is fully accessible when the universe is active, and fully invisible when deactivated.

**SC10.** Universe-tagged content can be edited only within its universe context. A user editing a Posleen ship sees only Posleen-relevant drives in the dropdown (or the dropdown is filtered to show all options with universe affiliations marked).

**SC11.** The system gracefully handles missing dependencies on import (e.g., a universe referencing a Faction that doesn't exist in the importing installation). Either fail with a clear error or succeed with explicit notes.

**SC12.** Performance does not degrade noticeably as more universes are added or activated. Activating a universe shall take less than 100ms; rendering should remain responsive.

These criteria are the test bed for the platform. If the implementation cannot satisfy them, the requirements are not yet met.

---

*End of requirements document.*
