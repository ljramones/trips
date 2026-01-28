# StarCatalogIds

An embedded value object that stores catalog identifiers for a star. This is embedded within [StarObject](star-object.md).

**Type**: `@Embeddable` (not a separate table)

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `simbadId` | String | SIMBAD database identifier |
| `bayerCatId` | String | Bayer designation (e.g., "Alpha Centauri") |
| `glieseCatId` | String | Gliese catalog ID (e.g., "GJ 551") |
| `hipCatId` | String | Hipparcos catalog ID (e.g., "HIP 70890") |
| `hdCatId` | String | Henry Draper catalog ID (e.g., "HD 128620") |
| `flamsteedCatId` | String | Flamsteed designation (e.g., "61 Cygni") |
| `tycho2CatId` | String | Tycho-2 catalog ID |
| `gaiaDR2CatId` | String | Gaia Data Release 2 ID |
| `gaiaDR3CatId` | String | Gaia Data Release 3 ID |
| `gaiaEDR3CatId` | String | Gaia Early Data Release 3 ID |
| `twoMassCatId` | String | 2MASS catalog ID |
| `csiCatId` | String | CSI catalog ID |
| `catalogIdList` | String (LOB) | Comma-separated list of all IDs |

## Catalog Descriptions

| Catalog | Description |
|---------|-------------|
| **SIMBAD** | Astronomical database of objects beyond the Solar System |
| **Bayer** | Greek letter designations (α, β, γ, etc.) by constellation |
| **Gliese** | Catalog of nearby stars within 25 parsecs |
| **Hipparcos** | ESA satellite mission catalog (118,000+ stars) |
| **Henry Draper (HD)** | Spectroscopic catalog (225,000+ stars) |
| **Flamsteed** | Numbered stars by constellation |
| **Tycho-2** | Astrometric catalog (2.5 million stars) |
| **Gaia** | ESA mission catalogs with precise positions and distances |
| **2MASS** | Two Micron All Sky Survey (infrared) |

## Usage

These identifiers allow cross-referencing stars across different astronomical catalogs. When importing data or querying external services (via the Data Workbench), these IDs are used to match and merge star records.

The `catalogIdList` field provides a convenient comma-separated list of all available identifiers for quick lookup.
