# ExoPlanet

Data model for exoplanets, moons, and other planetary bodies. Contains orbital parameters, physical properties, atmospheric data, and science fiction attributes.

**Database Table**: `EXOPLANET`

## Identity Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Primary key, auto-generated UUID |
| `name` | String | Planet name |
| `solarSystemId` | String | Foreign key to SolarSystem |
| `hostStarId` | String | Foreign key to specific host StarObject |
| `parentPlanetId` | String | For moons: FK to parent planet |
| `isMoon` | Boolean | True if this is a moon |
| `planetStatus` | String | Confirmed, candidate, or other status |

## Orbital Parameters

Keplerian orbital elements:

| Field | Type | Description |
|-------|------|-------------|
| `semiMajorAxis` | Double | Semi-major axis in AU |
| `eccentricity` | Double | Orbital eccentricity (0-1) |
| `inclination` | Double | Orbital inclination in degrees |
| `omega` | Double | Argument of periapsis in degrees |
| `longitudeOfAscendingNode` | Double | Longitude of ascending node in degrees |
| `orbitalPeriod` | Double | Orbital period in days |
| `angularDistance` | Double | Angular distance from star |

## Physical Properties

| Field | Type | Description |
|-------|------|-------------|
| `mass` | Double | Mass (units vary by source) |
| `massSini` | Double | Minimum mass (M sin i) |
| `radius` | Double | Radius (units vary by source) |
| `density` | Double | Bulk density |
| `surfaceGravity` | Double | Surface gravity |
| `surfaceAcceleration` | Double | Surface acceleration |
| `escapeVelocity` | Double | Escape velocity |
| `coreRadius` | Double | Core radius |
| `axialTilt` | Double | Axial tilt in degrees |
| `dayLength` | Double | Day length |
| `geometricAlbedo` | Double | Geometric albedo (reflectivity) |
| `logG` | Double | Log of surface gravity |

## Temperature Data

| Field | Type | Description |
|-------|------|-------------|
| `tempCalculated` | Double | Calculated equilibrium temperature |
| `tempMeasured` | Double | Measured temperature |
| `surfaceTemperature` | Double | Surface temperature |
| `highTemperature` | Double | Dayside/maximum temperature |
| `lowTemperature` | Double | Nightside/minimum temperature |
| `maxTemperature` | Double | Maximum recorded temperature |
| `minTemperature` | Double | Minimum recorded temperature |
| `boilingPoint` | Double | Water boiling point at surface |
| `exosphericTemperature` | Double | Exospheric temperature |
| `greenhouseRise` | Double | Temperature increase from greenhouse effect |
| `hotPoIntegerLon` | Double | Hottest point longitude |

## Atmosphere

| Field | Type | Description |
|-------|------|-------------|
| `atmosphereType` | String | Atmosphere classification |
| `atmosphereComposition` | String | Composition as JSON (max 2000 chars) |
| `surfacePressure` | Double | Surface pressure |
| `volatileGasInventory` | Double | Volatile gas inventory |
| `minimumMolecularWeight` | Double | Minimum retained molecular weight |

## Climate

| Field | Type | Description |
|-------|------|-------------|
| `hydrosphere` | Double | Fraction of surface covered by water |
| `cloudCover` | Double | Cloud cover fraction |
| `iceCover` | Double | Ice cover fraction |
| `albedo` | Double | Bond albedo |
| `greenhouseEffect` | Boolean | True if runaway greenhouse |

## Classification Flags

| Field | Type | Description |
|-------|------|-------------|
| `planetType` | String | Type classification |
| `orbitalZone` | Integer | Orbital zone (1, 2, or 3) |
| `habitable` | Boolean | Potentially habitable |
| `earthlike` | Boolean | Earth-like conditions |
| `gasGiant` | Boolean | Gas giant planet |
| `habitableJovian` | Boolean | Habitable Jovian (floating life) |
| `habitableMoon` | Boolean | Has potentially habitable moons |
| `tidallyLocked` | Boolean | Tidally locked to star |

## Discovery Information

| Field | Type | Description |
|-------|------|-------------|
| `discovered` | Integer | Discovery year |
| `updated` | String | Last update date |
| `detectionType` | String | Detection method |
| `massDetectionType` | String | Mass determination method |
| `radiusDetectionType` | String | Radius determination method |
| `publication` | String | Discovery publication |
| `alternateNames` | String | Alternative designations |
| `molecules` | String | Detected molecules |

## Host Star Data (Denormalized)

For convenience, some host star properties are stored:

| Field | Type | Description |
|-------|------|-------------|
| `starName` | String | Host star name |
| `ra` | Double | Star right ascension |
| `dec` | Double | Star declination |
| `starDistance` | Double | Distance to star |
| `starMass` | Double | Star mass |
| `starRadius` | Double | Star radius |
| `starTeff` | Double | Star effective temperature |
| `starMetallicity` | Double | Star metallicity |
| `starSpType` | Double | Star spectral type |
| `starAge` | Double | Star age |
| `magV`, `magI`, `magJ`, `magH`, `magK` | Double | Star magnitudes |

## Science Fiction / World Building

| Field | Type | Description |
|-------|------|-------------|
| `population` | Long | Planet population |
| `techLevel` | Integer | Technology level |
| `colonized` | Boolean | Has been colonized |
| `colonizationYear` | Integer | Year of colonization |
| `polity` | String | Controlling political entity |
| `strategicImportance` | Integer | Strategic rating |
| `primaryResource` | String | Main export/resource |
| `notes` | String | Notes (max 4000 chars) |

## Procedural Generation

For ACCRETE-generated planets:

| Field | Type | Description |
|-------|------|-------------|
| `proceduralSeed` | Long | Random seed for generation |
| `proceduralGeneratorVersion` | String | Generator version |
| `proceduralSource` | String | Generation source |
| `proceduralAccreteSnapshot` | String (LOB) | ACCRETE state as JSON |
| `proceduralOverrides` | String (LOB) | User overrides as JSON |
| `proceduralGeneratedAt` | String | Generation timestamp (ISO-8601) |
| `proceduralPreview` | byte[] (LOB) | Cached preview image |

## Relationships

- **SolarSystem**: Belongs to a system via `solarSystemId`
- **StarObject**: Orbits a specific star via `hostStarId` (important for binary systems)
- **Parent Planet**: Moons reference their parent via `parentPlanetId`

## Timing Parameters

Advanced transit and radial velocity timing:

| Field | Type | Description |
|-------|------|-------------|
| `tperi` | Double | Time of periastron |
| `tconj` | Double | Time of conjunction |
| `tzeroTr` | Double | Transit epoch |
| `tzeroTrSec` | Double | Secondary transit epoch |
| `tzeroVr` | Double | Radial velocity epoch |
| `lambdaAngle` | Double | Sky-projected obliquity |
| `impactParameter` | Double | Transit impact parameter |
| `k` | Double | Radial velocity semi-amplitude |
