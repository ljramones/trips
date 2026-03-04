# Terran Republic Interstellar Plotter System

## Introduction

TRIPS (Terran Interstellar Plotter System) is a JavaFX-based 3D stellar cartography application for visualizing and plotting interstellar routes. It combines Spring Boot for backend services with JavaFX for 3D visualization, using an embedded H2 database for persistence.

Origins: Initially developed with Chuck Gannon to help visualize the stellar neighborhood for his Caine Riordan series. See [charlesegannon.com](https://charlesegannon.com).

**Key capabilities:**
- View and edit stellar data in tabular form
- Plot stars in an interactive 3D visualization
- Plan interstellar routes using graph algorithms
- Explore solar systems with orbital visualization
- Generate procedural planets with realistic terrain
- Import data from major astronomical catalogs (Gaia, SIMBAD, VizieR)
- Use for scientific research or science fiction world-building

---

## Prerequisites

- **Java 25** (Eclipse Temurin recommended)
- **Maven** (or use the included `mvnw-java25.sh` wrapper)
- **Docker** — required for running integration tests (the project uses [Testcontainers](https://testcontainers.com/) with PostgreSQL for database testing). Docker Desktop or a compatible runtime must be running before you execute `mvn test` or `mvn install`. To skip tests: `./mvnw-java25.sh clean install -DskipTests`

## Building

```bash
# Build the project
./mvnw-java25.sh clean install

# Build without tests (no Docker required)
./mvnw-java25.sh clean install -DskipTests

# Run the application
cd tripsapplication
../mvnw-java25.sh spring-boot:run
```

### Packaging for Distribution

```bash
# macOS (.dmg installer)
./mvnw-java25.sh clean package -Pjpackage-mac

# Windows (.exe installer)
./mvnw-java25.sh clean package -Pjpackage-win
```

Output is written to `tripsapplication/target/jpackage/`. See the [Packaging Guide](PACKAGING.md) for more details.

### macOS Application Data

When running as a packaged macOS application, TRIPS stores all data under `~/Library/Application Support/TRIPS/`. For example, for a user named `jsmith`:

```
/Users/jsmith/Library/Application Support/TRIPS/
├── data/
│   └── tripsdb.mv.db          # H2 database
├── files/
│   ├── programdata/            # Application preferences and saved state
│   └── scriptfiles/            # Groovy scripts
```

Logs are written to `~/Library/Logs/TRIPS/terranrepublicviewer.log`.

To reset the application to a clean state, delete the `~/Library/Application Support/TRIPS/` directory.

## Quick Start

- [Installation](docs/INSTALLATION.md)
- [Running TRIPS](docs/RUNNING.md)
- [User Manual](docs/user-manual/README.md)

## Documentation Map

- [Features](docs/FEATURES.md)
- [Architecture and Technical Docs](docs/ARCHITECTURE.md)
- [Data Workbench Guide](DataWorkbench.md)
- [Packaging Guide](PACKAGING.md)

## Contributing

- [Helping with programming](https://github.com/ljramones/trips/wiki/Helping-With-Programming)
- [Helping with documentation](https://github.com/ljramones/trips/wiki/Documentation-Process)
- [Contributing](CONTRIBUTING.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
