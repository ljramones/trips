# Data Workbench Guide

The Data Workbench helps you bring external star datasets into TRIPS by mapping source CSV fields to the TRIPS CSV format, previewing/validating the data, and exporting a clean file.

## Open the Workbench
- Menu: Tools -> Data Workbench...
- Toolbar: Workbench

## Workflow

### 1) Connect
1. Click "Add Source..." and choose:
    - Local CSV: pick a file from disk.
    - URL CSV: paste a URL to a CSV file.
    - Gaia TAP: enter an ADQL query to fetch Gaia data.
    - Gaia TAP presets: quick TOP 200/1000/5000 queries.
    - SIMBAD TAP: enter an ADQL query to fetch SIMBAD data.
    - SIMBAD TAP presets: quick TOP 200/1000 queries filtered to non-null parallax, proper motion, and spectral type.
2. Optional: set the cache directory and enable "Use for downloads".
3. The source appears in the list.

### 2) Map
1. Select a source, then click "Load Source Fields".
2. Select a source field and a target field, then click "Add Mapping ->".
3. Use "Auto Map" to match common field names automatically.
    - Gaia sources also use a built-in mapping template (e.g., `ra`, `dec`, `pmra`, `pmdec`, `bp_rp`).
    - SIMBAD sources map common fields like `main_id`, `sp_type`, and `rvz_radvel`.
4. Save/Load mapping as needed.
   - The last mapping is cached in `~/trips-workbench-cache/last-mapping.map.csv`.

Required target fields are marked with `*` in the Target Fields list.

### 3) Preview
1. Click "Load Preview".
2. Use the pagination control to page through the mapped data.

### 4) Validate
- "Validate" checks the current preview page.
- "Validate Full File" checks all rows in the source file.

Validation results appear in the log panel.

### 5) Export
- Click "Export CSV" to produce a TRIPS-compatible CSV.
- A validation log is written alongside the exported file with the `.log` suffix.

If no source or mapping is selected, Export creates a template CSV with the TRIPS header.

## Notes
- URL sources are downloaded to the cache before preview/validate/export.
- Gaia TAP queries use async jobs with exponential backoff. Large results can take several minutes.
- The Workbench emits status updates to the main TRIPS status bar.

## Troubleshooting
- If preview or export fails, ensure the source file exists and a mapping is defined.
- For URL sources, verify the URL points directly to a CSV file (not HTML).
