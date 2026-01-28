# AsteroidBelt

Stores properties for asteroid belts within solar systems.

**Database Table**: `ASTEROIDBELT`

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key, auto-generated |
| `dataSetName` | String | Dataset this belt belongs to |
| `innerRadius` | double | Inner edge radius (in AU) |
| `outerRadius` | double | Outer edge radius (in AU) |
| `density` | double | Average density of asteroids |
| `diameter` | double | Average asteroid diameter |

## Usage

Asteroid belts are associated with solar systems and displayed in the Solar System view as a ring between `innerRadius` and `outerRadius`.

The `SolarSystem` entity has a `hasAsteroidBelt` boolean flag that indicates whether the system contains an asteroid belt.
