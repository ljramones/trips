# Data Models Reference

This section documents the database models used by TRIPS to store star data, solar systems, exoplanets, and application settings.

## Core Data Models

These are the primary models for astronomical data:

- [StarObject](star-object.md) - The central star catalog model with positions, physical properties, and catalog IDs
- [SolarSystem](solar-system.md) - Solar system aggregate containing stars, planets, and habitable zone data
- [ExoPlanet](exoplanet.md) - Exoplanet data including orbital parameters, physical properties, and atmosphere
- [DataSetDescriptor](dataset-descriptor.md) - Dataset metadata, configuration, and custom field definitions

## Settings and Preferences Models

These models store application settings:

- [GraphColorsPersist](graph-colors.md) - Graph color scheme settings
- [GraphEnablesPersist](graph-enables.md) - Graph display toggle settings
- [StarDetailsPersist](star-details.md) - Stellar type rendering settings (size, color)
- [CivilizationDisplayPreferences](civilization-preferences.md) - Polity/faction color settings
- [TransitSettings](transit-settings.md) - Transit/jump distance settings
- [TripsPrefs](trips-prefs.md) - General application preferences

## Other Models

- [AsteroidBelt](asteroid-belt.md) - Asteroid belt properties
- [AppRegistration](app-registration.md) - User registration information

## Embedded Value Objects

These are components embedded within other entities:

- [StarCatalogIds](star-catalog-ids.md) - Catalog identifiers (HIP, HD, Gaia, etc.) embedded in StarObject
- [StarWorldBuilding](star-world-building.md) - Science fiction world-building data embedded in StarObject

## Database

TRIPS uses an embedded H2 database stored in the `./data/` directory. The database file is `tripsdb` and can be accessed with:
- **URL**: `jdbc:h2:file:./data/tripsdb`
- **Username**: `sa`
- **Password**: (none)

To reset the database, delete the files in the `./data/` directory and restart TRIPS.
