# Terran Republic Interstellar Plotter System

## Introduction

TRIPS (Terran Interstellar Plotter System) is a JavaFX-based 3D stellar cartography application for visualizing and plotting interstellar routes. It combines Spring Boot for backend services with JavaFX for 3D visualization, using an embedded H2 database for persistence.

**Key capabilities:**
- View and edit stellar data in tabular form
- Plot stars in an interactive 3D visualization
- Plan interstellar routes using graph algorithms
- Explore solar systems with orbital visualization
- Generate procedural planets with realistic terrain
- Import data from major astronomical catalogs (Gaia, SIMBAD, VizieR)
- Use for scientific research or science fiction world-building

---

## Features

### 3D Interstellar Visualization

The main view displays stars in a 3D space with the following features:

- **Level of Detail (LOD) Rendering**: Stars are rendered with appropriate detail based on distance from camera, optimizing performance for large datasets
- **Spatial Indexing**: KD-tree based spatial queries for efficient star lookup and selection
- **Label Collision Detection**: Smart label placement with depth-sorted priority prevents overlapping text
- **Material Caching**: Optimized rendering with cached PhongMaterial objects by color
- **Spectral Class Coloring**: Stars colored by spectral type (O=blue through M=red)
- **Interactive Selection**: Click to select stars, view properties, and access context menus

### Route Planning

TRIPS includes sophisticated graph-based pathfinding for interstellar route planning:

- **Yen's K-Shortest Paths**: Find multiple alternative routes between star systems
- **Configurable Transit Limits**: Define maximum jump distances and transit filters
- **Route Visualization**: 3D display of routes with distance labels
- **Route Caching**: Computed routes are cached for performance
- **Dataset-Wide Search**: Asynchronous pathfinding across entire datasets (100K+ stars)

### Transit System

Transit calculations determine possible jumps between stars:

- **Transit Bands**: Visual display of reachable distances from any star
- **KD-Tree Optimization**: Fast spatial queries for transit calculations
- **Configurable Filters**: Filter transits by distance, spectral class, or custom criteria
- **Parallel Computation**: Multi-threaded transit calculation for large datasets

### Solar System Visualization

"Jump into" any star to explore its planetary system:

- **Orbital Mechanics**: Accurate Keplerian orbital paths with proper 3D rotations
- **Habitable Zone Display**: Visual ring showing the habitable zone (Kopparapu model)
- **Real-Time Animation**: Animate planetary orbits with configurable time speed
- **Multi-Star Systems**: Support for binary and multiple star systems
- **Planet Properties**: View detailed planet information including mass, radius, temperature

### Procedural Planet Generation

Generate detailed 3D planets with realistic terrain:

- **Goldberg Polyhedron Mesh**: Spherical mesh of hexagons and pentagons
- **Tectonic Plate Simulation**: Flood-fill plate assignment with configurable plate count (7-21)
- **Plate Boundary Effects**: Convergent, divergent, and transform boundaries create mountains, rifts, and trenches
- **Climate Zones**: Multiple climate models including seasonal insolation with axial tilt
- **Erosion & Hydrology**: Rainfall, river formation, sediment transport, and lake filling
- **Rain Shadow Effect**: Realistic moisture patterns based on prevailing winds
- **Reproducible Generation**: Seed-based generation for consistent results

### Night Sky View (Planetary)

View the night sky from the surface of any planet:

- **Horizon Coordinates**: Accurate altitude/azimuth calculations for any observer location
- **Day/Night Detection**: Host star altitude determines visible star magnitude limits
- **Atmospheric Effects**: Atmospheric extinction dims stars near the horizon
- **Magnitude Calculations**: Apparent magnitude adjusted for distance from observer
- **Sky Dome Rendering**: Stars rendered on a spherical sky projection

### Solar System Generation (ACCRETE)

Generate realistic planetary systems using the ACCRETE model:

- **Protoplanetary Disk Simulation**: Dust accretion and planetesimal growth
- **Planetesimal Coalescence**: Collision and merger of forming planets
- **Moon Capture**: Smaller bodies captured as moons based on mass criteria
- **Atmosphere Calculation**: Gas retention based on escape velocity and temperature
- **Kopparapu Habitable Zones**: Conservative and optimistic HZ boundary calculations
- **Planet Classification**: Automatic typing (Gas Giant, Super-Earth, Terrestrial, etc.)

### Data Import & Workbench

Import stellar data from multiple sources:

- **Gaia DR3**: TAP queries with async job support
- **SIMBAD**: Astronomical database queries
- **VizieR**: Access to thousands of astronomical catalogs (Hipparcos, Tycho-2, RAVE, LAMOST)
- **CSV/Excel Import**: Import from standard file formats
- **Field Mapping**: Map source fields to TRIPS format with auto-mapping
- **Validation**: Data validation with detailed error reporting
- **Exoplanet Import**: Import exoplanet.eu catalog data with star matching

### AT-HYG Data Enrichment

The Data Workbench includes tools to fill gaps in the AT-HYG stellar database (~2.5 million stars). The enrichment pipeline addresses missing values that are critical for simulations (ACCRETE, OreKit) and visualization:

#### Distance Enrichment
- **Gaia/Hipparcos TAP Lookup**: Query Gaia DR3 and Hipparcos catalogs for parallax data, calculate distances
- **Photometric Distance Estimation**: For stars without parallax, estimate distance from apparent magnitude and color index (B-V or BP-RP)

#### Stellar Parameter Estimation
- **Mass from Luminosity**: Estimate stellar mass using the mass-luminosity relation for main-sequence stars
- **Radius from Mass**: Estimate stellar radius using mass-radius relations
- **Temperature from BP-RP Color**: Estimate effective temperature from Gaia BP-RP color index (~2.4M stars)
- **Spectral Class from BP-RP Color**: Estimate MK spectral classification (e.g., G2V, K5V, M3V) from color
- **Cross-Fill Temperature/Spectral**: Fill remaining gaps by converting between temperature and spectral class

#### Enrichment Results (AT-HYG 2.5M stars)

| Field | Before | After | Coverage |
|-------|--------|-------|----------|
| Distance | ~1.2M | 2.55M | 99.95% |
| Mass | 1 | 2.55M | 99.95% |
| Radius | 1 | 2.55M | 99.95% |
| Temperature | 6 | 2.42M | 95% |
| Spectral Class | 360K | 2.44M | 95.5% |

#### Performance
The enrichment uses a batch-ID optimization for large datasets:
1. Single query fetches all eligible star IDs
2. Stars loaded and processed in batches of 5,000
3. ~1,000 stars/second throughput
4. Full enrichment of 2.5M stars completes in ~2 hours (vs. days with naive approach)

### Multi-View Architecture

TRIPS uses a pluggable view system with paired 3D and side panes:

| View | 3D Pane | Side Pane | Status      |
|------|---------|-----------|-------------|
| Interstellar | Star field with routes | Datasets, Objects, Properties, Routing | Implemented |
| Solar System | Orbital view with planets | System Overview, Planets, Selected Object | Implemented |
| Galactic | Galaxy-wide visualization | Galactic Position, Coverage, Statistics | Planned     |
| Planetary | Night sky from surface | Location, Sky Overview, Brightest Stars | Implemented |

---

## Installing
[Detailed installation steps](https://github.com/ljramones/trips/wiki/Installation) 
    
#### Short Version 
    a. Down load one of the release versions from the release page.
    b. unzip into a target direction of your choice
    c. acquire data files from one of the data pages (TBD) and unzip into the ./files directory

## Running
    a. Once installed, the install directory is complete and independent. Nothing else needs to be installed
    b. in the main director, run either
        - runme.bat (Windows version)
        - runme.sh (Mac/Linux version)        

## See the User Manual in the Wiki
[User Manual Home](https://github.com/ljramones/trips/wiki/User-Manual)

## Contributing
* [Helping with programming](https://github.com/ljramones/trips/wiki/Helping-With-Programming) 
* [Helping with documentation](https://github.com/ljramones/trips/wiki/Documentation-Process)

## Local Docs

### User Guides
* [Data Workbench Guide](DataWorkbench.md) - Importing external star catalogs
* [Packaging Guide](PACKAGING.md) - Building release packages
* [User Manual](https://github.com/ljramones/trips/wiki/User-Manual) - Complete user documentation

### Technical Documentation
* [Star Plotting System](tripsapplication/src/main/java/com/teamgannon/trips/starplotting/starplotting.md) - LOD rendering, spatial indexing, label management
* [Routing System](tripsapplication/src/main/java/com/teamgannon/trips/routing/routing.md) - Pathfinding algorithms, route caching
* [Transit System](tripsapplication/src/main/java/com/teamgannon/trips/transits/transits.md) - Transit calculations, KD-tree optimization
* [Solar System Generation](tripsapplication/src/main/java/com/teamgannon/trips/solarsysmodelling/solarsystem_generation.md) - ACCRETE model, orbital dynamics
* [Procedural Planets](tripsapplication/src/main/java/com/teamgannon/trips/planetarymodelling/procedural/procedural-planet-generator.md) - Terrain generation, tectonics, erosion
* [Night Sky System](tripsapplication/src/main/java/com/teamgannon/trips/nightsky/nightsky.md) - Planetary sky rendering
* [Future Views](docs/FUTURE_VIEWS.md) - Pluggable view architecture

### Development
* [CLAUDE Notes](CLAUDE.md) - Development guide and architecture overview
* [Contributing](CONTRIBUTING.md) - How to contribute
* [Code of Conduct](CODE_OF_CONDUCT.md)
* [PRs with IntelliJ](PR_WITH_INTELLIJ.md)
* [AGENTS Instructions](AGENTS.md)

---

## Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 21.0.5
- **Backend**: Spring Boot 4.0.2
- **Database**: H2 (embedded, file-based)
- **ORM**: Spring Data JPA with Hibernate
- **Graph Algorithms**: JGraphT 1.5.1
- **Orbital Mechanics**: Orekit 11.1.2
- **Build Tool**: Maven
- **3D Graphics**: JavaFX 3D

---

## Screenshots

*See the [Wiki](https://github.com/ljramones/trips/wiki) for screenshots and visual guides.*

---

Normal people believe that if it ain't broke, don't fix it.
Engineers believe that if it ain't broke, it doesn't have enough features yet.
