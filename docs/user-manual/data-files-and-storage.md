# Data Files and Storage

TRIPS uses local files and an embedded H2 database for persistent storage of all star data, datasets, routes, solar systems, and user preferences.

## Directory Structure

The TRIPS application creates and uses the following directory structure:

```
trips/
├── data/                          # Database files
│   ├── tripsdb.mv.db              # Main H2 database file
│   └── tripsdb.trace.db           # Database trace log (if enabled)
├── files/                         # Application data files
│   ├── programdata/               # Program resources
│   │   ├── *.csv                  # Sample datasets (e.g., 30ly.trips.csv)
│   │   └── icons/                 # UI icons
│   └── exports/                   # Exported data (created on first export)
└── logs/                          # Application logs (if configured)
```

## The H2 Database

TRIPS uses an embedded **H2 database** for all persistent storage. H2 is a lightweight, file-based SQL database that requires no separate installation or server process.

### Database Location

The database is stored in the `data/` directory:
- **Main file**: `data/tripsdb.mv.db`
- **Connection URL**: `jdbc:h2:file:./data/tripsdb`
- **Credentials**: Username `sa`, no password

### What's Stored in the Database

| Entity | Description |
|--------|-------------|
| **StarObject** | All imported star data (coordinates, spectral class, catalog IDs, custom fields) |
| **DataSetDescriptor** | Dataset metadata (name, author, star count, distance range, notes) |
| **SolarSystem** | Solar system information (habitable zone, planet count, polity) |
| **ExoPlanet** | Exoplanet data (orbital parameters, mass, radius, temperature) |
| **Route** | Saved interstellar routes and waypoints |
| **TransitDefinition** | Transit band configurations per dataset |
| **Preferences** | Application preferences and settings |

### Database Operations

The database is automatically:
- **Created** on first launch if it doesn't exist
- **Updated** when you import data, edit stars, or save routes
- **Backed up** when you export datasets

## Sample Datasets

TRIPS includes sample datasets in the `files/programdata/` directory:

| File | Description |
|------|-------------|
| `30ly.trips.csv` | Stars within 30 light-years of Sol, derived from Gaia DR2 |
| Other `.trips.csv` files | Additional sample datasets (may vary by version) |

These files use the **TRIPS native CSV format** which includes:
- Standard star properties (name, coordinates, spectral class, distance)
- Catalog IDs (Hipparcos, Henry Draper, Gaia, etc.)
- Custom data fields for science fiction world-building

## File Formats

### TRIPS Native Format (.trips.csv)

The recommended format for importing and exporting star data:

```csv
displayName,x,y,z,distance,spectralClass,constellation,mass,luminosity,...
Sol,0.0,0.0,0.0,0.0,G2V,---,1.0,1.0,...
Alpha Centauri A,-1.64,-1.37,-3.84,4.37,G2V,Cen,1.1,1.519,...
```

Key columns include:
- **displayName**: Primary star name
- **x, y, z**: Cartesian coordinates in light-years (Sol at origin)
- **distance**: Distance from Sol in light-years
- **spectralClass**: Full spectral classification (e.g., G2V, K5III)
- **constellation**: Three-letter constellation abbreviation
- **mass, luminosity, temperature**: Physical properties
- **catalogIds**: Semicolon-separated list of catalog identifiers

### Generic CSV Import

TRIPS can import CSV files with various column arrangements. The import dialog allows you to map columns to star properties. Required columns:
- Star name or identifier
- Position (either x/y/z coordinates or RA/Dec/distance)

### Excel Export

Datasets can be exported to Excel (.xlsx) format for use with external tools. The export includes all star properties and custom data fields.

## Backups and Recovery

### What to Back Up

For complete data preservation, back up these directories:

| Directory | Contents | Priority |
|-----------|----------|----------|
| `data/` | Database with all stars, routes, preferences | **Critical** |
| `files/programdata/` | Sample datasets | Optional (can re-download) |
| `files/exports/` | Your exported data | Recommended |

### Backup Procedure

1. **Close TRIPS** before backing up (ensures database consistency)
2. Copy the entire `data/` directory to your backup location
3. Optionally copy any custom datasets from `files/`

### Restoring from Backup

1. Close TRIPS if running
2. Replace the `data/` directory with your backup copy
3. Restart TRIPS

### Database Reset

If you need to start fresh:

1. Close TRIPS
2. Delete all files in the `data/` directory
3. Restart TRIPS (a new empty database will be created)
4. Re-import your datasets via **File → Import/Load or Manage dataset(s)**

See [Troubleshooting](troubleshooting.md) for more database recovery options.

## Storage Considerations

### Database Size

The database grows with the amount of data:
- A 30-light-year dataset (~600 stars): ~1-2 MB
- A 100-light-year dataset (~2,000 stars): ~5-10 MB
- Large datasets (100,000+ stars): 100+ MB

### Performance Tips

- **Smaller active datasets** load and display faster
- **Use query filters** to work with subsets of large datasets
- **Regularly export** important datasets as a backup

## Data Integrity

### Automatic Saves

TRIPS automatically saves changes to the database:
- Star edits are saved when you click **Update** in the edit dialog
- Route changes are saved when you complete a route
- Preference changes are saved when you click **Change** in the preferences dialog

### Transaction Safety

The H2 database uses transactions to ensure data consistency. If TRIPS crashes during an operation, the database will recover to its last consistent state on next launch.

## Related Topics

- [Datasets and Imports](datasets-and-imports.md) - Importing and managing datasets
- [Searching, Querying, and Editing Stars](searching-and-editing-stars.md) - Modifying star data
- [Troubleshooting](troubleshooting.md) - Database recovery and reset
