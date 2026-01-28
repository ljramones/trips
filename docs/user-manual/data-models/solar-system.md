# SolarSystem

The aggregate root for solar system data. A SolarSystem groups together stars, planets, and system-level metadata.

**Database Table**: `SOLAR_SYSTEM`

## Identity Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Primary key, auto-generated UUID |
| `systemName` | String | Human-readable system name (required) |
| `dataSetName` | String | Dataset this system belongs to |
| `primaryStarId` | String | Foreign key to the primary StarObject |

## System Composition

| Field | Type | Description |
|-------|------|-------------|
| `starCount` | int | Number of stars (1=single, 2=binary, 3=trinary, etc.) |
| `planetCount` | int | Total number of planets |
| `cometCount` | int | Number of known comets |
| `hasAsteroidBelt` | boolean | True if system has an asteroid belt |
| `hasDebrisDisk` | boolean | True if system has a debris disk |

## Habitable Zone

Calculated from stellar luminosity:

| Field | Type | Description |
|-------|------|-------------|
| `hasHabitableZonePlanets` | boolean | True if any planets are in the habitable zone |
| `habitableZoneInnerAU` | Double | Inner edge of habitable zone in AU |
| `habitableZoneOuterAU` | Double | Outer edge of habitable zone in AU |

## Location

| Field | Type | Description |
|-------|------|-------------|
| `distanceFromSol` | Double | Distance from Sol in light years |

## Science Fiction / World Building

| Field | Type | Description |
|-------|------|-------------|
| `polity` | String | Controlling faction or political entity |
| `colonized` | boolean | True if the system has been colonized |
| `colonizationYear` | Integer | Year of first colonization |
| `totalPopulation` | Long | Total population across all bodies |
| `strategicImportance` | Integer | Rating from 1-10 |
| `notes` | String | System notes (max 2000 characters) |

## Custom Data Fields

For user-defined extensibility:

| Field | Type | Description |
|-------|------|-------------|
| `customData1` | String | Custom field 1 |
| `customData2` | String | Custom field 2 |
| `customData3` | String | Custom field 3 |
| `customData4` | String | Custom field 4 |
| `customData5` | String | Custom field 5 |

## Relationships

```
SolarSystem (1) ──────> (1) StarObject (primary star via primaryStarId)
SolarSystem (1) <────── (N) StarObject (companion stars via solarSystemId FK)
SolarSystem (1) <────── (N) ExoPlanet (via solarSystemId FK)
```

- **Primary Star**: The main star, referenced by `primaryStarId`
- **Companion Stars**: Additional stars in multi-star systems reference back via their `solarSystemId`
- **Planets**: ExoPlanet entities reference this system via `solarSystemId`
- **Dataset**: Belongs to a dataset via `dataSetName`

## Usage Notes

- The habitable zone boundaries are calculated automatically from the primary star's luminosity using `HabitableZoneCalculator`
- For binary/multiple star systems, `starCount` indicates the total number of stars
- World-building fields (polity, colonized, population) are optional and intended for science fiction scenarios
