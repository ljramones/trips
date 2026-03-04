# Installation

## Detailed guide
- [Installation (Wiki)](https://github.com/ljramones/trips/wiki/Installation)

## Building from Source

### Prerequisites
- **Java 25** (Eclipse Temurin recommended)
- **Docker** — required for running integration tests. The project uses [Testcontainers](https://testcontainers.com/) with PostgreSQL for database testing. Docker Desktop or a compatible runtime must be running before you build with tests enabled. To build without tests: `./mvnw-java25.sh clean install -DskipTests`

### Steps
```bash
git clone https://github.com/ljramones/trips.git
cd trips
./mvnw-java25.sh clean install
```

## Short version (pre-built release)
1. Download one of the release versions from the release page.
2. Unzip into a target directory of your choice.
3. Acquire data files from one of the data pages (TBD) and unzip into the `./files` directory.

## Data notes
- The application expects program data in `files/programdata/`.
- For user documentation and data workflows, see the [User Manual](https://github.com/ljramones/trips/wiki/User-Manual).
