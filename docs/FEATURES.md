# TRIPS Features

## 3D Interstellar Visualization

The main view displays stars in a 3D space with the following features:

- **Level of Detail (LOD) Rendering**: Stars are rendered with appropriate detail based on distance from camera, optimizing performance for large datasets
- **Spatial Indexing**: KD-tree based spatial queries for efficient star lookup and selection
- **Label Collision Detection**: Smart label placement with depth-sorted priority prevents overlapping text
- **Material Caching**: Optimized rendering with cached PhongMaterial objects by color
- **Spectral Class Coloring**: Stars colored by spectral type (O=blue through M=red)
- **Interactive Selection**: Click to select stars, view properties, and access context menus

> **Technical Details**: [Star Plotting Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/starplotting/starplotting.md)

## Route Planning

TRIPS includes sophisticated graph-based pathfinding for interstellar route planning:

- **Yen's K-Shortest Paths**: Find multiple alternative routes between star systems
- **Configurable Transit Limits**: Define maximum jump distances and transit filters
- **Route Visualization**: 3D display of routes with distance labels
- **Route Caching**: Computed routes are cached for performance
- **Dataset-Wide Search**: Asynchronous pathfinding across entire datasets (100K+ stars)

> **Technical Details**: [Routing Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/routing/routing.md)

## Transit System

Transit calculations determine possible jumps between stars:

- **Transit Bands**: Visual display of reachable distances from any star
- **KD-Tree Optimization**: Fast spatial queries for transit calculations
- **Configurable Filters**: Filter transits by distance, spectral class, or custom criteria
- **Parallel Computation**: Multi-threaded transit calculation for large datasets

> **Technical Details**: [Transits Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/transits/transits.md)

## Solar System Visualization

"Jump into" any star to explore its planetary system:

- **Orbital Mechanics**: Accurate Keplerian orbital paths with proper 3D rotations
- **Habitable Zone Display**: Visual ring showing the habitable zone (Kopparapu model)
- **Real-Time Animation**: Animate planetary orbits with configurable time speed
- **Multi-Star Systems**: Support for binary and multiple star systems
- **Planet Properties**: View detailed planet information including mass, radius, temperature
- **Planetary Rings**: Saturn, Uranus, Neptune-style ring systems with customizable parameters
- **Context Menu Editing**: Right-click planets to edit properties, orbits, and rings

### Solar System Features

Add natural and artificial structures to any solar system for world-building:

**Natural Features:**
- Asteroid Belt, Kuiper Belt, Oort Cloud
- Debris Disk, Zodiacal Dust
- Trojan Clusters (at Lagrange points)
- Comet Trails

**Artificial Structures (Sci-Fi):**
- **Jump Gates**: FTL transit infrastructure with destination links
- **Orbital Habitats**: Space stations and O'Neill cylinders
- **Shipyards**: Construction facilities with production capacity
- **Mining Operations**: Industrial extraction sites
- **Research Stations**: Scientific outposts
- **Defense Perimeters**: Military installations (rendered as particle belts)
- **Sensor Networks**: Detection and communication arrays
- **Dyson Swarms**: Energy collection arrays around stars

**World-Building Properties:**
Each feature can be customized with:
- Controlling polity/faction
- Population (for habitats)
- Technology level (1-10)
- Year established
- Operational status (Active, Abandoned, Under Construction, Destroyed)
- Strategic importance rating
- Defensive capability rating
- Production capacity
- Navigation hazard warnings (type and severity)
- Notes for lore and background

> **Technical Details**: [Solar System View Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/solarsystem/SOLAR_SYSTEM_VIEW.md)

## Procedural Planet Generation

Generate detailed 3D planets with realistic terrain:

- **Goldberg Polyhedron Mesh**: Spherical mesh of hexagons and pentagons
- **Tectonic Plate Simulation**: Flood-fill plate assignment with configurable plate count (7-21)
- **Plate Boundary Effects**: Convergent, divergent, and transform boundaries create mountains, rifts, and trenches
- **Climate Zones**: Multiple climate models including seasonal insolation with axial tilt
- **Erosion & Hydrology**: Rainfall, river formation, sediment transport, and lake filling
- **Rain Shadow Effect**: Realistic moisture patterns based on prevailing winds
- **Reproducible Generation**: Seed-based generation for consistent results

> **Technical Details**: [Procedural Planet Generator Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/planetarymodelling/procedural/procedural-planet-generator.md)

## Night Sky View (Planetary)

View the night sky from the surface of any planet:

- **Horizon Coordinates**: Accurate altitude/azimuth calculations for any observer location
- **Day/Night Detection**: Host star altitude determines visible star magnitude limits
- **Atmospheric Effects**: Atmospheric extinction dims stars near the horizon
- **Magnitude Calculations**: Apparent magnitude adjusted for distance from observer
- **Sky Dome Rendering**: Stars rendered on a spherical sky projection

> **Technical Details**: [Night Sky Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/nightsky/nightsky.md)

## Solar System Generation (ACCRETE)

Generate realistic planetary systems using the ACCRETE model:

- **Protoplanetary Disk Simulation**: Dust accretion and planetesimal growth
- **Planetesimal Coalescence**: Collision and merger of forming planets
- **Moon Capture**: Smaller bodies captured as moons based on mass criteria
- **Atmosphere Calculation**: Gas retention based on escape velocity and temperature
- **Kopparapu Habitable Zones**: Conservative and optimistic HZ boundary calculations
- **Planet Classification**: Automatic typing (Gas Giant, Super-Earth, Terrestrial, etc.)

> **Technical Details**: [Solar System Generation Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/solarsysmodelling/solarsystem_generation.md)

## Particle Fields

Render distributed particle structures for astronomical and sci-fi visualization:

- **Planetary Rings**: Saturn-like (bright, icy) and Uranus-like (dark, narrow) ring systems
- **Asteroid Belts**: Main Belt and Kuiper Belt with realistic orbital mechanics
- **Debris Disks**: Protoplanetary disks and collision remnants with density wave structure
- **Nebulae**: Emission nebulae (glowing), dark nebulae (obscuring), reflection nebulae
- **Accretion Disks**: Black hole and neutron star accretion with temperature gradients
- **Keplerian Motion**: Particles follow proper orbital mechanics (inner particles orbit faster)
- **10 Built-in Presets**: Access via **Experimental → Ring Field Presets** menu
- **Scale Adapters**: Render at solar system scale (AU) or interstellar scale (light-years)

> **Technical Details**: [Particle Fields Documentation](../tripsapplication/src/main/java/com/teamgannon/trips/particlefields/PARTICLE_FIELDS.md)

## Data Import & Workbench

Import stellar data from multiple sources:

- **Gaia DR3**: TAP queries with async job support
- **SIMBAD**: Astronomical database queries
- **VizieR**: Access to thousands of astronomical catalogs (Hipparcos, Tycho-2, RAVE, LAMOST)
- **CSV/Excel Import**: Import from standard file formats
- **Field Mapping**: Map source fields to TRIPS format with auto-mapping
- **Validation**: Data validation with detailed error reporting
- **Exoplanet Import**: Import exoplanet.eu catalog data with star matching

## Built-in Problem Reporting

Report issues directly from the application:

- **Help > Report a Problem**: Submit diagnostic reports with one click
- **Automatic Attachments**: Include logs, screenshots, and system info
- **Crash Detection**: Automatically detects JVM crash files (`hs_err_pid*.log`)
- **Offline Support**: Reports saved locally and retried when connectivity returns
- **Privacy Aware**: You control exactly what gets included in each report

## AT-HYG Data Enrichment

The Data Workbench includes tools to fill gaps in the AT-HYG stellar database (~2.5 million stars). The enrichment pipeline addresses missing values that are critical for simulations (ACCRETE, OreKit) and visualization.

### Distance Enrichment
- **Gaia/Hipparcos TAP Lookup**: Query Gaia DR3 and Hipparcos catalogs for parallax data, calculate distances
- **Photometric Distance Estimation**: For stars without parallax, estimate distance from apparent magnitude and color index (B-V or BP-RP)

### Stellar Parameter Estimation
- **Mass from Luminosity**: Estimate stellar mass using the mass-luminosity relation for main-sequence stars
- **Radius from Mass**: Estimate stellar radius using mass-radius relations
- **Temperature from BP-RP Color**: Estimate effective temperature from Gaia BP-RP color index (~2.4M stars)
- **Spectral Class from BP-RP Color**: Estimate MK spectral classification (e.g., G2V, K5V, M3V) from color
- **Cross-Fill Temperature/Spectral**: Fill remaining gaps by converting between temperature and spectral class

### Enrichment Results (AT-HYG 2.5M stars)

| Field | Before | After | Coverage |
|-------|--------|-------|----------|
| Distance | ~1.2M | 2.55M | 99.95% |
| Mass | 1 | 2.55M | 99.95% |
| Radius | 1 | 2.55M | 99.95% |
| Temperature | 6 | 2.42M | 95% |
| Spectral Class | 360K | 2.44M | 95.5% |

### Performance
The enrichment uses a batch-ID optimization for large datasets:
1. Single query fetches all eligible star IDs
2. Stars loaded and processed in batches of 5,000
3. ~1,000 stars/second throughput
4. Full enrichment of 2.5M stars completes in ~2 hours (vs. days with naive approach)
