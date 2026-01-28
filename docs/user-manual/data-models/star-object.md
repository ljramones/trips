# StarObject

The central data model for star catalog entries. Each record represents a single star with its physical properties, position, catalog identifiers, and metadata.

**Database Table**: `STAR_OBJ`

## Identity Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Primary key, auto-generated UUID |
| `dataSetName` | String | Name of the dataset this star belongs to |
| `displayName` | String | Primary display name for the star |
| `commonName` | String | Common/popular name (e.g., "Barnard's Star") |
| `systemName` | String | Name of the star system |
| `solarSystemId` | String | Foreign key reference to SolarSystem entity |

## Position and Coordinates

All positions are heliocentric (centered on Sol) in J2000 epoch.

| Field | Type | Description |
|-------|------|-------------|
| `x` | double | X coordinate in light years |
| `y` | double | Y coordinate in light years |
| `z` | double | Z coordinate in light years |
| `distance` | double | Distance from Sol in light years |
| `ra` | double | Right ascension in degrees |
| `declination` | double | Declination in degrees |
| `galacticLat` | double | Galactic latitude |
| `galacticLong` | double | Galactic longitude |
| `parallax` | double | Parallax in milli-arc-seconds |
| `pmra` | double | Proper motion in RA |
| `pmdec` | double | Proper motion in declination |
| `radialVelocity` | double | Radial velocity in km/year |

## Physical Properties

| Field | Type | Description |
|-------|------|-------------|
| `mass` | double | Mass in solar masses |
| `radius` | double | Radius in solar radii |
| `temperature` | double | Surface temperature in Kelvin |
| `age` | double | Estimated age |
| `metallicity` | double | Metallicity value |
| `luminosity` | String | Luminosity class |

## Spectral Classification

| Field | Type | Description |
|-------|------|-------------|
| `spectralClass` | String | Full spectral classification (e.g., "G2V") |
| `orthoSpectralClass` | String | Single character class (O, B, A, F, G, K, M, L, T, Y) |

## Magnitude Data

| Field | Type | Description |
|-------|------|-------------|
| `apparentMagnitude` | String | Apparent visual magnitude |
| `absoluteMagnitude` | String | Absolute visual magnitude |
| `magu` | double | U-band magnitude |
| `magb` | double | B-band magnitude |
| `magv` | double | V-band magnitude |
| `magr` | double | R-band magnitude |
| `magi` | double | I-band magnitude |
| `bprp` | double | Gaia BP-RP color |
| `bpg` | double | Gaia BP-G color |
| `grp` | double | Gaia G-RP color |

## Catalog Identifiers (Embedded)

These fields are embedded from the `StarCatalogIds` component. See [StarCatalogIds](star-catalog-ids.md) for details.

| Field | Type | Description |
|-------|------|-------------|
| `hipCatId` | String | Hipparcos catalog ID |
| `hdCatId` | String | Henry Draper catalog ID |
| `glieseCatId` | String | Gliese catalog ID |
| `gaiaDR2CatId` | String | Gaia DR2 catalog ID |
| `gaiaDR3CatId` | String | Gaia DR3 catalog ID |
| `tycho2CatId` | String | Tycho 2 catalog ID |
| `twoMassCatId` | String | 2MASS catalog ID |
| `simbadId` | String | SIMBAD identifier |
| `bayerCatId` | String | Bayer designation |
| `flamsteedCatId` | String | Flamsteed designation |

## World Building (Embedded)

These fields are embedded from the `StarWorldBuilding` component. See [StarWorldBuilding](star-world-building.md) for details.

| Field | Type | Description |
|-------|------|-------------|
| `polity` | String | Controlling political entity |
| `worldType` | String | World classification |
| `fuelType` | String | Available fuel type |
| `portType` | String | Starport classification |
| `populationType` | String | Population level |
| `techType` | String | Technology level |
| `productType` | String | Primary export/product |
| `milSpaceType` | String | Military space presence |
| `milPlanType` | String | Military planetary presence |

## Metadata Fields

| Field | Type | Description |
|-------|------|-------------|
| `epoch` | String | Epoch of measurements (e.g., "J2000") |
| `constellationName` | String | Constellation the star belongs to |
| `notes` | String (LOB) | Free-form notes |
| `source` | String (LOB) | Source catalog information |
| `realStar` | boolean | True for real stars, false for fictional |
| `exoplanets` | boolean | True if star has known exoplanets |
| `numExoplanets` | int | Number of known exoplanets |
| `aliasList` | Set<String> | Alternative names for the star |

## Gaia Update Tracking

| Field | Type | Description |
|-------|------|-------------|
| `gaiaUpdated` | boolean | True if updated from Gaia data |
| `gaiaUpdatedDate` | String | Date of Gaia update |

## Custom Fields

For user-defined data:

| Field | Type | Description |
|-------|------|-------------|
| `miscText1` - `miscText5` | String | Custom text fields |
| `miscNum1` - `miscNum5` | double | Custom numeric fields |

## Display Properties

| Field | Type | Description |
|-------|------|-------------|
| `displayScore` | double | Calculated visibility score for label display |
| `forceLabelToBeShown` | boolean | Override to always show label |

## Relationships

- **SolarSystem**: A StarObject can belong to a SolarSystem via `solarSystemId`
- **ExoPlanet**: ExoPlanets reference their host star via `hostStarId`
- **DataSetDescriptor**: Stars belong to a dataset via `dataSetName`
