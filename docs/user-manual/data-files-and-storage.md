# Data Files and Storage

TRIPS uses local files and an embedded H2 database for storage.

## Key Locations

- `files/` contains datasets and program data.
- `files/programdata/` contains CSVs and icons used by the application.
- `data/` contains the local H2 database (`tripsdb.mv.db`).

## Backups

- Back up the `data/` directory to preserve edits and preferences.
- Back up your dataset CSVs in `files/` separately.
