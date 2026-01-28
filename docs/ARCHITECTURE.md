# Architecture and Technical Docs

## Multi-View Architecture

TRIPS uses a pluggable view system with paired 3D and side panes:

| View | 3D Pane | Side Pane | Status |
|------|---------|-----------|--------|
| Interstellar | Star field with routes | Datasets, Objects, Properties, Routing | Implemented |
| Solar System | Orbital view with planets | System Overview, Planets, Selected Object | Implemented |
| Galactic | Galaxy-wide visualization | Galactic Position, Coverage, Statistics | Planned |
| Planetary | Night sky from surface | Location, Sky Overview, Brightest Stars | Implemented |

## Technical Documentation

- [Star Plotting System](../tripsapplication/src/main/java/com/teamgannon/trips/starplotting/starplotting.md)
- [Routing System](../tripsapplication/src/main/java/com/teamgannon/trips/routing/routing.md)
- [Transit System](../tripsapplication/src/main/java/com/teamgannon/trips/transits/transits.md)
- [Solar System Generation](../tripsapplication/src/main/java/com/teamgannon/trips/solarsysmodelling/solarsystem_generation.md)
- [Procedural Planets](../tripsapplication/src/main/java/com/teamgannon/trips/planetarymodelling/procedural/procedural-planet-generator.md)
- [Night Sky System](../tripsapplication/src/main/java/com/teamgannon/trips/nightsky/nightsky.md)
- [Future Views](FUTURE_VIEWS.md)

## Local Guides

- [Data Workbench Guide](../DataWorkbench.md)
- [Packaging Guide](../PACKAGING.md)
- [Contributing](../CONTRIBUTING.md)
- [Code of Conduct](../CODE_OF_CONDUCT.md)
