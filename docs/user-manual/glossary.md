# Glossary

## A

- **ACCRETE**: Procedural model for generating planetary systems based on stellar properties.
- **Active Dataset**: See *Context*.
- **Advanced Search**: SQL-like query interface for searching stars using field names, operators, and complex conditions. Access via **Tools > Search for / Select stars > Select stars using advanced search**.
- **Albedo**: The reflectivity of a planet's surface. A value of 0 means no reflection (black); 1 means perfect reflection (white).
- **Asteroid Belt**: The region of rocky debris between Mars and Jupiter. Can be displayed in the Solar System view via Display Options.
- **AU (Astronomical Unit)**: The average distance from Earth to the Sun, approximately 93 million miles or 150 million kilometers. Used for measuring distances within solar systems.
- **Axial Tilt**: The angle between a planet's rotational axis and its orbital plane. Earth's axial tilt is 23.5°. Affects seasons and climate.

## B

- **Band**: See *Transit Band*.
- **Bayer Designation**: A star naming system using Greek letters and constellation names (e.g., Alpha Centauri, Beta Orionis).

## C

- **Catalog ID**: A unique identifier for a star in an astronomical catalog (e.g., HIP 71683 for Hipparcos, HD 128620 for Henry Draper).
- **Conservative Habitable Zone**: The stricter estimate of the habitable zone where liquid water is most likely to exist.
- **Context**: The currently selected dataset that TRIPS uses for all operations (searching, plotting, routing). Only one dataset can be active at a time. Set via **File → Open Dataset...** or the side panel.
- **CSV Import**: The primary method for importing star data into TRIPS using comma-separated value files.

## D

- **Dataset**: A catalog of stars loaded into TRIPS for visualization and routing. Each dataset has a name, star count, distance range, and optional notes.
- **Data Workbench**: An advanced tool for querying online astronomical sources (Gaia, SIMBAD, VizieR) and enriching existing data. Access via the **Workbench** toolbar button.
- **Display Options**: Toggles in the Solar System view for showing/hiding orbits, labels, habitable zone, asteroid belt, Kuiper belt, and other elements.

## E

- **Eccentricity**: A measure of how elliptical an orbit is. A value of 0 is a perfect circle; values approaching 1 are highly elongated ellipses.
- **Equilibrium Temperature**: The theoretical temperature of a planet based on stellar radiation, before considering atmospheric effects.
- **Escape Velocity**: The minimum speed needed for an object to escape a planet's gravitational pull.
- **Exoplanet**: A planet orbiting a star other than the Sun. TRIPS can display known exoplanets in the Solar System view.
- **Extensions**: Vertical lines connecting stars to the reference grid in the Interstellar view, showing stellar positions above or below the galactic plane. Also called *Stems*.

## F

- **Flamsteed Designation**: A star naming system using numbers and constellation names (e.g., 61 Cygni, 70 Ophiuchi).

## G

- **Gaia**: European Space Agency mission that has cataloged over a billion stars with precise positions and distances. Gaia DR2, DR3, and EDR3 data can be used with TRIPS.
- **GLIESE**: Catalog of nearby stars compiled by Wilhelm Gliese. Common prefix for nearby star names (e.g., GJ 581).
- **Grid**: The 3D coordinate reference grid displayed in the Interstellar view, showing distance in light-years from Sol.

## H

- **H2 Database**: Embedded local database used by TRIPS to store star data, datasets, routes, and preferences.
- **Habitable Zone**: The region around a star where liquid water could exist on a planet's surface. Displayed as dashed blue rings in the Solar System view.
- **Henry Draper (HD)**: Astronomical catalog with HD numbers for stars (e.g., HD 209458).
- **Hipparcos (HIP)**: ESA satellite mission and catalog with HIP numbers for stars (e.g., HIP 71683).
- **Hydrosphere**: The percentage of a planet's surface covered by liquid water.

## I

- **Inclination**: The tilt of an orbit relative to a reference plane (usually the ecliptic or equatorial plane).
- **Interstellar View**: The main 3D visualization showing stars in space, with Sol at the center.

## J

- **Jump Back**: Button in the Solar System view to return to the Interstellar view.

## K

- **Kuiper Belt**: The region of icy bodies beyond Neptune's orbit. Can be displayed in the Solar System view via Display Options.

## L

- **Land on Planet**: Option to enter the Night Sky view from a planet's surface. Access by right-clicking a planet in the Solar System view.
- **Light Year (ly)**: The distance light travels in one year, approximately 5.88 trillion miles or 9.46 trillion kilometers. The primary unit of distance in TRIPS.
- **Link Control**: Side panel section for managing transit band visibility and settings.
- **Links**: Lines showing possible transit connections between stars. Toggle with the **Links** toolbar button.
- **LOD (Level of Detail)**: Rendering technique that adjusts visual detail based on distance from the camera.
- **Luminosity Class**: Classification of a star's brightness category (I=Supergiant, II=Bright Giant, III=Giant, IV=Subgiant, V=Main Sequence, VI=Subdwarf, VII=White Dwarf).

## M

- **Main Sequence**: Stars that are fusing hydrogen in their cores. Most stars, including the Sun (G2V), are main sequence stars.

## N

- **Night Sky View**: A simulation of the sky as seen from a planet's surface, showing stars, planets, and the horizon.

## O

- **Objects in View**: Side panel section listing all stars currently visible in the Interstellar view with their coordinates.
- **Optimistic Habitable Zone**: The broader estimate of the habitable zone, including edge cases where liquid water might exist.
- **Orbital Period**: The time it takes for a planet to complete one orbit around its star.

## P

- **Plates**: In procedural terrain generation, the number of tectonic plates used to create continental patterns.
- **Plot Stars**: The action of displaying stars from the current dataset in the 3D Interstellar view. Accessed via the **Plot Stars** toolbar button.
- **Polity**: A political entity or civilization associated with a star system. Used for science fiction world-building.
- **Procedural Generation**: Algorithmic creation of content (planets, terrain, solar systems) based on parameters and random seeds.

## Q

- **Query Dialog**: The comprehensive search interface for filtering stars by distance, spectral class, and many other criteria. Access via **Edit > Select stars to display from current dataset**.

## R

- **Route**: A path between two or more star systems, calculated based on transit distances. Used for planning interstellar travel.
- **Routing State**: Shown in the status bar, indicates whether route planning is active or inactive.

## S

- **Scale**: The legend showing the size of grid squares in light-years. Toggle with the **Scale** toolbar button.
- **Seed**: A number used to initialize procedural generation. Using the same seed produces the same results.
- **Semi-major Axis**: Half of the longest diameter of an elliptical orbit. Represents the average distance from the orbiting body to its parent.
- **Side Panel**: The collapsible panel on the right side of the main window containing accordion sections for datasets, objects in view, stellar properties, and routing controls.
- **SIMBAD**: Astronomical database operated by the Centre de Données astronomiques de Strasbourg (CDS). Can be queried via the Data Workbench.
- **Sol**: The Sun, Earth's star. Located at the origin (0, 0, 0) in TRIPS coordinates.
- **Solar System View**: A detailed view of an individual star system showing the central star, planets, orbits, habitable zone, and asteroid/Kuiper belts.
- **Spectral Class**: A classification of stars based on their temperature and color (O, B, A, F, G, K, M from hottest to coolest). Determines star color in the visualization.
- **Spectral Subtype**: A numeric refinement of spectral class (0-9), where 0 is hottest and 9 is coolest within the class.
- **Star Labels**: Names displayed next to stars in the Interstellar view. Toggle with the **Star Labels** toolbar button.
- **Star Polities**: Political affiliation indicators on stars. Toggle with the **Star Polities** toolbar button.
- **Stellar Object Properties**: Side panel section showing detailed information about the selected star.
- **Stem**: See *Extensions*.
- **Surface Gravity**: The gravitational acceleration at a planet's surface, often expressed relative to Earth (g).

## T

- **Terrain Viewer**: Interface for procedurally generating and viewing planet terrain. Access by right-clicking a planet and selecting **View Terrain...**.
- **Tidally Locked**: A planet or moon that always shows the same face to its parent body, like Earth's Moon. One side is in perpetual daylight, the other in perpetual darkness.
- **Time Scale**: In the Solar System view, the speed multiplier for orbital animation (0.1x to 10x).
- **Transit**: A possible jump or connection between two star systems, typically limited by a maximum distance. Used for route planning.
- **Transit Band**: A defined range of distances for categorizing transits (e.g., 0-2.5 ly for short jumps, 2.5-5.5 ly for medium jumps). Each band has its own color.
- **TRIPS**: Terran Republic Interstellar Plotting System. The name of this application.
- **2MASS**: Two Micron All-Sky Survey, an infrared astronomical catalog.
- **Tycho-2**: Star catalog from the Hipparcos mission with over 2.5 million stars.

## V

- **VizieR**: Astronomical catalog service providing access to thousands of catalogs. Can be queried via the Data Workbench.

## W

- **Water**: In procedural planet generation, the percentage of the surface covered by oceans (0-100%).
